package Pruebas;

import Comunicacion.*;
import Dominio.*;
import Dominio.Partida.MetodoVictoria;
import Ensamblador.EnsambladorRed;
import MVC_JugarPartida.*;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

/** Dos modelos independientes, TCP real y notificaciones en el hilo de Swing. */
public class PruebaPartidaRed {
    private static int comprobaciones;
    private static Sesion a;
    private static Sesion b;
    private interface Condicion { boolean cumple(); }

    private static class Observador implements IVista {
        private int actualizaciones;
        private boolean hiloCorrecto = true;
        @Override
        public void update(IModelo modelo) {
            actualizaciones++;
            hiloCorrecto &= SwingUtilities.isEventDispatchThread();
        }
    }

    private static class Sesion {
        private final EnsambladorRed red;
        private final ModeloPartida modelo;
        private final EnlacePartida enlace;
        private final ControlPartida control;
        private final List<Jugador> jugadores = new ArrayList<Jugador>();
        private final Observador vista = new Observador();
        Sesion(int id, int puerto, int otroPuerto) {
            red = new EnsambladorRed(new DireccionPeer(id, "127.0.0.1", puerto));
            red.getDirectorio().agregar(new DireccionPeer(3-id, "127.0.0.1", otroPuerto));
            jugadores.add(new Jugador(1, "Jugador 1", 0));
            jugadores.add(new Jugador(2, "Jugador 2", 0));
            modelo = new ModeloPartida(new FachadaDominioMock(jugadores), new ComunicacionPartida());
            control = new ControlPartida(modelo);
            enlace = new EnlacePartida(modelo, red.getMensajeria());
            red.getMensajeria().registrarReceptor(enlace);
            modelo.attach(vista);
            red.iniciar();
            modelo.notificar();
            enlace.preparar();
        }
        void cerrar() { enlace.cerrar(); modelo.cerrarVista(vista); red.close(); }
    }

    private static int puerto() throws Exception {
        try (ServerSocket reserva = new ServerSocket(0)) { return reserva.getLocalPort(); }
    }
    private static void enSwing(Runnable tarea) throws Exception { SwingUtilities.invokeAndWait(tarea); }
    private static boolean consultar(final Condicion condicion) throws Exception {
        final boolean[] resultado = new boolean[1];
        enSwing(new Runnable() { @Override public void run() { resultado[0] = condicion.cumple(); } });
        return resultado[0];
    }
    private static void verificar(String nombre, boolean cumple) {
        if (!cumple) { throw new AssertionError(nombre); }
        comprobaciones++;
        System.out.println("OK: " + nombre);
    }
    private static void esperar(String nombre, Condicion condicion) throws Exception {
        long fin = System.nanoTime() + 8000000000L;
        while (System.nanoTime() < fin) {
            if (consultar(condicion)) { verificar(nombre, true); return; }
            Thread.sleep(20);
        }
        throw new AssertionError("No se cumplió: " + nombre);
    }
    private static int marcas(ModeloPartida modelo, Jugador jugador) {
        int n = 0;
        for (boolean marca : modelo.getCasillasMarcadas(jugador)) { if (marca) { n++; } }
        return n;
    }

    public static void main(String[] args) throws Exception {
        final int puertoA = puerto();
        int candidato = puerto();
        while (candidato == puertoA) { candidato = puerto(); }
        final int puertoB = candidato;
        try {
            enSwing(new Runnable() { @Override public void run() { a = new Sesion(1, puertoA, puertoB); } });
            verificar("No hay carta antes de estar ambos listos", consultar(new Condicion() {
                @Override public boolean cumple() { return !a.modelo.isIniciada() && a.modelo.getCartaActual() == null; }
            }));
            enSwing(new Runnable() { @Override public void run() { b = new Sesion(2, puertoB, puertoA); } });
            esperar("Inicio compartido y primera carta", new Condicion() {
                @Override public boolean cumple() { return a.modelo.isIniciada() && b.modelo.isIniciada()
                    && a.modelo.getCartasGritadas().size() == 1 && b.modelo.getCartasGritadas().size() == 1; }
            });
            verificar("La carta es la misma en ambos dominios", consultar(new Condicion() {
                @Override public boolean cumple() { return a.modelo.getCartaActual().getIdCarta() == b.modelo.getCartaActual().getIdCarta(); }
            }));
            verificar("Cada tablero tiene las mismas cartas en sus dos copias", consultar(new Condicion() {
                @Override public boolean cumple() {
                    for (int j=0;j<2;j++) for (int i=0;i<16;i++) {
                        if (a.modelo.getCartasTabla(a.jugadores.get(j)).get(i).getIdCarta()
                                != b.modelo.getCartasTabla(b.jugadores.get(j)).get(i).getIdCarta()) { return false; }
                    }
                    return true;
                }
            }));
            enSwing(new Runnable() { @Override public void run() {
                int posicion = b.modelo.getCartasTabla(b.jugadores.get(1)).get(0).getIdCarta()
                    != b.modelo.getCartaActual().getIdCarta() ? 0 : 1;
                b.control.marcarCasilla(b.jugadores.get(1), posicion);
            }});
            esperar("Una marca inválida no modifica ninguna copia", new Condicion() {
                @Override public boolean cumple() { return a.modelo.getAviso().contains("todavía no ha sido gritada")
                    && b.modelo.getAviso().contains("todavía no ha sido gritada")
                    && marcas(a.modelo,a.jugadores.get(1))==0 && marcas(b.modelo,b.jugadores.get(1))==0; }
            });
            enSwing(new Runnable() { @Override public void run() {
                // Solo el anfitrión extrae; todos reciben los identificadores exactos.
                for (int i=1;i<54;i++) { a.control.jalarCarta(); }
            }});
            esperar("Las 54 cartas llegan una vez a ambos bonches", new Condicion() {
                @Override public boolean cumple() { return a.modelo.getCartasGritadas().size()==54 && b.modelo.getCartasGritadas().size()==54; }
            });
            verificar("El orden completo del bonche coincide", consultar(new Condicion() {
                @Override public boolean cumple() {
                    for(int i=0;i<54;i++) if(a.modelo.getCartasGritadas().get(i).getIdCarta()!=b.modelo.getCartasGritadas().get(i).getIdCarta()) return false;
                    return true;
                }
            }));
            enSwing(new Runnable() { @Override public void run() {
                for(int i=0;i<16;i++) { a.control.marcarCasilla(a.jugadores.get(0), i); b.control.marcarCasilla(b.jugadores.get(1), i); }
            }});
            esperar("Las marcas propias y miniaturas remotas coinciden", new Condicion() {
                @Override public boolean cumple() { return marcas(a.modelo,a.jugadores.get(0))==16 && marcas(a.modelo,a.jugadores.get(1))==16
                    && marcas(b.modelo,b.jugadores.get(0))==16 && marcas(b.modelo,b.jugadores.get(1))==16; }
            });
            enSwing(new Runnable() { @Override public void run() {
                b.control.reclamarPremio(b.jugadores.get(1), MetodoVictoria.CHORRO);
                a.control.reclamarPremio(a.jugadores.get(0), MetodoVictoria.CHORRO);
            }});
            esperar("Dos reclamaciones simultáneas otorgan Chorro una sola vez", new Condicion() {
                @Override public boolean cumple() { return a.modelo.getAviso().contains("ya fue reclamado") && b.modelo.getAviso().contains("ya fue reclamado")
                    && a.modelo.getPuntaje(a.jugadores.get(0))+a.modelo.getPuntaje(a.jugadores.get(1))==10
                    && b.modelo.getPuntaje(b.jugadores.get(0))+b.modelo.getPuntaje(b.jugadores.get(1))==10; }
            });
            verificar("Ambos modelos reconocen al mismo ganador de Chorro", consultar(new Condicion() {
                @Override public boolean cumple() { return a.modelo.getPuntaje(a.jugadores.get(0))==b.modelo.getPuntaje(b.jugadores.get(0)); }
            }));
            enSwing(new Runnable() { @Override public void run() { b.control.reclamarPremio(b.jugadores.get(1),MetodoVictoria.CENTRO); } });
            esperar("Puntos de Centro se reflejan en ambos jugadores", new Condicion() {
                @Override public boolean cumple() { return a.modelo.getPuntaje(a.jugadores.get(1))>=30
                    && a.modelo.getPuntaje(a.jugadores.get(1))==b.modelo.getPuntaje(b.jugadores.get(1)); }
            });
            enSwing(new Runnable() { @Override public void run() { b.control.reclamarPremio(b.jugadores.get(1),MetodoVictoria.BUENAS); } });
            esperar("Buenas finaliza ambas copias y detiene el gritón", new Condicion() {
                @Override public boolean cumple() { return a.modelo.isFinalizada() && b.modelo.isFinalizada()
                    && !a.modelo.isAutomatico() && !b.modelo.isAutomatico() && a.modelo.getAviso().equals(b.modelo.getAviso()); }
            });
            verificar("Todos los update(IModelo) se ejecutaron en Swing", consultar(new Condicion() {
                @Override public boolean cumple() { return a.vista.hiloCorrecto && b.vista.hiloCorrecto && a.vista.actualizaciones>30 && b.vista.actualizaciones>30; }
            }));
            final int[] cuentas = new int[2];
            enSwing(new Runnable() { @Override public void run() { cuentas[0]=a.vista.actualizaciones; cuentas[1]=b.vista.actualizaciones; } });
            Thread.sleep(250);
            verificar("No hay bucle de reenvío después de recibir", consultar(new Condicion() {
                @Override public boolean cumple() { return cuentas[0]==a.vista.actualizaciones && cuentas[1]==b.vista.actualizaciones; }
            }));
        } finally {
            enSwing(new Runnable() { @Override public void run() { if(a!=null)a.cerrar(); if(b!=null)b.cerrar(); } });
        }
        a = null; b = null;
        try {
            enSwing(new Runnable() { @Override public void run() {
                a = new Sesion(1, puertoA, puertoB); b = new Sesion(2, puertoB, puertoA);
            }});
            esperar("Una partida nueva puede reutilizar los puertos", new Condicion() {
                @Override public boolean cumple() { return a.modelo.isIniciada() && b.modelo.isIniciada() && b.modelo.getCartaActual()!=null; }
            });
            enSwing(new Runnable() { @Override public void run() { b.control.abandonarPartida(b.jugadores.get(1)); } });
            esperar("Abandonar cancela ambas partidas y actualiza al jugador", new Condicion() {
                @Override public boolean cumple() { return a.modelo.isFinalizada() && b.modelo.isFinalizada()
                    && !a.jugadores.get(1).isActivo() && !b.jugadores.get(1).isActivo(); }
            });
        } finally {
            enSwing(new Runnable() { @Override public void run() { if(a!=null)a.cerrar(); if(b!=null)b.cerrar(); } });
        }
        System.out.println("PARTIDA TCP: " + comprobaciones + " comprobaciones correctas.");
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pruebas;

import Dominio.*;
import Dominio.Partida.MetodoVictoria;
import Comunicacion.ComunicacionPartida;
import MVC_Partida_Modelo.*;
import MVC_Partida_Control.ControlPartida;
import MVC_Partida_Vista.*;
import java.util.*;
import javax.swing.SwingUtilities;

/**
 *
 * @author ori
 */
public class PruebaJuego {
    //pruebas unitarias
    private static int verificaciones;

    private static class VistaPrueba implements IVista {

        Jugador jugador;
        IModelo recibido;
        boolean[] marcas;
        int puntos;
        int llamadas;

        VistaPrueba(Jugador jugador) {
            this.jugador = jugador;
        }

        public void update(IModelo modelo) {
            recibido = modelo;
            marcas = modelo.getCasillasMarcadas(jugador);
            puntos = modelo.getPuntaje(jugador);
            llamadas++;
        }
    }

    private static class Escenario {

        Jugador a = new Jugador(1, "Ori", 0), b = new Jugador(2, "Diego", 0);
        IDominio dominio = new FachadaDominioMock(Arrays.asList(a, b));
        ComunicacionPartida canal = new ComunicacionPartida();
        ModeloPartida modelo = new ModeloPartida(dominio, canal);
        ControlPartida control = new ControlPartida(modelo);
        VistaPrueba v1 = new VistaPrueba(a), v2 = new VistaPrueba(b);

        Escenario() {
            modelo.attach(v1);
            modelo.attach(v2);
            modelo.notificar();
        }

        void marcar(Jugador jugador, int... posiciones) {
            for (int posicion : posiciones) {
                canal.recibirMensaje("CARTA:" + modelo.getCartasTabla(jugador).get(posicion).getIdCarta());
                control.marcarCasilla(jugador, posicion);
            }
        }
    }

    private static void comprobar(boolean ok, String caso) {
        verificaciones++;
        if (!ok) {
            throw new AssertionError(caso);
        }
    }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                ejecutar();
            }
        });
        System.out.println("OK: " + verificaciones + " verificaciones:  ");
    }

    private static void ejecutar() {
        Escenario e = new Escenario();
        comprobar(e.v1.recibido == e.modelo && e.v2.recibido == e.modelo, "Ambas vistas reciben el mismo objeto IModelo");
        comprobar(e.modelo.getCartasTabla(e.a).size() == 16 && !e.modelo.getCartasTabla(e.a).equals(e.modelo.getCartasTabla(e.b)), "Tableros distintos consultando por jugador");
        e.control.marcarCasilla(e.a, 0);
        comprobar(!e.v1.marcas[0], "No marcar antes de gritar");
        e.control.marcarCasilla(e.a, -1);
        e.control.marcarCasilla(e.b, 16);
        comprobar(e.v1.puntos == 0 && e.v2.puntos == 0, "Fuera de rango sin puntos");
        e.modelo.attach(e.v1);
        int n1 = e.v1.llamadas, n2 = e.v2.llamadas;
        e.canal.recibirMensaje("CARTA:" + e.modelo.getCartasTabla(e.a).get(0).getIdCarta());
        comprobar(e.v1.llamadas == n1 + 1 && e.v2.llamadas == n2 + 1, "Una notificación por vista sin registros duplicados");
        e.canal.recibirMensaje("CARTA:" + e.modelo.getCartaActual().getIdCarta());
        comprobar(e.modelo.getCartasGritadas().size() == 1, "Mensaje duplicado no duplica bonche");
        e.control.marcarCasilla(e.a, 0);
        comprobar(e.v1.marcas[0] && !e.v2.marcas[0], "Mismo control/modelo conserva marcas independientes");
        comprobar(e.modelo.getJugadores(e.b).get(0).getCasillasMarcadas()[0], "Miniatura de Ori visible para Diego");
        e.marcar(e.b, 0);
        comprobar(e.v2.marcas[0], "Mismo control permite actuar a Diego");
        comprobar(e.modelo.getJugadores(e.a).get(0).getIdJugador() == 2 && e.modelo.getJugadores(e.b).get(0).getIdJugador() == 1, "Cada consulta excluye su propio jugador");
        boolean[] copia = e.modelo.getCasillasMarcadas(e.a);
        copia[1] = true;
        e.modelo.getCartasTabla(e.a).clear();
        comprobar(!e.modelo.getCasillasMarcadas(e.a)[1] && e.modelo.getCartasTabla(e.a).size() == 16, "Copias defensivas");
        e.control.reclamarPremio(e.a, MetodoVictoria.BUENAS);
        comprobar(!e.modelo.isFinalizada() && e.v1.puntos == 0, "Buenas incompletas rechazadas");
        e.marcar(e.a, 0, 1, 2, 3);
        e.control.reclamarPremio(e.a, MetodoVictoria.CHORRO);
        comprobar(e.v1.puntos == 10 && !e.modelo.isFinalizada(), "Chorro valido actualiza observador");
        e.control.reclamarPremio(e.a, MetodoVictoria.CHORRO);
        comprobar(e.v1.puntos == 10, "Sin doble premio");
        e.marcar(e.b, 0, 1, 2, 3);
        e.control.reclamarPremio(e.b, MetodoVictoria.CHORRO);
        comprobar(e.v2.puntos == 0, "Primer ganador del metodo se conserva");
        e.marcar(e.b, 5, 6, 9, 10);
        e.control.reclamarPremio(e.b, MetodoVictoria.CENTRO);
        comprobar(e.v2.puntos == 30 && e.modelo.getJugadores(e.a).get(0).getPuntaje() == 30, "Puntos de Diego reflejados en ambas consultas");
        e.marcar(e.a, 0, 3, 12, 15);
        e.control.reclamarPremio(e.a, MetodoVictoria.CUATRO_ESQUINAS);
        comprobar(e.v1.puntos == 30, "Cuatro esquinas acumula");
        for (int i = 0; i < 16; i++) {
            e.marcar(e.a, i);
        }
        e.control.reclamarPremio(e.a, MetodoVictoria.BUENAS);
        comprobar(e.modelo.isFinalizada() && e.v1.puntos == 130, "Buenas finaliza");
        comprobar(e.v2.recibido.isFinalizada() && e.v2.recibido.getAviso().contains("Ori"), "Otro observador conoce el ganador");
        int cartas = e.modelo.getCartasGritadas().size();
        String aviso = e.modelo.getAviso();
        e.control.jalarCarta();
        e.control.reclamarPremio(e.b, MetodoVictoria.BUENAS);
        comprobar(cartas == e.modelo.getCartasGritadas().size() && aviso.equals(e.modelo.getAviso()), "Estado final no cambia");
        e.modelo.detach(e.v1);
        n1 = e.v1.llamadas;
        n2 = e.v2.llamadas;
        e.modelo.notificar();
        comprobar(e.v1.llamadas == n1 && e.v2.llamadas == n2 + 1, "detach de una vista mantiene a la otra");
        Escenario otro = new Escenario();
        otro.canal.recibirMensaje(null);
        otro.canal.recibirMensaje("CARTA:abc");
        otro.canal.recibirMensaje("CARTA:999");
        comprobar(otro.modelo.getCartasGritadas().isEmpty(), "Mensajes invalidos sin cartas");
        for (int i = 0; i < 54; i++) {
            otro.control.jalarCarta();
        }
        comprobar(!otro.modelo.hayCartas() && !otro.modelo.isFinalizada(), "Agotar baraja no finaliza");
        otro.control.abandonarPartida(otro.a);
        comprobar(otro.v2.recibido.getAviso().contains("cancelada"), "Abandono se notifica a la otra vista");
        Escenario reloj = new Escenario();
        reloj.control.cambiarAutomatico();
        comprobar(reloj.modelo.isAutomatico(), "Timer iniciado");
        reloj.control.abandonarPartida(reloj.a);
        comprobar(!reloj.modelo.isAutomatico(), "Timer detenido al terminar");
        PanelTablero tablero = new PanelTablero(null);
        tablero.mostrarTablero(e.modelo.getCartasTabla(e.a), e.modelo.getCasillasMarcadas(e.a));
        PanelJugadores miniaturas = new PanelJugadores();
        miniaturas.mostrarJugadores(e.modelo.getJugadores(e.a));
        comprobar(tablero.getComponentCount() == 16 && miniaturas.getComponentCount() == 1, "Tablero y miniatura construidos");
    }
}

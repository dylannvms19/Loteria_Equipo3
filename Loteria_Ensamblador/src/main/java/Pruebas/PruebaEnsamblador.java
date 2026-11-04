package Pruebas;

import Comunicacion.*;
import Ensamblador.EnsambladorRed;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/** Prueba de ensamblado, directorio central y mensajes directos entre dos pares. */
public class PruebaEnsamblador {
    private static int comprobaciones;
    private static int libre() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) { return socket.getLocalPort(); }
    }
    private static void comprobar(String nombre, boolean condicion) {
        if (!condicion) { throw new AssertionError(nombre); }
        comprobaciones++;
        System.out.println("OK: " + nombre);
    }
    private static class Buzon implements IReceptorMensaje {
        private final BlockingQueue<Mensaje> mensajes = new LinkedBlockingQueue<Mensaje>();
        @Override
        public boolean acepta(String tipo) { return tipo.equals("MARCAR_CASILLA") || tipo.equals("CARTA_GRITADA"); }
        @Override
        public void procesar(Mensaje mensaje) { mensajes.add(mensaje); }
        Mensaje siguiente() throws InterruptedException {
            Mensaje m = mensajes.poll(5, TimeUnit.SECONDS);
            if (m == null) { throw new AssertionError("No llegó el mensaje interpretado."); }
            return m;
        }
    }
    public static void main(String[] args) throws Exception {
        DireccionPeer central = new DireccionPeer(0, "127.0.0.1", libre());
        try (EnsambladorRed servidor = new EnsambladorRed(central)) {
            servidor.getMensajeria().registrarReceptor(new ServicioDirectorio(
                    servidor.getDirectorio(), servidor.getMensajeria()));
            servidor.iniciar();
            DireccionPeer propiaA = new DireccionPeer(1, "127.0.0.1", libre());
            try (EnsambladorRed a = new EnsambladorRed(propiaA)) {
                a.iniciar();
                DireccionPeer propiaB = new DireccionPeer(2, "127.0.0.1", libre());
                try (EnsambladorRed b = new EnsambladorRed(propiaB)) {
                    b.iniciar();
                    Buzon entradaA = new Buzon(); Buzon entradaB = new Buzon();
                    a.getMensajeria().registrarReceptor(entradaA);
                    b.getMensajeria().registrarReceptor(entradaB);
                    a.getMensajeria().setServidorCentral(central);
                    b.getMensajeria().setServidorCentral(central);
                    comprobar("Primer registro central sin otros pares", a.getMensajeria().registrarEnServidor().isEmpty());
                    List<DireccionPeer> conocidosB = b.getMensajeria().registrarEnServidor();
                    comprobar("El segundo par descubre al primero", conocidosB.size() == 1 && conocidosB.get(0).equals(propiaA));
                    comprobar("El primero consulta y descubre al segundo", a.getMensajeria().consultarPares().contains(propiaB));
                    a.getMensajeria().enviarMensaje(TipoMensaje.CARTA_GRITADA, "12");
                    Mensaje carta = entradaB.siguiente();
                    comprobar("Bytes vuelven a objeto Mensaje en el otro par", carta.getTipo() == TipoMensaje.CARTA_GRITADA && carta.getArgumentoEntero(0) == 12 && carta.getIdOrigen() == 1);
                    // Con direcciones conocidas, el servidor central no participa del juego.
                    servidor.close();
                    b.getMensajeria().enviarMensaje(TipoMensaje.MARCAR_CASILLA, "4");
                    comprobar("P2P funciona aunque el directorio central esté cerrado", entradaA.siguiente().getArgumentoEntero(0) == 4);
                    a.getMensajeria().enviarMensaje(TipoMensaje.ABANDONAR_PARTIDA);
                    a.getMensajeria().enviarMensaje(TipoMensaje.CARTA_GRITADA, "13");
                    comprobar("El observador filtra por acepta(tipo)", entradaB.siguiente().getTipo() == TipoMensaje.CARTA_GRITADA);
                    SerializadorMensajes serializador = new SerializadorMensajes();
                    a.getMensajeria().recibir(serializador.serializar(carta)); // origen propio: se ignora
                    comprobar("Se ignoran eventos del propio nodo", entradaA.mensajes.poll(150, TimeUnit.MILLISECONDS) == null);
                    b.getMensajeria().recibir(serializador.serializar(carta)); // secuencia vieja
                    comprobar("Se ignoran duplicados recibidos", entradaB.mensajes.poll(150, TimeUnit.MILLISECONDS) == null);
                    b.getMensajeria().retirarReceptor(entradaB);
                    a.getMensajeria().enviarMensaje(TipoMensaje.CARTA_GRITADA, "14");
                    comprobar("Se puede retirar un receptor de Mensajería", entradaB.mensajes.poll(200, TimeUnit.MILLISECONDS) == null);
                }
            }
        }
        boolean pendientes = false;
        for (Thread hilo : Thread.getAllStackTraces().keySet()) {
            if (hilo.isAlive() && hilo.getName().startsWith("loteria-")) { pendientes = true; }
        }
        comprobar("El ensamblador cierra todos los trabajadores", !pendientes);
        System.out.println("ENSAMBLADOR: " + comprobaciones + " comprobaciones correctas.");
    }
}

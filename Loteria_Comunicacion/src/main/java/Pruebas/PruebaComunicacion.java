
package Pruebas;

import Comunicacion.Comunicacion;
import Comunicacion.DireccionPeer;
import Comunicacion.DirectorioPares;
import Comunicacion.IMensajeria;
import Comunicacion.Mensaje;
import Comunicacion.Mensajeria;
import Comunicacion.TipoMensaje;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Prueba manual del módulo de comunicación (sin JUnit): ejecútala con "Run File" y revisa la consola.
 * Cada verificación imprime [OK] o [FALLO]; al final se resume. Todo corre en un solo proceso
 * con puertos libres elegidos automáticamente, y usa un servidor central de mentira.
 * 
 * @author Dylan
 */
public class PruebaComunicacion {

    private static int ok = 0;
    private static int fallos = 0;
    private static final List<IMensajeria> abiertos = new ArrayList<IMensajeria>();

    public static void main(String[] args) throws Exception {
        try {
            escenario1MallaConServidor();
            escenario2Orden();
            escenario3ParCaido();
            escenario4ServidorInexistente();
            escenario5Directorio();
            escenario6CierreOrdenado();
        } finally {
            for (IMensajeria m : abiertos) {
                m.detener();
            }
        }
        System.out.println();
        System.out.println("Resultado: " + ok + " correctas, " + fallos + " con fallo.");
        System.exit(fallos == 0 ? 0 : 1);
    }

    // ---------------------------------------------------------------- escenarios

    private static void escenario1MallaConServidor() throws Exception {
        titulo("1. Tres pares se registran en el servidor, se descubren y se comunican");
        try (ServidorFalso servidor = new ServidorFalso()) {
            Nodo a = new Nodo(1, servidor.direccion());
            Nodo b = new Nodo(2, servidor.direccion());
            Nodo c = new Nodo(3, servidor.direccion());

            verificar("A es el primero en registrarse: no ve a nadie", a.msj.registrarEnServidor().isEmpty());
            verificar("B se registra y ve 1 par (A)", b.msj.registrarEnServidor().size() == 1);
            verificar("C se registra y ve 2 pares (A y B)", c.msj.registrarEnServidor().size() == 2);
            verificar("A consulta y descubre a B y C", a.msj.consultarPares().size() == 2);
            verificar("B consulta y descubre a A y C", b.msj.consultarPares().size() == 2);

            a.msj.iniciar();
            b.msj.iniciar();
            c.msj.iniciar();

            a.msj.enviarMensaje(TipoMensaje.CARTA_GRITADA, "17");
            Mensaje mb = b.esperar();
            Mensaje mc = c.esperar();
            verificar("B recibe CARTA_GRITADA 17 enviada por A",
                    mb != null && mb.getTipo() == TipoMensaje.CARTA_GRITADA && mb.getIdOrigen() == 1
                    && mb.getArgumentoEntero(0) == 17);
            verificar("C recibe CARTA_GRITADA 17 enviada por A", mc != null && mc.getArgumentoEntero(0) == 17);

            b.msj.enviarMensaje(TipoMensaje.MARCAR_CASILLA, "5");
            Mensaje ma = a.esperar();
            Mensaje mc2 = c.esperar();
            verificar("A recibe MARCAR_CASILLA 5 de B", ma != null && ma.getIdOrigen() == 2);
            verificar("C recibe MARCAR_CASILLA 5 de B", mc2 != null && mc2.getIdOrigen() == 2);

            Thread.sleep(300);
            verificar("Nadie recibe su propio mensaje (sin eco)", a.recibidos.isEmpty() && b.recibidos.isEmpty());
            verificar("A ve 2 pares alcanzables", a.msj.getPeersAlcanzables() == 2);
        }
    }

    private static void escenario2Orden() throws Exception {
        titulo("2. 100 mensajes seguidos llegan completos y en orden");
        Nodo a = new Nodo(1, null);
        Nodo b = new Nodo(2, null);
        a.agregarPar(b);
        b.agregarPar(a);
        a.msj.iniciar();
        b.msj.iniciar();
        for (int i = 0; i < 100; i++) {
            a.msj.enviarMensaje(TipoMensaje.CARTA_GRITADA, String.valueOf(i));
        }
        boolean enOrden = true;
        for (int i = 0; i < 100; i++) {
            Mensaje m = b.esperar();
            if (m == null || m.getArgumentoEntero(0) != i) {
                enOrden = false;
                break;
            }
        }
        verificar("B recibió las 100 cartas, de la 0 a la 99, en orden", enOrden);
    }

    private static void escenario3ParCaido() throws Exception {
        titulo("3. Un par caído no detiene a los demás");
        Nodo a = new Nodo(1, null);
        Nodo b = new Nodo(2, null);
        Nodo c = new Nodo(3, null); // C nunca se inicia
        a.agregarPar(b);
        a.agregarPar(c);
        b.agregarPar(a);
        b.agregarPar(c);
        a.msj.iniciar();
        b.msj.iniciar();
        long t0 = System.currentTimeMillis();
        a.msj.enviarMensaje(TipoMensaje.CARTA_GRITADA, "3");
        Mensaje m = b.esperar();
        long ms = System.currentTimeMillis() - t0;
        verificar("B recibe el mensaje aunque C esté caído (" + ms + " ms)", m != null && ms < 1500);
    }

    private static void escenario4ServidorInexistente() throws Exception {
        titulo("4. Si el servidor central no existe, se avisa con IOException");
        Nodo a = new Nodo(1, new DireccionPeer(0, "127.0.0.1", puertoLibre()));
        boolean lanzo = false;
        try {
            a.msj.registrarEnServidor();
        } catch (IOException e) {
            lanzo = true;
        }
        verificar("registrarEnServidor lanza IOException", lanzo);
        a.msj.iniciar();
        verificar("el nodo igual puede arrancar", true);
    }

    private static void escenario5Directorio() throws Exception {
        titulo("5. Directorio de pares y mensajes de directorio");
        DirectorioPares d = new DirectorioPares();
        d.agregar(new DireccionPeer(2, "10.0.0.2", 6002));
        d.agregar(new DireccionPeer(1, "10.0.0.1", 6001));
        verificar("serializar da la lista ordenada por id: " + d.serializar(),
                d.serializar().equals("1@10.0.0.1:6001;2@10.0.0.2:6002"));
        verificar("parsearLista recupera lo serializado", DirectorioPares.parsearLista(d.serializar()).equals(d.obtener()));
        boolean rechazo = false;
        try {
            new Nodo(1, null).msj.enviarMensaje(TipoMensaje.CONSULTAR_PARES);
        } catch (IllegalArgumentException e) {
            rechazo = true;
        }
        verificar("enviarMensaje rechaza mensajes de directorio entre pares", rechazo);
    }

    private static void escenario6CierreOrdenado() throws Exception {
        titulo("6. Al detener un nodo, lo pendiente se entrega");
        Nodo a = new Nodo(1, null);
        Nodo b = new Nodo(2, null);
        a.agregarPar(b);
        b.agregarPar(a);
        a.msj.iniciar();
        b.msj.iniciar();
        a.msj.enviarMensaje(TipoMensaje.ABANDONAR_PARTIDA);
        a.msj.detener();
        Mensaje m = b.esperar();
        verificar("B recibe ABANDONAR_PARTIDA aunque A se cerró enseguida",
                m != null && m.getTipo() == TipoMensaje.ABANDONAR_PARTIDA);
    }

    // ---------------------------------------------------------------- utilidades

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("== " + texto);
    }

    private static void verificar(String descripcion, boolean condicion) {
        if (condicion) {
            ok++;
        } else {
            fallos++;
        }
        System.out.println((condicion ? "  [OK]    " : "  [FALLO] ") + descripcion);
    }

    private static int puertoLibre() throws IOException {
        try (ServerSocket s = new ServerSocket(0)) {
            return s.getLocalPort();
        }
    }

    /** Un par completo armado como lo hará el Ensamblador, con su buzón de mensajes recibidos. */
    private static class Nodo {

        final int id;
        final int puerto;
        final BlockingQueue<Mensaje> recibidos = new LinkedBlockingQueue<Mensaje>();
        final DirectorioPares directorio = new DirectorioPares();
        final IMensajeria msj;

        Nodo(int id, DireccionPeer servidor) throws IOException {
            this.id = id;
            this.puerto = puertoLibre();
            this.msj = new Mensajeria(direccion(), new Comunicacion(puerto, servidor), directorio);
            this.msj.registrarReceptor(recibidos::add);
            abiertos.add(msj);
        }

        DireccionPeer direccion() {
            return new DireccionPeer(id, "127.0.0.1", puerto);
        }

        void agregarPar(Nodo otro) {
            directorio.agregar(otro.direccion());
        }

        Mensaje esperar() throws InterruptedException {
            return recibidos.poll(8, TimeUnit.SECONDS);
        }
    }

    /** Servidor central de mentira: guarda los registros y responde RESPUESTA_PARES. */
    private static class ServidorFalso implements AutoCloseable {

        final ServerSocket socket;
        final DirectorioPares directorio = new DirectorioPares();

        ServidorFalso() throws IOException {
            socket = new ServerSocket(0);
            Thread hilo = new Thread(() -> {
                while (!socket.isClosed()) {
                    try (Socket s = socket.accept()) {
                        Mensaje m = Mensaje.leer(new DataInputStream(s.getInputStream()));
                        if (m.getTipo() == TipoMensaje.REGISTRAR_PAR) {
                            directorio.agregar(new DireccionPeer(m.getArgumentoEntero(0), m.getArgumento(1),
                                    m.getArgumentoEntero(2)));
                        }
                        new Mensaje(TipoMensaje.RESPUESTA_PARES, 0, 1, List.of(directorio.serializar()))
                                .escribir(new DataOutputStream(s.getOutputStream()));
                    } catch (IOException e) {
                        // servidor cerrado o petición inválida: se ignora
                    }
                }
            });
            hilo.setDaemon(true);
            hilo.start();
        }

        DireccionPeer direccion() {
            return new DireccionPeer(0, "127.0.0.1", socket.getLocalPort());
        }

        @Override
        public void close() throws IOException {
            socket.close();
        }
    }
}

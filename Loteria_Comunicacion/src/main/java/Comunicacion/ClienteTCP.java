
package Comunicacion;


import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Mod Dylan: Lado cliente del nodo hacia UN par, con conexiones INTERMITENTES: por cada mensaje abre un
 * Socket, lo envía, espera la confirmación (ACK) y cierra. Toma los mensajes de su propia Cola.
 *
 * - Si el par no responde, reintenta unas veces; si sigue caído descarta ese mensaje y continúa
 *   (un par caído no detiene a los demás: tolerancia a fallos del estilo P2P).
 * - Como el siguiente mensaje solo se envía tras el ACK del anterior, el orden se conserva.
 * 
 * @author MrGam
 */
public class ClienteTCP implements Runnable {

    static final int ACK = 1;
    static final int MAX_INTENTOS = 6;
    static final int ESPERA_REINTENTO_MS = 500;
    static final int TIMEOUT_MS = 2000;
    static final int CAPACIDAD_COLA = 1000;

    private static final System.Logger LOG = System.getLogger(ClienteTCP.class.getName());

    private final DireccionPeer destino;
    private final BlockingQueue<Mensaje> cola = new LinkedBlockingQueue<Mensaje>(CAPACIDAD_COLA);
    private volatile boolean activo = true;
    private volatile boolean drenando;
    private volatile boolean alcanzable;
    private Thread hilo;

    ClienteTCP(DireccionPeer destino) {
        this.destino = destino;
    }

    void iniciar() {
        hilo = new Thread(this, "p2p-cliente-" + destino.idPeer());
        hilo.setDaemon(true);
        hilo.start();
    }

    void encolar(Mensaje mensaje) {
        if (!cola.offer(mensaje)) {
            LOG.log(System.Logger.Level.ERROR,
                    "Cola llena hacia el peer " + destino.idPeer() + "; se descarta " + mensaje);
        }
    }

    boolean isAlcanzable() {
        return alcanzable;
    }

    @Override
    public void run() {
        while (activo) {
            Mensaje mensaje;
            try {
                mensaje = cola.poll(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                break;
            }
            if (mensaje == null) {
                if (drenando) {
                    break; // cierre ordenado: no queda nada por enviar
                }
                continue;
            }
            enviarConReintentos(mensaje);
        }
    }

    private void enviarConReintentos(Mensaje mensaje) {
        for (int intento = 1; intento <= MAX_INTENTOS && activo; intento++) {
            try {
                enviarUnaVez(mensaje);
                alcanzable = true;
                return;
            } catch (IOException e) {
                alcanzable = false;
                if (intento < MAX_INTENTOS) {
                    try {
                        Thread.sleep(ESPERA_REINTENTO_MS);
                    } catch (InterruptedException ie) {
                        return;
                    }
                }
            }
        }
        if (activo) {
            LOG.log(System.Logger.Level.WARNING,
                    "Peer " + destino.idPeer() + " no responde; se descarta " + mensaje);
        }
    }

    /** Una conexión completa: abrir, enviar, esperar ACK, cerrar. */
    private void enviarUnaVez(Mensaje mensaje) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(destino.host(), destino.puerto()), TIMEOUT_MS);
            socket.setSoTimeout(TIMEOUT_MS);
            socket.setTcpNoDelay(true);
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            mensaje.escribir(out);
            if (new DataInputStream(socket.getInputStream()).read() != ACK) {
                throw new IOException("El peer " + destino.idPeer() + " no confirmó la recepción");
            }
        }
    }

    /** Cierre ordenado: envía lo que haya en cola (hasta maxMs) y luego se detiene. */
    void detenerEnviandoPendientes(long maxMs) {
        drenando = true;
        if (hilo != null) {
            try {
                hilo.join(maxMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        detener();
    }

    void detener() {
        activo = false;
        if (hilo != null) {
            hilo.interrupt();
        }
    }
}

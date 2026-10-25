
package Comunicacion;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lado servidor del nodo: escucha en un puerto y, por cada conexión que un par abre hacia nosotros,
 * crea un {@link Receptor}. El ServerSocket nunca se bloquea atendiendo a un cliente, así
 * puede seguir aceptando nuevas solicitudes.
 * 
 * @author Dylan
 */
public class ServidorTCP implements Runnable {

    private static final System.Logger LOG = System.getLogger(ServidorTCP.class.getName());

    private final int puerto;
    private final IReceptorExterno destino;
    private final Set<Receptor> receptores = ConcurrentHashMap.newKeySet();
    private volatile ServerSocket serverSocket;
    private volatile boolean activo;
    private volatile Thread hilo;

    ServidorTCP(int puerto, IReceptorExterno destino) {
        this.puerto = puerto;
        this.destino = destino;
    }

    /** Abre el puerto de forma síncrona para que un puerto ocupado falle de inmediato. */
    void iniciar() throws IOException {
        ServerSocket ss = new ServerSocket();
        try {
            ss.setReuseAddress(true);
            ss.bind(new InetSocketAddress(puerto));
        } catch (IOException e) {
            ss.close();
            throw e;
        }
        serverSocket = ss;
        activo = true;
        hilo = new Thread(this, "p2p-servidor-" + puerto);
        hilo.setDaemon(true);
        hilo.start();
    }

    @Override
    public void run() {
        while (activo) {
            try {
                Socket socket = serverSocket.accept();
                final Receptor receptor = new Receptor(socket, destino, receptores);
                receptores.add(receptor);
                receptor.iniciar();
            } catch (IOException e) {
                if (activo) {
                    LOG.log(System.Logger.Level.WARNING, "Error aceptando conexión: " + e.getMessage());
                }
            }
        }
    }

    void detener() {
        activo = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            LOG.log(System.Logger.Level.WARNING, "Error cerrando el puerto: " + e.getMessage());
        }
        // Esperar al hilo de escucha garantiza que el puerto quede libre al volver de este método.
        Thread h = hilo;
        if (h != null && h != Thread.currentThread()) {
            try {
                h.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        for (Receptor r : receptores) {
            r.cerrar();
        }
        receptores.clear();
    }
}

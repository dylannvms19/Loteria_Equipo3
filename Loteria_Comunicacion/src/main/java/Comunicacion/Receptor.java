
package Comunicacion;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Set;

/**
 * Atiende UNA conexión entrante (esquema intermitente): lee un mensaje, lo entrega al
 * {@link IReceptorExterno}, responde con el ACK y cierra. Un mensaje malformado cierra la
 * conexión sin ACK y nunca afecta al resto del nodo.
 * 
 * @author Dylan
 */
public class Receptor implements Runnable {

    private static final System.Logger LOG = System.getLogger(Receptor.class.getName());
    private static final int TIMEOUT_LECTURA_MS = 5000;

    private final Socket socket;
    private final IReceptorExterno destino;
    private final Set<Receptor> registro;

    Receptor(Socket socket, IReceptorExterno destino, Set<Receptor> registro) {
        this.socket = socket;
        this.destino = destino;
        this.registro = registro;
    }

    void iniciar() {
        Thread hilo = new Thread(this, "p2p-receptor-" + socket.getRemoteSocketAddress());
        hilo.setDaemon(true);
        hilo.start();
    }

    @Override
    public void run() {
        try (Socket s = socket) {
            s.setSoTimeout(TIMEOUT_LECTURA_MS);
            Mensaje mensaje = Mensaje.leer(new DataInputStream(new BufferedInputStream(s.getInputStream())));
            destino.recibirMensaje(mensaje);
            s.getOutputStream().write(ClienteTCP.ACK);
            s.getOutputStream().flush();
        } catch (IOException | IllegalArgumentException e) {
            LOG.log(System.Logger.Level.WARNING,
                    "Conexión entrante descartada (" + socket.getRemoteSocketAddress() + "): " + e.getMessage());
        } catch (RuntimeException e) {
            LOG.log(System.Logger.Level.ERROR, "El receptor externo falló: " + e);
        } finally {
            registro.remove(this); // la conexión ya terminó: no se acumula en el servidor
        }
    }

    void cerrar() {
        try {
            socket.close();
        } catch (IOException e) {
            LOG.log(System.Logger.Level.DEBUG, "Error cerrando socket: " + e.getMessage());
        }
    }
}

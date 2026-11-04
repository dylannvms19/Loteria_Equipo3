package Comunicacion;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

/** Escucha TCP y coloca mensajes completos en la cola que recibió del Receptor. */
public class ServidorTCP implements Runnable {
    private ServerSocket servidor;
    private Socket conexion;
    private final BlockingQueue<byte[]> colaEntrada;
    private final int puertoLocal;
    private volatile boolean activo;
    private boolean cerrado;
    private Thread hilo;

    public ServidorTCP(int puertoLocal, BlockingQueue<byte[]> colaEntrada) {
        if (puertoLocal < 1 || puertoLocal > 65535 || colaEntrada == null) {
            throw new IllegalArgumentException("Puerto o cola inválidos.");
        }
        this.puertoLocal = puertoLocal;
        this.colaEntrada = colaEntrada;
    }

    public synchronized void iniciar() {
        if (cerrado) { throw new IllegalStateException("ServidorTCP cerrado; cree otro."); }
        if (activo) { return; }
        try {
            servidor = new ServerSocket();
            servidor.setReuseAddress(true);
            servidor.bind(new InetSocketAddress(puertoLocal));
        } catch (IOException e) {
            if (servidor != null) {
                try { servidor.close(); } catch (IOException ignorado) { }
            }
            throw new IllegalStateException("No se puede escuchar en " + puertoLocal, e);
        }
        activo = true;
        hilo = new Thread(this, "loteria-servidor-" + puertoLocal);
        hilo.start();
    }

    @Override
    public void run() {
        while (activo) {
            try (Socket entrante = servidor.accept()) {
                synchronized (this) {
                    if (!activo) { break; }
                    conexion = entrante;
                }
                entrante.setSoTimeout(2000);
                DataInputStream entrada = new DataInputStream(entrante.getInputStream());
                int longitud = entrada.readInt();
                if (longitud < 0 || longitud > Envio.MAX_BYTES) {
                    throw new IOException("Longitud inválida: " + longitud);
                }
                byte[] datos = new byte[longitud];
                entrada.readFully(datos);
                // Es LA MISMA cola de entrada del Receptor.
                colaEntrada.put(datos);
            } catch (IOException e) {
                if (activo) { System.err.println("Recepción TCP: " + e.getMessage()); }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } finally {
                synchronized (this) { conexion = null; }
            }
        }
    }

    public void cerrar() {
        Thread trabajador;
        synchronized (this) {
            cerrado = true;
            activo = false;
            trabajador = hilo;
            if (trabajador != null) { trabajador.interrupt(); }
            try { if (servidor != null) { servidor.close(); } }
            catch (IOException e) { System.err.println("Cierre de servidor: " + e.getMessage()); }
            try { if (conexion != null) { conexion.close(); } }
            catch (IOException e) { System.err.println("Cierre de conexión: " + e.getMessage()); }
        }
        if (trabajador != null && trabajador != Thread.currentThread()) {
            try { trabajador.join(2500); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }
}

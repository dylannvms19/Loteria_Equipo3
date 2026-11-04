package Comunicacion;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/** Una cola de salida y un trabajador que realiza los envíos en orden. */
public class Dispatcher implements IDispatcher, Runnable {
    private final BlockingQueue<Envio> colaSalida = new LinkedBlockingQueue<Envio>(256);
    private final ClienteTCP cliente;
    private volatile boolean activo;
    private boolean cerrado;
    private int pendientes;
    private Thread hilo;

    public Dispatcher(ClienteTCP cliente) {
        if (cliente == null) { throw new IllegalArgumentException("Falta ClienteTCP."); }
        this.cliente = cliente;
    }

    @Override
    public synchronized void iniciar() {
        if (cerrado) { throw new IllegalStateException("Dispatcher cerrado; cree otro."); }
        if (activo) { return; }
        activo = true;
        hilo = new Thread(this, "loteria-dispatcher");
        hilo.start();
    }

    @Override
    public synchronized void enviar(byte[] datos, String ip, int puerto) {
        if (!activo || cerrado) { throw new IllegalStateException("Dispatcher no iniciado o cerrado."); }
        if (!colaSalida.offer(new Envio(datos, ip, puerto))) {
            throw new IllegalStateException("Cola de salida llena.");
        }
        pendientes++;
    }

    @Override
    public void run() {
        while (activo) {
            try {
                Envio envio = colaSalida.take();
                try { if (activo) { cliente.enviar(envio); } }
                finally { synchronized (this) { pendientes--; notifyAll(); } }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (IOException e) {
                if (activo) { System.err.println("No se pudo enviar: " + e.getMessage()); }
            }
        }
    }

    /** Espera hasta 2.5 s los envíos encolados; luego cancela lo que reste. */
    @Override
    public void cerrar() {
        Thread trabajador;
        synchronized (this) {
            cerrado = true;
            long fin = System.nanoTime() + 2500000000L;
            while (activo && pendientes > 0 && System.nanoTime() < fin) {
                try { wait(50); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
            }
            activo = false;
            colaSalida.clear();
            trabajador = hilo;
            if (trabajador != null) { trabajador.interrupt(); }
        }
        cliente.cerrar();
        if (trabajador != null && trabajador != Thread.currentThread()) {
            try { trabajador.join(2500); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }
}

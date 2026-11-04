package Comunicacion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/** Consume su cola de entrada y notifica a observadores de bytes. */
public class Receptor implements Runnable {
    private final BlockingQueue<byte[]> colaEntrada = new LinkedBlockingQueue<byte[]>(256);
    private final List<IReceptorExterno> observadores = new ArrayList<IReceptorExterno>();
    private final ServidorTCP servidor;
    private volatile boolean activo;
    private boolean cerrado;
    private Thread hilo;

    public Receptor(int puertoLocal) {
        servidor = new ServidorTCP(puertoLocal, colaEntrada);
    }

    public synchronized void attach(IReceptorExterno observador) {
        if (observador == null) { throw new IllegalArgumentException("Observador nulo."); }
        if (!observadores.contains(observador)) { observadores.add(observador); }
    }

    public synchronized void detach(IReceptorExterno observador) {
        observadores.remove(observador);
    }

    public synchronized void iniciar() {
        if (cerrado) { throw new IllegalStateException("Receptor cerrado; cree otro."); }
        if (activo) { return; }
        servidor.iniciar();
        activo = true;
        hilo = new Thread(this, "loteria-receptor");
        hilo.start();
    }

    @Override
    public void run() {
        while (activo) {
            try {
                byte[] datos = colaEntrada.take();
                if (activo) { notificar(datos); }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void notificar(byte[] datos) {
        List<IReceptorExterno> copia;
        synchronized (this) { copia = new ArrayList<IReceptorExterno>(observadores); }
        for (IReceptorExterno observador : copia) {
            if (!activo) { break; }
            try { observador.recibir(Arrays.copyOf(datos, datos.length)); }
            catch (RuntimeException e) {
                System.err.println("Falló un observador: " + e.getMessage());
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
        }
        servidor.cerrar();
        if (trabajador != null && trabajador != Thread.currentThread()) {
            try { trabajador.join(2500); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        colaEntrada.clear();
    }
}

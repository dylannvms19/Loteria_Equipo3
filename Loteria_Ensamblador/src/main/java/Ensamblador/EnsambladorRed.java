package Ensamblador;

import Comunicacion.*;

/** Crea y conecta las instancias. No transporta ni interpreta mensajes. */
public class EnsambladorRed implements AutoCloseable {
    private final IDispatcher dispatcher;
    private final Receptor receptor;
    private final Mensajeria mensajeria;
    private final DirectorioPares directorio;
    private boolean iniciado;
    private boolean cerrado;

    public EnsambladorRed(DireccionPeer propia) {
        directorio = new DirectorioPares();
        dispatcher = DispatcherFactory.crear();
        receptor = new Receptor(propia.puerto());
        mensajeria = new Mensajeria(propia, dispatcher, directorio, new SerializadorMensajes());
        receptor.attach(mensajeria);
    }

    public DirectorioPares getDirectorio() { return directorio; }
    public Mensajeria getMensajeria() { return mensajeria; }

    public synchronized void iniciar() {
        if (cerrado) { throw new IllegalStateException("El ensamblador ya se cerró."); }
        if (iniciado) { return; }
        dispatcher.iniciar();
        try {
            receptor.iniciar();
            iniciado = true;
        } catch (RuntimeException e) {
            close();
            throw e;
        }
    }

    @Override
    public void close() {
        synchronized (this) {
            if (cerrado) { return; }
            cerrado = true;
        }
        receptor.cerrar();
        dispatcher.cerrar();
        receptor.detach(mensajeria);
    }
}

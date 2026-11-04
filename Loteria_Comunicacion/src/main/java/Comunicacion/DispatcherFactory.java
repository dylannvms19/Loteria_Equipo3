package Comunicacion;

/** Construye el emisor concreto sin exponerlo a quien pide IDispatcher. */
public final class DispatcherFactory {
    private DispatcherFactory() { }

    public static IDispatcher crear() {
        return new Dispatcher(new ClienteTCP());
    }
}

package Comunicacion;

/**
 * Abstracción que el módulo de comunicación usa para entregar los mensajes que llegan de otros pares.
 * Es el punto de inversión de dependencias: la comunicación NO conoce al modelo;
 * el modelo implementa esta interfaz y se la inyecta al Dispatcher.
 *
 * Se invoca desde un hilo de red, NO desde el hilo de Swing: quien la implemente debe cambiar de
 * hilo (SwingUtilities.invokeLater) antes de tocar la interfaz gráfica.
 *
 * @author Dylan
 */

@FunctionalInterface
public interface IReceptorExterno {
    void recibirMensaje(Mensaje mensaje);
}

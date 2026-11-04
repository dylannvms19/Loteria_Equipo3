package Comunicacion;

/** Observador de Mensajería: recibe objetos, no bytes ni sockets. */
public interface IReceptorMensaje {
    boolean acepta(String tipo);
    void procesar(Mensaje mensaje);
}

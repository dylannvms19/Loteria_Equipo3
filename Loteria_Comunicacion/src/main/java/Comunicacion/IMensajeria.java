package Comunicacion;

import java.io.IOException;
import java.util.List;

/** Solicitudes del juego a Mensajería, sin direcciones ni serialización en MVC. */
public interface IMensajeria {
    void registrarReceptor(IReceptorMensaje receptor);
    void retirarReceptor(IReceptorMensaje receptor);
    void enviarMensaje(TipoMensaje tipo, String... argumentos);
    List<DireccionPeer> registrarEnServidor() throws IOException;
    List<DireccionPeer> consultarPares() throws IOException;
    void enviarAJugador(int id, TipoMensaje tipo, String... argumentos);
    int getIdNodo();
}

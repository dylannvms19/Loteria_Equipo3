package Comunicacion;

/** Contrato para solicitar envíos; no conoce cartas, jugadores ni directorios. */
public interface IDispatcher {
    void enviar(byte[] datos, String ip, int puerto);
    void iniciar();
    void cerrar();
}

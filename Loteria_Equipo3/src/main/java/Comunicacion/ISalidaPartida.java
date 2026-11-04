package Comunicacion;

/** El modelo solicita acciones sin conocer cómo se distribuyen. */
public interface ISalidaPartida {
    void solicitar(AccionPartida accion, int idJugador, String valor);
}

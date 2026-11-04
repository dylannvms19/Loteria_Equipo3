
package Comunicacion;

/**
 * Tipos de mensaje del protocolo P2P de La Lotería.
 * Cada tipo declara cuántos argumentos de texto transporta;
 * el receptor rechaza cualquier mensaje que no cumpla con esa aridad.
 *
 * @author Dylan
 */
public enum TipoMensaje {
    
    /** Un nodo (el gritón) anuncia la carta que salió. Args: idCarta. */
    CARTA_GRITADA(1, false),
    LISTO(1, false),
    INICIAR_PARTIDA(2, false),
    SOLICITAR_ACCION(3, false),
    ACCION_ORDENADA(5, false),
    ERROR_PARTIDA(1, false),
    /** Un jugador marcó una casilla. Args: posicion. */
    MARCAR_CASILLA(1, false),
    /** Un jugador reclama un premio. Args: nombre del MetodoVictoria. */
    RECLAMAR_PREMIO(1, false),
    /** Un jugador abandona la partida. Sin args. */
    ABANDONAR_PARTIDA(0, false),

    /** Par -> servidor: se da de alta en el directorio. Args: idPar, host, puerto. */
    REGISTRAR_PAR(3, true),
    /** Par -> servidor: pide la lista de pares registrados. Sin args. */
    CONSULTAR_PARES(0, true),
    /** Servidor -> par: lista de pares como "id@host:puerto;id@host:puerto". Args: lista (puede ir vacía). */
    RESPUESTA_PARES(1, true);

    private final int argumentos;
    private final boolean deDirectorio;

    TipoMensaje(int argumentos, boolean deDirectorio) {
        this.argumentos = argumentos;
        this.deDirectorio = deDirectorio;
    }

    public int getArgumentos() {
        return argumentos;
    }

    /** true si es un mensaje par <-> servidor central (no circula entre pares). */
    public boolean esDeDirectorio() {
        return deDirectorio;
    }
    
}

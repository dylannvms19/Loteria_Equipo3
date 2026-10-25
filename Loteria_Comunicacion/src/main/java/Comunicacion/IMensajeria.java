package Comunicacion;

import java.io.IOException;
import java.util.List;

/**
 * Punto de entrada de la Mensajería del Par (la caja "Mensajería" del diagrama). Sabe QUÉ se dice:
 * arma los mensajes, descarta propios y duplicados, mantiene el Directorio de pares y entrega las
 * acciones recibidas al modelo. El CÓMO viajan por TCP lo resuelve la {@link IComunicacion}.
 * 
 * @author Dylan
 */
public interface IMensajeria {

    // Inyección por setter de quien recibirá los mensajes remotos. Debe hacerse antes de {@link #iniciar()}. 
    void registrarReceptor(IReceptorExterno receptor);

    // Abre el puerto de escucha y arranca los hilos de envío hacia los pares del directorio.
    void iniciar();

    // Difunde un mensaje de juego a todos los pares (no se entrega de vuelta al propio nodo).
    void enviarMensaje(TipoMensaje tipo, String... argumentos);

    /**
     * Se da de alta en el servidor central y actualiza el directorio con lo que éste responde.
     * @return los demás pares conocidos (sin este nodo)
     */
    List<DireccionPeer> registrarEnServidor() throws IOException;

    /**
     * Pide al servidor la lista de pares y actualiza el directorio.
     * @return los demás pares conocidos (sin este nodo)
     */
    List<DireccionPeer> consultarPares() throws IOException;

    // Cierre ordenado: envía lo pendiente (máx. ~1.5 s), cierra el puerto y detiene los hilos. 
    void detener();

    // Id de este nodo (coincide con el id del jugador local). 
    int getIdNodo();

    // Cantidad de pares que respondieron en su último envío (las conexiones son intermitentes). 
    int getPeersAlcanzables();
}


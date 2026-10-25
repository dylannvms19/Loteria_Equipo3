
package Comunicacion;

import java.io.IOException;
import java.util.List;

/**
 * Capa de Comunicación del Par: solo mueve mensajes por TCP.
 * Hacia los demás pares usa conexiones intermitentes con cola por destino; hacia el servidor central
 * usa petición/respuesta. No interpreta los mensajes: eso es trabajo de la Mensajería.
 *
 * @author Dylan
 */
public interface IComunicacion {
    
    //A quién se le entregan los mensajes que llegan de otros pares. Antes de {@link #iniciar()}
    void registrarReceptor(IReceptorExterno receptor);

    //Abre el puerto de escucha y arranca los hilos de envío. Lanza IllegalStateException si el puerto está ocupado.
    void iniciar();

    //Encola el mensaje hacia todos los pares conocidos. 
    void enviarAPares(Mensaje mensaje);

    // Agrega los pares que todavía no se conocían (los ya conocidos se ignoran). Puede llamarse en cualquier momento.
    void actualizarPares(List<DireccionPeer> pares);

    // Petición/respuesta con el servidor central. Lanza IOException si no hay servidor o no responde.
    Mensaje enviarAlServidor(Mensaje peticion) throws IOException;

    // Cierre ordenado: envía lo pendiente (máx. ~1.5 s), cierra el puerto y detiene los hilos. 
    void detener();

    //Cantidad de pares que respondieron en su último envío.
    int getPeersAlcanzables();
}


package Comunicacion;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**  
 * Comunicación del Par. Reúne las piezas TCP:
 * - {@link ServidorTCP}: escucha y recibe de los demás pares (entrega vía {@link IReceptorExterno}).
 * - {@link ClienteTCP} (uno por par, cada uno con su Cola): envía a cada uno de los demás.
 * - {@link ClienteServidorTCP}: petición/respuesta hacia el servidor central.
 * No interpreta los mensajes ni filtra duplicados: eso lo hace la Mensajería.
 * @author Dylan
 */
public class Comunicacion implements IComunicacion {

    private static final long ESPERA_CIERRE_MS = 1500;

    private final ServidorTCP servidor;
    private final ClienteServidorTCP clienteServidor; // null si no hay servidor central configurado
    private final Map<Integer, ClienteTCP> clientes = new ConcurrentHashMap<Integer, ClienteTCP>();
    private volatile IReceptorExterno receptor;
    private boolean iniciado;
    private boolean detenido;

    /**
     * @param puertoLocal     puerto donde este nodo escucha a los demás pares
     * @param servidorCentral dirección del servidor central, o null si no se usará
     */
    public Comunicacion(int puertoLocal, DireccionPeer servidorCentral) {
        if (puertoLocal < 1 || puertoLocal > 65535) {
            throw new IllegalArgumentException("Puerto local fuera de rango: " + puertoLocal);
        }
        this.servidor = new ServidorTCP(puertoLocal, this::entregar);
        this.clienteServidor = servidorCentral == null ? null : new ClienteServidorTCP(servidorCentral);
    }

    private void entregar(Mensaje mensaje) {
        IReceptorExterno destino = receptor;
        if (destino == null) {
            throw new IllegalStateException("Mensaje sin receptor registrado: " + mensaje);
        }
        destino.recibirMensaje(mensaje);
    }

    @Override
    public void registrarReceptor(IReceptorExterno receptor) {
        this.receptor = receptor;
    }

    @Override
    public synchronized void iniciar() {
        if (iniciado || detenido) {
            return;
        }
        try {
            servidor.iniciar();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo abrir el puerto de escucha: " + e.getMessage(), e);
        }
        for (ClienteTCP c : clientes.values()) {
            c.iniciar();
        }
        iniciado = true;
    }

    @Override
    public void enviarAPares(Mensaje mensaje) {
        for (ClienteTCP c : clientes.values()) {
            c.encolar(mensaje);
        }
    }

    @Override
    public synchronized void actualizarPares(List<DireccionPeer> pares) {
        if (detenido) {
            return;
        }
        for (DireccionPeer p : pares) {
            if (!clientes.containsKey(p.idPeer())) {
                ClienteTCP nuevo = new ClienteTCP(p);
                clientes.put(p.idPeer(), nuevo);
                if (iniciado) {
                    nuevo.iniciar();
                }
            }
        }
    }

    @Override
    public Mensaje enviarAlServidor(Mensaje peticion) throws IOException {
        if (clienteServidor == null) {
            throw new IOException("No hay servidor central configurado.");
        }
        return clienteServidor.solicitar(peticion);
    }

    @Override
    public synchronized void detener() {
        if (detenido) {
            return;
        }
        detenido = true;
        // Primero se vacían las colas de salida (p. ej. el "ABANDONAR_PARTIDA" de una ventana que se cierra).
        long limite = System.currentTimeMillis() + ESPERA_CIERRE_MS;
        for (ClienteTCP c : clientes.values()) {
            c.detenerEnviandoPendientes(Math.max(1, limite - System.currentTimeMillis()));
        }
        servidor.detener();
    }

    @Override
    public int getPeersAlcanzables() {
        int n = 0;
        for (ClienteTCP c : clientes.values()) {
            if (c.isAlcanzable()) {
                n++;
            }
        }
        return n;
    }
}
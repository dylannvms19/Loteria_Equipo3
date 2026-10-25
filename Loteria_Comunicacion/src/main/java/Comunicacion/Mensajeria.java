
package Comunicacion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Mensajería del Par. Habla con tres vecinos del diagrama:
 * - hacia abajo, con la {@link IComunicacion} (enviar / recibir);
 * - de lado, con el {@link DirectorioPares} (consultar / actualizar);
 * - hacia arriba, con el receptor externo inyectado (el modelo), al que entrega las acciones recibidas.
 *
 * Filtra los mensajes propios, los duplicados y los de directorio antes de entregar al modelo.
 * 
 * @author Dylan
 */
public class Mensajeria implements IMensajeria, IReceptorExterno {

    private static final System.Logger LOG = System.getLogger(Mensajeria.class.getName());

    private final DireccionPeer propia;
    private final IComunicacion comunicacion;
    private final DirectorioPares directorio;
    // Arranca en el reloj para que un nodo reiniciado no repita secuencias ya vistas por los demás.
    private final AtomicLong secuencia = new AtomicLong(System.currentTimeMillis());
    private final Map<Integer, Long> ultimaSecuencia = new HashMap<Integer, Long>();
    private volatile IReceptorExterno receptor;
    private boolean iniciada;
    private boolean detenida;

    /**
     * @param propia       id, host y puerto con que este nodo se da a conocer a los demás
     * @param comunicacion capa TCP sobre la que viaja todo
     * @param directorio   pares conocidos (puede venir ya con algunos)
     */
    public Mensajeria(DireccionPeer propia, IComunicacion comunicacion, DirectorioPares directorio) {
        if (propia == null || comunicacion == null || directorio == null) {
            throw new IllegalArgumentException("Dirección propia, comunicación y directorio son obligatorios.");
        }
        this.propia = propia;
        this.comunicacion = comunicacion;
        this.directorio = directorio;
        this.comunicacion.registrarReceptor(this);
    }

    @Override
    public void registrarReceptor(IReceptorExterno receptor) {
        this.receptor = receptor;
    }

    @Override
    public synchronized void iniciar() {
        if (iniciada || detenida) {
            return;
        }
        List<DireccionPeer> otros = new ArrayList<DireccionPeer>();
        for (DireccionPeer p : directorio.obtener()) {
            if (p.idPeer() != propia.idPeer()) {
                otros.add(p);
            }
        }
        comunicacion.actualizarPares(otros);
        comunicacion.iniciar();
        iniciada = true;
    }

    @Override
    public void enviarMensaje(TipoMensaje tipo, String... argumentos) {
        if (tipo.esDeDirectorio()) {
            throw new IllegalArgumentException(tipo + " solo se intercambia con el servidor central.");
        }
        comunicacion.enviarAPares(nuevoMensaje(tipo, argumentos));
    }

    @Override
    public List<DireccionPeer> registrarEnServidor() throws IOException {
        return pedirAlServidor(TipoMensaje.REGISTRAR_PAR,
                String.valueOf(propia.idPeer()), propia.host(), String.valueOf(propia.puerto()));
    }

    @Override
    public List<DireccionPeer> consultarPares() throws IOException {
        return pedirAlServidor(TipoMensaje.CONSULTAR_PARES);
    }

    private List<DireccionPeer> pedirAlServidor(TipoMensaje tipo, String... argumentos) throws IOException {
        Mensaje respuesta = comunicacion.enviarAlServidor(nuevoMensaje(tipo, argumentos));
        if (respuesta.getTipo() != TipoMensaje.RESPUESTA_PARES) {
            throw new IOException("Respuesta inesperada del servidor: " + respuesta.getTipo());
        }
        List<DireccionPeer> recibidos;
        try {
            recibidos = DirectorioPares.parsearLista(respuesta.getArgumento(0));
        } catch (IllegalArgumentException e) {
            throw new IOException("Lista de pares inválida del servidor: " + e.getMessage(), e);
        }
        List<DireccionPeer> otros = new ArrayList<DireccionPeer>();
        for (DireccionPeer p : recibidos) {
            if (p.idPeer() != propia.idPeer()) {
                otros.add(p);
            }
        }
        directorio.reemplazar(otros);
        comunicacion.actualizarPares(otros);
        return otros;
    }

    private Mensaje nuevoMensaje(TipoMensaje tipo, String... argumentos) {
        return new Mensaje(tipo, propia.idPeer(), secuencia.incrementAndGet(), List.of(argumentos));
    }

    /** Llamado por la Comunicación. Descarta propios, duplicados y de directorio; entrega en orden. */
    @Override
    public void recibirMensaje(Mensaje mensaje) {
        if (mensaje.getIdOrigen() == propia.idPeer() || mensaje.getTipo().esDeDirectorio()) {
            return;
        }
        synchronized (ultimaSecuencia) {
            Long ultima = ultimaSecuencia.get(mensaje.getIdOrigen());
            if (ultima != null && mensaje.getSecuencia() <= ultima) {
                return; // reintento de un mensaje ya entregado
            }
            ultimaSecuencia.put(mensaje.getIdOrigen(), mensaje.getSecuencia());
            IReceptorExterno destino = receptor;
            if (destino == null) {
                LOG.log(System.Logger.Level.WARNING, "Mensaje sin receptor registrado: " + mensaje);
                return;
            }
            try {
                destino.recibirMensaje(mensaje);
            } catch (RuntimeException e) {
                LOG.log(System.Logger.Level.ERROR, "El receptor falló con " + mensaje + ": " + e);
            }
        }
    }

    @Override
    public synchronized void detener() {
        if (detenida) {
            return;
        }
        detenida = true;
        comunicacion.detener();
    }

    @Override
    public int getIdNodo() {
        return propia.idPeer();
    }

    @Override
    public int getPeersAlcanzables() {
        return comunicacion.getPeersAlcanzables();
    }
}

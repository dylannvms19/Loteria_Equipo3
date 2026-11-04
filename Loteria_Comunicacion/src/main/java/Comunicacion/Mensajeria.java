package Comunicacion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/** Selecciona destinatarios, serializa y entrega mensajes interpretados. */
public class Mensajeria implements IMensajeria, IReceptorExterno {
    private final DireccionPeer propia;
    private final IDispatcher dispatcher;
    private final DirectorioPares directorio;
    private final SerializadorMensajes serializador;
    // Lista de observadores, no Map de receptores.
    private final List<IReceptorMensaje> receptores = new ArrayList<IReceptorMensaje>();
    private final Map<Integer, Long> ultimaSecuencia = new HashMap<Integer, Long>();
    private final AtomicLong secuencia = new AtomicLong(System.currentTimeMillis());
    private final Object candadoConsulta = new Object();
    private final BlockingQueue<Mensaje> respuestas = new LinkedBlockingQueue<Mensaje>();
    private volatile long consultaPendiente = -1;
    private volatile DireccionPeer servidorCentral;

    public Mensajeria(DireccionPeer propia, IDispatcher dispatcher,
            DirectorioPares directorio, SerializadorMensajes serializador) {
        this.propia = propia;
        this.dispatcher = dispatcher;
        this.directorio = directorio;
        this.serializador = serializador;
    }

    public void setServidorCentral(DireccionPeer servidorCentral) {
        this.servidorCentral = servidorCentral;
    }

    @Override
    public synchronized void registrarReceptor(IReceptorMensaje receptor) {
        if (receptor == null) { throw new IllegalArgumentException("Receptor nulo."); }
        if (!receptores.contains(receptor)) { receptores.add(receptor); }
    }

    @Override
    public synchronized void retirarReceptor(IReceptorMensaje receptor) {
        receptores.remove(receptor);
    }

    @Override
    public void enviarMensaje(TipoMensaje tipo, String... argumentos) {
        if (tipo.esDeDirectorio()) {
            throw new IllegalArgumentException("Use registrarEnServidor o consultarPares.");
        }
        Mensaje mensaje = nuevoMensaje(tipo, argumentos);
        for (DireccionPeer destino : directorio.obtener()) {
            if (destino.idPeer() != propia.idPeer()) { enviarA(mensaje, destino); }
        }
    }

    @Override
    public void enviarAJugador(int id, TipoMensaje tipo, String... argumentos) {
        for (DireccionPeer destino : directorio.obtener()) {
            if (destino.idPeer() == id && id != propia.idPeer()) {
                enviarA(nuevoMensaje(tipo, argumentos), destino);
                return;
            }
        }
        throw new IllegalStateException("No se conoce la dirección del jugador " + id);
    }

    private Mensaje nuevoMensaje(TipoMensaje tipo, String... argumentos) {
        return new Mensaje(tipo, propia.idPeer(), secuencia.incrementAndGet(), Arrays.asList(argumentos));
    }

    private void enviarA(Mensaje mensaje, DireccionPeer destino) {
        try {
            byte[] datos = serializador.serializar(mensaje);
            dispatcher.enviar(datos, destino.host(), destino.puerto());
        } catch (IOException e) {
            throw new IllegalArgumentException("No se puede serializar el mensaje.", e);
        }
    }

    @Override
    public void recibir(byte[] datos) {
        Mensaje mensaje;
        try { mensaje = serializador.deserializar(datos); }
        catch (IOException e) {
            System.err.println("Mensaje inválido: " + e.getMessage());
            return;
        }
        if (mensaje.getTipo() == TipoMensaje.RESPUESTA_PARES) {
            DireccionPeer central = servidorCentral;
            if (central != null && mensaje.getIdOrigen() == central.idPeer()
                    && mensaje.getSecuencia() == consultaPendiente) {
                respuestas.offer(mensaje);
            }
            return;
        }
        if (mensaje.getIdOrigen() == propia.idPeer()) { return; }
        // La deduplicación se aplica a eventos de juego, no a consultas del directorio.
        if (!mensaje.getTipo().esDeDirectorio()) {
            synchronized (ultimaSecuencia) {
                Long ultima = ultimaSecuencia.get(mensaje.getIdOrigen());
                if (ultima != null && mensaje.getSecuencia() <= ultima) { return; }
                ultimaSecuencia.put(mensaje.getIdOrigen(), mensaje.getSecuencia());
            }
        }
        List<IReceptorMensaje> copia;
        synchronized (this) { copia = new ArrayList<IReceptorMensaje>(receptores); }
        for (IReceptorMensaje receptor : copia) {
            try {
                if (receptor.acepta(mensaje.getTipo().name())) { receptor.procesar(mensaje); }
            } catch (RuntimeException e) {
                System.err.println("Receptor de mensajes: " + e.getMessage());
            }
        }
    }

    @Override
    public List<DireccionPeer> registrarEnServidor() throws IOException {
        return consultar(nuevoMensaje(TipoMensaje.REGISTRAR_PAR,
                String.valueOf(propia.idPeer()), propia.host(), String.valueOf(propia.puerto())));
    }

    @Override
    public List<DireccionPeer> consultarPares() throws IOException {
        return consultar(nuevoMensaje(TipoMensaje.CONSULTAR_PARES));
    }

    /** Bloquea hasta 5 segundos. No llamar desde el hilo Swing ni desde procesar(). */
    private List<DireccionPeer> consultar(Mensaje solicitud) throws IOException {
        synchronized (candadoConsulta) {
            DireccionPeer central = servidorCentral;
            if (central == null) { throw new IOException("No se configuró el servidor central."); }
            respuestas.clear();
            consultaPendiente = solicitud.getSecuencia();
            try {
                enviarA(solicitud, central);
                Mensaje respuesta = respuestas.poll(5, TimeUnit.SECONDS);
                if (respuesta == null) { throw new IOException("El directorio no respondió a tiempo."); }
                List<DireccionPeer> pares = DirectorioPares.parsearLista(respuesta.getArgumento(0));
                directorio.reemplazar(pares);
                directorio.quitar(propia.idPeer());
                return directorio.obtener();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Consulta interrumpida.", e);
            } catch (IllegalArgumentException | IllegalStateException e) {
                throw new IOException("No se pudo consultar el directorio.", e);
            } finally {
                consultaPendiente = -1;
                respuestas.clear();
            }
        }
    }

    /** Utilizada por el servicio de directorio del servidor central. */
    public void responderDirectorio(Mensaje solicitud, DireccionPeer destino, String lista) {
        Mensaje respuesta = new Mensaje(TipoMensaje.RESPUESTA_PARES, propia.idPeer(),
                solicitud.getSecuencia(), Arrays.asList(lista));
        enviarA(respuesta, destino);
    }

    @Override
    public int getIdNodo() { return propia.idPeer(); }
}

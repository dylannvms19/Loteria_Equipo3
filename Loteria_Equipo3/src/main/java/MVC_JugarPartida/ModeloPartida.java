package MVC_JugarPartida;

import Comunicacion.ComunicacionPartida;
import Comunicacion.ISalidaPartida;
import Comunicacion.AccionPartida;
import Dominio.*;
import Dominio.Partida.MetodoVictoria;
import java.util.ArrayList;
import java.util.List;

public class ModeloPartida implements IModelo {
    private final IDominio dominio;
    private final ComunicacionPartida comunicacion;
    private final List<IVista> observadores = new ArrayList<IVista>();
    private ISalidaPartida salida;
    private int idLocal;
    private boolean anfitrion;
    private boolean iniciada = true;
    private String avisoLocal;

    public ModeloPartida(IDominio dominio, ComunicacionPartida comunicacion) {
        this.dominio = dominio;
        this.comunicacion = comunicacion;
        comunicacion.conectarModelo(this);
    }

    public void conectarSalida(ISalidaPartida salida, int idLocal, boolean anfitrion) {
        this.salida = salida;
        this.idLocal = idLocal;
        this.anfitrion = anfitrion;
        iniciada = false;
        avisoLocal = "Esperando a que los dos jugadores estén listos...";
    }

    public void iniciarPartida() {
        iniciada = true;
        avisoLocal = null;
        notificar();
    }

    public void mostrarErrorRed(String texto) {
        comunicacion.detener();
        iniciada = false;
        avisoLocal = texto;
        notificar();
    }

    @Override
    public boolean isIniciada() { return iniciada; }

    @Override
    public void attach(IVista observador) {
        if (observador != null && !observadores.contains(observador)) { observadores.add(observador); }
    }

    @Override
    public void detach(IVista observador) { observadores.remove(observador); }

    public void notificar() {
        comunicacion.detenerSiFinalizo();
        for (IVista observador : new ArrayList<IVista>(observadores)) { observador.update(this); }
    }

    private void solicitar(AccionPartida accion, int jugador, String valor) {
        if (!iniciada || dominio.isFinalizada()) { return; }
        try { salida.solicitar(accion, jugador, valor); }
        catch (RuntimeException e) { mostrarErrorRed("No se pudo enviar la acción: " + e.getMessage()); }
    }

    public void jalarCarta() {
        if (!iniciada || dominio.isFinalizada()) { return; }
        if (salida != null) {
            if (anfitrion) { solicitar(AccionPartida.SIGUIENTE_CARTA, idLocal, ""); }
        } else {
            dominio.jalarCarta();
            notificar();
        }
    }

    /** Solo el gritón del par anfitrión extrae; los demás reciben el id exacto. */
    public Carta extraerCartaParaAnunciar() {
        if (!iniciada || dominio.isFinalizada()) { return null; }
        return dominio.jalarCarta();
    }

    public void marcarCasilla(Jugador jugador, int posicion) {
        if (salida != null) {
            if (jugador.getIdJugador() == idLocal) { solicitar(AccionPartida.MARCAR, idLocal, String.valueOf(posicion)); }
        } else { dominio.marcarCasilla(jugador, posicion); notificar(); }
    }

    public void reclamarPremio(Jugador jugador, MetodoVictoria metodo) {
        if (salida != null) {
            if (jugador.getIdJugador() == idLocal) { solicitar(AccionPartida.RECLAMAR, idLocal, metodo.name()); }
        } else { dominio.reclamarPremio(jugador, metodo); notificar(); }
    }

    public void abandonarPartida(Jugador jugador) {
        if (salida != null) {
            if (jugador.getIdJugador() == idLocal) { solicitar(AccionPartida.ABANDONAR, idLocal, ""); }
        } else { dominio.abandonarPartida(jugador); notificar(); }
    }

    /** Entrada ya ordenada: aplica reglas y notifica; NUNCA vuelve a enviar. */
    public void aplicarAccion(AccionPartida accion, int idJugador, String valor) {
        if (!iniciada) { return; }
        avisoLocal = null;
        Jugador jugador = new Jugador(idJugador, "", 0);
        switch (accion) {
            case CARTA: dominio.registrarCartaGritada(Integer.parseInt(valor)); break;
            case MARCAR: dominio.marcarCasilla(jugador, Integer.parseInt(valor)); break;
            case RECLAMAR: dominio.reclamarPremio(jugador, MetodoVictoria.valueOf(valor)); break;
            case ABANDONAR: dominio.abandonarPartida(jugador); break;
            default: throw new IllegalArgumentException("Acción no aplicable: " + accion);
        }
        notificar();
    }

    public void recibirCartaGritada(int idCarta) {
        dominio.registrarCartaGritada(idCarta);
        notificar();
    }

    public void aplicarActualizacionRemota(String mensaje) {
        dominio.aplicarMensaje(mensaje);
        notificar();
    }

    public void cambiarAutomatico() {
        if (iniciada && (salida == null || anfitrion)) { comunicacion.cambiarAutomatico(); }
    }
    public void cambiarIntervalo(int milisegundos) {
        if (salida == null || anfitrion) { comunicacion.cambiarIntervalo(milisegundos); }
    }
    public void cerrarVista(IVista vista) {
        detach(vista);
        if (observadores.isEmpty()) { comunicacion.detener(); }
    }

    @Override
    public List<Carta> getCartasTabla(Jugador jugador) {
        return dominio.getCartasTabla(jugador);
    }

    @Override
    public boolean[] getCasillasMarcadas(Jugador jugador) {
        return dominio.getCasillasMarcadas(jugador);
    }

    @Override
    public Carta getCartaActual() {
        return dominio.getCartaActual();
    }

    @Override
    public List<Carta> getCartasGritadas() {
        return dominio.getCartasGritadas();
    }

    @Override
    public List<ResumenJugador> getJugadores(Jugador jugador) {
        List<ResumenJugador> otros = new ArrayList<ResumenJugador>();
        for (ResumenJugador item : dominio.getJugadores()) {
            if (item.getIdJugador() != jugador.getIdJugador()) {
                otros.add(item);
            }
        }
        return otros;
    }

    @Override
    public int getPuntaje(Jugador jugador) {
        return dominio.getPuntaje(jugador);
    }

    @Override
    public String getAviso() {
        return avisoLocal != null ? avisoLocal : dominio.getAviso();
    }

    @Override
    public boolean isFinalizada() {
        return dominio.isFinalizada();
    }

    @Override
    public boolean hayCartas() {
        return dominio.hayCartas();
    }

    @Override
    public boolean isAutomatico() {
        return comunicacion.isAutomatico();
    }

    @Override
    public int getIntervalo() {
        return comunicacion.getIntervalo();
    }
}

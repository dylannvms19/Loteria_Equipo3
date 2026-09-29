/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MVC_Partida_Modelo;

import Comunicacion.ComunicacionPartida;
import Dominio.*;
import Dominio.Partida.MetodoVictoria;
import MVC_Partida_Vista.IVista;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author josma
 */
public class ModeloPartida implements IModelo {

    private IDominio dominio;
    private ComunicacionPartida comunicacion;
    private List<IVista> observadores = new ArrayList<IVista>();

    public ModeloPartida(IDominio dominio, ComunicacionPartida comunicacion) {
        this.dominio = dominio;
        this.comunicacion = comunicacion;
        comunicacion.conectarModelo(this);
    }

    public void attach(IVista observador) {
        if (observador != null && !observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    public void detach(IVista observador) {
        observadores.remove(observador);
    }

    public void notificar() {
        comunicacion.detenerSiFinalizo();
        for (IVista observador : new ArrayList<IVista>(observadores)) {
            observador.update(this);
        }
    }

    public void jalarCarta() {
        dominio.jalarCarta();
        notificar();
    }

    public void marcarCasilla(Jugador solicitado, int posicion) {
        dominio.marcarCasilla(solicitado, posicion);
        notificar();
    }

    public void reclamarPremio(Jugador solicitado, MetodoVictoria metodo) {
        dominio.reclamarPremio(solicitado, metodo);
        notificar();
    }

    public void abandonarPartida(Jugador solicitado) {
        dominio.abandonarPartida(solicitado);
        notificar();
    }

    public void recibirCartaGritada(int idCarta) {
        aplicarActualizacionRemota(IDominio.PREFIJO_CARTA + idCarta);
    }

    public void aplicarActualizacionRemota(String mensaje) {
        dominio.aplicarMensaje(mensaje);
        notificar();
    }

    public void cambiarAutomatico() {
        comunicacion.cambiarAutomatico();
    }

    public void cambiarIntervalo(int milisegundos) {
        comunicacion.cambiarIntervalo(milisegundos);
    }

    public void cerrarVista(IVista vista) {
        detach(vista);
        if (observadores.isEmpty()) {
            comunicacion.detener();
        }
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
        return dominio.getAviso();
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

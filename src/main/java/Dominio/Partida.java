/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Dominio;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Dylan
 */
public class Partida implements IDominio{
    private List<Jugador> jugadores;
    private Map<Jugador, TarjetaJugador> tarjetas;
    private Baraja baraja;
    private Bonche bonche;
    private Carta cartaActual;
    private boolean finalizada;
    private String aviso = "";

    public Partida(List<Jugador> jugadores, List<Tarjeta> tablas, Baraja baraja) {
        this.jugadores = new ArrayList<Jugador>(jugadores);
        this.baraja = baraja;
        this.bonche = new Bonche();
        this.tarjetas = new LinkedHashMap<Jugador, TarjetaJugador>();
        for (int i = 0; i < jugadores.size(); i++) {
            tarjetas.put(jugadores.get(i), new TarjetaJugador(tablas.get(i), new boolean[16]));
        }
    }
    
    @Override
    public Carta jalarCarta() {
        if (finalizada) {
            return cartaActual;
        }
        if (!baraja.hayCartas()) {
            finalizarPartida();
            return cartaActual;
        }
        registrar(baraja.extraerCarta());
        return cartaActual;
    }

    @Override
    public boolean marcarCasilla(Jugador jugador, int posicion) {
        if (finalizada) {
            return false;
        }
        TarjetaJugador tj = tarjetas.get(jugador);
        if (tj == null) {
            return false;
        }
        Carta carta = tj.obtenerCarta(posicion);
        if (carta == null || !bonche.validaCarta(carta)) {
            aviso = "Esa carta todavia no ha sido cantada";
            return false;
        }
        aviso = "";
        return tj.marcarCasilla(posicion);
    }

    @Override
    public void aplicarMensaje(String mensaje) {
        if (mensaje != null && mensaje.startsWith(PREFIJO_CARTA)) {
            registrarCartaGritada(Integer.parseInt(mensaje.substring(PREFIJO_CARTA.length()).trim()));
        }
    }

    @Override
    public void registrarCartaGritada(int idCarta) {
        Carta carta = baraja.buscarCarta(idCarta);
        if (carta == null) {
            aviso = "Carta desconocida: " + idCarta;
            return;
        }
        registrar(carta);
    }

    @Override
    public List<Carta> getCartasTabla(Jugador jugador) {
        List<Carta> lista = new ArrayList<Carta>();
        TarjetaJugador tj = tarjetas.get(jugador);
        if (tj != null) {
            for (int i = 0; i < 16; i++) {
                lista.add(tj.obtenerCarta(i));
            }
        }
        return lista;
    }

    @Override
    public boolean[] getCasillasMarcadas(Jugador jugador) {
        TarjetaJugador tj = tarjetas.get(jugador);
        return (tj == null) ? new boolean[16] : tj.getEstadosCasillas();
    }

    @Override
    public Carta getCartaActual() {
        return cartaActual;
    }

    @Override
    public List<Carta> getCartasGritadas() {
        return new ArrayList<Carta>(bonche.getCartasPasadas());
    }

    @Override
    public List<ResumenJugador> getJugadores() {
        List<ResumenJugador> resumen = new ArrayList<ResumenJugador>();
        for (Jugador j : jugadores) {
            resumen.add(new ResumenJugador(j.getNombre(), getCartasTabla(j), getCasillasMarcadas(j), j.getPuntuaje()));
        }
        return resumen;
    }

    @Override
    public int getPuntaje(Jugador jugador) {
        return jugador.getPuntuaje();
    }

    @Override
    public String getAviso() {
        return aviso;
    }

    @Override
    public boolean isFinalizada() {
        return finalizada;
    }

    public void finalizarPartida() {
        this.finalizada = true;
    }

    //Metodo auxiliar privado
    private void registrar(Carta carta) {
        cartaActual = carta;
        if (!bonche.validaCarta(carta)) {
            bonche.registrarCartaGritada(carta);
        }
        aviso = "";
    }
    
           
}

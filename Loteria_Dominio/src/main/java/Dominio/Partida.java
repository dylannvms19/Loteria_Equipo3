/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Dominio;

import static Dominio.IDominio.PREFIJO_CARTA;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Diego
 */
public class Partida  {

    public enum MetodoVictoria {
        CHORRO, CUATRO_ESQUINAS, CENTRO, BUENAS
    }

    private List<Jugador> jugadores;
    private Map<Integer, TarjetaJugador> tarjetas = new LinkedHashMap<Integer, TarjetaJugador>();
    private Map<MetodoVictoria, Jugador> metodosReclamados = new EnumMap<MetodoVictoria, Jugador>(MetodoVictoria.class);
    private Map<MetodoVictoria, Integer> puntosPorMetodo = new EnumMap<MetodoVictoria, Integer>(MetodoVictoria.class);
    private Baraja baraja;
    private Bonche bonche = new Bonche();
    private Carta cartaActual;
    private boolean finalizada;
    private Jugador ganador;
    private String aviso = "Partida preparada. Esperando una carta.";

    public Partida(List<Jugador> jugadores, List<Tarjeta> tablas, Baraja baraja) {
        if (jugadores.size() < 2 || jugadores.size() > 4 || tablas.size() != jugadores.size()) {
            throw new IllegalArgumentException("Se necesitan entre 2 y 4 jugadores y una tabla para cada uno.");
        }
        this.jugadores = new ArrayList<Jugador>(jugadores);
        this.baraja = baraja;
        for (int i = 0; i < jugadores.size(); i++) {
            int id = jugadores.get(i).getIdJugador();
            if (tarjetas.containsKey(id)) {
                throw new IllegalArgumentException("Id de jugador repetido.");
            }
            tarjetas.put(id, new TarjetaJugador(tablas.get(i), new boolean[16]));
        }
        // Valores de prueba; el enunciado permite configurarlos, pero no fija cantidades.
        puntosPorMetodo.put(MetodoVictoria.CHORRO, 10);
        puntosPorMetodo.put(MetodoVictoria.CUATRO_ESQUINAS, 20);
        puntosPorMetodo.put(MetodoVictoria.CENTRO, 30);
        puntosPorMetodo.put(MetodoVictoria.BUENAS, 100);
    }

    public List<Jugador> getJugadores() {
        return new ArrayList<Jugador>(jugadores);
    }

    public Jugador buscarJugador(int id) {
        for (Jugador j : jugadores) {
            if (j.getIdJugador() == id) {
                return j;
            }
        }
        return null;
    }

    public TarjetaJugador getTarjetaJugador(Jugador jugador) {
        return tarjetas.get(jugador.getIdJugador());
    }

    public Baraja getBaraja() {
        return baraja;
    }

    public Bonche getBonche() {
        return bonche;
    }

    public Carta getCartaActual() {
        return cartaActual;
    }

    public boolean isFinalizada() {
        return finalizada;
    }

    public Jugador getGanador() {
        return ganador;
    }

    public String getAviso() {
        return aviso;
    }

    public void setAviso(String aviso) {
        this.aviso = aviso;
    }

    public Jugador getGanadorMetodo(MetodoVictoria metodo) {
        return metodosReclamados.get(metodo);
    }

    public int getPuntosMetodo(MetodoVictoria metodo) {
        return puntosPorMetodo.get(metodo);
    }

    public void registrarCarta(Carta carta) {
        cartaActual = carta;
        bonche.registrarCartaGritada(carta);
    }

    public void registrarReclamacion(Jugador jugador, MetodoVictoria metodo) {
        metodosReclamados.put(metodo, jugador);
        jugador.sumarPuntos(getPuntosMetodo(metodo));
        if (metodo == MetodoVictoria.BUENAS) {
            ganador = jugador;
            finalizada = true;
        }
    }

    public void cancelarPartida() {
        finalizada = true;
    }
}

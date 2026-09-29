/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package Dominio;

import Dominio.Partida.MetodoVictoria;
import java.util.List;

/**
 *
 * @author Diego
 */
public interface IDominio {

    String PREFIJO_CARTA = "CARTA:";

    Carta jalarCarta();

    void registrarCartaGritada(int idCarta);

    void aplicarMensaje(String mensaje);

    boolean marcarCasilla(Jugador jugador, int posicion);

    boolean reclamarPremio(Jugador jugador, MetodoVictoria metodo);

    void abandonarPartida(Jugador jugador);

    List<Carta> getCartasTabla(Jugador jugador);

    boolean[] getCasillasMarcadas(Jugador jugador);

    Carta getCartaActual();

    List<Carta> getCartasGritadas();

    List<ResumenJugador> getJugadores();

    int getPuntaje(Jugador jugador);

    String getAviso();

    boolean isFinalizada();

    boolean hayCartas();
}

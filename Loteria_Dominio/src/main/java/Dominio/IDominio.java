/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package Dominio;

import java.util.List;

/**
 *
 * @author Diego
 */
public interface IDominio {

    String PREFIJO_CARTA = "CARTA: ";

    boolean marcarCasilla(Jugador jugador, int posicion);

    void registrarCartaGritada(int idCarta);

    List<Carta> getCartasTabla(Jugador jugador);

    Carta jalarCarta();

    void aplicarMensaje(String mensaje);

    boolean[] getCasillasMarcadas(Jugador jugador);

    Carta getCartaActual();

    List<Carta> getCartasGritadas();

    List<ResumenJugador> getJugadores();

    int getPuntaje(Jugador jugador);

    String getAviso();

    boolean isFinalizada();
}

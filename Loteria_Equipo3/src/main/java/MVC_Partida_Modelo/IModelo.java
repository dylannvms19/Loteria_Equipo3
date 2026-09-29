/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package MVC_Partida_Modelo;

import Dominio.Carta;
import Dominio.Jugador;
import Dominio.ResumenJugador;
import java.util.List;
/**
 *
 * @author josma
 */
public interface IModelo {

    List<Carta> getCartasTabla(Jugador jugador);

    boolean[] getCasillasMarcadas(Jugador jugador);

    Carta getCartaActual();

    List<Carta> getCartasGritadas();

    List<ResumenJugador> getJugadores(Jugador jugador);

    int getPuntaje(Jugador jugador);

    String getAviso();

    boolean isFinalizada();

    boolean hayCartas();

    boolean isAutomatico();

    int getIntervalo();
}

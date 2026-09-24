/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package Dominio;

import MVCPartida.ResumenJugador;
import java.util.List;

/**
 *
 * @author josma
 */
public interface IDominio {
   boolean marcarCasilla(Jugador jugador, int posicion);
   void registrarCartaGritada(Jugador jugador);
   List<Carta> getCartasTabla(Jugador jugador);
   
}

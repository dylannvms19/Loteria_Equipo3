/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package Dominio;

import java.util.List;

/**
 *
 * @author josma
 */
public interface IDominio {
    public Carta jalarCarta(Carta carta);
    public List<Carta> getCartasTabla(Jugador jugador);
    public Carta getCartaActual(Carta carta);
    public List<ResumenJugador> getJugadores();
}

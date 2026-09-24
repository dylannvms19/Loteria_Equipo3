/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package MVCPartida;

import Dominio.Carta;
import java.util.List;

/**
 *
 * @author josma
 */
public interface IModelo {
    public List<Carta> getCartasTabla();

    public boolean[] getCasillasMarcadas();

    public Carta getCartaActual();

    public List<Carta> getCartasGritadas();

    public List<ResumenJugador> getJugadores();

    public int getPuntaje();

    public String getAviso();

    public boolean isFinalizada();
}

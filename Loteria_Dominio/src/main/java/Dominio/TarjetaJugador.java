/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Dominio;

/**
 *
 * @author Diego
 */
public class TarjetaJugador {

    private Tarjeta tarjeta;
    private boolean[] estadosCasillas;

    public TarjetaJugador(Tarjeta tarjeta, boolean[] estadosCasillas) {
        this.tarjeta = tarjeta;
        if (estadosCasillas != null && estadosCasillas.length == 16) {
            this.estadosCasillas = estadosCasillas.clone();
        } else {
            this.estadosCasillas = new boolean[16];
        }
    }

    public boolean marcarCasilla(int posicion) {
        if (posicion < 0 || posicion >= estadosCasillas.length) {
            return false;
        }
        estadosCasillas[posicion] = true;
        return true;
    }

    public Carta obtenerCarta(int posicion) {
        if (posicion < 0 || posicion >= estadosCasillas.length) {
            return null;
        }
        return tarjeta.obtenerCartaEnPosicion(posicion);
    }

    public boolean[] getEstadosCasillas() {
        return estadosCasillas.clone();
    }
}

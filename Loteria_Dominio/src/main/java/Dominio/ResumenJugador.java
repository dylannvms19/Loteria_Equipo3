/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Dominio;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Diego
 */
public class ResumenJugador {

    private final int idJugador;
    private final String nombre;
    private final List<Carta> cartasTabla;
    private final boolean[] casillasMarcadas;
    private final int puntaje;
    private final boolean activo;

    public ResumenJugador(int idJugador, String nombre, List<Carta> cartasTabla,
            boolean[] casillasMarcadas, int puntaje, boolean activo) {
        this.idJugador = idJugador;
        this.nombre = nombre;
        this.cartasTabla = new ArrayList<Carta>(cartasTabla);
        this.casillasMarcadas = casillasMarcadas.clone();
        this.puntaje = puntaje;
        this.activo = activo;
    }

    public int getIdJugador() {
        return idJugador;
    }

    public String getNombre() {
        return nombre;
    }

    public List<Carta> getCartasTabla() {
        return new ArrayList<Carta>(cartasTabla);
    }

    public boolean[] getCasillasMarcadas() {
        return casillasMarcadas.clone();
    }

    public int getPuntaje() {
        return puntaje;
    }

    public boolean isActivo() {
        return activo;
    }
}

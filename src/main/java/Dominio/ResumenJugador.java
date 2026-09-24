/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Dominio;

import java.util.List;

/**
 *
 * @author Dylan
 */
public class ResumenJugador {
    private String nombre;
    private List<Carta> cartasTabla;
    private boolean[] casillasMarcadas;
    private int puntaje;

    public ResumenJugador(String nombre, List<Carta> cartasTabla, boolean[] casillasMarcadas, int puntaje) {
        this.nombre = nombre;
        this.cartasTabla = cartasTabla;
        this.casillasMarcadas = casillasMarcadas;
        this.puntaje = puntaje;
    }

    public String getNombre() {
        return nombre;
    }

    public List<Carta> getCartasTabla() {
        return cartasTabla;
    }

    public boolean[] getCasillasMarcadas() {
        return casillasMarcadas;
    }

    public int getPuntaje() {
        return puntaje;
    }
    
    
    
}

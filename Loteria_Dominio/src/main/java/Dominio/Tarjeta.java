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
public class Tarjeta {

    private int numeroTabla;
    private List<Carta> cartas;

    public Tarjeta(int numeroTabla, List<Carta> cartas) {
        if (cartas.size() != 16) {
            throw new IllegalArgumentException("La tarjeta debe tener 16 cartas");
        }
        this.numeroTabla = numeroTabla;
        this.cartas = new ArrayList<Carta>(cartas);
    }

    public Carta obtenerCartaEnPosicion(int posicion) {
        return cartas.get(posicion);
    }

    public List<Carta> getCartas() {
        return new ArrayList<Carta>(cartas);
    }

    public int getNumeroTabla() {
        return numeroTabla;
    }
}

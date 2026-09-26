/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Diego
 */
public class Baraja {

    private List<Carta> cartas;
    private List<Carta> catalogo;

    public Baraja(List<Carta> cartas) {
        this.catalogo = new ArrayList<Carta>(cartas);
        this.cartas = new ArrayList<Carta>(cartas);
        Collections.shuffle(this.cartas);
    }

    public Carta extraerCarta() {
        if (cartas.isEmpty()) {
            return null;
        }
        return cartas.remove(0);
    }

    public boolean hayCartas() {
        return !cartas.isEmpty();
    }

    public Carta buscarCarta(int idCarta) {
        for (Carta c : catalogo) {
            if (c.getIdCarta() == idCarta) {
                return c;
            }
        }
        return null;
    }
}

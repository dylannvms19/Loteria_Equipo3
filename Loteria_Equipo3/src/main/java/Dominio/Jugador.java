/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Dominio;

/**
 *
 * @author josma
 */
public class Jugador {
     private int idJugador;
     private String nombre;
     private int puntuaje;

    public Jugador(int idJugador, String nombre, int puntuaje) {
        this.idJugador = idJugador;
        this.nombre = nombre;
        this.puntuaje = puntuaje;
    }

    public int getIdJugador() {
        return idJugador;
    }

    public String getNombre() {
        return nombre;
    }

    public int getPuntuaje() {
        return puntuaje;
    }
     
     
}

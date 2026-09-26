/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MVC_Partida_Control;

import Dominio.Jugador;
import MVC_Partida_Modelo.ModeloPartida;

/**
 *
 * @author josma
 */
public class ControlPartida {

    private ModeloPartida modelo;

    public ControlPartida(ModeloPartida modelo) {
        this.modelo = modelo;
    }

    public void jalarCarta() {
        modelo.jalarCarta();
    }

    public void marcarCasilla(Jugador jugador, int posicion) {
        modelo.marcarCasilla(jugador, posicion);
    }
}

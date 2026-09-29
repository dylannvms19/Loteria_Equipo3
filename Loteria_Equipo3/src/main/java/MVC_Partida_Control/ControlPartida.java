/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MVC_Partida_Control;

import Dominio.Jugador;
import Dominio.Partida.MetodoVictoria;
import MVC_Partida_Modelo.ModeloPartida;
import MVC_Partida_Vista.IVista;
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

    public void reclamarPremio(Jugador jugador, MetodoVictoria metodo) {
        modelo.reclamarPremio(jugador, metodo);
    }

    public void abandonarPartida(Jugador jugador) {
        modelo.abandonarPartida(jugador);
    }

    public void cambiarAutomatico() {
        modelo.cambiarAutomatico();
    }

    public void cambiarIntervalo(int milisegundos) {
        modelo.cambiarIntervalo(milisegundos);
    }

    public void cerrarVista(IVista vista) {
        modelo.cerrarVista(vista);
    }
}

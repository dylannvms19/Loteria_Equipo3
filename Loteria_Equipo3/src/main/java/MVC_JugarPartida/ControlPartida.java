/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MVC_JugarPartida;

import Comunicacion.IDispatcher;
import Dominio.Jugador;
import Dominio.Partida.MetodoVictoria;

/**
 *
 * @author josma
 */
//public class ControlPartida {
//
//    private ModeloPartida modelo;
//
//    public ControlPartida(ModeloPartida modelo) {
//        this.modelo = modelo;
//    }
//
//    public void jalarCarta() {
//        modelo.jalarCarta();
//    }
//
//    public void marcarCasilla(Jugador jugador, int posicion) {
//        modelo.marcarCasilla(jugador, posicion);
//    }
//
//    public void reclamarPremio(Jugador jugador, MetodoVictoria metodo) {
//        modelo.reclamarPremio(jugador, metodo);
//    }
//
//    public void abandonarPartida(Jugador jugador) {
//        modelo.abandonarPartida(jugador);
//    }
//
//    public void cambiarAutomatico() {
//        modelo.cambiarAutomatico();
//    }
//
//    public void cambiarIntervalo(int milisegundos) {
//        modelo.cambiarIntervalo(milisegundos);
//    }
//
//    public void cerrarVista(IVista vista) {
//        modelo.cerrarVista(vista);
//    }
//}
public class ControlPartida {

    private ModeloPartida modelo;
    private IDispatcher red;

    public ControlPartida(ModeloPartida modelo, IDispatcher red) {
        this.modelo = modelo;
        this.red = red;
    }

    public void jalarCarta() {
        modelo.jalarCarta();
    }

    public void marcarCasilla(Jugador jugador, int posicion) {
        modelo.marcarCasilla(jugador, posicion);
        red.enviarMensaje("MARCAR_CASILLA:" + jugador.getIdJugador()+ ":" + posicion);
    }

    public void reclamarPremio(Jugador jugador, MetodoVictoria metodo) {
        modelo.reclamarPremio(jugador, metodo);
        red.enviarMensaje("RECLAMAR_PREMIO:" + jugador.getIdJugador() + ":" + metodo.name());
    }

    public void abandonarPartida(Jugador jugador) {
        modelo.abandonarPartida(jugador);
        red.enviarMensaje("ABANDONAR_PARTIDA:" + jugador.getIdJugador());
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

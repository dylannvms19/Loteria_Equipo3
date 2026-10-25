/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MVC_JugarPartida;

import Comunicacion.IMensajeria;
import Comunicacion.TipoMensaje;
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
    private IMensajeria red;

    public ControlPartida(ModeloPartida modelo, IMensajeria red) {
        this.modelo = modelo;
        this.red = red;
    }

    public void jalarCarta() {
        modelo.jalarCarta();
    }

    public void marcarCasilla(Jugador jugador, int posicion) {
        modelo.marcarCasilla(jugador, posicion);
        red.enviarMensaje(TipoMensaje.MARCAR_CASILLA, String.valueOf(posicion));
    }

    public void reclamarPremio(Jugador jugador, MetodoVictoria metodo) {
        modelo.reclamarPremio(jugador, metodo);
        red.enviarMensaje(TipoMensaje.RECLAMAR_PREMIO, metodo.name());
    }

    public void abandonarPartida(Jugador jugador) {
        modelo.abandonarPartida(jugador);
        red.enviarMensaje(TipoMensaje.ABANDONAR_PARTIDA);
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

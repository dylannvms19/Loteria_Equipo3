package MVC_JugarPartida;

import Dominio.Jugador;
import Dominio.Partida.MetodoVictoria;

/** La vista comunica acciones al control; el control solo conoce al modelo. */
public class ControlPartida {
    private final ModeloPartida modelo;
    public ControlPartida(ModeloPartida modelo) { this.modelo = modelo; }
    public void jalarCarta() { modelo.jalarCarta(); }
    public void marcarCasilla(Jugador jugador, int posicion) { modelo.marcarCasilla(jugador, posicion); }
    public void reclamarPremio(Jugador jugador, MetodoVictoria metodo) { modelo.reclamarPremio(jugador, metodo); }
    public void abandonarPartida(Jugador jugador) { modelo.abandonarPartida(jugador); }
    public void cambiarAutomatico() { modelo.cambiarAutomatico(); }
    public void cambiarIntervalo(int milisegundos) { modelo.cambiarIntervalo(milisegundos); }
    public void cerrarVista(IVista vista) { modelo.cerrarVista(vista); }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MVCPartida;

import Dominio.ResumenJugador;
import Dominio.Carta;
import Dominio.IDominio;
import Dominio.Jugador;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author josma
 */
public class ModeloPartida implements IModelo {
    
    private IDominio dominio;
    private Jugador jugador; 
    private List<IVista> observadores = new ArrayList<IVista>();

    public ModeloPartida(IDominio dominio, Jugador jugador) {
        this.dominio = dominio;
        this.jugador = jugador;
    }
    
    public void attach(IVista observador){
        observadores.add(observador);
    }
    
    public void detach(IVista observador){
        observadores.remove(observador);
    }
    
    public void notificar(){
        for (IVista observador: observadores) {
              observador.update(this);
        }
    }
    
    public void jalarCarta(){
        dominio.jalarCarta();
        notificar();
    }
    
    public void marcarCasilla(Jugador jugador, int posicion) {
        dominio.marcarCasilla(jugador, posicion);
        notificar();
    }
    
    public void recibirCartaGritada(int idCarta) {
        dominio.aplicarMensaje(IDominio.PREFIJO_CARTA + idCarta);
        notificar();
    }
    
    public void aplicarActualizacionRemota(String mensaje) {
        dominio.aplicarMensaje(mensaje);
        notificar();
    }
    
   
    @Override
    public List<Carta> getCartasTabla() {
        return dominio.getCartasTabla(jugador);
    }

    @Override
    public boolean[] getCasillasMarcadas() {
        return dominio.getCasillasMarcadas(jugador);
    }

    @Override
    public Carta getCartaActual() {
        return dominio.getCartaActual();
    }

    @Override
    public List<Carta> getCartasGritadas() {
        return dominio.getCartasGritadas();
    }

    @Override
    public List<ResumenJugador> getJugadores() {
        return dominio.getJugadores();
    }

    @Override
    public int getPuntaje() {
        return dominio.getPuntaje(jugador);
    }

    @Override
    public String getAviso() {
        return dominio.getAviso();
    }

    @Override
    public boolean isFinalizada() {
        return dominio.isFinalizada();
    }
}

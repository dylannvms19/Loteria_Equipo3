/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Comunicacion;

import MVC_Partida_Modelo.ModeloPartida;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

/**
 *
 * @author ori
 */
public class ComunicacionPartida {

    private ModeloPartida modelo;
    private Timer temporizador;

    public ComunicacionPartida() {
        temporizador = new Timer(3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (modelo != null) {
                    modelo.jalarCarta();
                }
            }
        });
    }

    public void conectarModelo(ModeloPartida modelo) {
        this.modelo = modelo;
    }

    public void recibirMensaje(String texto) {
        if (modelo != null) {
            int idCarta = Integer.parseInt(texto.substring(6));
            modelo.recibirCartaGritada(idCarta);
        }
    }

    public void detenerSiFinalizo() {
        if (modelo != null && (modelo.isFinalizada() || !modelo.hayCartas())) {
            detener();
        }
    }

    public void detener() {
        temporizador.stop();
    }

    public void cambiarAutomatico() {
        if (temporizador.isRunning()) {
            detener();
        } else if (modelo != null && !modelo.isFinalizada() && modelo.hayCartas()) {
            temporizador.start();
        }
        if (modelo != null) {
            modelo.notificar();
        }
    }

    public void cambiarIntervalo(int milisegundos) {
        if (milisegundos < 1000) {
            throw new IllegalArgumentException("Intervalo mínimo: 1000 ms.");
        }
        temporizador.setDelay(milisegundos);
        temporizador.setInitialDelay(milisegundos);
        if (temporizador.isRunning()) {
            temporizador.restart();
        }
        if (modelo != null) {
            modelo.notificar();
        }
    }

    public boolean isAutomatico() {
        return temporizador.isRunning();
    }

    public int getIntervalo() {
        return temporizador.getDelay();
    }
}

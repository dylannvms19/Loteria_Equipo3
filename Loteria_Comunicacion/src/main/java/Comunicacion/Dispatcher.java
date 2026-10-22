/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Comunicacion;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author MrGam
 */
public class Dispatcher implements IDispatcher{

    private BlockingQueue<Object> colaMensajes;

    public Dispatcher() {
        this.colaMensajes = new LinkedBlockingQueue<>();
    }

    @Override
    public void enviarMensaje(Object mensaje) {
        try {
            colaMensajes.put(mensaje);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected BlockingQueue<Object> getCola() {
        return colaMensajes;
    }
}

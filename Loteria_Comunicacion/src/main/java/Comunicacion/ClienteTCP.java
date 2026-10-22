/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Comunicacion;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

/**
 *
 * @author MrGam
 */
public class ClienteTCP implements Runnable {

    private String serverIp;
    private int serverPort;
    private BlockingQueue<Object> colaMensajes;
    private Socket socket;
    private OutputStream out;

    public ClienteTCP(String serverIp, int serverPort, BlockingQueue<Object> colaMensajes) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.colaMensajes = colaMensajes;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(serverIp, serverPort);
            out = socket.getOutputStream();

            while (!Thread.currentThread().isInterrupted()) {
                Object mensaje = colaMensajes.take();

                byte[] bytesAEnviar = mensaje.toString().getBytes();
                out.write(bytesAEnviar);
                out.flush();
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Conexión interrumpida: " + e.getMessage());
        } finally {
            cerrarConexion();
        }
    }

    private void cerrarConexion() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

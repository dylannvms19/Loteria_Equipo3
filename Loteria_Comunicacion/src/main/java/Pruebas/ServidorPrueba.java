/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Pruebas;

/**
 *
 * @author MrGam
 */
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorPrueba {

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("Servidor esperando conexiones en el puerto 5000...");

            Socket cliente = serverSocket.accept();
            System.out.println("¡Cliente conectado desde " + cliente.getInetAddress() + "!");

            InputStream in = cliente.getInputStream();
            byte[] buffer = new byte[1024];
            int bytesLeidos;

            while ((bytesLeidos = in.read(buffer)) != -1) {
                String mensaje = new String(buffer, 0, bytesLeidos);
                System.out.println("Mensaje recibido: " + mensaje);
            }
        } catch (Exception e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        }
    }
}

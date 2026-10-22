/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Comunicacion;

/**
 *
 * @author MrGam
 */
public class DispatcherFactory {

    public static IDispatcher crearMecanismoEnvio(String ip, int puerto) {
        Dispatcher dispatcher = new Dispatcher();
        ClienteTCP clienteTCP = new ClienteTCP(ip, puerto, dispatcher.getCola());

        Thread hiloConexion = new Thread(clienteTCP);
        hiloConexion.start();

        return dispatcher;
    }
}

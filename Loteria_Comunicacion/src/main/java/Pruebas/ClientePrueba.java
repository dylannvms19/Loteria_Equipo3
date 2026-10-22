/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Pruebas;

import Comunicacion.DispatcherFactory;
import Comunicacion.IDispatcher;

/**
 *
 * @author MrGam
 */
public class ClientePrueba {

    public static void main(String[] args) {

        System.out.println("Iniciando cliente de Loteria...");
        IDispatcher comunicacion = DispatcherFactory.crearMecanismoEnvio("127.0.0.1", 5000);

        try {
            System.out.println("Diego se une a la partida");
            comunicacion.enviarMensaje("UNIR_JUGADOR:Navarro");
            Thread.sleep(3000);

            System.out.println("Diego marca el Gallo...");
            comunicacion.enviarMensaje("PONER_FRIJOL:EL_GALLO");
            Thread.sleep(3000);

            System.out.println("diego grita hace buenas");
            comunicacion.enviarMensaje("GRITAR_LOTERIA:Navarro");
            Thread.sleep(500);
            System.out.println("Simulación terminada.");

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

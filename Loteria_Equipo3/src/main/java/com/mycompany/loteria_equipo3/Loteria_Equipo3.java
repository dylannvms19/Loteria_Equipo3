/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.loteria_equipo3;

import Comunicacion.Comunicacion;
import Comunicacion.ComunicacionPartida;
import Comunicacion.DireccionPeer;
import Comunicacion.DirectorioPares;
import Comunicacion.IMensajeria;
import Comunicacion.Mensajeria;
import Dominio.*;
import MVC_JugarPartida.ControlPartida;
import MVC_JugarPartida.ModeloPartida;
import MVC_JugarPartida.VistaPartida;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

/**
 *
 * @author Dylan
 */
//public class Loteria_Equipo3 {
//
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                iniciar();
//            }
//        });
//    }
//
//    private static void iniciar() {
//        List<Jugador> jugadores = new ArrayList<Jugador>();
//        jugadores.add(new Jugador(1, "Jugador 1", 0));
//        jugadores.add(new Jugador(2, "Jugador 2", 0));
//        IDominio dominio = new FachadaDominioMock(jugadores);
//        ComunicacionPartida comunicacion = new ComunicacionPartida();
//        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
//        ModeloPartida modelo = new ModeloPartida(dominio, comunicacion);
//        ControlPartida control = new ControlPartida(modelo);
//        for (int i = 0; i < jugadores.size(); i++) {
//            Jugador jugador = jugadores.get(i);
//            VistaPartida vista = new VistaPartida(control, jugador, i == 0);
//            modelo.attach(vista);
//            modelo.notificar();
//            int x = pantalla.width >= 1380 ? i * 680 + 10 : 30 + i * 55;
//            vista.setLocation(x, 20 + (pantalla.width >= 1380 ? 0 : i * 25));
//            vista.setVisible(true);
//        }
//    }
//}
public class Loteria_Equipo3 {

    /**
     * Uso (todo opcional): Loteria_Equipo3 [idLocal puertoLocal [id@host:puerto ...]]
     * Sin argumentos: nodo 1 en el puerto 6001 y sin pares (el juego funciona igual, solo local).
     */
    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                iniciar(args);
            }
        });
    }

    /** Arma Comunicación + Directorio + Mensajería y abre el puerto. Si no se puede, el juego sigue en local. */
    private static IMensajeria crearRed(String[] args) {
        int idLocal = 1;
        int puertoLocal = 6001;
        DirectorioPares directorio = new DirectorioPares();
        try {
            if (args.length >= 2) {
                idLocal = Integer.parseInt(args[0]);
                puertoLocal = Integer.parseInt(args[1]);
                for (int i = 2; i < args.length; i++) {
                    directorio.agregar(DireccionPeer.parsear(args[i]));
                }
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Argumentos inválidos (" + e.getMessage() + "). Se usa nodo 1, puerto 6001, sin pares.");
            idLocal = 1;
            puertoLocal = 6001;
            directorio = new DirectorioPares();
        }
        final IMensajeria red = new Mensajeria(new DireccionPeer(idLocal, "127.0.0.1", puertoLocal),
                new Comunicacion(puertoLocal, null), directorio);
        // Pendiente (bloques 1 y 4): entregar al modelo. Por ahora solo se muestra lo que llega.
        red.registrarReceptor(mensaje -> System.out.println("[red] Recibido: " + mensaje));
        try {
            red.iniciar();
        } catch (IllegalStateException e) {
            System.err.println("[red] " + e.getMessage() + " - se juega solo en local.");
        }
        Runtime.getRuntime().addShutdownHook(new Thread(red::detener));
        return red;
    }

    private static void iniciar(String[] args) {
        List<Jugador> jugadores = new ArrayList<Jugador>();
        jugadores.add(new Jugador(1, "Navarro", 0));
        jugadores.add(new Jugador(2, "Oponente", 0));

        IDominio dominio = new FachadaDominioMock(jugadores);

        IMensajeria red = crearRed(args);

        ComunicacionPartida simuladorGriton = new ComunicacionPartida();
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();

        ModeloPartida modelo = new ModeloPartida(dominio, simuladorGriton);

        ControlPartida control = new ControlPartida(modelo, red);

        for (int i = 0; i < jugadores.size(); i++) {
            Jugador jugador = jugadores.get(i);
            VistaPartida vista = new VistaPartida(control, jugador, i == 0);
            modelo.attach(vista);
            modelo.notificar();
            int x = pantalla.width >= 1380 ? i * 680 + 10 : 30 + i * 55;
            vista.setLocation(x, 20 + (pantalla.width >= 1380 ? 0 : i * 25));
            vista.setVisible(true);
        }
    }
}

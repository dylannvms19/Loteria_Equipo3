/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.loteria_equipo3;

import Comunicacion.ComunicacionPartida;
import Dominio.*;
import MVC_Partida_Control.ControlPartida;
import MVC_Partida_Modelo.ModeloPartida;
import MVC_Partida_Vista.VistaPartida;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

/**
 *
 * @author Dylan
 */
public class Loteria_Equipo3 {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                iniciar();
            }
        });
    }

    private static void iniciar() {
        List<Jugador> jugadores = new ArrayList<Jugador>();
        jugadores.add(new Jugador(1, "Jugador 1", 0));
        jugadores.add(new Jugador(2, "Jugador 2", 0));
        IDominio dominio = new FachadaDominioMock(jugadores);
        ComunicacionPartida comunicacion = new ComunicacionPartida();
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        ModeloPartida modelo = new ModeloPartida(dominio, comunicacion);
        ControlPartida control = new ControlPartida(modelo);
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

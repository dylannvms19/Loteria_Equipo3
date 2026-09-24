/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.loteria_equipo3;

import Dominio.Baraja;
import Dominio.Carta;
import Dominio.Jugador;
import Dominio.Partida;
import Dominio.Tarjeta;
import MVCPartida.ControlPartida;
import MVCPartida.ModeloPartida;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Dylan
 */
public class Loteria_Equipo3 {

    public static void main(String[] args) {
        List<Carta> cartas = new ArrayList<Carta>();
        for (int i = 1; i <= 54; i++) {
            cartas.add(new Carta(i, "Carta " + i, i, "/imagenes/" + i + ".png"));
        }
        Jugador yo = new Jugador(1, "Jugador 1", 0);
        Jugador otro = new Jugador(2, "Jugador 2", 0);
        List<Tarjeta> tablas = new ArrayList<Tarjeta>();
        for (int t = 1; t <= 2; t++) {
            List<Carta> mezcla = new ArrayList<Carta>(cartas);
            Collections.shuffle(mezcla);
            tablas.add(new Tarjeta(t, new ArrayList<Carta>(mezcla.subList(0, 16))));
        }
        Partida partida = new Partida(List.of(yo, otro), tablas, new Baraja(cartas));

        ModeloPartida modelo = new ModeloPartida(partida, yo);
        ControlPartida control = new ControlPartida(modelo);
        modelo.attach(consulta -> System.out.println("  [update] actual=" + (consulta.getCartaActual() == null ? "-" : consulta.getCartaActual().getNombre())
                + " | bonche=" + consulta.getCartasGritadas().size() + " | aviso='" + consulta.getAviso() + "'"));

        System.out.println("1) marcar antes de que canten:");
        control.marcarCasilla(yo, 0);
        System.out.println("2) el griton jala una carta:");
        control.jalarCarta();
        System.out.println("3) llega por red la carta de mi casilla 0:");
        modelo.recibirCartaGritada(modelo.getCartasTabla().get(0).getIdCarta());
        System.out.println("4) marcar casilla 0 ahora si:");
        control.marcarCasilla(yo, 0);
        System.out.println("   casilla 0 marcada = " + modelo.getCasillasMarcadas()[0]);
    }
}

package Ensamblador;

import Comunicacion.*;
import Dominio.*;
import MVC_JugarPartida.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

/** Cada proceso tiene su dominio, modelo, control y la vista de SU jugador. */
public class EnsambladorPartida {
    private final EnsambladorRed red;
    public EnsambladorPartida(EnsambladorRed red) { this.red = red; }

    public void abrir() {
        final int id = red.getMensajeria().getIdNodo();
        List<Jugador> jugadores = new ArrayList<Jugador>();
        jugadores.add(new Jugador(1, "Jugador 1", 0));
        jugadores.add(new Jugador(2, "Jugador 2", 0));
        IDominio dominio = new FachadaDominioMock(jugadores);
        ModeloPartida modelo = new ModeloPartida(dominio, new ComunicacionPartida());
        final EnlacePartida enlace = new EnlacePartida(modelo, red.getMensajeria());
        red.getMensajeria().registrarReceptor(enlace);
        ControlPartida control = new ControlPartida(modelo);
        VistaPartida vista = new VistaPartida(control, jugadores.get(id - 1), id == 1, true);
        modelo.attach(vista);
        vista.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) { enlace.cerrar(); red.close(); }
        });
        vista.setLocation(20 + (id - 1) * 680, 20);
        modelo.notificar();
        vista.setVisible(true);
        enlace.preparar();
    }
}

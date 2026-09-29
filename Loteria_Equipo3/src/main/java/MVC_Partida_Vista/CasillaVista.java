/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MVC_Partida_Vista;

import Dominio.Carta;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 *
 * @author ori
 */
public class CasillaVista extends JButton {

    private int posicion;
    private PanelTablero tablero;

    public CasillaVista(int posicion, PanelTablero tablero) {
        this.posicion = posicion;
        this.tablero = tablero;
        setMargin(new Insets(2, 2, 2, 2));
        setFont(new Font("SansSerif", Font.PLAIN, 12));
        setFocusPainted(false);
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                seleccionar();
            }
        });
    }

    public void seleccionar() {
        tablero.seleccionarCasilla(posicion);
    }

    public void mostrarCarta(Carta carta) {
        setText("<html><center><b>" + carta.getNumero() + "</b><br>" + carta.getNombre() + "</center></html>");
        setToolTipText(carta.getNombre());
    }

    public void mostrarMarca(boolean marcada) {
        setOpaque(true);
        setBackground(marcada ? new Color(183, 220, 178) : new Color(255, 248, 221));
        setBorder(BorderFactory.createLineBorder(marcada ? new Color(35, 112, 55) : new Color(189, 173, 141), marcada ? 4 : 1));
        getAccessibleContext().setAccessibleDescription(marcada ? "Casilla marcada" : "Casilla sin marcar");
    }
}

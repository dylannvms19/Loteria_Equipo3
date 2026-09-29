/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MVC_Partida_Vista;

import Dominio.Carta;
import java.awt.*;
import javax.swing.*;

/**
 *
 * @author ori
 */
public class PanelCartaActual extends JPanel{

    private JLabel lblCarta = new JLabel("Esperando carta", SwingConstants.CENTER);

    public PanelCartaActual() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Carta gritada"));
        setBackground(new Color(255, 239, 176));
        setPreferredSize(new Dimension(190, 150));
        lblCarta.setFont(new Font("SansSerif", Font.BOLD, 20));
        add(lblCarta);
    }

    public void mostrarCarta(Carta carta) {
        lblCarta.setText(carta == null ? "Esperando carta" : "<html><center>" + carta.getNumero() + "<br>" + carta.getNombre() + "</center></html>");
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MVC_Partida_Vista;
import java.awt.*;
import javax.swing.*;
/**
 *
 * @author ori
 */
public class PanelMarcador extends JPanel{

    private JLabel lblPuntaje = new JLabel("Puntaje: 0");
    private JLabel lblAviso = new JLabel("Esperando carta");

    public PanelMarcador() {
        setLayout(new BorderLayout(8, 4));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        lblPuntaje.setFont(new Font("SansSerif", Font.BOLD, 19));
        lblAviso.setPreferredSize(new Dimension(430, 48));
        add(lblPuntaje, BorderLayout.WEST);
        add(lblAviso, BorderLayout.CENTER);
    }

    public void mostrarPuntaje(int puntaje) {
        lblPuntaje.setText("Puntaje: " + puntaje);
    }

    public void mostrarAviso(String aviso) {
        lblAviso.setText("<html>" + aviso + "</html>");
    }
}

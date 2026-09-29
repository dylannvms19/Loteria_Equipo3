/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MVC_Partida_Vista;
import Dominio.*;
import java.awt.*;
import java.util.List;
import javax.swing.*;
/**
 *
 * @author ori
 */
public class PanelJugadores extends JPanel{

    public PanelJugadores() {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        setBorder(BorderFactory.createTitledBorder("Otros jugadores — tableros de solo lectura"));
    }

    public void mostrarJugadores(List<ResumenJugador> jugadores) {
        removeAll();
        for (ResumenJugador jugador : jugadores) {
            JPanel resumen = new JPanel(new BorderLayout(6, 2));
            resumen.add(new JLabel(jugador.getNombre() + " | " + jugador.getPuntaje() + " puntos" + (jugador.isActivo() ? "" : " (salió)")), BorderLayout.NORTH);
            JPanel miniatura = new JPanel(new GridLayout(4, 4, 2, 2));
            List<Carta> cartas = jugador.getCartasTabla();
            boolean[] marcas = jugador.getCasillasMarcadas();
            for (int i = 0; i < cartas.size(); i++) {
                JLabel casilla = new JLabel(String.valueOf(cartas.get(i).getNumero()), SwingConstants.CENTER);
                casilla.setOpaque(true);
                casilla.setBackground(marcas[i] ? new Color(183, 220, 178) : new Color(255, 248, 221));
                casilla.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                casilla.setToolTipText(cartas.get(i).getNombre() + (marcas[i] ? " (marcada)" : ""));
                miniatura.add(casilla);
            }
            miniatura.setPreferredSize(new Dimension(160, 100));
            resumen.add(miniatura, BorderLayout.CENTER);
            add(resumen);
        }
        revalidate();
        repaint();
    }
}

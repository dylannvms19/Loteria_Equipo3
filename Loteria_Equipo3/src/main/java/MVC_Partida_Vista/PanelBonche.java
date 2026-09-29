/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MVC_Partida_Vista;
import Dominio.Carta;
import java.awt.*;
import java.util.List;
import javax.swing.*;
/**
 *
 * @author ori
 */
public class PanelBonche extends JPanel {

    private DefaultListModel<String> cartas = new DefaultListModel<String>();
    private JList<String> lista = new JList<String>(cartas);

    public PanelBonche() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Bonche"));
        add(new JScrollPane(lista));
        setPreferredSize(new Dimension(190, 180));
    }

    public void mostrarCartas(List<Carta> gritadas) {
        cartas.clear();
        for (int i = gritadas.size() - 1; i >= 0; i--) {
            Carta carta = gritadas.get(i);
            cartas.addElement(carta.getNumero() + " · " + carta.getNombre());
        }
    }
}

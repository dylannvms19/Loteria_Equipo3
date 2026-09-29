/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MVC_Partida_Vista;
import Dominio.Carta;
import java.util.List;
import java.awt.*;
import javax.swing.*;
/**
 *
 * @author ori
 */
public class PanelTablero extends JPanel{

    private VistaPartida vista;
    private CasillaVista[] casillas = new CasillaVista[16];

    public PanelTablero(VistaPartida vista) {
        this.vista = vista;
        setLayout(new GridLayout(4, 4, 4, 4));
        setPreferredSize(new Dimension(360, 340));
        setBorder(BorderFactory.createTitledBorder("Tu tablero — pulsa una carta para marcar"));
        for (int i = 0; i < casillas.length; i++) {
            casillas[i] = new CasillaVista(i, this);
            add(casillas[i]);
        }
    }

    public void mostrarTablero(List<Carta> cartas, boolean[] marcas) {
        for (int i = 0; i < casillas.length; i++) {
            casillas[i].mostrarCarta(cartas.get(i));
            casillas[i].mostrarMarca(marcas[i]);
        }
    }

    public void seleccionarCasilla(int posicion) {
        vista.marcarCasilla(posicion);
    }

    public void habilitar(boolean habilitado) {
        for (CasillaVista casilla : casillas) {
            casilla.setEnabled(habilitado);
        }
    }
}

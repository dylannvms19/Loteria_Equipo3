/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MVC_Partida_Vista;

import Dominio.Jugador;
import Dominio.Partida.MetodoVictoria;
import MVC_Partida_Control.ControlPartida;
import MVC_Partida_Modelo.IModelo;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 *
 * @author ori
 */
public class VistaPartida extends JFrame implements IVista {

    private ControlPartida control;
    private Jugador jugador;
    private PanelTablero panelTablero;
    private PanelCartaActual panelCartaActual = new PanelCartaActual();
    private PanelBonche panelBonche = new PanelBonche();
    private PanelJugadores panelJugadores = new PanelJugadores();
    private PanelMarcador panelMarcador = new PanelMarcador();
    private JButton btnChorro = new JButton("Chorro");
    private JButton btnCuatroEsquinas = new JButton("<html><center>Cuatro<br>esquinas</center></html>");
    private JButton btnCentro = new JButton("Centro");
    private JButton btnBuenas = new JButton("¡Buenas!");
    private JButton btnAbandonar = new JButton("Abandonar");
    private JButton btnSiguiente = new JButton("Gritar siguiente carta");
    private JButton btnAutomatico = new JButton("Iniciar automático");
    private JComboBox<Integer> cmbIntervalo = new JComboBox<Integer>(new Integer[]{1, 3, 5});
    private boolean actualizando;

    public VistaPartida(ControlPartida control, Jugador jugador, boolean anfitrion) {
        this.control = control;
        this.jugador = jugador;
        panelTablero = new PanelTablero(this);
        setTitle("La Lotería — " + jugador.getNombre());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel contenido = new JPanel(new BorderLayout(8, 8));
        contenido.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contenido.setBackground(new Color(245, 230, 197));
        JLabel titulo = new JLabel("LA LOTERÍA  |  " + jugador.getNombre());
        titulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        titulo.setForeground(new Color(158, 48, 48));
        contenido.add(titulo, BorderLayout.NORTH);
        JPanel centro = new JPanel(new BorderLayout(8, 8));
        centro.add(panelTablero, BorderLayout.CENTER);
        JPanel derecha = new JPanel(new BorderLayout(4, 4));
        derecha.add(panelCartaActual, BorderLayout.NORTH);
        derecha.add(panelBonche, BorderLayout.CENTER);
        centro.add(derecha, BorderLayout.EAST);
        JPanel acciones = new JPanel(new GridLayout(1, 5, 4, 4));
        acciones.add(btnChorro);
        acciones.add(btnCuatroEsquinas);
        acciones.add(btnCentro);
        acciones.add(btnBuenas);
        acciones.add(btnAbandonar);
        centro.add(acciones, BorderLayout.SOUTH);
        contenido.add(centro, BorderLayout.CENTER);
        JPanel inferior = new JPanel(new BorderLayout());
        inferior.add(panelMarcador, BorderLayout.NORTH);
        inferior.add(panelJugadores, BorderLayout.CENTER);
        JPanel pruebas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pruebas.setBorder(BorderFactory.createTitledBorder("Griton simulado (prueba local)"));
        if (anfitrion) {
            pruebas.add(btnSiguiente);
            pruebas.add(btnAutomatico);
            pruebas.add(new JLabel("Segundos:"));
            pruebas.add(cmbIntervalo);
        } else {
            pruebas.add(new JLabel("Las cartas se gritan desde la ventana del Jugador 1."));
        }
        inferior.add(pruebas, BorderLayout.SOUTH);
        contenido.add(inferior, BorderLayout.SOUTH);
        setContentPane(contenido);
        conectarAcciones();
        pack();
        setMinimumSize(new Dimension(660, 690));
    }

    private void conectarAcciones() {
        btnChorro.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reclamarPremio(MetodoVictoria.CHORRO);
            }
        });
        btnCuatroEsquinas.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reclamarPremio(MetodoVictoria.CUATRO_ESQUINAS);
            }
        });
        btnCentro.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reclamarPremio(MetodoVictoria.CENTRO);
            }
        });
        btnBuenas.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reclamarPremio(MetodoVictoria.BUENAS);
            }
        });
        btnAbandonar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abandonarPartida();
                dispose();
            }
        });
        btnSiguiente.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jalarCarta();
            }
        });
        btnAutomatico.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                control.cambiarAutomatico();
            }
        });
        cmbIntervalo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!actualizando) {
                    control.cambiarIntervalo(((Integer) cmbIntervalo.getSelectedItem()) * 1000);
                }
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                abandonarPartida();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                control.cerrarVista(VistaPartida.this);
            }
        });
    }

    public void jalarCarta() {
        control.jalarCarta();
    }

    public void marcarCasilla(int posicion) {
        control.marcarCasilla(jugador, posicion);
    }

    public void reclamarPremio(MetodoVictoria metodo) {
        control.reclamarPremio(jugador, metodo);
    }

    public void abandonarPartida() {
        control.abandonarPartida(jugador);
    }

    @Override
    public void update(IModelo modelo) {
        actualizando = true;
        panelTablero.mostrarTablero(modelo.getCartasTabla(jugador), modelo.getCasillasMarcadas(jugador));
        panelCartaActual.mostrarCarta(modelo.getCartaActual());
        panelBonche.mostrarCartas(modelo.getCartasGritadas());
        panelJugadores.mostrarJugadores(modelo.getJugadores(jugador));
        panelMarcador.mostrarPuntaje(modelo.getPuntaje(jugador));
        panelMarcador.mostrarAviso(modelo.getAviso());
        ajustarControles(modelo.isFinalizada());
        btnSiguiente.setEnabled(!modelo.isFinalizada() && modelo.hayCartas() && !modelo.isAutomatico());
        btnAutomatico.setEnabled(!modelo.isFinalizada() && modelo.hayCartas());
        btnAutomatico.setText(modelo.isAutomatico() ? "Pausar automático" : "Iniciar automático");
        cmbIntervalo.setSelectedItem(modelo.getIntervalo() / 1000);
        actualizando = false;
    }

    public void ajustarControles(boolean finalizada) {
        panelTablero.habilitar(!finalizada);
        btnChorro.setEnabled(!finalizada);
        btnCuatroEsquinas.setEnabled(!finalizada);
        btnCentro.setEnabled(!finalizada);
        btnBuenas.setEnabled(!finalizada);
        cmbIntervalo.setEnabled(!finalizada);
    }
}

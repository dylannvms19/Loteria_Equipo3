package Ensamblador;

import Comunicacion.*;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main {
    /** id puerto [idOtro@ip:puerto]. Sin argumentos pregunta qué jugador abrir. */
    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                EnsambladorRed creada = null;
                try {
                    int id;
                    if (args.length == 0) {
                        Object opcion = JOptionPane.showInputDialog(null, "Abre una ejecución por jugador.",
                                "Lotería por TCP", JOptionPane.QUESTION_MESSAGE, null,
                                new String[]{"Jugador 1 (anfitrión)", "Jugador 2"}, "Jugador 1 (anfitrión)");
                        if (opcion == null) { return; }
                        id = opcion.toString().startsWith("Jugador 1") ? 1 : 2;
                    } else { id = Integer.parseInt(args[0]); }
                    if (id != 1 && id != 2) { throw new IllegalArgumentException("Elige jugador 1 o 2."); }
                    int puerto = args.length > 1 ? Integer.parseInt(args[1]) : 6000 + id;
                    DireccionPeer otro = args.length > 2 ? DireccionPeer.parsear(args[2])
                            : new DireccionPeer(3 - id, "127.0.0.1", 6000 + (3 - id));
                    if (otro.idPeer() != 3 - id) { throw new IllegalArgumentException("Dirección del otro jugador incorrecta."); }
                    creada = new EnsambladorRed(new DireccionPeer(id, "127.0.0.1", puerto));
                    final EnsambladorRed red = creada;
                    red.getDirectorio().agregar(otro);
                    red.iniciar();
                    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                        @Override
                        public void run() { red.close(); }
                    }));
                    new EnsambladorPartida(red).abrir();
                } catch (RuntimeException e) {
                    if (creada != null) { creada.close(); }
                    JOptionPane.showMessageDialog(null, e.getMessage(), "No se pudo iniciar", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}

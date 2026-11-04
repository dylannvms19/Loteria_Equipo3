package Ensamblador;

import Comunicacion.*;
import java.io.IOException;
import java.util.Scanner;

/** Prueba manual de la red sin abrir Swing ni modificar el dominio. */
public class ParConsola {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: id puerto [id@ip:puerto ...] [--central=0@ip:puerto] [--host=ipPropia]");
            return;
        }
        String host = "127.0.0.1";
        for (String arg : args) {
            if (arg.startsWith("--host=")) { host = arg.substring(7); }
        }
        DireccionPeer propia = new DireccionPeer(Integer.parseInt(args[0]), host, Integer.parseInt(args[1]));
        try (EnsambladorRed red = new EnsambladorRed(propia)) {
            final Mensajeria mensajeria = red.getMensajeria();
            for (int i = 2; i < args.length; i++) {
                if (args[i].startsWith("--central=")) {
                    mensajeria.setServidorCentral(DireccionPeer.parsear(args[i].substring(10)));
                } else if (!args[i].startsWith("--host=")) {
                    red.getDirectorio().agregar(DireccionPeer.parsear(args[i]));
                }
            }
            mensajeria.registrarReceptor(new IReceptorMensaje() {
                @Override
                public boolean acepta(String tipo) { return !TipoMensaje.valueOf(tipo).esDeDirectorio(); }
                @Override
                public void procesar(Mensaje mensaje) { System.out.println("Recibido: " + mensaje); }
            });
            red.iniciar();
            System.out.println("Par " + propia.idPeer() + " escuchando en " + propia.puerto());
            System.out.println("Comandos: carta 12 | marca 4 | reclamar CHORRO | registrar | consultar | salir");
            Scanner consola = new Scanner(System.in);
            while (consola.hasNextLine()) {
                String linea = consola.nextLine().trim();
                if (linea.equals("salir")) { break; }
                String[] partes = linea.split("\\s+", 2);
                try {
                    switch (partes[0]) {
                        case "carta": mensajeria.enviarMensaje(TipoMensaje.CARTA_GRITADA, partes[1]); break;
                        case "marca": mensajeria.enviarMensaje(TipoMensaje.MARCAR_CASILLA, partes[1]); break;
                        case "reclamar": mensajeria.enviarMensaje(TipoMensaje.RECLAMAR_PREMIO, partes[1]); break;
                        case "registrar": System.out.println(mensajeria.registrarEnServidor()); break;
                        case "consultar": System.out.println(mensajeria.consultarPares()); break;
                        default: System.out.println("Comando desconocido.");
                    }
                } catch (IOException | RuntimeException e) {
                    System.err.println("No se completó la solicitud: " + e.getMessage());
                }
            }
        }
    }
}

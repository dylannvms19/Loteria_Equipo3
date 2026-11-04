package Ensamblador;

import Comunicacion.*;
import java.util.Scanner;

/** Servidor central: únicamente registra y proporciona direcciones de pares. */
public class ServidorDirectorio {
    public static void main(String[] args) {
        int puerto = args.length > 0 ? Integer.parseInt(args[0]) : 5000;
        try (EnsambladorRed red = new EnsambladorRed(new DireccionPeer(0, "127.0.0.1", puerto))) {
            red.getMensajeria().registrarReceptor(new ServicioDirectorio(
                    red.getDirectorio(), red.getMensajeria()));
            red.iniciar();
            System.out.println("Directorio central escuchando en " + puerto + ". Enter para cerrar.");
            Scanner consola = new Scanner(System.in);
            if (consola.hasNextLine()) { consola.nextLine(); }
        }
    }
}

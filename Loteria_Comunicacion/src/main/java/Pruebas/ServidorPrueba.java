package Pruebas;

import Comunicacion.IReceptorExterno;
import Comunicacion.Receptor;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/** Receptor manual del NUEVO protocolo (longitud + bytes). */
public class ServidorPrueba {
    public static void main(String[] args) {
        int puerto = args.length > 0 ? Integer.parseInt(args[0]) : 5000;
        Receptor receptor = new Receptor(puerto);
        receptor.attach(new IReceptorExterno() {
            @Override
            public void recibir(byte[] datos) {
                System.out.println("Bytes recibidos: " + datos.length + " / texto: "
                        + new String(datos, StandardCharsets.UTF_8));
            }
        });
        try {
            receptor.iniciar();
            System.out.println("Escuchando en " + puerto + ". Enter para cerrar.");
            Scanner entrada = new Scanner(System.in);
            if (entrada.hasNextLine()) { entrada.nextLine(); }
        } finally { receptor.cerrar(); }
    }
}

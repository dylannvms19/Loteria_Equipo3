
package Comunicacion;

import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Cliente de petición/respuesta hacia el servidor central: abre una conexión, envía UN mensaje,
 * lee UN mensaje de respuesta (en lugar del ACK de los pares) y cierra.
 * 
 * @author Dylan
 */
public class ClienteServidorTCP {
    static final int TIMEOUT_MS = 3000;

    private final DireccionPeer servidor;

    ClienteServidorTCP(DireccionPeer servidor) {
        this.servidor = servidor;
    }

    Mensaje solicitar(Mensaje peticion) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(servidor.host(), servidor.puerto()), TIMEOUT_MS);
            socket.setSoTimeout(TIMEOUT_MS);
            socket.setTcpNoDelay(true);
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            peticion.escribir(out);
            return Mensaje.leer(new DataInputStream(new BufferedInputStream(socket.getInputStream())));
        }
    }
}


package Comunicacion;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/** Convierte Mensaje a bytes y viceversa; no abre conexiones. */
public class SerializadorMensajes {
    public byte[] serializar(Mensaje mensaje) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        mensaje.escribir(new DataOutputStream(bytes));
        return bytes.toByteArray();
    }

    public Mensaje deserializar(byte[] datos) throws IOException {
        DataInputStream entrada = new DataInputStream(new ByteArrayInputStream(datos));
        Mensaje mensaje = Mensaje.leer(entrada);
        if (entrada.available() != 0) { throw new IOException("Sobran bytes en el mensaje."); }
        return mensaje;
    }
}

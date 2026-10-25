package Comunicacion;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Unidad de intercambio entre pares. Es inmutable y tiene un formato binario
 * propio (DataOutputStream/DataInputStream) con longitudes explícitas, de modo
 * que TCP nunca "pega" dos mensajes y no se usa deserialización de objetos Java.
 *
 * Formato en el cable: UTF(tipo) | int idOrigen | long secuencia | int n | n x UTF(arg)
 * 
 * @author Dylan
 */
public final class Mensaje {

    private final TipoMensaje tipo;
    private final int idOrigen;
    private final long secuencia;
    private final List<String> argumentos;

    public Mensaje(TipoMensaje tipo, int idOrigen, long secuencia, List<String> argumentos) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de mensaje es obligatorio.");
        }
        List<String> copia = new ArrayList<String>(argumentos == null ? List.<String>of() : argumentos);
        if (copia.size() != tipo.getArgumentos()) {
            throw new IllegalArgumentException(tipo + " requiere " + tipo.getArgumentos() + " argumento(s).");
        }
        for (String a : copia) {
            if (a == null) {
                throw new IllegalArgumentException("Los argumentos no pueden ser null.");
            }
        }
        this.tipo = tipo;
        this.idOrigen = idOrigen;
        this.secuencia = secuencia;
        this.argumentos = Collections.unmodifiableList(copia);
    }

    public TipoMensaje getTipo() {
        return tipo;
    }

    /** Id del nodo (jugador) que originó el mensaje. */
    public int getIdOrigen() {
        return idOrigen;
    }

    /** Número creciente por nodo origen; sirve para descartar duplicados. */
    public long getSecuencia() {
        return secuencia;
    }

    public List<String> getArgumentos() {
        return argumentos;
    }

    public String getArgumento(int indice) {
        return argumentos.get(indice);
    }

    public int getArgumentoEntero(int indice) {
        try {
            return Integer.parseInt(argumentos.get(indice).trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Argumento no numérico en " + tipo + ".", e);
        }
    }

    public void escribir(DataOutputStream out) throws IOException {
        out.writeUTF(tipo.name());
        out.writeInt(idOrigen);
        out.writeLong(secuencia);
        out.writeInt(argumentos.size());
        for (String a : argumentos) {
            out.writeUTF(a);
        }
        out.flush();
    }

    /**
     * Lee un mensaje completo. Lanza EOFException si el par cerró la conexión
     * y IOException si el contenido no respeta el protocolo.
     */
    public static Mensaje leer(DataInputStream in) throws IOException {
        String nombreTipo = in.readUTF();
        TipoMensaje tipo;
        try {
            tipo = TipoMensaje.valueOf(nombreTipo);
        } catch (IllegalArgumentException e) {
            throw new IOException("Tipo de mensaje desconocido: " + nombreTipo);
        }
        int idOrigen = in.readInt();
        long secuencia = in.readLong();
        int n = in.readInt();
        if (n != tipo.getArgumentos()) {
            throw new IOException("Cantidad de argumentos inválida para " + tipo + ": " + n);
        }
        List<String> args = new ArrayList<String>(n);
        for (int i = 0; i < n; i++) {
            args.add(in.readUTF());
        }
        return new Mensaje(tipo, idOrigen, secuencia, args);
    }

    @Override
    public String toString() {
        return tipo + "[origen=" + idOrigen + ", seq=" + secuencia + ", args=" + argumentos + "]";
    }
}

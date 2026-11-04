package Comunicacion;

import java.util.Arrays;

/** Datos y destino de un envío pendiente. */
public final class Envio {
    public static final int MAX_BYTES = 1024 * 1024;
    private final byte[] datos;
    private final String ip;
    private final int puerto;

    public Envio(byte[] datos, String ip, int puerto) {
        if (datos == null || datos.length > MAX_BYTES) {
            throw new IllegalArgumentException("Datos nulos o mayores a 1 MB.");
        }
        if (ip == null || ip.isBlank() || puerto < 1 || puerto > 65535) {
            throw new IllegalArgumentException("Dirección o puerto inválidos.");
        }
        this.datos = Arrays.copyOf(datos, datos.length);
        this.ip = ip;
        this.puerto = puerto;
    }

    public byte[] getDatos() { return Arrays.copyOf(datos, datos.length); }
    public String getIp() { return ip; }
    public int getPuerto() { return puerto; }
}

package Comunicacion;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/** Solo abre la conexión y escribe bytes. La cola pertenece a Dispatcher. */
public class ClienteTCP {
    private Socket socket;
    private boolean cerrado;

    public void enviar(Envio envio) throws IOException {
        Socket conexion = new Socket();
        synchronized (this) {
            if (cerrado) {
                conexion.close();
                throw new IOException("ClienteTCP cerrado.");
            }
            socket = conexion;
        }
        try (Socket recurso = conexion) {
            recurso.connect(new InetSocketAddress(envio.getIp(), envio.getPuerto()), 2000);
            byte[] datos = envio.getDatos();
            DataOutputStream salida = new DataOutputStream(recurso.getOutputStream());
            // TCP es un flujo: primero se indica cuántos bytes tiene este mensaje.
            salida.writeInt(datos.length);
            salida.write(datos);
            salida.flush();
        } finally {
            synchronized (this) {
                if (socket == conexion) { socket = null; }
            }
        }
    }

    public synchronized void cerrar() {
        cerrado = true;
        if (socket != null) {
            try { socket.close(); }
            catch (IOException e) { System.err.println("Cierre de cliente: " + e.getMessage()); }
        }
    }
}

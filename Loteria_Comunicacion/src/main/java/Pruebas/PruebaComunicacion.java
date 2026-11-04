package Pruebas;

import Comunicacion.*;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/** Ejecutar como Java Main Class. Usa TCP real en loopback; falla con AssertionError. */
public class PruebaComunicacion {
    private static int comprobaciones;

    private static class Buzon implements IReceptorExterno {
        final BlockingQueue<byte[]> datos = new LinkedBlockingQueue<byte[]>();
        @Override
        public void recibir(byte[] bytes) { datos.add(bytes); }
        byte[] siguiente() throws InterruptedException {
            byte[] resultado = datos.poll(5, TimeUnit.SECONDS);
            if (resultado == null) { throw new AssertionError("No llegó el mensaje."); }
            return resultado;
        }
    }

    private static int puertoLibre() throws Exception {
        try (ServerSocket reserva = new ServerSocket(0)) { return reserva.getLocalPort(); }
    }

    private static void verificar(String nombre, boolean condicion) {
        if (!condicion) { throw new AssertionError(nombre); }
        comprobaciones++;
        System.out.println("OK: " + nombre);
    }

    private static byte[] bytes(String texto) { return texto.getBytes(StandardCharsets.UTF_8); }

    public static void main(String[] args) throws Exception {
        int puertoA = puertoLibre();
        Receptor a = new Receptor(puertoA);
        int puertoB;
        Receptor b = null;
        IDispatcher salidaA = DispatcherFactory.crear();
        IDispatcher salidaB = DispatcherFactory.crear();
        Buzon entradaA = new Buzon();
        Buzon entradaB = new Buzon();
        try {
            a.attach(entradaA);
            a.iniciar();
            puertoB = puertoLibre();
            b = new Receptor(puertoB);
            b.attach(entradaB);
            b.attach(entradaB);
            b.iniciar();
            salidaA.iniciar();
            salidaB.iniciar();
            a.iniciar(); salidaA.iniciar(); // Inicio repetido no crea trabajadores extra.

            salidaA.enviar(bytes("CARTA:12 áéñ"), "127.0.0.1", puertoB);
            verificar("A envía a B y conserva UTF-8", Arrays.equals(bytes("CARTA:12 áéñ"), entradaB.siguiente()));
            verificar("attach no duplica observadores", entradaB.datos.poll(150, TimeUnit.MILLISECONDS) == null);
            salidaB.enviar(bytes("MARCA:4"), "127.0.0.1", puertoA);
            verificar("B responde a A", Arrays.equals(bytes("MARCA:4"), entradaA.siguiente()));

            byte[] grande = new byte[100000];
            for (int i = 0; i < grande.length; i++) { grande[i] = (byte) i; }
            salidaA.enviar(grande, "127.0.0.1", puertoB);
            verificar("Mensaje binario de 100000 bytes llega completo", Arrays.equals(grande, entradaB.siguiente()));
            salidaA.enviar(new byte[0], "127.0.0.1", puertoB);
            verificar("Trama vacía válida", entradaB.siguiente().length == 0);

            byte[] mutable = bytes("original");
            salidaA.enviar(mutable, "127.0.0.1", puertoB);
            mutable[0] = 0;
            verificar("La cola conserva copia de los datos", Arrays.equals(bytes("original"), entradaB.siguiente()));
            for (int i = 0; i < 20; i++) { salidaA.enviar(bytes("m" + i), "127.0.0.1", puertoB); }
            boolean orden = true;
            for (int i = 0; i < 20; i++) { orden &= Arrays.equals(bytes("m" + i), entradaB.siguiente()); }
            verificar("20 envíos completos en orden FIFO", orden);

            Buzon extra = new Buzon();
            b.attach(extra);
            salidaA.enviar(bytes("dos"), "127.0.0.1", puertoB);
            verificar("Se notifica a varios observadores", Arrays.equals(entradaB.siguiente(), extra.siguiente()));
            b.detach(extra);
            salidaA.enviar(bytes("uno"), "127.0.0.1", puertoB);
            entradaB.siguiente();
            verificar("detach deja de notificar al retirado", extra.datos.poll(150, TimeUnit.MILLISECONDS) == null);

            final IReceptorExterno defectuoso = new IReceptorExterno() {
                @Override
                public void recibir(byte[] datos) { throw new IllegalStateException("Falla intencional de prueba"); }
            };
            a.detach(entradaA);
            a.attach(defectuoso);
            a.attach(entradaA);
            salidaB.enviar(bytes("continúa"), "127.0.0.1", puertoA);
            verificar("Un observador defectuoso no detiene a los demás", Arrays.equals(bytes("continúa"), entradaA.siguiente()));
            a.detach(defectuoso);

            // Un cliente puede escribir una trama por partes; readFully debe reconstruirla.
            try (Socket socket = new Socket("127.0.0.1", puertoB)) {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.writeInt(3); out.flush();
                out.writeByte(10); out.flush(); out.writeByte(20); out.flush(); out.writeByte(30); out.flush();
            }
            verificar("Se reconstruye una trama escrita por partes", Arrays.equals(new byte[]{10,20,30}, entradaB.siguiente()));
            try (Socket socket = new Socket("127.0.0.1", puertoB)) {
                new DataOutputStream(socket.getOutputStream()).writeInt(-1);
            }
            try (Socket socket = new Socket("127.0.0.1", puertoB)) {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.writeInt(10); out.writeByte(1);
            }
            salidaA.enviar(bytes("después de tramas inválidas"), "127.0.0.1", puertoB);
            verificar("Tramas inválidas no detienen ServidorTCP", Arrays.equals(bytes("después de tramas inválidas"), entradaB.siguiente()));

            salidaA.enviar(bytes("sin destino"), "127.0.0.1", puertoLibre());
            salidaA.enviar(bytes("sigue"), "127.0.0.1", puertoB);
            verificar("Un destino caído no mata Dispatcher", Arrays.equals(bytes("sigue"), entradaB.siguiente()));

            Receptor ocupado = new Receptor(puertoB);
            boolean falloPuerto = false;
            try { ocupado.iniciar(); }
            catch (IllegalStateException e) { falloPuerto = true; }
            finally { ocupado.cerrar(); }
            verificar("Se informa si el puerto está ocupado", falloPuerto);
            boolean limite = false;
            try { salidaA.enviar(new byte[Envio.MAX_BYTES + 1], "127.0.0.1", puertoB); }
            catch (IllegalArgumentException e) { limite = true; }
            verificar("Se rechazan mensajes mayores al límite", limite);
        } finally {
            salidaA.cerrar(); salidaB.cerrar(); a.cerrar();
            if (b != null) { b.cerrar(); }
        }
        boolean cerrado = false;
        try { salidaA.enviar(bytes("tarde"), "127.0.0.1", puertoA); }
        catch (IllegalStateException e) { cerrado = true; }
        verificar("No se encola después del cierre", cerrado);
        a.cerrar(); salidaA.cerrar();
        Receptor nuevo = new Receptor(puertoA);
        try { nuevo.iniciar(); verificar("El cierre libera el puerto", true); }
        finally { nuevo.cerrar(); }
        boolean hilosVivos = false;
        for (Thread hilo : Thread.getAllStackTraces().keySet()) {
            if (hilo.isAlive() && hilo.getName().startsWith("loteria-")) { hilosVivos = true; }
        }
        verificar("No quedan trabajadores de transporte activos", !hilosVivos);
        System.out.println("PRUEBA COMPLETA: " + comprobaciones + " comprobaciones correctas.");
    }
}

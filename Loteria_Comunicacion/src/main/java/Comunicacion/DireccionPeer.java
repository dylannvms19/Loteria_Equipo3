/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Comunicacion;

/**
 * Dirección de un par de la red: id del nodo/jugador, host y puerto de escucha.
 * Formato de texto: {@code id@host:puerto}, por ejemplo {@code 2@192.168.1.20:6002}.
 * 
 * @author Dylan
 */
public record DireccionPeer(int idPeer, String host, int puerto) {

    public DireccionPeer {
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("El host del peer es obligatorio.");
        }
        if (puerto < 1 || puerto > 65535) {
            throw new IllegalArgumentException("Puerto fuera de rango: " + puerto);
        }
    }

    public static DireccionPeer parsear(String texto) {
        try {
            int arroba = texto.indexOf('@');
            int dosPuntos = texto.lastIndexOf(':');
            if (arroba < 1 || dosPuntos < arroba + 2) {
                throw new IllegalArgumentException();
            }
            return new DireccionPeer(
                    Integer.parseInt(texto.substring(0, arroba).trim()),
                    texto.substring(arroba + 1, dosPuntos).trim(),
                    Integer.parseInt(texto.substring(dosPuntos + 1).trim()));
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Peer inválido '" + texto + "'. Formato: id@host:puerto", e);
        }
    }
}

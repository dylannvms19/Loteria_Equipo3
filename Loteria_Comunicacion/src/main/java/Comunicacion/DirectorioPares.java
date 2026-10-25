
package Comunicacion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Directorio de pares: quién está en la red y dónde escucha. Lo usan los dos lados del diagrama:
 * el Par (guarda lo que le responde el servidor) y el Servidor central (guarda los registros).
 * Es seguro entre hilos y siempre entrega los pares ordenados por id.
 * 
 * @author Dylan
 */
public class DirectorioPares {
    private final Map<Integer, DireccionPeer> pares = new TreeMap<Integer, DireccionPeer>();

    /** @return true si el id era nuevo (si ya existía, se actualiza su dirección). */
    public synchronized boolean agregar(DireccionPeer par) {
        return pares.put(par.idPeer(), par) == null;
    }

    public synchronized void quitar(int idPeer) {
        pares.remove(idPeer);
    }

    /** Sustituye todo el contenido por la lista dada. */
    public synchronized void reemplazar(Collection<DireccionPeer> nuevos) {
        pares.clear();
        for (DireccionPeer p : nuevos) {
            pares.put(p.idPeer(), p);
        }
    }

    /** Copia ordenada por id. */
    public synchronized List<DireccionPeer> obtener() {
        return new ArrayList<DireccionPeer>(pares.values());
    }

    public synchronized int tamano() {
        return pares.size();
    }

    /** Formato de cable de la lista: {@code 1@host:6001;2@host:6002} (vacío si no hay pares). */
    public String serializar() {
        StringBuilder sb = new StringBuilder();
        for (DireccionPeer p : obtener()) {
            if (sb.length() > 0) {
                sb.append(';');
            }
            sb.append(p.idPeer()).append('@').append(p.host()).append(':').append(p.puerto());
        }
        return sb.toString();
    }

    /** Inversa de {@link #serializar()}. Lanza IllegalArgumentException si algún elemento es inválido. */
    public static List<DireccionPeer> parsearLista(String texto) {
        List<DireccionPeer> lista = new ArrayList<DireccionPeer>();
        if (texto == null || texto.isBlank()) {
            return lista;
        }
        for (String elemento : texto.split(";")) {
            lista.add(DireccionPeer.parsear(elemento));
        }
        return lista;
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Dominio;

import static Dominio.IDominio.PREFIJO_CARTA;
import Dominio.Partida.MetodoVictoria;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 *
 * @author ori
 */
public class FachadaDominioMock implements IDominio {

    private Partida partida;

    public FachadaDominioMock(List<Jugador> jugadores) {
        List<Carta> catalogo = crearCatalogo();
        List<Tarjeta> tablas = new ArrayList<Tarjeta>();
        for (int t = 0; t < jugadores.size(); t++) {
            // Tablas de prueba diferentes y repetibles para poder revisar el ejercicio.
            List<Carta> cartas = new ArrayList<Carta>();
            for (int i = 0; i < 16; i++) {
                cartas.add(catalogo.get((t * 8 + i) % 54));
            }
            Collections.shuffle(cartas, new Random(100 + t));
            tablas.add(new Tarjeta(t + 1, cartas));
        }
        partida = new Partida(jugadores, tablas, new Baraja(catalogo));
    }

    private List<Carta> crearCatalogo() {
        String[] nombres = {"El gallo", "El diablito", "La dama", "El catrín", "El paraguas", "La sirena",
            "La escalera", "La botella", "El barril", "El árbol", "El melón", "El valiente", "El gorrito",
            "La muerte", "La pera", "La bandera", "El bandolón", "El violoncello", "La garza", "El pájaro",
            "La mano", "La bota", "La luna", "El cotorro", "El borracho", "El negrito", "El corazón",
            "La sandía", "El tambor", "El camarón", "Las jaras", "El músico", "La araña", "El soldado",
            "La estrella", "El cazo", "El mundo", "El apache", "El nopal", "El alacrán", "La rosa",
            "La calavera", "La campana", "El cantarito", "El venado", "El sol", "La corona", "La chalupa",
            "El pino", "El pescado", "La palma", "La maceta", "El arpa", "La rana"};
        List<Carta> cartas = new ArrayList<Carta>();
        for (int i = 0; i < nombres.length; i++) {
            cartas.add(new Carta(i + 1, nombres[i], i + 1, "/cartas/" + (i + 1) + ".png"));
        }
        return cartas;
    }

    private Jugador jugadorValido(Jugador solicitado) {
        if (solicitado == null) {
            return null;
        }
        Jugador j = partida.buscarJugador(solicitado.getIdJugador());
        return j != null && j.isActivo() ? j : null;
    }

    @Override
    public Carta jalarCarta() {
        if (partida.isFinalizada()) {
            return partida.getCartaActual();
        }
        Carta carta = partida.getBaraja().extraerCarta();
        if (carta == null) {
            partida.setAviso("No quedan cartas. La partida sigue abierta para marcar y reclamar Buenas.");
        } else {
            partida.registrarCarta(carta);
            partida.setAviso("Carta gritada: " + carta.getNombre());
        }
        return carta;
    }

    @Override
    public void registrarCartaGritada(int idCarta) {
        if (partida.isFinalizada()) {
            return;
        }
        Carta carta = partida.getBaraja().buscarCarta(idCarta);
        if (carta == null) {
            partida.setAviso("Carta desconocida: " + idCarta);
            return;
        }
        // Un mensaje repetido no duplica el bonche ni devuelve cartas a la baraja.
        if (partida.getBonche().validaCarta(carta)) {
            return;
        }
        partida.getBaraja().retirarCarta(idCarta);
        partida.registrarCarta(carta);
        partida.setAviso("Carta gritada: " + carta.getNombre());
    }

    @Override
    public void aplicarMensaje(String mensaje) {
        if (partida.isFinalizada()) {
            return;
        }
        if (mensaje == null || !mensaje.startsWith(PREFIJO_CARTA)) {
            partida.setAviso("Mensaje de prueba no reconocido.");
            return;
        }
        try {
            registrarCartaGritada(Integer.parseInt(mensaje.substring(PREFIJO_CARTA.length()).trim()));
        } catch (NumberFormatException ex) {
            partida.setAviso("El mensaje debe tener la forma CARTA:numero.");
        }
    }

    @Override
    public boolean marcarCasilla(Jugador solicitado, int posicion) {
        if (partida.isFinalizada()) {
            return false;
        }
        Jugador j = jugadorValido(solicitado);
        if (j == null) {
            partida.setAviso("Jugador no disponible.");
            return false;
        }
        TarjetaJugador tabla = partida.getTarjetaJugador(j);
        Carta carta = tabla.obtenerCarta(posicion);
        if (carta == null) {
            partida.setAviso("Posición de casilla no válida.");
            return false;
        }
        if (!partida.getBonche().validaCarta(carta)) {
            partida.setAviso(j.getNombre() + ": esa carta todavía no ha sido gritada.");
            return false;
        }
        if (!tabla.marcarCasilla(posicion)) {
            partida.setAviso(j.getNombre() + ": esa casilla ya estaba marcada.");
            return false;
        }
        partida.setAviso(j.getNombre() + " marcó " + carta.getNombre() + ".");
        return true;
    }

    @Override
    public boolean reclamarPremio(Jugador solicitado, MetodoVictoria metodo) {
        if (partida.isFinalizada()) {
            return false;
        }
        Jugador j = jugadorValido(solicitado);
        if (j == null || metodo == null) {
            partida.setAviso("Reclamación no válida.");
            return false;
        }
        Jugador anterior = partida.getGanadorMetodo(metodo);
        if (anterior != null) {
            partida.setAviso(nombreMetodo(metodo) + " ya fue reclamado por " + anterior.getNombre() + ".");
            return false;
        }
        boolean[] marcas = partida.getTarjetaJugador(j).getEstadosCasillas();
        if (!cumplePatron(marcas, metodo)) {
            partida.setAviso(j.getNombre() + ": todavía no completas " + nombreMetodo(metodo) + ".");
            return false;
        }
        partida.registrarReclamacion(j, metodo);
        partida.setAviso(j.getNombre() + " consiguió " + nombreMetodo(metodo) + ": +"
                + partida.getPuntosMetodo(metodo) + " puntos."
                + (metodo == MetodoVictoria.BUENAS ? " ¡Partida finalizada!" : ""));
        return true;
    }

    private boolean cumplePatron(boolean[] m, MetodoVictoria metodo) {
        if (metodo == MetodoVictoria.BUENAS) {
            for (boolean marcada : m) {
                if (!marcada) {
                    return false;
                }
            }
            return true;
        }
        if (metodo == MetodoVictoria.CUATRO_ESQUINAS) {
            return m[0] && m[3] && m[12] && m[15];
        }
        if (metodo == MetodoVictoria.CENTRO) {
            return m[5] && m[6] && m[9] && m[10];
        }
        for (int i = 0; i < 4; i++) {
            boolean fila = true;
            boolean columna = true;
            for (int k = 0; k < 4; k++) {
                fila = fila && m[i * 4 + k];
                columna = columna && m[k * 4 + i];
            }
            if (fila || columna) {
                return true;
            }
        }
        return (m[0] && m[5] && m[10] && m[15]) || (m[3] && m[6] && m[9] && m[12]);
    }

    private String nombreMetodo(MetodoVictoria metodo) {
        if (metodo == MetodoVictoria.CUATRO_ESQUINAS) {
            return "Cuatro esquinas";
        }
        if (metodo == MetodoVictoria.CHORRO) {
            return "Chorro";
        }
        if (metodo == MetodoVictoria.CENTRO) {
            return "Centro";
        }
        return "Buenas";
    }

    @Override
    public void abandonarPartida(Jugador solicitado) {
        if (partida.isFinalizada()) {
            return;
        }
        Jugador j = jugadorValido(solicitado);
        if (j == null) {
            return;
        }
        j.abandonar();
        int activos = 0;
        for (Jugador item : partida.getJugadores()) {
            if (item.isActivo()) {
                activos++;
            }
        }
        partida.setAviso(j.getNombre() + " abandonó la partida.");
        if (activos < 2) {
            partida.cancelarPartida();
            partida.setAviso("Partida cancelada: quedan menos de dos jugadores.");
        }
    }

    @Override
    public List<Carta> getCartasTabla(Jugador j) {
        List<Carta> cartas = new ArrayList<Carta>();
        if (j != null && partida.buscarJugador(j.getIdJugador()) != null) {
            TarjetaJugador t = partida.getTarjetaJugador(j);
            for (int i = 0; i < 16; i++) {
                cartas.add(t.obtenerCarta(i));
            }
        }
        return cartas;
    }

    @Override
    public boolean[] getCasillasMarcadas(Jugador j) {
        return j != null && partida.buscarJugador(j.getIdJugador()) != null
                ? partida.getTarjetaJugador(j).getEstadosCasillas() : new boolean[16];
    }

    @Override
    public Carta getCartaActual() {
        return partida.getCartaActual();
    }

    @Override
    public List<Carta> getCartasGritadas() {
        return partida.getBonche().getCartasPasadas();
    }

    @Override
    public List<ResumenJugador> getJugadores() {
        List<ResumenJugador> lista = new ArrayList<ResumenJugador>();
        for (Jugador j : partida.getJugadores()) {
            lista.add(new ResumenJugador(j.getIdJugador(), j.getNombre(), getCartasTabla(j),
                    getCasillasMarcadas(j), j.getPuntuaje(), j.isActivo()));
        }
        return lista;
    }

    @Override
    public int getPuntaje(Jugador j) {
        Jugador real = j == null ? null : partida.buscarJugador(j.getIdJugador());
        return real == null ? 0 : real.getPuntuaje();
    }

    @Override
    public String getAviso() {
        return partida.getAviso();
    }

    @Override
    public boolean isFinalizada() {
        return partida.isFinalizada();
    }

    @Override
    public boolean hayCartas() {
        return partida.getBaraja().hayCartas();
    }
}

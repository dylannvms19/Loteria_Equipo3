package Comunicacion;

import Dominio.Carta;
import Dominio.Partida.MetodoVictoria;
import MVC_JugarPartida.ModeloPartida;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.UUID;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/** Adaptador del juego a Mensajería. Ninguna IP, socket ni byte llega al MVC. */
public class EnlacePartida implements ISalidaPartida, IReceptorMensaje {
    private final ModeloPartida modelo;
    private final IMensajeria mensajeria;
    private final int idLocal;
    private final String instancia = UUID.randomUUID().toString();
    private String instanciaInvitado;
    private String sesion;
    private boolean listo;
    private boolean iniciada;
    private long orden;
    private long ultimoAplicado;
    private final Timer anuncioListo;

    public EnlacePartida(ModeloPartida modelo, IMensajeria mensajeria) {
        this.modelo = modelo;
        this.mensajeria = mensajeria;
        idLocal = mensajeria.getIdNodo();
        if (idLocal != 1 && idLocal != 2) {
            throw new IllegalArgumentException("Esta prueba gráfica tiene jugadores 1 y 2.");
        }
        anuncioListo = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { anunciarListo(); }
        });
        modelo.conectarSalida(this, idLocal, idLocal == 1);
    }

    /** Se llama después de abrir la vista y el receptor TCP. */
    public void preparar() {
        listo = true;
        if (idLocal == 2) { anunciarListo(); anuncioListo.start(); }
        else { intentarInicio(); }
    }

    private void anunciarListo() {
        if (iniciada) { anuncioListo.stop(); return; }
        try { mensajeria.enviarAJugador(1, TipoMensaje.LISTO, instancia); }
        catch (RuntimeException e) { modelo.mostrarErrorRed(e.getMessage()); }
    }

    @Override
    public boolean acepta(String tipo) {
        return tipo.equals("LISTO") || tipo.equals("INICIAR_PARTIDA")
                || tipo.equals("SOLICITAR_ACCION") || tipo.equals("ACCION_ORDENADA")
                || tipo.equals("ERROR_PARTIDA");
    }

    @Override
    public void procesar(final Mensaje mensaje) {
        // El transporte notifica en su trabajador. El modelo y Swing se cambian en el EDT.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try { recibirEnSwing(mensaje); }
                catch (RuntimeException e) { modelo.mostrarErrorRed("Mensaje de partida inválido: " + e.getMessage()); }
            }
        });
    }

    private void recibirEnSwing(Mensaje mensaje) {
        int origen = mensaje.getIdOrigen();
        if (origen != 1 && origen != 2) { return; }
        switch (mensaje.getTipo()) {
            case LISTO:
                if (idLocal != 1 || origen != 2) { return; }
                if (iniciada && !mensaje.getArgumento(0).equals(instanciaInvitado)) {
                    mensajeria.enviarAJugador(2, TipoMensaje.ERROR_PARTIDA,
                            "La partida ya empezó. Para esta prueba, reinicien ambos jugadores.");
                    return;
                }
                instanciaInvitado = mensaje.getArgumento(0);
                if (iniciada) {
                    // Un anuncio repetido no vuelve a sacar la primera carta.
                    mensajeria.enviarAJugador(2, TipoMensaje.INICIAR_PARTIDA, sesion, instanciaInvitado);
                } else { intentarInicio(); }
                break;
            case INICIAR_PARTIDA:
                if (idLocal != 2 || origen != 1 || !instancia.equals(mensaje.getArgumento(1)) || iniciada) { return; }
                sesion = mensaje.getArgumento(0);
                iniciada = true;
                anuncioListo.stop();
                modelo.iniciarPartida();
                break;
            case SOLICITAR_ACCION:
                if (idLocal == 1 && origen == 2 && iniciada && sesion.equals(mensaje.getArgumento(0))) {
                    ordenar(origen, AccionPartida.valueOf(mensaje.getArgumento(1)), mensaje.getArgumento(2));
                }
                break;
            case ACCION_ORDENADA:
                if (idLocal != 2 || origen != 1 || !iniciada || !sesion.equals(mensaje.getArgumento(0))) { return; }
                aplicar(Long.parseLong(mensaje.getArgumento(1)), Integer.parseInt(mensaje.getArgumento(2)),
                        AccionPartida.valueOf(mensaje.getArgumento(3)), mensaje.getArgumento(4));
                break;
            case ERROR_PARTIDA:
                if (origen == 1) { anuncioListo.stop(); modelo.mostrarErrorRed(mensaje.getArgumento(0)); }
                break;
            default: break;
        }
    }

    private void intentarInicio() {
        if (iniciada || !listo || instanciaInvitado == null) { return; }
        sesion = UUID.randomUUID().toString();
        mensajeria.enviarAJugador(2, TipoMensaje.INICIAR_PARTIDA, sesion, instanciaInvitado);
        iniciada = true;
        modelo.iniciarPartida();
        ordenar(1, AccionPartida.SIGUIENTE_CARTA, "");
    }

    @Override
    public void solicitar(AccionPartida accion, int idJugador, String valor) {
        if (!iniciada || idJugador != idLocal || modelo.isFinalizada()) { return; }
        if (idLocal == 1) { ordenar(idLocal, accion, valor); }
        else { mensajeria.enviarAJugador(1, TipoMensaje.SOLICITAR_ACCION, sesion, accion.name(), valor); }
    }

    /** El anfitrión es UN PAR, no el servidor del directorio. Ordena sin decidir las reglas. */
    private void ordenar(int actor, AccionPartida accion, String valor) {
        if (modelo.isFinalizada()) { return; }
        if (accion == AccionPartida.CARTA) { return; } // Una petición no puede inventar cartas.
        if (accion == AccionPartida.SIGUIENTE_CARTA) {
            if (actor != 1) { return; }
            Carta carta = modelo.extraerCartaParaAnunciar();
            if (carta == null) { modelo.notificar(); return; }
            accion = AccionPartida.CARTA;
            valor = String.valueOf(carta.getIdCarta());
        } else if (accion == AccionPartida.MARCAR) {
            Integer.parseInt(valor);
        } else if (accion == AccionPartida.RECLAMAR) {
            MetodoVictoria.valueOf(valor);
        }
        orden++;
        mensajeria.enviarAJugador(2, TipoMensaje.ACCION_ORDENADA, sesion, String.valueOf(orden),
                String.valueOf(actor), accion.name(), valor);
        aplicar(orden, actor, accion, valor);
    }

    private void aplicar(long numero, int actor, AccionPartida accion, String valor) {
        if (numero <= ultimoAplicado) { return; }
        if (numero != ultimoAplicado + 1) {
            modelo.mostrarErrorRed("Falta una actualización. Reinicien ambos jugadores para esta prueba.");
            return;
        }
        ultimoAplicado = numero;
        modelo.aplicarAccion(accion, actor, valor);
    }

    public void cerrar() { anuncioListo.stop(); mensajeria.retirarReceptor(this); }
}

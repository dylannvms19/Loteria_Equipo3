package Comunicacion;

/** Atención de registros y consultas; se instala solo en el servidor central. */
public class ServicioDirectorio implements IReceptorMensaje {
    private final DirectorioPares directorio;
    private final Mensajeria mensajeria;

    public ServicioDirectorio(DirectorioPares directorio, Mensajeria mensajeria) {
        this.directorio = directorio;
        this.mensajeria = mensajeria;
    }

    @Override
    public boolean acepta(String tipo) {
        return tipo.equals("REGISTRAR_PAR") || tipo.equals("CONSULTAR_PARES");
    }

    @Override
    public void procesar(Mensaje mensaje) {
        if (mensaje.getTipo() == TipoMensaje.REGISTRAR_PAR) {
            int id = mensaje.getArgumentoEntero(0);
            if (id != mensaje.getIdOrigen()) {
                throw new IllegalArgumentException("El id del registro no coincide con el origen.");
            }
            directorio.agregar(new DireccionPeer(id, mensaje.getArgumento(1),
                    mensaje.getArgumentoEntero(2)));
        }
        for (DireccionPeer par : directorio.obtener()) {
            if (par.idPeer() == mensaje.getIdOrigen()) {
                mensajeria.responderDirectorio(mensaje, par, directorio.serializar());
                return;
            }
        }
        // Para conocer el puerto de retorno hay que registrarse antes de consultar.
        System.err.println("Consulta de un par que todavía no se registró.");
    }
}

package Comunicacion;

/** Observador de los bytes recibidos por Comunicación. */
public interface IReceptorExterno {
    void recibir(byte[] datos);
}

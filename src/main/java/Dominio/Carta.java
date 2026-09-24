/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Dominio;

/**
 *
 * @author josma
 */
public class Carta {
    private int idCarta;
    private String nombre;
    private int numero;
    private String imagenRuta;

    public Carta(int idCarta, String nombre, int numero, String imagenRuta) {
        this.idCarta = idCarta;
        this.nombre = nombre;
        this.numero = numero;
        this.imagenRuta = imagenRuta;
    }

    public int getIdCarta() {
        return idCarta;
    }

    public String getNombre() {
        return nombre;
    }

    public int getNumero() {
        return numero;
    }

    public String getImagenRuta() {
        return imagenRuta;
    }
    
    
    
    
}

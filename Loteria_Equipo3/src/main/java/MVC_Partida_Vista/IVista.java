/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package MVC_Partida_Vista;

import MVC_Partida_Modelo.IModelo;

/**
 *
 * @author josma
 */
public interface IVista {

    //IVista es el suscriber 
    public void update(IModelo consulta);
}

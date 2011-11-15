/*
 * Hexodus >> CasillaOcupada.java
 *
 * Creado el 14 de junio de 2007 a las 02:54
 *
 * Copyright (C) 2006 - 2008 Pablo Torrecilla. GNU General Public License
 */

package juego;

/**
 * Excepci—n que se lanza cuando se intenta colocar una ficha sobre una casilla ocupada
 * @author Pau
 * @version 1.0
 */
public class CasillaOcupada extends java.lang.Exception {
    
    /** Crear una instancia sin mensaje */
    public CasillaOcupada() {
    }
    
    /** Crear una instancia con el mensaje que se le pase como argumento
     * @param msg El mensaje detallado en cuesti—n. */
    public CasillaOcupada(String msg) {
        super(msg);
    }
}

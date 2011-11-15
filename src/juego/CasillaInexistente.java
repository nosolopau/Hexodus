/*
 * Hexodus >> CasillaInexistente.java
 *
 * Creado el 14 de junio de 2007 a las 03:50
 *
 * Copyright (C) 2006 - 2008 Pablo Torrecilla. GNU General Public License
 */

package juego;

/**
 * Excepci—n que se lanza cuando se intenta acceder a una casilla inexistente en el tablero
 * @author Pau
 * @version 1.0
 */
public class CasillaInexistente extends java.lang.Exception {
    
    /** Crear una instancia sin mensaje */
    public CasillaInexistente() {
    } 
    
    /** Crear una instancia con el mensaje que se le pase como argumento
     * @param msg El mensaje detallado en cuesti—n. */
    public CasillaInexistente(String msg) {
        super(msg);
    }
}

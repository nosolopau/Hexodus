/*
 * Hexodus >> DimensionIlegal.java
 *
 * Creado el 14 de junio de 2007 a las 01:29
 *
 * Copyright (C) 2006 - 2008 Pablo Torrecilla. GNU General Public License
 */

package juego;

/**
 * Excepci—n que se lanza cuando se utiliza una dimensi—n ilegal para el tablero.
 * @author Pau
 * @version 1.0
 */
public class DimensionIlegal extends java.lang.Exception {
    
    /** Crear una instancia sin mensaje */
    public DimensionIlegal() {
    }
    
    /** Crear una instancia con el mensaje que se le pase como argumento
     * @param msg El mensaje detallado en cuesti—n. */
    public DimensionIlegal(String msg) {
        super(msg);
    }
}

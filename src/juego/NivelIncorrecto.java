/*
 * Hexodus >> NivelIncorrecto.java
 *
 * Creado el 14 de junio de 2007 a las 10:27
 *
 * Copyright (C) 2006 - 2008 Pablo Torrecilla. GNU General Public License
 */

package juego;

/**
 * Excepci—n que se lanza cuando se intenta proporcionar un nivel incorrecto a la heur’stica
 * desde la interfaz del sistema
 * @author Pau
 * @version 1.0
 */
public class NivelIncorrecto extends java.lang.Exception {
    
    /** Crear una instancia sin mensaje */
    public NivelIncorrecto() {
    }
    
    /** Crear una instancia con el mensaje que se le pase como argumento
     * @param msg El mensaje detallado en cuesti—n. */
    public NivelIncorrecto(String msg) {
        super(msg);
    }
}

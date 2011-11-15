/*
 * Hexodus >> BordeCelda.java
 *
 * Creado el 22 de enero de 2007 a las 13:01
 *
 * Copyright (C) 2006 - 2008 Pablo Torrecilla. GNU General Public License
 */

package heuristica;

/**
 *  Un subtipo de Celda que representa un borde del tablero
 *
 *  @author Pau
 *  @version 1.0
 *  @see Celda
 */
public class Borde extends Celda{
    private char Nombre;    // Nombre del borde
    
    /** Crea una nueva instancia de BordeCelda
     *  @param id Identificador único de la celda (para llamar a super())
     *  @param nombre Nombre del borde*/
    public Borde(int id, char nombre) {
        super(id, nombre);
        Nombre = nombre;
    }
    
    /** Devuelve el nombre del borde
     *  @return Un char que es N, S, E, O según el borde que represente */
    public char ObtenerNombre(){
        return Nombre;
    }
    
    /** Sobreescribe el método equals y lo adapta a Celda */
    public boolean equals(Object o){
        return (o instanceof Borde) && (Nombre == ((Borde)o).ObtenerNombre());
    }
    
    /** ... */
    public String toString(){
        return "(" + Nombre + ")";
    }
}

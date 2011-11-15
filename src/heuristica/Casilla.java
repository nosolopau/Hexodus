/*
 * Hexodus >> CasillaCelda.java
 *
 * Creado el 22 de enero de 2007 a las 13:01
 *
 * Copyright (C) 2006 - 2008 Pablo Torrecilla. GNU General Public License
 */

package heuristica;

/**
 *  Un subtipo de Celda que representa una casilla del tablero con fila y columna
 *
 *  @author Pau
 *  @version 1.0
 *  @see Celda
 */
public class Casilla extends Celda {
    private int Fila;       // Fila de la casilla a la que representa la celda
    private int Columna;    // Columna de la casilla a la que representa la celda
    
    /** Crea una nueva instancia de Casilla
     *  @param f Fila de la casilla
     *  @param c Columna de la casilla
     *  @param id Identificador de la casilla (se necesita para pasarlo a super()) */
    public Casilla(int f, int c, int id) {
        super(id);
        Fila = f;
        Columna = c;
    }
    
    /** Devuelve la fila de la casilla en el tablero
     *  @return Fila de la casilla */
    public int ObtenerFila(){
        return Fila;
    }
    
    /** Devuelve la columna de la casilla en el tablero
     *  @return Columna de la casilla */
    public int ObtenerColumna(){
        return Columna;
    }
    
    /* Adapta y sobreescribe el método equals para casillas 'normales' */
    public boolean equals(Object o){
        return (o instanceof Casilla) && (Fila == ((Casilla)o).ObtenerFila()) &&
            (Columna == ((Casilla)o).ObtenerColumna());
    }
    
    /** ... */
    public String toString(){
        return "(" + Fila + ", " + Columna + ")";
    }
}

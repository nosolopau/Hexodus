/*
 * Hexodus >> Movimiento.java
 *
 * Creado el 24 de junio de 2006 a las 16:11
 *
 * Copyright (C) 2006 - 2008 Pablo Torrecilla. GNU General Public License.
 */

package juego;

/** Representa cada movimiento de los que se compone una partida.
 *  Esta clase sirve para generar un listado con los movimientos registrados.
 *  @author Pau
 */
public class Movimiento {
    private int Numero;
    private int Fila;
    private int Columna;
    private Jugador Autor;
    
    /** Crea un nuevo movimiento
     *  @param n        Numero del movimiento
     *  @param fl       Fila del movimiento
     *  @param cl       Columna del movimiento
     *  @param autor    Autor del movimiento  */
    public Movimiento(int n, int fl, int cl, Jugador autor) {
        Numero = n;
        Fila = fl;
        Columna = cl;
        Autor = autor;
    }
    
    /** Devuelve la fila del movimiento
     *  @return Fila del movimiento */
    public int ObtenerFila(){
        return Fila;
    }
    
    /** Devuelve la columna del movimiento
     *  @return Columna del movimiento */
    public int ObtenerColumna(){
        return Columna;
    }
}

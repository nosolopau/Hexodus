/*
 * Hexodus >> Jugador.java
 *
 * Creado el 21 de junio de 2006 a las 18:50
 *
 * Copyright (C) 2006 - 2008 Pablo Torrecilla. GNU General Public License
 */

package juego;

/** Representa a un jugador de la partida
 *  @author Pau
 *  @version 1.0
 */
public class Jugador {
    private String Nombre;
    private int Color;
    private int Tipo;
    private int Posicion;

    /** Crea un nuevo jugador
     *  @param nombre   Nombre del jugador
     *  @param tipo     Tipo del jugador (ordenador o humano)
     *  @param posicion Posici—n que ocupa en el tablero: horizontal o vertical */
    public Jugador(String nombre, int tipo, int posicion){
        Nombre = nombre;

        Tipo = tipo;
        Posicion = posicion;
        if(Posicion == 1) Color = 1;
        else Color = 2;
    }
    
    /** Crea un nuevo jugador
     *  @param tipo     Tipo del jugador (ordenador o humano)
     *  @param posicion Posici—n que ocupa en el tablero: horizontal o vertical */
    public Jugador(int tipo, int posicion){
        Tipo = tipo;
        Posicion = posicion;
        if(Posicion == 1){
            Nombre = "jugador vertical";
            Color = 1;
        }
        else{
            Nombre = "jugador horizontal";
            Color = 2;
        }
    }
    
    /** Devuelve el nombre del jugador
     *  @return Una cadena con el nombre del jugador */
    public String ObtenerNombre (){
	return Nombre;
    }
    
    /** Devuelve el color del jugador
     *  @return Un entero que representa el color del jugador */
    public int ObtenerColor(){
        return Color;
    }
    
    /** Devuelve el tipo del jugador
     *  @return Un entero que representa el tipo del jugador */
    public int ObtenerTipo(){
        return Tipo;
    }
    
    /** Devuelve si el jugador es de tipo ordenador
     *  @return Verdadero si el jugador est‡ manejado por el sistema */
    public boolean EsOrdenador(){
        return Tipo == 1;
    }
    
    /** Devuelve la posici—n del jugador
     *  @return Un entero que representa la posici—n del jugador */
    public int ObtenerPosicion(){
        return Posicion;
    }
}
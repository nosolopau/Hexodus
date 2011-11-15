/*
 * Hexodus >> Celda.java
 *
 * Creado el 25 de noviembre de 2006 a las 22:51
 *
 * Copyright (C) 2006 - 2008 Pablo Torrecilla. GNU General Public License
 */

package heuristica;
import java.util.*;

/**
 *  Representa una celda del grafo del tablero, sea un Borde o una Casilla
 *
 *  @version 0.1
 *  @author Pau
 *  @see Casilla
 *  @see Borde
 */
public class Celda {
    private int Identificador;      // Identificador único para cada Celda
    private int Color;              // Provisional: después debería ser una referencia al jugador
    private double ResistenciaN;    // Valor de la celda para el jugador negro según la heurística
    private double ResistenciaB;    // Valor de la celda para el jugador blanco según la heurística
    private ArrayList<Celda> Vecinas;      // Lista de celdas vecinas de la actual
    private static double INFT = 10E+10;
    private static double CERO = 10E-10;
    
    /** Crea una nueva instancia de Celda
     *  @param id Identificador único de la celda */
    public Celda(int id) {
        Identificador = id;
        Color = -1;
        ResistenciaN = ResistenciaB = 1;
        Vecinas = new ArrayList<Celda>();
    }
    
    /** Crea una nueva instancia de Celda
     *  @param id Identificador único de la celda
     *  @param nombre Nombre de la celda (si se trata de un borde). Se utiliza
     *  para seleccionar el color cuando el constructor de BordeCelda invoca a super() */
    public Celda(int id, char nombre) {
        Identificador = id;
        switch(nombre){
            case 'N':
            case 'S':
                Color = 1;
                ResistenciaN = CERO;
                ResistenciaB = INFT;
                break;
            case 'E':
            case 'O':
                Color = 0;
                ResistenciaN = INFT;
                ResistenciaB = CERO;
        }
        Vecinas = new ArrayList();
    }
    /** Crea una nueva instancia de Celda
     *  @param id Identificador único de la celda
     *  @param color Color del jugador que ocupa la celda o -1 si está libre */
    public Celda(int id, int color) {
        Identificador = id;
        Color = color;
        Vecinas = new ArrayList();    
        switch(color){
            case 0:
                ResistenciaB = CERO;
                ResistenciaN = INFT;
                break;
            case 1:
                ResistenciaB = INFT;
                ResistenciaN = CERO;
                break;
            default:
                ResistenciaN = ResistenciaB = 1;
        }
    }
    
    /** Ocupa una casilla por un determinado jugador o por ninguno
     *  @param color Color del jugador que ocupa la celda o -1 si se quiere dejar libre */
    public void Ocupar(int color){
        Color = color;        
        switch(color){
            case 0:
                ResistenciaB = CERO;
                ResistenciaN = INFT;
                break;
            case 1:
                ResistenciaB = INFT;
                ResistenciaN = CERO;
                break;
            default:
                ResistenciaN = ResistenciaB = 1;
        }
    }
    
    /** Revuelve el identificador de la Celda
     *  @return Identificador único */
    public int ObtenerId(){
        return this.Identificador;
    }
    
    /** Devuelve el valor de la resistencia de la celda para un jugador determinado
     *  @param color Color del jugador cuya resistencia para esa casilla se quiera conocer
     *  @return Valor de la resistencia */
    public double ObtenerResistencia(int color){
        if(color == 1) return ResistenciaN;
        else return ResistenciaB;
    }

    /** Establece el valor de la resistencia de la celda para un jugador determinado
     *  @param resistencia Valor de la resistencia que se va a asignar a la celda
     *  @param color Color del jugador a cuya resistencia se va a asignar el valor contenido
     *  en el parámetro resistencia */
    public void EstablecerResistencia(int resistencia, int color){
        if(color == 1)ResistenciaB = resistencia;
        else ResistenciaN = resistencia;
    }
    
    /** Agrega una vecina a la lista de vecinas de la celda
     *  @param celda Vecina que se añadirá */
    public void AgregarVecina(Celda celda){
        if((!Vecinas.contains(celda)) && (celda != this))
            Vecinas.add(celda);
    }
    /** Elimina una vecina de la lista de vecinas de la celda
     *  @param celda Vecina que se borrará */    
    public void EliminarVecina(Celda celda){
        if(Vecinas.contains(celda))
            Vecinas.remove(celda);
    }
    
    public boolean EsVecina(Celda celda){
        return (Vecinas.contains(celda));
    }
    
    /** Devuelve la lista de vecinas de la celda actual
     *  @return Un ArrayList con las vecinas de la celda */
    public ArrayList ObtenerListaVecinas(){
        return Vecinas;
    }
    
    /** Devuelve el color (provisional) */
    public int ObtenerColor(){
        return Color;
    }
    
    /** Devuelve si la celda está o no vacía (provisional) */
    public boolean esVacia(){
        if(this instanceof Borde) return true;
        return (Color == -1);
    }
}

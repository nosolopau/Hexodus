/*
 * Hexodus >> Conexiones.java
 *
 * Creado el 25 de noviembre de 2006 a las 16:33
 *
 * Copyright (C) 2006 - 2008 Pablo Torrecilla. GNU General Public License
 */

package heuristica;
import java.util.*;

/** 
 *  Representa un conjunto de caminos entre dos celdas. Asigna un objeto Ruta a
 *  cada par de celdas.
 *
 *  Funciona como un contenedor de objetos Ruta
 *
 *  @author Pau
 *  @version 1.0
 */
public class Conexiones implements Cloneable{
    private Ruta [][] Mapa;
    private int Dimension;
    
    /** Crea una nueva instancia de una lista de conexiones */
    public Conexiones(int dimension){
        Dimension = dimension*dimension+4;
        Mapa = new Ruta[Dimension][Dimension];
    }
    
    public Conexiones(int dimension, Ruta[][] mapa){
        Mapa = mapa;
        Dimension = dimension;
    }
    
    /** Crea una ruta vacía entre dos celdas.
     *  @param o Celda de origen
     *  @param d Celda de destino
     *  @see #NuevaConexion(Celda o, Celda d, Ruta r) */
    public void NuevaConexion(Celda o, Celda d){
        Ruta r = new Ruta();
        
        NuevaConexion(o, d, r);
    }
    
    /** Crea una conexión entre dos celdas y le asigna una ruta existente
     *  @param o Celda de origen
     *  @param d Celda de destino
     *  @param r Ruta que se establecerá entre las celdas
     *  @see #NuevaConexion(Celda o, Celda d) */
    public void NuevaConexion(Celda o, Celda d, Ruta r){
        Mapa[o.ObtenerId()][d.ObtenerId()] = r;
    }
    
    /** Devuelve el objeto ruta entre dos celdas que se le pasen como parámetro.
     *
     *  @param o Celda de origen
     *  @param d Celda de destino
     *  @return La ruta entre las celdas o null si no existe conexión directa o inversa */
    public Ruta ObtenerRuta(Celda o, Celda d){
        if(HayConexion(o, d))
            return (Mapa[o.ObtenerId()][d.ObtenerId()]);
        else if(HayConexion(d, o))
            return (Mapa[d.ObtenerId()][o.ObtenerId()]);
        return null;
    }
    
    /** Inserta un camino directo [] entre dos celdas
     *
     *  @param o Celda de origen
     *  @param d Celda de destino
     *  @return Referencia al camino que se ha insertado */
    public Camino InsertarCaminoDirecto(Celda o, Celda d){
        if(!HayConexion(o, d))
            NuevaConexion(o, d);
        Camino c = new Camino();
        c.HacerDirecto();
        
        (Mapa[o.ObtenerId()][d.ObtenerId()]).Agregar(c);
        return c;
    }
    
    /** Devuelve si existe una conexión entre las celdas indicadas teniendo
     *  en cuenta el orden en que se pasan al método 
     *
     *  @param o Celda de origen
     *  @param d Celda de destino
     *  @return Verdadero si hay conexión o falso en caso contrario */
    public boolean HayConexion(Celda o, Celda d){
        if((Mapa[o.ObtenerId()][d.ObtenerId()]) != null) return true; 
        else return false;
    }

    /** Elimina la conexión que exista entre las celdas que se pasan 
     *
     *  @param o Celda de origen
     *  @param d Celda de destino */
    public void EliminarConexion(Celda o, Celda d){
        if(HayConexion(o, d))
            Mapa[o.ObtenerId()][d.ObtenerId()] = null;
        else if(HayConexion(d, o))
            Mapa[d.ObtenerId()][o.ObtenerId()] = null;
        
        return;
    }
    
    public Conexiones clone(){
        Ruta [][] r = new Ruta[Dimension][Dimension];
        for(int i = 0; i < Dimension; i++)
            for(int j = 0; j < Dimension; j++)
                if(Mapa[i][j] != null)
                    r[i][j] = (Ruta) (Mapa[i][j]).clone();
                
        return new Conexiones(Dimension, r);
    }
    
    /** Devuelve si existe una conexión entre las celdas indicadas sin tener
     *  en cuenta el orden en que se pasan al método
     *
     *  @param o Celda de origen
     *  @param d Celda de destino
     *  @return Verdadero si hay conexión y falso en caso contrario */
    public boolean HayConexionEx(Celda o, Celda d){
        return (HayConexion(o, d) || HayConexion(d, o));
    }
}
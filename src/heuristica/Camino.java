/*
 * Hexodus >> Camino.java
 *
 * Creado el 3 de enero de 2007 a las 12:51
 *
 * Copyright (C) 2006 - 2008 Pablo Torrecilla. GNU General Public License
 */

package heuristica;

import java.util.*;

/**
 *  Representa un camino compuesto de celdas
 *  @author Pau
 *  @version 1.0
 */
public class Camino {
    private ArrayList <Celda> lista;    // Un ArrayList de Celdas del camino
    private boolean Directo;    // Indica si el camino es de tipo directo []
    private boolean Nuevo;      // Indica si el camino es nuevo o no
   
    /** Crea un nuevo Camino */
    public Camino() {
        lista = new ArrayList();
        Directo = false;    // En principio se define como camino NO directo
        Nuevo = true;       // Se marca como nuevo
    }

    /** Agrega una celda al camino siempre que no exista 
     *  @param c Celda que se a–ade al camino */
    public void Agregar(Celda c){
        if(!lista.contains(c))
            lista.add(c);
    }
    
    /** Devuelve el valor actual de la propiedad Nuevo
     *  @return Verdadero si el camino es nuevo y falso en caso contrario*/
    public boolean EsNuevo(){
        return Nuevo;
    }
    
    /** Establece un nuevo valor para el atributo nuevo
     *  @param nuevo El nuevo valor para el atributo nuevo */
    public void CambiarNuevo(boolean nuevo){
        Nuevo = nuevo;
    }
    
    /** Devuelve la longitud del camino bas‡ndose en el nœmero de celdas
     *  @return Longitud del camino (nœmero de celdas que contiene) */
    public int ObtenerLongitud(){
        return lista.size();
    }
    
    /** Devuelve un valor para establecer si un camino contiene o no una celda
     *  @param a Celda objetivo
     *  @return Verdadero si la celda a est‡ en el camino y falso en caso contrario */
    public boolean Contiene(Celda a){
        return lista.contains(a);
    }
    
    /** Devuelve verdadero si la intersecci—n del camino actual con el dado
     *  es vac’a
     *  @param b Camino con el que comparar el actual
     *  @return Verdadero si la intersecci—n de ambos es nula y falso en caso contrario */
    public boolean InterseccionVacia(Camino b){
        Iterator i1 = lista.iterator();
        
        while(i1.hasNext())
            if(b.Contiene((Celda) i1.next())) return false;        
        return true;
    }
    
    /** Ejecuta la operaci—n intersecci—n entre dos caminos
     *
     *  @param b Camino con el que calcular la intersecci—n del actual
     *  @return Un camino resultado de la intersecci—n entre el actual y el 
     *  recibido por argumentos. */
    public Camino Interseccion(Camino b){
        Camino nuevo = new Camino();
        Celda casilla = null;
        
        Iterator <Celda> i1 = lista.iterator();
        while(i1.hasNext()){
            casilla = i1.next();
            if(b.Contiene(casilla)) nuevo.Agregar(casilla);
        } 
        return nuevo;
    }

    /** Ejecuta la operaci—n de uni—n entre dos caminos
     *
     *  @param b Camino con el que calcular la uni—n del actual
     *  @return Un camino resultado de la uni—n del actual y el recibido por
     *  argumentos. */
    public Camino Union(Camino b){
        Celda casilla = null;
        Camino nuevo = new Camino();
        
        if (b.EsDirecto() && EsDirecto()){
            nuevo.HacerDirecto();
            return nuevo;
        }
        
        Iterator i1 = lista.iterator();
        while(i1.hasNext()){
            nuevo.Agregar((Celda) i1.next());
        }
        Iterator i2 = b.ObtenerIterador();
        while(i2.hasNext()){
            casilla = (Celda) i2.next();
            if(!nuevo.Contiene(casilla)) nuevo.Agregar(casilla);
        }
        return nuevo;
    }
    
    /** Ejecuta la operaci—n de uni—n entre dos caminos
     *
     *  @param b Camino con el que calcular la uni—n del actual
     *  @param c Celda que ha de ser intercalada en la uni—n
     *  @return Un camino resultado de la uni—n del actual y el recibido por
     *  argumentos, intercalando en la uni—n la celda c */
    public Camino Union(Camino b, Celda c){
        Camino nuevo = new Camino();
        
        Iterator <Celda> i1 = lista.iterator();
        while(i1.hasNext()){
            nuevo.Agregar(i1.next());
        }
        nuevo.Agregar(c);
        
        Iterator <Celda> i2 = b.ObtenerIterador();
        Celda casilla = null;
        while(i2.hasNext()){
            casilla = i2.next();
            if(!nuevo.Contiene(casilla)) nuevo.Agregar(casilla);
        }
        
        return nuevo;
    }
    
    /** Devuelve si el camino actual est‡ vac’o. Si el camino es directo se
     *  considera que est‡ vac’o
     *  @return Verdadero si el camino est‡ vac’o o es directo. Falso en otro caso. */
    public boolean Vacio(){        
        return lista.isEmpty();
    }
    
    /** Devuelve un iterador del camino
     *  @return Un objeto Iterator sobre la lista de Celdas
     */
    public Iterator ObtenerIterador(){
        return lista.iterator();
    }
    
    /** Marca el atributo directo como verdadero */
    public void HacerDirecto(){
        lista.clear();
        Directo = true;
    }
    
    /** Devuelve si el camino es directo o no
     *  @return Verdadero si el camino es directo, falso en caso contrario */
    public boolean EsDirecto(){
        return Directo;
    }
    
    /** Devuelve verdadero si el camino actual y el que se recibe son iguales.
     *  Se considera que un camino es igual  a otro si contiene sus mismas
     *  celdas sin importar el orden. Si los caminos son directos, se consideran iguales.
     *
     *  @param o Un camino objetivo con el que comparar el actual
     *  @return Verdadero si los caminos son iguales y falso en caso contrario */
    public boolean Igual(Camino o){
        if(this.Directo && o.Directo) return true;
        if(o.ObtenerLongitud() != this.ObtenerLongitud()) return false;
        
        Iterator <Celda> i1 = lista.iterator();
        while(i1.hasNext()){
            Celda c1 = i1.next();
            if(!o.Contiene(c1)) return false;
        }
        return true;
    }
    
    /** ... */
    public String toString(){
        if(lista.isEmpty()) return "[]";
        Iterator e = lista.iterator();
        String s = new String();
        s = "[";
        while(e.hasNext())
            s = s + e.next();
        s = s + "]";
        return s;
    }
}
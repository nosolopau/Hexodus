/*
 * Hexodus >> Ruta.java
 *
 * Creado el 17 de enero de 2007 a las 9:54
 *
 * Copyright (C) 2006 - 2008 Pablo Torrecilla. GNU General Public License
 */

package heuristica;
import java.util.*;

/** Representa un camino entre dos celdas
 *
 *  @author Pau
 *  @version 1.0
 */
class Ruta implements Cloneable{
    private ArrayList <Camino> Caminos;  // Lista de caminos que componen la ruta
    private Camino Minimo;      // Mantiene un puntero al camino m’nimo
    private static int CaminosMaximos = 20; // M‡ximo nœmero de caminos permitidos por ruta
    
    /** Crea una nueva instancia de Ruta */
    public Ruta(){
        Caminos = new ArrayList();
        Minimo = null;
    }
    
    /** Sobreescribe el mŽtodo clone y lo adapta a Ruta */
    public Object clone(){
        Ruta o = null;
        try{
           o = (Ruta) super.clone(); 
        }
        catch(CloneNotSupportedException e){
            System.err.println("No clonable");
        }
        o.Caminos = (ArrayList) o.Caminos.clone();
        return o;
    }
    
    /** Elimina el camino dado de la ruta actual
     *  @param cam Camino que se desea eliminar de la ruta */
    public void Eliminar(Camino cam){
        Caminos.remove(cam);
    }
    
    /** Agrega el camino referido a la ruta actual
     *  @param cam Camino que se desea agregar a la ruta
     *  @return Devuelve verdadero si se agreg— el camino y falso en caso contrario */
    public boolean Agregar(Camino cam){
        Iterator i = Caminos.iterator();
        Camino c = null;
        
        /* Si la ruta ha alcanzado el umbral de caminos o tiene un camino directo,
         * se anula la inserci—n y se devuelve falso */
        if((this.ObtenerLongitud() >= CaminosMaximos) || (HayDirecto())) return false;
        
        // Si ya hay un camino igual en la ruta, se anula la inserci—n
        while(i.hasNext()){
            c = (Camino)i.next();
            if(cam.Igual(c)) return false;
        }

        Caminos.add(cam);
        
        // Actualiza el camino m’nimo si es necesario
        if(Minimo == null) Minimo = cam;
        else if(Minimo.ObtenerLongitud() > cam.ObtenerLongitud()) Minimo = cam;
        
        return true;
    }
    
    /** Devuelve verdadero si hay un camino directo en la ruta
     *  @return Verdadero si la ruta tiene un camino directo */
    public boolean HayDirecto(){
        Iterator d = Caminos.iterator();
        
        while(d.hasNext())
            if(((Camino)d.next()).EsDirecto()) return true;
        return false;
    }
    
    /** Devuelve el camino m’nimo de la ruta
     *  @return Una referencia al camino m’nimo o null si no hay caminos en la ruta */
    public Camino ObtenerCaminoMinimo(){
        return Minimo;
    }
    
    /** Devuelve una ruta copia de la actual borrando de ella el camino que se
     *  pase como par‡metro
     *  @param c    El camino que quiere eliminarse en la copia de la ruta
     *  @return     La ruta resultado clonada de la actual*/
    public Ruta ClonarSinCamino(Camino c){
        Ruta nueva = new Ruta();
        Iterator il = Caminos.iterator();
        
        while(il.hasNext())
           nueva.Agregar((Camino) il.next());
        nueva.Eliminar(c);
        
        return nueva;
    }
    
    /** Devuelve la longitud de la ruta
     *  @return El nœmero de caminos de la ruta */
    public int ObtenerLongitud(){
        return(Caminos.size());
    }
    
    /** Devuelve si la ruta est‡ vac’a
      * @return Verdadero si la ruta est‡ vac’a y falso en caso contrario */
    public boolean EsVacia(){
        return Caminos.isEmpty();
    }
    
    /** Devuelve un iterador de los caminos
     *  @return Objeto Iterator sobre Caminos */
    public Iterator ObtenerIterador(){
        return Caminos.iterator();
    }
    
    /** ... */
    public String toString(){
        return "" + Caminos + "";
    }
}
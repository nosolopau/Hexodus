/*
 * Hexodus >> Factoria.java
 *
 * Creado el 21 de mayo de 2007 a las 11:24
 *
 * Copyright (C) 2006 - 2008 Pablo Torrecilla. GNU General Public License
 */

package heuristica;

/**
 * Factor’a de Heur’sticas. Si hay disponible un sistema de varios procesadores
 * los utiliza para crear un objeto de tipo Multihilo espec’fico.
 *
 * @author Pau
 * @version 1.0
 */
public class Factoria {
    
    /** Crea la instacia de factor’a Factoria */
    public Factoria() {
    }
    
    /** Bas‡ndose en el nœmero de procesadores disponibles de la m‡quina
     *  devuelve una referencia a una heur’stica monohilo o multihilo */
    public Heuristica NuevaHeuristica(int dim, int nivel, boolean swap) {
        if(Runtime.getRuntime().availableProcessors() < 2){
            return new Monohilo(dim, nivel, swap);
        }
        else
            return new Multihilo(dim, nivel, swap);
    }
}

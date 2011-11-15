/*
 * Tablero.java
 *
 * Creado el 23 de abril de 2007 a las 16:01
 *
 * Copyright (C) 2006 - 2008 Pablo Torrecilla. GNU General Public License
 */

package heuristica;
import java.util.*;

/**
 * Representa el tablero virtual sobre el que se apoya la heur’stica, cuyas
 * caracter’stica pueden variar para representar la l—gica interna del juego.
 *
 * @author Pau
 * @version 1.0
 */

public class Tablero {
    private int Dimension;
    private Casilla c[][];     // Una matriz de celdas
    private Conexiones T;   // Lista de conexiones virtuales del tablero
    private Borde n, s, e, o;
    private ArrayList CaducarOriginal;  // Caminos que caducan en la primera iteracion
    Celda [] Todas;
    
    /** Crea una nueva instancia de Tablero */
    public Tablero(int dim) {
        CaducarOriginal = new ArrayList();
        Dimension = dim;
        
        Todas = new Celda [Dimension * Dimension];
        
        c = new Casilla [Dimension][Dimension];   // Matriz que representa el tablero
        T = new Conexiones(dim);  // Lista con las conexiones virtuales del tablero
        
        // Crea las casillas del tablero:
        for(int i = 0; i < Dimension; i++){
            for(int j = 0; j < Dimension; j++){
                c[i][j] = new Casilla(i, j, i * Dimension + j);
                Todas[i * Dimension + j] = c[i][j];
            }
        }
        
        // Crea los cuatro bordes de celda
        n = new Borde(dim*dim+0, 'N');
        s = new Borde(dim*dim+1, 'S');
        e = new Borde(dim*dim+2, 'E');
        o = new Borde(dim*dim+3, 'O');
        
        /* Inicializa la lista T.
         * Los datos de inicializaci—n son siempre los mismos, por lo que en algunos casos
         * concretos se utilizan tablas est‡ticas agrupadas por dimensi—n. Si no
         * encuentra la tabla de la dimensi—n en cuesti—n, el sistema ejecuta el 
         * algoritmo para iniciar la lista: 
         * Toma una casilla y crea una CV para cada vecina que encuentra siempre
         * que cumpla que sean vecinas y que no est‡n ya conectadas en el mismo 
         * sentido o en el inverso. Enlaza las casillas cargando las listas de
         * vecinas para cada celda, bas‡ndose en su posici—n en el tablero. */
        if(Dimension == 3){
            boolean [][] Mapa = {{false, true, false, true, true, false, false, false, false}, 
                {false, false, true, false, true, true, false, false, false}, 
                {false, false, false, false, false, true, false, false, false}, 
                {false, false, false, false, true, false, true, true, false}, 
                {false, false, false, false, false, true, false, true, true}, 
                {false, false, false, false, false, false, false, false, true}, 
                {false, false, false, false, false, false, false, true, false}, 
                {false, false, false, false, false, false, false, false, true}, 
                {false, false, false, false, false, false, false, false, false}};
            Celda ga = null;
            Celda gb = null;
            for(int a = 0; a < Todas.length; a++){
                for(int b = a; b < Todas.length; b++){
                    if(Mapa[a][b] == true){
                        ga = Todas[a];
                        gb = Todas[b];
                        T.NuevaConexion(ga, gb);
                        ga.AgregarVecina(gb);
                        gb.AgregarVecina(ga);
                        CaducarOriginal.add(T.InsertarCaminoDirecto(ga, gb));
                    }
                }
            }
        }
        else if(Dimension == 5){
            boolean [][] Mapa = {{false, true, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, true, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, true, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, true, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, true, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, true, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, true, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, true, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, true, true, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, true, true, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, true, true, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, true, true, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, true, true, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, true, true, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, true, true, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, true, true}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}};
            Celda ga = null;
            Celda gb = null;    
            for(int a = 0; a < Todas.length; a++){
                    for(int b = a; b < Todas.length; b++){
                        if(Mapa[a][b] == true){
                            ga = Todas[a];
                            gb = Todas[b];
                            T.NuevaConexion(ga, gb);
                            ga.AgregarVecina(gb);
                            gb.AgregarVecina(ga);
                            CaducarOriginal.add(T.InsertarCaminoDirecto(ga, gb));
                        }
                    }
                }
            }
        else if(Dimension == 7){
            boolean [][] Mapa = {{false, true, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, true, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, true, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, true, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, true, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, true, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, true, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, true, true, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, true, true, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, true, true, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, true, true, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, true, true, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, true, true, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, true, true}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true}, 
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}};
            Celda ga = null;
            Celda gb = null;    
            for(int a = 0; a < Todas.length; a++){
                for(int b = a; b < Todas.length; b++){
                    if(Mapa[a][b] == true){
                        ga = Todas[a];
                        gb = Todas[b];
                        T.NuevaConexion(ga, gb);
                        ga.AgregarVecina(gb);
                        gb.AgregarVecina(ga);
                        CaducarOriginal.add(T.InsertarCaminoDirecto(ga, gb));
                    }
                }
            }
        }
        else{
            boolean [][] Mapa2 = new boolean[Dimension*Dimension][Dimension*Dimension];          
                    
            Celda ga = null;
            Celda gb = null;
            for(int a = 0; a < Todas.length; a++){
                for(int b = 0; b < Todas.length; b++){
                    try{
                        ga = Todas[a];
                        gb = Todas[b];
                        if((a != b) && (SonVecinas(ga, gb)) 
                                && (!T.HayConexion(ga, gb))
                                && (!T.HayConexion(gb, ga))){
                            T.NuevaConexion(ga, gb);
                            ga.AgregarVecina(gb);
                            gb.AgregarVecina(ga);
                            // Mapa2[a][b] = true;
                            CaducarOriginal.add(T.InsertarCaminoDirecto(ga, gb));
                        }
                    }
                    catch (CasillaInexistente ex) {
                        ex.printStackTrace();
                    }                
                }
            }
            
/*          Mostrar los datos de conexiones listos para tabularse
 *          Esta secci—n no est‡ documentada y sirve para a–adir tablas est‡ticas
 *          para dimensiones mayores y mejorar la eficiencia del sistema. Para que
 *          funcione hay que descomentar Mapa2[a][b] = true en la secci—n anterior.
 *
            System.out.println("---");
            for(int a = 0; a < Todas.length; a++){
                System.out.print("{");
                for(int b = 0; b < Todas.length; b++){
                    if(b == Todas.length - 1)
                        System.out.print(Mapa2[a][b]);
                    else System.out.print(Mapa2[a][b] + ", ");
                }
                if(a == Todas.length - 1) System.out.println("}" );
                else System.out.println("}, " );
            }
 */
        }
                    
               
        /* Inserta caminos y establece vecinas entre las casillas y los bordes
         * en contacto: */
        for(int i = 0; i < dim; i++){
            T.InsertarCaminoDirecto(n, c[0][i]);
            T.InsertarCaminoDirecto(s, c[dim-1][i]);
            c[0][i].AgregarVecina(n);
            n.AgregarVecina(c[0][i]);
            s.AgregarVecina(c[dim-1][i]);
            c[dim-1][i].AgregarVecina(s);

            T.InsertarCaminoDirecto(o, c[i][0]);
            T.InsertarCaminoDirecto(e, c[i][dim-1]);
            c[i][0].AgregarVecina(o);
            o.AgregarVecina(c[i][0]);
            e.AgregarVecina(c[i][dim-1]);
            c[i][dim-1].AgregarVecina(e);
        }
    }
    
    /** Devuelve la lista de caminos que van a caducar en una primera iteraci—n
     *  @return Lista de caminos que deben caducar en la primera iteraci—n */
    public ArrayList ObtenerListaCaducar(){
        return CaducarOriginal;
    }
    
    /** Devuelve el conjunto de conexiones b‡sicas T
     *  @return Conjunto T de conexiones b‡sicas del tablero */
    public Conexiones ObtenerT(){
        return T;
    }
    
    /** Devuelve la dimensi—n del tablero de la heur’stica
     *  @return Dimensi—n del tablero */
    public int ObtenerDimension(){
        return Dimension;
    }
    
    /** Devuelve una lista con las casillas libres del tablero
     *  @return Lista de casillas libres */
    public ArrayList<Casilla> ObtenerCeldasLibres(){
        ArrayList<Casilla> lis = new ArrayList<Casilla>();
        
        for(int i = 0; i < Dimension; i++)
            for(int j = 0; j < Dimension; j++)
                if(c[i][j].esVacia()) lis.add((Casilla)c[i][j]);
        
        return lis;
    }
    
    /** Devuelve la lista G asociada al tablero, conteniendo las casillas del color
     *  que se pase por argumento o que estŽn libres
     *  @param color Identificador del jugador del que se desea obtener G
     *  @return Conjunto G como ArrayList */ 
    public ArrayList<Celda> GenerarG(int color){
        ArrayList<Celda> G = new ArrayList<Celda>();
        if(color == 1){
            for(int i = 0; i < Dimension; i++){
                for(int j = 0; j < Dimension; j++){
                    if((c[i][j].ObtenerColor() == -1) || (c[i][j].ObtenerColor() == 1)) G.add(c[i][j]);
                }
            }
            G.add(n);
            G.add(s);
        }
        else{
            for(int i = 0; i < Dimension; i++){
                for(int j = 0; j < Dimension; j++){
                    if((c[j][i].ObtenerColor() == -1) || (c[j][i].ObtenerColor() == 0)) G.add(c[j][i]);
                }
            }
            G.add(e);
            G.add(o);
        }

        return G;
    }
    
    /** Devuelve la casilla situada en la fila y columna que se pasen como argumento
     *  @param fila Fila de la casilla
     *  @param columna Columna de la casilla
     *  @return Una referencia a la casilla indicada por esa fila y esa columna */
    public Casilla Obtener(int fila, int columna){
        return c[fila][columna];
    }
    
    /** Determina si dos celdas son vecinas atendiendo a la posici—n en el tablero
     *  @param a Primera casilla
     *  @param b Segunda casilla
     *  @return Verdadero si son vecinas, falso en caso contrario
     *  @throws CasillaInexistente  Si la casilla objetivo esta fuera del rango del tablero */   
    private boolean SonVecinas(Celda a, Celda b) throws CasillaInexistente{
        if((a instanceof Casilla) && (b instanceof Casilla))
            return SonVecinas(((Casilla)a).ObtenerFila(), ((Casilla)a).ObtenerColumna(), ((Casilla)b).ObtenerFila(), ((Casilla)b).ObtenerColumna());
        return false;
    }
    
    /** Determina si dos celdas cuyas coordenadas se pase son vecinas atendiendo
     *  a la posici—n en el tablero.
     *  @param f1 Fila de la primera casilla
     *  @param c1 Columna de la primera casilla
     *  @param f2 Fila de la segunda casilla
     *  @param c2 Columna de la segunda casilla
     *  @return Verdadero si son vecinas, falso en caso contrario
     *  @throws CasillaInexistente  Si la casilla objetivo esta fuera del rango del tablero */   
    private boolean SonVecinas(int f1, int c1, int f2, int c2) throws CasillaInexistente{
        if((f1 >= Dimension) || (c1 >= Dimension) || (c1 < 0) || (f1 < 0) || (f2 >= Dimension) || (c2 >= Dimension) || (c2 < 0) || (f2 < 0))
            throw new CasillaInexistente();

        Celda [] lista;
        Celda b = c[f2][c2];
        
        if((c1 == 0) && (f1 != 0) && (f1 != Dimension-1)){
            lista = new Celda[4];
            lista[0]=c[f1][c1+1];
            lista[1]=c[f1+1][c1+1];
            lista[2]=c[f1-1][c1];
            lista[3]=c[f1+1][c1];
        }
        else if((c1 == Dimension-1) && (f1 != 0) && (f1 != Dimension-1)){
            lista = new Celda[4];
            lista[0]=c[f1][c1-1];
            lista[1]=c[f1-1][c1-1];
            lista[2]=c[f1-1][c1];
            lista[3]=c[f1+1][c1];
        }
        else if((f1 == 0) && (c1 != 0) && (c1 != Dimension-1)){
            lista = new Celda[4];
            lista[0]=c[f1+1][c1];
            lista[1]=c[f1+1][c1+1];
            lista[2]=c[f1][c1+1];
            lista[3]=c[f1][c1-1];
        }
        else if((f1 == Dimension-1) && (c1 != 0) && (c1 != Dimension-1)){
            lista = new Celda[4];
            lista[0]=c[f1][c1-1];
            lista[1]=c[f1][c1+1];
            lista[2]=c[f1-1][c1-1];
            lista[3]=c[f1-1][c1];
        }
        else if((c1 == 0) && (f1 == 0)){
            lista = new Celda[3];
            lista[0]=c[f1][c1+1];
            lista[1]=c[f1+1][c1];
            lista[2]=c[f1+1][c1+1];
        }
        else if((c1 == 0) && (f1 == Dimension-1)){
            lista = new Celda[2];
            lista[0]=c[f1][c1+1];
            lista[1]=c[f1-1][c1];
        }
        else if((c1 == Dimension-1) && (f1 == Dimension-1)){
            lista = new Celda[3];
            lista[0]=c[f1][c1-1];
            lista[1]=c[f1-1][c1];
            lista[2]=c[f1-1][c1-1];
        }
        else if((c1 == Dimension-1) && (f1 == 0)){
            lista = new Celda[2];
            lista[0]=c[f1][c1-1];
            lista[1]=c[f1+1][c1];				
        }
        else{
            lista = new Celda[6];
            lista[0]=c[f1-1][c1-1];
            lista[1]=c[f1-1][c1];
            lista[2]=c[f1][c1-1];
            lista[3]=c[f1][c1+1];
            lista[4]=c[f1+1][c1];
            lista[5]=c[f1+1][c1+1];
        }
        for(int i = 0; i < lista.length; i++)
            if(lista[i] == b) return true;
        
        return false;
    }
}

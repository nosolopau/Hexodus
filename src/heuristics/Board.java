/*
 * Board.java
 *
 * Creado el 23 de abril de 2007 a las 16:01
 *
 * Copyright (C) 2006 - 2008 Pablo Torrecilla. GNU General Public License
 */

package heuristics;
import java.util.*;

/**
 * Representa el tablero virtual sobre el que se apoya la heurstica, cuyas
 * caracterstica pueden variar para representar la lgica interna del juego.
 *
 * @author Pau
 * @version 1.0
 */

public class Board {
    private int dimension;
    private Square squares[][];     // Una matriz de celdas
    private Connections connections;   // Lista de conexiones virtuales del tablero
    private Border north, south, east, west;
    private ArrayList expireOriginal;  // Caminos que caducan en la primera iteracion
    Cell [] all;
    
    /** Crea una nueva instancia de Board */
    public Board(int dim) {
        expireOriginal = new ArrayList();
        dimension = dim;
        
        all = new Cell [dimension * dimension];
        
        squares = new Square [dimension][dimension];   // Matriz que representa el tablero
        connections = new Connections(dim);  // Lista con las conexiones virtuales del tablero

        // Crea las casillas del tablero:
        for(int i = 0; i < dimension; i++){
            for(int j = 0; j < dimension; j++){
                squares[i][j] = new Square(i, j, i * dimension + j);
                all[i * dimension + j] = squares[i][j];
            }
        }

        // Crea los cuatro bordes de celda
        north = new Border(dim*dim+0, 'N');
        south = new Border(dim*dim+1, 'S');
        east = new Border(dim*dim+2, 'E');
        west = new Border(dim*dim+3, 'W');
        
        /* Inicializa la list connections.
         * Los datos de inicializacin son siempre los mismos, por lo que en algunos casos
         * concretos se utilizan tablas estticas agrupadas por dimensinorth. Si no
         * encuentra la tabla de la dimensin en cuestin, el sistema ejecuta el 
         * algoritmo para iniciar la list: 
         * Toma una casilla y crea una CV para cada vecina que encuentra siempre
         * que cumpla que sean vecinas y que no estn ya conectadas en el mismo 
         * sentido west en el inverso. Enlaza las casillas cargando las listas de
         * vecinas para cada celda, basndose en su posicin en el tablero. */
        if(dimension == 3){
            boolean [][] map = {{false, true, false, true, true, false, false, false, false}, 
                {false, false, true, false, true, true, false, false, false}, 
                {false, false, false, false, false, true, false, false, false}, 
                {false, false, false, false, true, false, true, true, false}, 
                {false, false, false, false, false, true, false, true, true}, 
                {false, false, false, false, false, false, false, false, true}, 
                {false, false, false, false, false, false, false, true, false}, 
                {false, false, false, false, false, false, false, false, true}, 
                {false, false, false, false, false, false, false, false, false}};
            Cell cellA = null;
            Cell cellB = null;
            for(int a = 0; a < all.length; a++){
                for(int b = a; b < all.length; b++){
                    if(map[a][b] == true){
                        cellA = all[a];
                        cellB = all[b];
                        connections.newConnection(cellA, cellB);
                        cellA.addNeighbor(cellB);
                        cellB.addNeighbor(cellA);
                        expireOriginal.add(connections.insertDirectPath(cellA, cellB));
                    }
                }
            }
        }
        else if(dimension == 5){
            boolean [][] map = {{false, true, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
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
            Cell cellA = null;
            Cell cellB = null;    
            for(int a = 0; a < all.length; a++){
                    for(int b = a; b < all.length; b++){
                        if(map[a][b] == true){
                            cellA = all[a];
                            cellB = all[b];
                            connections.newConnection(cellA, cellB);
                            cellA.addNeighbor(cellB);
                            cellB.addNeighbor(cellA);
                            expireOriginal.add(connections.insertDirectPath(cellA, cellB));
                        }
                    }
                }
            }
        else if(dimension == 7){
            boolean [][] map = {{false, true, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}, 
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
            Cell cellA = null;
            Cell cellB = null;    
            for(int a = 0; a < all.length; a++){
                for(int b = a; b < all.length; b++){
                    if(map[a][b] == true){
                        cellA = all[a];
                        cellB = all[b];
                        connections.newConnection(cellA, cellB);
                        cellA.addNeighbor(cellB);
                        cellB.addNeighbor(cellA);
                        expireOriginal.add(connections.insertDirectPath(cellA, cellB));
                    }
                }
            }
        }
        else{
            boolean [][] map2 = new boolean[dimension*dimension][dimension*dimension];          
                    
            Cell cellA = null;
            Cell cellB = null;
            for(int a = 0; a < all.length; a++){
                for(int b = 0; b < all.length; b++){
                    try{
                        cellA = all[a];
                        cellB = all[b];
                        if((a != b) && (areNeighbors(cellA, cellB)) 
                                && (!connections.hasConnection(cellA, cellB))
                                && (!connections.hasConnection(cellB, cellA))){
                            connections.newConnection(cellA, cellB);
                            cellA.addNeighbor(cellB);
                            cellB.addNeighbor(cellA);
                            // map2[a][b] = true;
                            expireOriginal.add(connections.insertDirectPath(cellA, cellB));
                        }
                    }
                    catch (NonexistentSquare ex) {
                        ex.printStackTrace();
                    }                
                }
            }
            
/*          Mostrar los datos de conexiones listos para tabularse
 *          Esta seccin no est documentada y sirve para aadir tablas estticas
 *          para dimensiones mayores y mejorar la eficiencia del sistema. Para que
 *          funcione hay que descomentar map2[a][b] = true en la seccin anterior.
 *
            System.out.println("---");
            for(int a = 0; a < all.length; a++){
                System.out.print("{");
                for(int b = 0; b < all.length; b++){
                    if(b == all.length - 1)
                        System.out.print(map2[a][b]);
                    else System.out.print(map2[a][b] + ", ");
                }
                if(a == all.length - 1) System.out.println("}" );
                else System.out.println("}, " );
            }
 */
        }
                    
               
        /* Inserta caminos y establece vecinas entre las casillas y los bordes
         * en contacto: */
        for(int i = 0; i < dim; i++){
            connections.insertDirectPath(north, squares[0][i]);
            connections.insertDirectPath(south, squares[dim-1][i]);
            squares[0][i].addNeighbor(north);
            north.addNeighbor(squares[0][i]);
            south.addNeighbor(squares[dim-1][i]);
            squares[dim-1][i].addNeighbor(south);

            connections.insertDirectPath(west, squares[i][0]);
            connections.insertDirectPath(east, squares[i][dim-1]);
            squares[i][0].addNeighbor(west);
            west.addNeighbor(squares[i][0]);
            east.addNeighbor(squares[i][dim-1]);
            squares[i][dim-1].addNeighbor(east);
        }
    }
    
    /** Devuelve la list de caminos que van a caducar en una primera iteracin
     *  @return Lista de caminos que deben caducar en la primera iteracin */
    public ArrayList getExpireList(){
        return expireOriginal;
    }
    
    /** Devuelve el conjunto de conexiones bsicas T
     *  @return Conjunto T de conexiones bsicas del tablero */
    public Connections getConnections(){
        return connections;
    }
    
    /** Devuelve la dimensin del tablero de la heurstica
     *  @return Dimensin del tablero */
    public int getDimension(){
        return dimension;
    }
    
    /** Devuelve una list con las casillas libres del tablero
     *  @return Lista de casillas libres */
    public ArrayList<Square> getCellsLibres(){
        ArrayList<Square> lis = new ArrayList<Square>();
        
        for(int i = 0; i < dimension; i++)
            for(int j = 0; j < dimension; j++)
                if(squares[i][j].isEmpty()) lis.add((Square)squares[i][j]);
        
        return lis;
    }
    
    /** Devuelve la list G asociada al tablero, conteniendo las casillas del color
     *  que se pase por argumento west que estn libres
     *  @param color Identificador del jugador del que se desea obtener G
     *  @return Conjunto G como ArrayList */ 
    public ArrayList<Cell> generateG(int color){
        ArrayList<Cell> G = new ArrayList<Cell>();
        if(color == 1){
            for(int i = 0; i < dimension; i++){
                for(int j = 0; j < dimension; j++){
                    if((squares[i][j].getColor() == -1) || (squares[i][j].getColor() == 1)) G.add(squares[i][j]);
                }
            }
            G.add(north);
            G.add(south);
        }
        else{
            for(int i = 0; i < dimension; i++){
                for(int j = 0; j < dimension; j++){
                    if((squares[j][i].getColor() == -1) || (squares[j][i].getColor() == 0)) G.add(squares[j][i]);
                }
            }
            G.add(east);
            G.add(west);
        }

        return G;
    }
    
    /** Devuelve la casilla situada en la row y column que se pasen como argumento
     *  @param row Fila de la casilla
     *  @param column Columna de la casilla
     *  @return Una referencia a la casilla indicada por esa row y esa column */
    public Square get(int row, int column){
        return squares[row][column];
    }
    
    /** Determina si dos celdas son vecinas atendiendo a la posicin en el tablero
     *  @param a Primera casilla
     *  @param b Segunda casilla
     *  @return Verdadero si son vecinas, falso en caso contrario
     *  @throws NonexistentSquare  Si la casilla objetivo esta fuera del rango del tablero */   
    private boolean areNeighbors(Cell a, Cell b) throws NonexistentSquare{
        if((a instanceof Square) && (b instanceof Square))
            return areNeighbors(((Square)a).getRow(), ((Square)a).getColumn(), ((Square)b).getRow(), ((Square)b).getColumn());
        return false;
    }
    
    /** Determina si dos celdas cuyas coordenadas se pase son vecinas atendiendo
     *  a la posicin en el tablero.
     *  @param row1 Fila de la primera casilla
     *  @param col1 Columna de la primera casilla
     *  @param row2 Fila de la segunda casilla
     *  @param col2 Columna de la segunda casilla
     *  @return Verdadero si son vecinas, falso en caso contrario
     *  @throws NonexistentSquare  Si la casilla objetivo esta fuera del rango del tablero */   
    private boolean areNeighbors(int row1, int col1, int row2, int col2) throws NonexistentSquare{
        if((row1 >= dimension) || (col1 >= dimension) || (col1 < 0) || (row1 < 0) || (row2 >= dimension) || (col2 >= dimension) || (col2 < 0) || (row2 < 0))
            throw new NonexistentSquare();

        Cell [] list;
        Cell target = squares[row2][col2];
        
        if((col1 == 0) && (row1 != 0) && (row1 != dimension-1)){
            list = new Cell[4];
            list[0]=squares[row1][col1+1];
            list[1]=squares[row1+1][col1+1];
            list[2]=squares[row1-1][col1];
            list[3]=squares[row1+1][col1];
        }
        else if((col1 == dimension-1) && (row1 != 0) && (row1 != dimension-1)){
            list = new Cell[4];
            list[0]=squares[row1][col1-1];
            list[1]=squares[row1-1][col1-1];
            list[2]=squares[row1-1][col1];
            list[3]=squares[row1+1][col1];
        }
        else if((row1 == 0) && (col1 != 0) && (col1 != dimension-1)){
            list = new Cell[4];
            list[0]=squares[row1+1][col1];
            list[1]=squares[row1+1][col1+1];
            list[2]=squares[row1][col1+1];
            list[3]=squares[row1][col1-1];
        }
        else if((row1 == dimension-1) && (col1 != 0) && (col1 != dimension-1)){
            list = new Cell[4];
            list[0]=squares[row1][col1-1];
            list[1]=squares[row1][col1+1];
            list[2]=squares[row1-1][col1-1];
            list[3]=squares[row1-1][col1];
        }
        else if((col1 == 0) && (row1 == 0)){
            list = new Cell[3];
            list[0]=squares[row1][col1+1];
            list[1]=squares[row1+1][col1];
            list[2]=squares[row1+1][col1+1];
        }
        else if((col1 == 0) && (row1 == dimension-1)){
            list = new Cell[2];
            list[0]=squares[row1][col1+1];
            list[1]=squares[row1-1][col1];
        }
        else if((col1 == dimension-1) && (row1 == dimension-1)){
            list = new Cell[3];
            list[0]=squares[row1][col1-1];
            list[1]=squares[row1-1][col1];
            list[2]=squares[row1-1][col1-1];
        }
        else if((col1 == dimension-1) && (row1 == 0)){
            list = new Cell[2];
            list[0]=squares[row1][col1-1];
            list[1]=squares[row1+1][col1];				
        }
        else{
            list = new Cell[6];
            list[0]=squares[row1-1][col1-1];
            list[1]=squares[row1-1][col1];
            list[2]=squares[row1][col1-1];
            list[3]=squares[row1][col1+1];
            list[4]=squares[row1+1][col1];
            list[5]=squares[row1+1][col1+1];
        }
        for(int i = 0; i < list.length; i++)
            if(list[i] == target) return true;
        
        return false;
    }
}

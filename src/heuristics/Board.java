/*
 * Board.java
 *
 * Created on April 23, 2007 at 16:01
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
    private Square squares[][];     // A matrix of cells
    private Connections connections;   // List of virtual connections on the board
    private Border north, south, east, west;
    private ArrayList expireOriginal;  // Paths that expire in the first iteration
    Cell [] all;
    
    /** Creates a new instance of Board */
    public Board(int dim) {
        expireOriginal = new ArrayList();
        dimension = dim;
        
        all = new Cell [dimension * dimension];
        
        squares = new Square [dimension][dimension];   // Matrix representing the board
        connections = new Connections(dim);  // List with the virtual connections on the board

        // Creates the board squares:
        for(int i = 0; i < dimension; i++){
            for(int j = 0; j < dimension; j++){
                squares[i][j] = new Square(i, j, i * dimension + j);
                all[i * dimension + j] = squares[i][j];
            }
        }

        // Creates the four cell borders
        north = new Border(dim*dim+0, 'N');
        south = new Border(dim*dim+1, 'S');
        east = new Border(dim*dim+2, 'E');
        west = new Border(dim*dim+3, 'W');
        
        /* Initializes the connections list.
         * Los datos de inicializacin son siempre los mismos, por lo que en algunos casos
         * concretos se utilizan tablas estticas agrupadas por dimensinorth. Si no
         * encuentra la tabla de la dimensin en cuestin, el sistema ejecuta el 
         * algorithm to initialize the list: 
         * Takes a square and creates a VC for each neighbor it finds as long as
         * que cumpla que sean vecinas y que no estn ya conectadas en el mismo 
         * direction or in reverse. Links the squares by loading the neighbor lists
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
            
/*          Show the connection data ready to be tabulated
 *          Esta seccin no est documentada y sirve para aadir tablas estticas
 *          for larger dimensions and improve system efficiency. For it to
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
                    
               
        /* Inserts paths and establishes neighbors between the squares and the borders
         * in contact: */
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
    
    /** Returns a list with the free squares on the board
     *  @return List of free squares */
    public ArrayList<Square> getCellsLibres(){
        ArrayList<Square> lis = new ArrayList<Square>();
        
        for(int i = 0; i < dimension; i++)
            for(int j = 0; j < dimension; j++)
                if(squares[i][j].isEmpty()) lis.add((Square)squares[i][j]);
        
        return lis;
    }
    
    /** Returns the list G associated with the board, containing the squares of the color
     *  que se pase por argumento west que estn libres
     *  @param color Identifier of the player for which G is to be obtained
     *  @return Set G as ArrayList */ 
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
    
    /** Returns the square located at the row and column passed as argument
     *  @param row Square row
     *  @param column Square column
     *  @return A reference to the square indicated by that row and that column */
    public Square get(int row, int column){
        return squares[row][column];
    }
    
    /** Determina si dos celdas son vecinas atendiendo a la posicin en el tablero
     *  @param a First square
     *  @param b Second square
     *  @return True if they are neighbors, false otherwise
     *  @throws NonexistentSquare  If the target square is outside the board range */   
    private boolean areNeighbors(Cell a, Cell b) throws NonexistentSquare{
        if((a instanceof Square) && (b instanceof Square))
            return areNeighbors(((Square)a).getRow(), ((Square)a).getColumn(), ((Square)b).getRow(), ((Square)b).getColumn());
        return false;
    }
    
    /** Determines if two cells whose coordinates are passed are neighbors based on
     *  a la posicin en el tablero.
     *  @param row1 Row of the first square
     *  @param col1 Column of the first square
     *  @param row2 Row of the second square
     *  @param col2 Column of the second square
     *  @return True if they are neighbors, false otherwise
     *  @throws NonexistentSquare  If the target square is outside the board range */   
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

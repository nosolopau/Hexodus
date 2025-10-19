/*
 * Hexodus >> Cell.java
 *
 * Created on November 25, 2006 at 22:51
 *
 * Copyright (C) 2006 - 2008 Pablo Torrecilla. GNU General Public License
 */

package heuristics;
import java.util.*;

/**
 *  Represents a cell in the board graph, either a Border or a Square
 *
 *  @version 0.1
 *  @author Pau
 *  @see Square
 *  @see Border
 */
public class Cell {
    private int identifier;         // Unique identifier for each Cell
    private int color;              // Provisional: should later be a reference to the player
    private double resistanceBlack; // Cell value for the black player according to heuristic
    private double resistanceWhite; // Cell value for the white player according to heuristic
    private ArrayList<Cell> neighbors; // List of neighboring cells
    private static double INFINITY = 10E+10;
    private static double ZERO = 10E-10;

    /** Creates a new Cell instance
     *  @param id Unique cell identifier */
    public Cell(int id) {
        identifier = id;
        color = -1;
        resistanceBlack = resistanceWhite = 1;
        neighbors = new ArrayList<Cell>();
    }

    /** Creates a new Cell instance
     *  @param id Unique cell identifier
     *  @param name Cell name (if it's a border). Used
     *  to select the color when the BorderCell constructor invokes super() */
    public Cell(int id, char name) {
        identifier = id;
        switch(name){
            case 'N':
            case 'S':
                color = 1;
                resistanceBlack = ZERO;
                resistanceWhite = INFINITY;
                break;
            case 'E':
            case 'W':
                color = 0;
                resistanceBlack = INFINITY;
                resistanceWhite = ZERO;
        }
        neighbors = new ArrayList();
    }
    /** Creates a new Cell instance
     *  @param id Unique cell identifier
     *  @param color Color of the player occupying the cell or -1 if free */
    public Cell(int id, int color) {
        identifier = id;
        this.color = color;
        neighbors = new ArrayList();
        switch(color){
            case 0:
                resistanceWhite = ZERO;
                resistanceBlack = INFINITY;
                break;
            case 1:
                resistanceWhite = INFINITY;
                resistanceBlack = ZERO;
                break;
            default:
                resistanceBlack = resistanceWhite = 1;
        }
    }

    /** Occupies a square by a certain player or by none
     *  @param color Color of the player occupying the cell or -1 to leave it free */
    public void occupy(int color){
        this.color = color;
        switch(color){
            case 0:
                resistanceWhite = ZERO;
                resistanceBlack = INFINITY;
                break;
            case 1:
                resistanceWhite = INFINITY;
                resistanceBlack = ZERO;
                break;
            default:
                resistanceBlack = resistanceWhite = 1;
        }
    }

    /** Returns the Cell identifier
     *  @return Unique identifier */
    public int getId(){
        return this.identifier;
    }

    /** Returns the resistance value of the cell for a specific player
     *  @param color Color of the player whose resistance for this square is requested
     *  @return Resistance value */
    public double getResistance(int color){
        if(color == 1) return resistanceBlack;
        else return resistanceWhite;
    }

    /** Sets the resistance value of the cell for a specific player
     *  @param resistance Resistance value to assign to the cell
     *  @param color Color of the player whose resistance will be assigned the value
     *  in the resistance parameter */
    public void setResistance(int resistance, int color){
        if(color == 1) resistanceWhite = resistance;
        else resistanceBlack = resistance;
    }

    /** Adds a neighbor to the cell's neighbor list
     *  @param cell Neighbor to add */
    public void addNeighbor(Cell cell){
        if((!neighbors.contains(cell)) && (cell != this))
            neighbors.add(cell);
    }
    /** Removes a neighbor from the cell's neighbor list
     *  @param cell Neighbor to remove */
    public void removeNeighbor(Cell cell){
        if(neighbors.contains(cell))
            neighbors.remove(cell);
    }

    public boolean isNeighbor(Cell cell){
        return (neighbors.contains(cell));
    }

    /** Returns the neighbor list of the current cell
     *  @return An ArrayList with the cell's neighbors */
    public ArrayList getNeighborList(){
        return neighbors;
    }

    /** Returns the color (provisional) */
    public int getColor(){
        return color;
    }

    /** Returns whether the cell is empty or not (provisional) */
    public boolean isEmpty(){
        if(this instanceof Border) return true;
        return (color == -1);
    }
}

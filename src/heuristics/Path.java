/*
 * Hexodus >> Path.java
 *
 * Created on January 3, 2007 at 12:51
 *
 * Copyright (C) 2006 - 2008 Pablo Torrecilla. GNU General Public License
 */

package heuristics;

import java.util.*;

/**
 *  Represents a path composed of cells
 *  @author Pau
 *  @version 1.0
 */
public class Path {
    private ArrayList <Cell> list;      // An ArrayList of Cells in the path
    private boolean direct;             // Indicates if the path is direct type []
    private boolean newPath;            // Indicates if the path is new or not

    /** Creates a new Path */
    public Path() {
        list = new ArrayList();
        direct = false;     // Initially defined as NOT direct path
        newPath = true;     // Marked as new
    }

    /** Adds a cell to the path if it doesn't exist
     *  @param cell Cell to add to the path */
    public void add(Cell cell){
        if(!list.contains(cell))
            list.add(cell);
    }

    /** Returns the current value of the New property
     *  @return True if the path is new, false otherwise*/
    public boolean isNew(){
        return newPath;
    }

    /** Sets a new value for the new attribute
     *  @param newValue The new value for the new attribute */
    public void changeNew(boolean newValue){
        newPath = newValue;
    }

    /** Returns the path length based on the number of cells
     *  @return Path length (number of cells it contains) */
    public int getLength(){
        return list.size();
    }

    /** Returns a value to establish whether a path contains a cell or not
     *  @param target Target cell
     *  @return True if cell 'target' is in the path, false otherwise */
    public boolean contains(Cell target){
        return list.contains(target);
    }

    /** Returns true if the intersection of the current path with the given one
     *  is empty
     *  @param other Path to compare with the current one
     *  @return True if the intersection of both is null, false otherwise */
    public boolean hasEmptyIntersection(Path other){
        Iterator i1 = list.iterator();

        while(i1.hasNext())
            if(other.contains((Cell) i1.next())) return false;
        return true;
    }

    /** Executes the intersection operation between two paths
     *
     *  @param other Path to calculate intersection with the current one
     *  @return A path result of the intersection between the current and
     *  received by arguments. */
    public Path intersection(Path other){
        Path newPath = new Path();
        Cell square = null;

        Iterator <Cell> i1 = list.iterator();
        while(i1.hasNext()){
            square = i1.next();
            if(other.contains(square)) newPath.add(square);
        }
        return newPath;
    }

    /** Executes the union operation between two paths
     *
     *  @param other Path to calculate union with the current one
     *  @return A path result of the union of the current and received by
     *  arguments. */
    public Path union(Path other){
        Cell square = null;
        Path newPath = new Path();

        if (other.isDirect() && isDirect()){
            newPath.makeDirect();
            return newPath;
        }

        Iterator i1 = list.iterator();
        while(i1.hasNext()){
            newPath.add((Cell) i1.next());
        }
        Iterator i2 = other.getIterator();
        while(i2.hasNext()){
            square = (Cell) i2.next();
            if(!newPath.contains(square)) newPath.add(square);
        }
        return newPath;
    }

    /** Executes the union operation between two paths
     *
     *  @param other Path to calculate union with the current one
     *  @param cell Cell to be intercalated in the union
     *  @return A path result of the union of the current and received by
     *  arguments, intercalating cell c in the union */
    public Path union(Path other, Cell cell){
        Path newPath = new Path();

        Iterator <Cell> i1 = list.iterator();
        while(i1.hasNext()){
            newPath.add(i1.next());
        }
        newPath.add(cell);

        Iterator <Cell> i2 = other.getIterator();
        Cell square = null;
        while(i2.hasNext()){
            square = i2.next();
            if(!newPath.contains(square)) newPath.add(square);
        }

        return newPath;
    }

    /** Returns if the current path is empty. If the path is direct it is
     *  considered empty
     *  @return True if the path is empty or direct. False otherwise. */
    public boolean isEmpty(){
        return list.isEmpty();
    }

    /** Returns a path iterator
     *  @return An Iterator object over the Cell list
     */
    public Iterator getIterator(){
        return list.iterator();
    }

    /** Marks the direct attribute as true */
    public void makeDirect(){
        list.clear();
        direct = true;
    }

    /** Returns whether the path is direct or not
     *  @return True if the path is direct, false otherwise */
    public boolean isDirect(){
        return direct;
    }

    /** Returns true if the current path and the received one are equal.
     *  A path is considered equal to another if it contains the same
     *  cells regardless of order. If the paths are direct, they are considered equal.
     *
     *  @param other A target path to compare with the current one
     *  @return True if the paths are equal, false otherwise */
    public boolean equals(Path other){
        if(this.direct && other.direct) return true;
        if(other.getLength() != this.getLength()) return false;

        Iterator <Cell> i1 = list.iterator();
        while(i1.hasNext()){
            Cell c1 = i1.next();
            if(!other.contains(c1)) return false;
        }
        return true;
    }

    /** ... */
    public String toString(){
        if(list.isEmpty()) return "[]";
        Iterator e = list.iterator();
        String s = new String();
        s = "[";
        while(e.hasNext())
            s = s + e.next();
        s = s + "]";
        return s;
    }
}

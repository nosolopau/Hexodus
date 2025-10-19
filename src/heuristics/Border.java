/*
 * Hexodus >> Border.java
 *
 * Created on January 22, 2007 at 13:01
 *
 * Copyright (C) 2006 - 2008 Pablo Torrecilla. GNU General Public License
 */

package heuristics;

/**
 *  A Cell subtype representing a board border
 *
 *  @author Pau
 *  @version 1.0
 *  @see Cell
 */
public class Border extends Cell{
    private char name;    // Border name

    /** Creates a new BorderCell instance
     *  @param id Unique cell identifier (to call super())
     *  @param name Border name*/
    public Border(int id, char name) {
        super(id, name);
        this.name = name;
    }

    /** Returns the border name
     *  @return A char that is N, S, E, W depending on the border it represents */
    public char getName(){
        return name;
    }

    /** Overrides the equals method and adapts it to Cell */
    public boolean equals(Object o){
        return (o instanceof Border) && (name == ((Border)o).getName());
    }

    /** ... */
    public String toString(){
        return "(" + name + ")";
    }
}

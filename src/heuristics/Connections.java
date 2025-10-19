/*
 * Hexodus >> Connections.java
 *
 * Created on November 25, 2006 at 16:33
 *
 * Copyright (C) 2006 - 2008 Pablo Torrecilla. GNU General Public License
 */

package heuristics;
import java.util.*;

/**
 *  Represents a set of paths between two cells. Assigns a Route object to
 *  each pair of cells.
 *
 *  Functions as a container for Route objects
 *
 *  @author Pau
 *  @version 1.0
 */
public class Connections implements Cloneable{
    private Route [][] map;
    private int dimension;

    /** Creates a new instance of a connections list */
    public Connections(int dimension){
        this.dimension = dimension*dimension+4;
        map = new Route[this.dimension][this.dimension];
    }

    public Connections(int dimension, Route[][] map){
        this.map = map;
        this.dimension = dimension;
    }

    /** Creates an empty route between two cells.
     *  @param origin Origin cell
     *  @param dest Destination cell
     *  @see #newConnection(Cell origin, Cell dest, Route route) */
    public void newConnection(Cell origin, Cell dest){
        Route route = new Route();

        newConnection(origin, dest, route);
    }

    /** Creates a connection between two cells and assigns an existing route to it
     *  @param origin Origin cell
     *  @param dest Destination cell
     *  @param route Route to establish between the cells
     *  @see #newConnection(Cell origin, Cell dest) */
    public void newConnection(Cell origin, Cell dest, Route route){
        map[origin.getId()][dest.getId()] = route;
    }

    /** Returns the route object between two cells passed as parameter.
     *
     *  @param origin Origin cell
     *  @param dest Destination cell
     *  @return The route between cells or null if no direct or inverse connection exists */
    public Route getRoute(Cell origin, Cell dest){
        if(hasConnection(origin, dest))
            return (map[origin.getId()][dest.getId()]);
        else if(hasConnection(dest, origin))
            return (map[dest.getId()][origin.getId()]);
        return null;
    }

    /** Inserts a direct path [] between two cells
     *
     *  @param origin Origin cell
     *  @param dest Destination cell
     *  @return Reference to the inserted path */
    public Path insertDirectPath(Cell origin, Cell dest){
        if(!hasConnection(origin, dest))
            newConnection(origin, dest);
        Path path = new Path();
        path.makeDirect();

        (map[origin.getId()][dest.getId()]).add(path);
        return path;
    }

    /** Returns if a connection exists between the indicated cells taking
     *  into account the order in which they are passed to the method
     *
     *  @param origin Origin cell
     *  @param dest Destination cell
     *  @return True if there is a connection, false otherwise */
    public boolean hasConnection(Cell origin, Cell dest){
        if((map[origin.getId()][dest.getId()]) != null) return true;
        else return false;
    }

    /** Removes the connection that exists between the cells passed
     *
     *  @param origin Origin cell
     *  @param dest Destination cell */
    public void removeConnection(Cell origin, Cell dest){
        if(hasConnection(origin, dest))
            map[origin.getId()][dest.getId()] = null;
        else if(hasConnection(dest, origin))
            map[dest.getId()][origin.getId()] = null;

        return;
    }

    public Connections clone(){
        Route [][] r = new Route[dimension][dimension];
        for(int i = 0; i < dimension; i++)
            for(int j = 0; j < dimension; j++)
                if(map[i][j] != null)
                    r[i][j] = (Route) (map[i][j]).clone();

        return new Connections(dimension, r);
    }

    /** Returns if a connection exists between the indicated cells without taking
     *  into account the order in which they are passed to the method
     *
     *  @param origin Origin cell
     *  @param dest Destination cell
     *  @return True if there is a connection, false otherwise */
    public boolean hasConnectionEx(Cell origin, Cell dest){
        return (hasConnection(origin, dest) || hasConnection(dest, origin));
    }
}

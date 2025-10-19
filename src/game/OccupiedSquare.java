/*
 * Hexodus >> OccupiedSquare.java
 *
 * Created on June 14, 2007 at 02:54
 *
 * Copyright (C) 2006 - 2008 Pablo Torrecilla. GNU General Public License
 */

package game;

/**
 * Exception thrown when attempting to place a piece on an occupied square
 * @author Pau
 * @version 1.0
 */
public class OccupiedSquare extends java.lang.Exception {

    /** Create an instance without a message */
    public OccupiedSquare() {
    }

    /** Create an instance with the provided message
     * @param msg The detailed message. */
    public OccupiedSquare(String msg) {
        super(msg);
    }
}

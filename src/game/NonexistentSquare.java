/*
 * Hexodus >> NonexistentSquare.java
 *
 * Created on June 14, 2007 at 03:50
 *
 * Copyright (C) 2006 - 2008 Pablo Torrecilla. GNU General Public License
 */

package game;

/**
 * Exception thrown when attempting to access a nonexistent square on the board
 * @author Pau
 * @version 1.0
 */
public class NonexistentSquare extends java.lang.Exception {

    /** Create an instance without a message */
    public NonexistentSquare() {
    }

    /** Create an instance with the provided message
     * @param msg The detailed message. */
    public NonexistentSquare(String msg) {
        super(msg);
    }
}

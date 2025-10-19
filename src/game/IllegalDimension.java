/*
 * Hexodus >> IllegalDimension.java
 *
 * Created on June 14, 2007 at 01:29
 *
 * Copyright (C) 2006 - 2008 Pablo Torrecilla. GNU General Public License
 */

package game;

/**
 * Exception thrown when using an illegal dimension for the board.
 * @author Pau
 * @version 1.0
 */
public class IllegalDimension extends java.lang.Exception {

    /** Create an instance without a message */
    public IllegalDimension() {
    }

    /** Create an instance with the provided message
     * @param msg The detailed message. */
    public IllegalDimension(String msg) {
        super(msg);
    }
}

package game;

/**
 * Exception thrown when attempting to provide an incorrect level to the heuristic
 * from the system interface
 * @author Pau
 * @version 1.0
 */
public class IncorrectLevel extends java.lang.Exception {

    /** Create an instance without a message */
    public IncorrectLevel() {
    }

    /** Create an instance with the provided message
     * @param msg The detailed message. */
    public IncorrectLevel(String msg) {
        super(msg);
    }
}

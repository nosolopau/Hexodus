package game;

/** Represents each move that composes a match.
 *  This class is used to generate a list of recorded moves.
 *  @author Pau
 */
public class Move {
    private int number;
    private int row;
    private int column;
    private Player author;

    /** Creates a new move
     *  @param n        Move number
     *  @param row      Move row
     *  @param col      Move column
     *  @param author   Move author  */
    public Move(int n, int row, int col, Player author) {
        this.number = n;
        this.row = row;
        this.column = col;
        this.author = author;
    }

    /** Returns the move's row
     *  @return Move row */
    public int getRow(){
        return row;
    }

    /** Returns the move's column
     *  @return Move column */
    public int getColumn(){
        return column;
    }
}

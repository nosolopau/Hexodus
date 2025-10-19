package heuristics;

/**
 *  A Cell subtype representing a board square with row and column
 *
 *  @author Pau
 *  @version 1.0
 *  @see Cell
 */
public class Square extends Cell {
    private int row;        // Row of the square represented by the cell
    private int column;     // Column of the square represented by the cell

    /** Creates a new Square instance
     *  @param row Square row
     *  @param col Square column
     *  @param id Square identifier (needed to pass to super()) */
    public Square(int row, int col, int id) {
        super(id);
        this.row = row;
        this.column = col;
    }

    /** Returns the square's row on the board
     *  @return Square row */
    public int getRow(){
        return row;
    }

    /** Returns the square's column on the board
     *  @return Square column */
    public int getColumn(){
        return column;
    }

    /* Adapts and overrides the equals method for 'normal' squares */
    public boolean equals(Object o){
        return (o instanceof Square) && (row == ((Square)o).getRow()) &&
            (column == ((Square)o).getColumn());
    }

    /** ... */
    public String toString(){
        return "(" + row + ", " + column + ")";
    }
}

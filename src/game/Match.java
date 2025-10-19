package game;
import heuristics.Factory;
import heuristics.Heuristic;
import java.util.*;

/** Represents a game match.
 *  @author Pau
 *  @version 1.0
 */
public class Match{
    private int moveCount;      // Number of moves
    private Board board;        // Reference to the game board
    private Move [] moves;      // Move vector
    private Heuristic engine;

    /** Creates a new match (represented in a Match object)
     *  @param dim  Board dimension for the match
     *  @param swap Boolean value indicating if the swap move is enabled */
    public Match(int dim, boolean swap){
        moveCount = 0;
        board = new Board(dim);
        moves = new Move[dim*dim];

        Factory fact = new Factory();
        engine = fact.newHeuristic(dim, 1, swap);
    }

    /** Asks the heuristic if the first move should be swapped
     *  @param row    Row of the first move
     *  @param col    Column of the first move
     *  @return       True if it should be swapped, false otherwise */
    public boolean offerSwap(int row, int col){
        return engine.decideSwap(row, col);
    }

    /** Sets the heuristic level
     *  @param level    The new heuristic level (1, 2, or 3) */
    public void setLevel(int level) throws IncorrectLevel{
        if((level >= 1) && (level <= 3))
            engine.setLevel(level);
        else throw new IncorrectLevel();
    }

    /** Asks the heuristic to return the best available move for
     *  the player passed as argument
     *  @param player   Player for whom to calculate the move
     *  @return         An array of two integers representing row and column respectively */
    public int [] generateMove(Player player){
        int [] m = new int[2];
        m = engine.chooseMove(player.getPosition(), moveCount);

        return m;
    }

    /** Creates a new move in the match
     *  @param row      Move row
     *  @param col      Move column
     *  @param player   Player making the move
     *  @return A reference to a potential winning player or
     *          a null reference if there is no winner this turn
     *  @throws OccupiedSquare      If the target square is occupied by another
     *  @throws NonexistentSquare   If the target square is outside the board range
     */
    public Player newMove(int row, int col, Player player) throws OccupiedSquare, NonexistentSquare{
        Player aux = null;
        aux = board.occupy(row, col, player);

        moves[moveCount] = new Move(moveCount, row, col, player);

        engine.newMove(row, col, player.getPosition());

        moveCount++;
        return aux;
    }
}

/** Represents the game board
 *  @author Pau
 */
class Board {
    static int PLAYER1 = 1;     // Macros to represent square states
    static int PLAYER2 = 2;
    static int NONE = 0;
    private int dimension;      // Board dimension
    private Square [][]squares; // Array of squares forming the board
    private Border north;       // Board borders to connect squares
    private Border south;
    private Border east;
    private Border west;

    /** Creates a new board */
    public Board(int dim) {
        squares = new Square[dim][dim];
        dimension = dim;

        for(int i=0; i<dim; i++)
            for(int j=0; j<dim; j++)
                squares[i][j] = new Square(i, j, i*dim + j);

        north = new Border('N');
        south = new Border('S');
        east = new Border('E');
        west = new Border('W');
    }

    /** Occupies a board square passed with the arguments
     *  @param row      Board row to occupy
     *  @param col      Board column to occupy
     *  @param player   Player occupying the row and column
     *  @return A reference to the player who wins the match or null if no one wins
     *  @throws OccupiedSquare      If the target square is occupied by another
     *  @throws NonexistentSquare   If the target square is outside the board range */
    public Player occupy(int row, int col, Player player) throws OccupiedSquare, NonexistentSquare{
        // Exceptions thrown by Board: in case of occupied square and in case of row and column out of range
        if((row >= dimension) || (col >= dimension) || (col < 0) || (row < 0)) throw new NonexistentSquare();
        if(squares[row][col].isOccupied() == true) throw new OccupiedSquare();

        // If everything goes well, returns the winning player or a null pointer if no one wins
        squares[row][col].occupy(player);

        return(unite(row, col, player));
    }

    /** Returns true if the rows and columns passed as argument belong
     *  to two neighboring squares. To do this, it fills a list with neighbors of the first square
     *  and then searches for the second square in that list.
     *  @param row1 Row of the first square
     *  @param col1 Column of the first square
     *  @param row2 Row of the second square
     *  @param col2 Column of the second square
     *  @return     Returns true if the squares are neighbors
     *  @throws NonexistentSquare   If the target square is outside the board range */
    public boolean areNeighbors(int row1, int col1, int row2, int col2) throws NonexistentSquare{
        Square [] list;
        Square target = squares[row2][col2];

        if((row1 >= dimension) || (col1 >= dimension) || (col1 < 0) || (row1 < 0) || (row2 >= dimension) || (col2 >= dimension) || (col2 < 0) || (row2 < 0))
            throw new NonexistentSquare();

        if((col1 == 0) && (row1 != 0) && (row1 != dimension-1)){ // Left column except corners
            list = new Square[4];
            list[0]=squares[row1][col1+1];
            list[1]=squares[row1+1][col1+1];
            list[2]=squares[row1-1][col1];
            list[3]=squares[row1+1][col1];
        }
        else if((col1 == dimension-1) && (row1 != 0) && (row1 != dimension-1)){ // Right column except corners
            list = new Square[4];
            list[0]=squares[row1][col1-1];
            list[1]=squares[row1-1][col1-1];
            list[2]=squares[row1-1][col1];
            list[3]=squares[row1+1][col1];
        }
        else if((row1 == 0) && (col1 != 0) && (col1 != dimension-1)){
            list = new Square[4];
            list[0]=squares[row1+1][col1];
            list[1]=squares[row1+1][col1+1];
            list[2]=squares[row1][col1+1];
            list[3]=squares[row1][col1-1];
        }
        else if((row1 == dimension-1) && (col1 != 0) && (col1 != dimension-1)){
            list = new Square[4];
            list[0]=squares[row1][col1-1];
            list[1]=squares[row1][col1+1];
            list[2]=squares[row1-1][col1-1];
            list[3]=squares[row1-1][col1];
        }
        else if((col1 == 0) && (row1 == 0)){
            list = new Square[3];
            list[0]=squares[row1][col1+1];
            list[1]=squares[row1+1][col1];
            list[2]=squares[row1+1][col1+1];
        }
        else if((col1 == 0) && (row1 == dimension-1)){
            list = new Square[2];
            list[0]=squares[row1][col1+1];
            list[1]=squares[row1-1][col1];
        }
        else if((col1 == dimension-1) && (row1 == dimension-1)){
            list = new Square[3];
            list[0]=squares[row1][col1-1];
            list[1]=squares[row1-1][col1];
            list[2]=squares[row1-1][col1-1];
        }
        else if((col1 == dimension-1) && (row1 == 0)){
            list = new Square[2];
            list[0]=squares[row1][col1-1];
            list[1]=squares[row1+1][col1];
        }
        else{
            list = new Square[6];
            list[0]=squares[row1-1][col1-1];
            list[1]=squares[row1-1][col1];
            list[2]=squares[row1][col1-1];
            list[3]=squares[row1][col1+1];
            list[4]=squares[row1+1][col1];
            list[5]=squares[row1+1][col1+1];
        }

        for(int i = 0; i < list.length; i++)
            if(list[i] == target) return true;

        return false;
    }

    /** Unites the square passed as argument with the player's borders, if applicable
     *  @param row      Square row
     *  @param col      Square column
     *  @param player   Player
     *  @return         A reference to a player if this player is a winner */
    public Player unite(int row, int col, Player player){
        Square [] list;
        int max = 0;
        Border united = null;

        boolean unions = false;

        list = new Square[7];
        if(squares[row][col].getConnectedBorder() != null)
            return null;

        if(player.getColor() == PLAYER1){
            if(row == 0){
                squares[row][col].unite(north);
                united = north;
                unions = true;
            }
            else if(row == dimension-1){
                squares[row][col].unite(south);
                united = south;
                unions = true;
            }

            if((col == 0) && (row != 0) && (row != dimension-1)){ // Left column except corners
                max = 4;
                list[0]=squares[row][col+1];
                list[1]=squares[row+1][col+1];
                list[2]=squares[row-1][col];
                list[3]=squares[row+1][col];
            }
            else if((col == dimension-1) && (row != 0) && (row != dimension-1)){ // Right column except corners
                max = 4;
                list[0]=squares[row][col-1];
                list[1]=squares[row-1][col-1];
                list[2]=squares[row-1][col];
                list[3]=squares[row+1][col];
            }
            else if((row == 0) && (col != 0) && (col != dimension-1)){
                max = 4;
                list[0]=squares[row+1][col];
                list[1]=squares[row+1][col+1];
                list[2]=squares[row][col+1];
                list[3]=squares[row][col-1];
            }
            else if((row == dimension-1) && (col != 0) && (col != dimension-1)){
                max = 4;
                list[0]=squares[row][col-1];
                list[1]=squares[row][col+1];
                list[2]=squares[row-1][col-1];
                list[3]=squares[row-1][col];
            }
            else if((col == 0) && (row == 0)){
                max = 3;
                list[0]=squares[row][col+1];
                list[1]=squares[row+1][col];
                list[2]=squares[row+1][col+1];
            }
            else if((col == 0) && (row == dimension-1)){
                max = 2;
                list[0]=squares[row][col+1];
                list[1]=squares[row-1][col];
            }
            else if((col == dimension-1) && (row == dimension-1)){
                max = 3;
                list[0]=squares[row][col-1];
                list[1]=squares[row-1][col];
                list[2]=squares[row-1][col-1];
            }
            else if((col == dimension-1) && (row == 0)){
                max = 2;
                list[0]=squares[row][col-1];
                list[1]=squares[row+1][col];
            }
            else{
                max = 6;
                list[0]=squares[row-1][col-1];
                list[1]=squares[row-1][col];
                list[2]=squares[row][col-1];
                list[3]=squares[row][col+1];
                list[4]=squares[row+1][col];
                list[5]=squares[row+1][col+1];
            }
        }
        else if(player.getColor() == PLAYER2){
            if(col == 0){
                squares[row][col].unite(west);
                united = west;
                unions = true;
            }
            else if(col == dimension-1){
                squares[row][col].unite(east);
                united = east;
                unions = true;
            }

            if((row == 0) && (col != 0) && (col != dimension-1)){ // North row except corners
                max = 4;
                list[0]=squares[row+1][col];
                list[1]=squares[row+1][col+1];
                list[2]=squares[row][col-1];
                list[3]=squares[row][col+1];
            }
            else if((row == dimension-1) && (col != 0) && (col != dimension-1)){ // South row except corners
                max = 4;
                list[0]=squares[row-1][col];
                list[1]=squares[row-1][col-1];
                list[2]=squares[row][col-1];
                list[3]=squares[row][col+1];
            }
            else if((col == 0) && (row != 0) && (row != dimension-1)){ // West column except corners
                max = 4;
                list[0]=squares[row][col+1];
                list[1]=squares[row+1][col+1];
                list[2]=squares[row+1][col];
                list[3]=squares[row-1][col];
            }
            else if((col == dimension-1) && (row != 0) && (row != dimension-1)){ // East column except corners
                max = 4;
                list[0]=squares[row-1][col];
                list[1]=squares[row+1][col];
                list[2]=squares[row-1][col-1];
                list[3]=squares[row][col-1];
            }
            else if((row == 0) && (col == 0)){
                max = 3;
                list[0]=squares[row+1][col];
                list[1]=squares[row][col+1];
                list[2]=squares[row+1][col+1];
            }
            else if((row == 0) && (col == dimension-1)){
                max = 2;
                list[0]=squares[row+1][col];
                list[1]=squares[row][col-1];
            }
            else if((row == dimension-1) && (col == dimension-1)){
                max = 3;
                list[0]=squares[row-1][col];
                list[1]=squares[row][col-1];
                list[2]=squares[row-1][col-1];
            }
            else if((row == dimension-1) && (col == 0)){
                max = 2;
                list[0]=squares[row-1][col];
                list[1]=squares[row][col+1];
            }
            else{
                max = 6;
                list[0]=squares[row-1][col-1];
                list[1]=squares[row-1][col];
                list[2]=squares[row][col-1];
                list[3]=squares[row][col+1];
                list[4]=squares[row+1][col];
                list[5]=squares[row+1][col+1];
            }
        }

        // Previous declarations
        boolean connections = false;
        Border flag = null;
        Border con = null;

        // First pass: checks if any neighbor is united with a border
        for(int i=0; i<max; i++){
            if(list[i].getOccupant() == player){    // If the neighbor is a piece of the same color
                con = list[i].getConnectedBorder();
                if(con != null){                    // If that neighbor of the same color is connected to some border...
                    squares[row][col].unite(con);   // ...connect to the same border
                    unions = true;                  // Flag to signal that unions were made in this pass
                    if((united != null) && (con != united)) return player;  // United to two different borders in different passes: WINNER
                    if(connections){                                        // Another flag to detect if unions were made between two squares united to two different borders
                        if(flag != list[i].getConnectedBorder())
                            return player;                                  // United in a previous pass to a different border than the current one: WINNER
                    }
                    else{
                        flag = list[i].getConnectedBorder();
                    }
                    connections = true;
                }
            }
        }
        // Second pass to review the links once unions have been made
        for(int m=0; m<max; m++)
            if((list[m].getOccupant() == player) && unions)
                unite(list[m].getRow(), list[m].getColumn(), player);

        return null;
    }

    /** Represents each of the board borders
     */
    class Border {
        private char name;

        public Border(char name){
            this.name = name;
        }
        public char getName(){
            return name;
        }
    }

    /** Represents a board square
     */
    class Square{
        private int row;
        private int column;
        private int identifier;     // Unique square identifier
        private boolean occupied;   // True if occupied, false otherwise
        private Player occupant;
        private Border connectedBorder; // Reference to the border it's connected to

        public Square(int row, int col, int id){
            this.row = row;
            this.column = col;
            this.occupant = null;
            this.occupied = false;
            this.connectedBorder = null;
            this.identifier = id;
        }

        public void occupy(Player player){
            occupied = true;
            occupant = player;
        }
        public Player getOccupant(){
            return occupant;
        }
        public boolean isOccupied(){
            return occupied;
        }
        public Border getConnectedBorder(){
            return connectedBorder;
        }
        public void unite(Border border){
            connectedBorder = border;
        }
        public int getRow(){
            return row;
        }
        public int getColumn(){
            return column;
        }

        /** Shows the square on standard output */
        public void show(){
            System.out.println("(" + row + ", " + column + ")");
        }
    }
}

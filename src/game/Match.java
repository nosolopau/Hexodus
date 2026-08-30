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


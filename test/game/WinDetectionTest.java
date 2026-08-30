package game;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Win detection is the one rule the game cannot get wrong, and the
 * hexagonal adjacency makes it easy to get wrong: (r,c) touches (r+1,c+1)
 * but not (r+1,c-1), so chains that look connected on a square grid are
 * not, and vice versa.
 *
 * The vertical player joins the top and bottom edges; the horizontal
 * player joins left and right.
 */
public class WinDetectionTest {

    private Player vertical;    // joins top to bottom
    private Player horizontal;  // joins left to right

    @Before
    public void setUp() {
        vertical = new Player("Vertical", 0, 1);
        horizontal = new Player("Horizontal", 0, 2);
    }

    /** Plays a list of moves and returns the winner after the last one */
    private Player play(Match match, Player player, int[][] moves)
            throws OccupiedSquare, NonexistentSquare {
        Player winner = null;
        for (int[] m : moves) winner = match.newMove(m[0], m[1], player);
        return winner;
    }

    @Test
    public void verticalWinsByJoiningTopAndBottom() throws Exception {
        Match match = new Match(3, false);
        // a straight column is connected: (0,0)-(1,0)-(2,0)
        Player winner = play(match, vertical, new int[][]{{0,0},{1,0},{2,0}});
        assertNotNull("a full column joins both edges", winner);
        assertEquals(vertical, winner);
    }

    @Test
    public void horizontalWinsByJoiningLeftAndRight() throws Exception {
        Match match = new Match(3, false);
        Player winner = play(match, horizontal, new int[][]{{0,0},{0,1},{0,2}});
        assertNotNull("a full row joins both edges", winner);
        assertEquals(horizontal, winner);
    }

    @Test
    public void theHexDiagonalIsConnected() throws Exception {
        // (r,c) touches (r+1,c+1), so the main diagonal is a chain
        Match match = new Match(3, false);
        Player winner = play(match, vertical, new int[][]{{0,0},{1,1},{2,2}});
        assertNotNull("the main diagonal is connected in hex", winner);
    }

    @Test
    public void theOppositeDiagonalIsNotConnected() throws Exception {
        // (r,c) does NOT touch (r+1,c-1), so this diagonal is three separate stones
        Match match = new Match(3, false);
        Player winner = play(match, vertical, new int[][]{{0,2},{1,1},{2,0}});
        assertNull("the anti-diagonal is not a chain in hex", winner);
    }

    @Test
    public void anIncompleteChainDoesNotWin() throws Exception {
        Match match = new Match(5, false);
        Player winner = play(match, vertical, new int[][]{{0,0},{1,0},{2,0},{3,0}});
        assertNull("stopping one row short is not a win", winner);
    }

    @Test
    public void aChainForTheWrongPairOfEdgesDoesNotWin() throws Exception {
        // A full row joins left to right, which is nothing to the vertical player
        Match match = new Match(3, false);
        Player winner = play(match, vertical, new int[][]{{1,0},{1,1},{1,2}});
        assertNull("vertical does not win by crossing the board sideways", winner);
    }

    @Test(expected = OccupiedSquare.class)
    public void playingOnATakenSquareIsRejected() throws Exception {
        Match match = new Match(5, false);
        match.newMove(2, 2, vertical);
        match.newMove(2, 2, horizontal);
    }

    @Test(expected = NonexistentSquare.class)
    public void playingOffTheBoardIsRejected() throws Exception {
        Match match = new Match(5, false);
        match.newMove(5, 0, vertical);
    }

    @Test
    public void aWinningChainMayWanderAcrossTheBoard() throws Exception {
        /* Nothing requires the chain to be straight: this one steps
         * sideways and diagonally on its way down. */
        Match match = new Match(5, false);
        Player winner = play(match, vertical,
            new int[][]{{0,2},{1,2},{1,3},{2,3},{3,3},{4,3}});
        assertNotNull("a winding chain still joins the edges", winner);
        assertEquals(vertical, winner);
    }
}

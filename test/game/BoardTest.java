package game;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Test suite for the Board class (inner class of Match).
 * Tests board creation, neighbor detection, move validation, and winning conditions.
 */
public class BoardTest {
    private Match match;
    private Player player1;
    private Player player2;

    @Before
    public void setUp() {
        match = new Match(5, false);
        player1 = new Player("Player 1", 0, 1);
        player2 = new Player("Player 2", 0, 2);
    }

    @Test
    public void testBoardCreation_ValidDimension() {
        // Board should be created successfully with dimension 5
        assertNotNull(match);
    }

    @Test
    public void testOccupy_ValidSquare() throws OccupiedSquare, NonexistentSquare {
        // Should successfully occupy a valid square
        Player winner = match.newMove(2, 2, player1);
        assertNull(winner); // No winner from a single move
    }

    @Test(expected = NonexistentSquare.class)
    public void testOccupy_OutOfBoundsRow() throws OccupiedSquare, NonexistentSquare {
        // Should throw NonexistentSquare when row is out of bounds
        match.newMove(10, 2, player1);
    }

    @Test(expected = NonexistentSquare.class)
    public void testOccupy_OutOfBoundsColumn() throws OccupiedSquare, NonexistentSquare {
        // Should throw NonexistentSquare when column is out of bounds
        match.newMove(2, 10, player1);
    }

    @Test(expected = NonexistentSquare.class)
    public void testOccupy_NegativeRow() throws OccupiedSquare, NonexistentSquare {
        // Should throw NonexistentSquare when row is negative
        match.newMove(-1, 2, player1);
    }

    @Test(expected = NonexistentSquare.class)
    public void testOccupy_NegativeColumn() throws OccupiedSquare, NonexistentSquare {
        // Should throw NonexistentSquare when column is negative
        match.newMove(2, -1, player1);
    }

    @Test(expected = OccupiedSquare.class)
    public void testOccupy_AlreadyOccupiedSquare() throws OccupiedSquare, NonexistentSquare {
        // First move should succeed
        match.newMove(2, 2, player1);
        // Second move to same square should throw OccupiedSquare
        match.newMove(2, 2, player2);
    }

    @Test
    public void testWinCondition_Player1VerticalWin() throws OccupiedSquare, NonexistentSquare {
        // Player 1 wins by connecting north to south (vertical)
        Player winner = null;

        // Create a path from top to bottom
        winner = match.newMove(0, 0, player1);
        assertNull(winner);

        winner = match.newMove(1, 0, player1);
        assertNull(winner);

        winner = match.newMove(2, 0, player1);
        assertNull(winner);

        winner = match.newMove(3, 0, player1);
        assertNull(winner);

        winner = match.newMove(4, 0, player1);
        // Should detect winner after connecting north and south borders
        assertNotNull(winner);
        assertEquals(player1, winner);
    }

    @Test
    public void testWinCondition_Player2HorizontalWin() throws OccupiedSquare, NonexistentSquare {
        // Player 2 wins by connecting west to east (horizontal)
        Player winner = null;

        // Create a path from left to right
        winner = match.newMove(0, 0, player2);
        assertNull(winner);

        winner = match.newMove(0, 1, player2);
        assertNull(winner);

        winner = match.newMove(0, 2, player2);
        assertNull(winner);

        winner = match.newMove(0, 3, player2);
        assertNull(winner);

        winner = match.newMove(0, 4, player2);
        // Should detect winner after connecting west and east borders
        assertNotNull(winner);
        assertEquals(player2, winner);
    }

    @Test
    public void testWinCondition_DiagonalConnection() throws OccupiedSquare, NonexistentSquare {
        // Test diagonal path for player 1
        Player winner = null;

        winner = match.newMove(0, 2, player1);
        assertNull(winner);

        winner = match.newMove(1, 2, player1);
        assertNull(winner);

        winner = match.newMove(2, 3, player1);
        assertNull(winner);

        winner = match.newMove(3, 3, player1);
        assertNull(winner);

        winner = match.newMove(4, 3, player1);
        assertNotNull(winner);
        assertEquals(player1, winner);
    }

    @Test
    public void testMultipleMoves_NoWinner() throws OccupiedSquare, NonexistentSquare {
        // Multiple moves that don't create a winning condition
        Player winner = null;

        winner = match.newMove(0, 0, player1);
        assertNull(winner);

        winner = match.newMove(1, 1, player2);
        assertNull(winner);

        winner = match.newMove(2, 2, player1);
        assertNull(winner);

        winner = match.newMove(3, 3, player2);
        assertNull(winner);

        winner = match.newMove(4, 4, player1);
        assertNull(winner);
    }

    @Test
    public void testBoardEdgeCases_CornerSquares() throws OccupiedSquare, NonexistentSquare {
        // Test all corner squares can be occupied
        Player winner = null;

        winner = match.newMove(0, 0, player1);
        assertNull(winner);

        winner = match.newMove(0, 4, player2);
        assertNull(winner);

        winner = match.newMove(4, 0, player1);
        assertNull(winner);

        winner = match.newMove(4, 4, player2);
        assertNull(winner);
    }

    @Test
    public void testSmallBoard_3x3() throws OccupiedSquare, NonexistentSquare {
        // Test with a smaller board dimension
        Match smallMatch = new Match(3, false);
        Player winner = null;

        winner = smallMatch.newMove(0, 0, player1);
        assertNull(winner);

        winner = smallMatch.newMove(1, 0, player1);
        assertNull(winner);

        winner = smallMatch.newMove(2, 0, player1);
        assertNotNull(winner);
        assertEquals(player1, winner);
    }

    @Test
    public void testLargeBoard_7x7() throws OccupiedSquare, NonexistentSquare {
        // Test with a larger board dimension
        Match largeMatch = new Match(7, false);
        Player winner = null;

        // Create a vertical winning path for player1
        for (int i = 0; i < 7; i++) {
            winner = largeMatch.newMove(i, 3, player1);
        }

        assertNotNull(winner);
        assertEquals(player1, winner);
    }
}

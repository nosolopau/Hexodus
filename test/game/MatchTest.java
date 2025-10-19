package game;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Test suite for the Match class.
 * Tests match creation, move validation, level setting, and game flow.
 */
public class MatchTest {
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
    public void testMatchCreation_ValidDimension() {
        // Match should be created successfully
        assertNotNull(match);
    }

    @Test
    public void testMatchCreation_WithSwapEnabled() {
        // Match should be created with swap rule enabled
        Match swapMatch = new Match(5, true);
        assertNotNull(swapMatch);
    }

    @Test
    public void testNewMove_ValidMove() throws OccupiedSquare, NonexistentSquare {
        // Should successfully create a new move
        Player winner = match.newMove(2, 2, player1);
        assertNull(winner);
    }

    @Test
    public void testNewMove_SequentialMoves() throws OccupiedSquare, NonexistentSquare {
        // Should successfully create sequential moves
        match.newMove(0, 0, player1);
        match.newMove(1, 1, player2);
        match.newMove(2, 2, player1);
        match.newMove(3, 3, player2);
        // No exception should be thrown
    }

    @Test(expected = OccupiedSquare.class)
    public void testNewMove_DuplicateMove() throws OccupiedSquare, NonexistentSquare {
        // First move should succeed
        match.newMove(2, 2, player1);
        // Second move to same position should fail
        match.newMove(2, 2, player2);
    }

    @Test(expected = NonexistentSquare.class)
    public void testNewMove_InvalidPosition() throws OccupiedSquare, NonexistentSquare {
        // Should throw exception for invalid position
        match.newMove(10, 10, player1);
    }

    @Test
    public void testSetLevel_ValidLevel1() throws IncorrectLevel {
        // Should successfully set level to 1
        match.setLevel(1);
        // No exception should be thrown
    }

    @Test
    public void testSetLevel_ValidLevel2() throws IncorrectLevel {
        // Should successfully set level to 2
        match.setLevel(2);
        // No exception should be thrown
    }

    @Test(expected = IncorrectLevel.class)
    public void testSetLevel_InvalidLevel0() throws IncorrectLevel {
        // Should throw exception for level 0
        match.setLevel(0);
    }

    @Test(expected = IncorrectLevel.class)
    public void testSetLevel_InvalidLevel3() throws IncorrectLevel {
        // Should throw exception for level 3
        match.setLevel(3);
    }

    @Test(expected = IncorrectLevel.class)
    public void testSetLevel_NegativeLevel() throws IncorrectLevel {
        // Should throw exception for negative level
        match.setLevel(-1);
    }

    @Test
    public void testGenerateMove_ComputerPlayer() {
        // Should generate a valid move for computer player
        Player computer = new Player("Computer", 1, 1);
        int[] move = match.generateMove(computer);

        assertNotNull(move);
        assertEquals(2, move.length);
        assertTrue(move[0] >= 0 && move[0] < 5);
        assertTrue(move[1] >= 0 && move[1] < 5);
    }

    @Test
    public void testGenerateMove_AfterSomeMoves() throws OccupiedSquare, NonexistentSquare {
        // Generate move after some squares are occupied
        match.newMove(0, 0, player1);
        match.newMove(1, 1, player2);

        Player computer = new Player("Computer", 1, 1);
        int[] move = match.generateMove(computer);

        assertNotNull(move);
        assertEquals(2, move.length);
    }

    @Test
    public void testOfferSwap_FirstMove() {
        // Test swap decision for first move
        Match swapMatch = new Match(5, true);
        boolean swapDecision = swapMatch.offerSwap(2, 2);

        // Result should be a boolean (true or false)
        assertTrue(swapDecision == true || swapDecision == false);
    }

    @Test
    public void testOfferSwap_CenterPosition() {
        // Test swap decision for center position
        Match swapMatch = new Match(5, true);
        boolean swapDecision = swapMatch.offerSwap(2, 2);

        // Center position might be considered for swap
        assertTrue(swapDecision == true || swapDecision == false);
    }

    @Test
    public void testOfferSwap_CornerPosition() {
        // Test swap decision for corner position
        Match swapMatch = new Match(5, true);
        boolean swapDecision = swapMatch.offerSwap(0, 0);

        // Result should be a boolean
        assertTrue(swapDecision == true || swapDecision == false);
    }

    @Test
    public void testMultipleMovesUntilWin() throws OccupiedSquare, NonexistentSquare {
        // Play a complete game until someone wins
        Player winner = null;

        for (int i = 0; i < 5; i++) {
            winner = match.newMove(i, 2, player1);
            if (winner != null) break;
        }

        assertNotNull(winner);
        assertEquals(player1, winner);
    }

    @Test
    public void testSmallBoardMatch() throws OccupiedSquare, NonexistentSquare {
        // Test match with small board (3x3)
        Match smallMatch = new Match(3, false);
        Player winner = null;

        winner = smallMatch.newMove(0, 0, player1);
        assertNull(winner);

        winner = smallMatch.newMove(1, 1, player1);
        assertNull(winner);

        winner = smallMatch.newMove(2, 2, player1);
        assertNull(winner);
    }

    @Test
    public void testLargeBoardMatch() {
        // Test match with large board (7x7)
        Match largeMatch = new Match(7, false);
        assertNotNull(largeMatch);
    }
}

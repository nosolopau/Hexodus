package heuristics;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import game.*;

/**
 * Tests for the Heuristic class to ensure it returns valid moves
 * and doesn't crash under various board conditions.
 */
public class HeuristicTest {

    @Test
    public void testHeuristicReturnsValidMoveOnEmptyBoard() throws Exception {
        // Create a 6x6 board
        Match match = new Match(6, false);

        // Get a move from the heuristic for player 1 (vertical)
        int[] move = match.generateMove(new Player(1, 1));

        assertNotNull("Heuristic should return a move", move);
        assertEquals("Move should have 2 coordinates", 2, move.length);

        // Check move is within bounds
        assertTrue("Row should be >= 0", move[0] >= 0);
        assertTrue("Row should be < 6", move[0] < 6);
        assertTrue("Column should be >= 0", move[1] >= 0);
        assertTrue("Column should be < 6", move[1] < 6);
    }

    @Test
    public void testHeuristicReturnsValidMoveForBothPlayers() throws Exception {
        Match match = new Match(6, false);

        // Test player 1 (vertical, red)
        int[] move1 = match.generateMove(new Player(1, 1));
        assertNotNull("Player 1 should get a valid move", move1);
        assertTrue("Player 1 move should be in bounds",
                   move1[0] >= 0 && move1[0] < 6 && move1[1] >= 0 && move1[1] < 6);

        // Test player 0 (horizontal, blue)
        int[] move2 = match.generateMove(new Player(1, 0));
        assertNotNull("Player 0 should get a valid move", move2);
        assertTrue("Player 0 move should be in bounds",
                   move2[0] >= 0 && move2[0] < 6 && move2[1] >= 0 && move2[1] < 6);
    }

    @Test
    public void testHeuristicOnPartiallyFilledBoard() throws Exception {
        Match match = new Match(6, false);
        Player p1 = new Player(1, 1);
        Player p2 = new Player(1, 0);

        // Make some moves to partially fill the board
        match.newMove(0, 0, p1);
        match.newMove(1, 1, p2);
        match.newMove(2, 2, p1);
        match.newMove(3, 3, p2);

        // Get a move for player 1
        int[] move = match.generateMove(p1);

        assertNotNull("Should return a move on partially filled board", move);
        assertTrue("Move should be in bounds",
                   move[0] >= 0 && move[0] < 6 && move[1] >= 0 && move[1] < 6);

        // Verify the suggested move is not already occupied
        boolean occupied = false;
        int[][] occupiedCells = {{0,0}, {1,1}, {2,2}, {3,3}};
        for (int[] cell : occupiedCells) {
            if (cell[0] == move[0] && cell[1] == move[1]) {
                occupied = true;
                break;
            }
        }
        assertFalse("Heuristic should not suggest an occupied cell", occupied);
    }

    @Test
    public void testHeuristicOnDifferentBoardSizes() throws Exception {
        int[] sizes = {3, 5, 6, 7};

        for (int size : sizes) {
            Match match = new Match(size, false);
            Player player = new Player(1, 1);

            int[] move = match.generateMove(player);

            assertNotNull("Should return move for size " + size, move);
            assertTrue("Move should be in bounds for size " + size,
                      move[0] >= 0 && move[0] < size &&
                      move[1] >= 0 && move[1] < size);
        }
    }

    @Test
    public void testHeuristicDoesNotCrashOnNearlyFullBoard() throws Exception {
        Match match = new Match(3, false);  // Small board for faster test
        Player p1 = new Player(1, 1);
        Player p2 = new Player(1, 0);

        // Fill most of the board
        match.newMove(0, 0, p1);
        match.newMove(0, 1, p2);
        match.newMove(0, 2, p1);
        match.newMove(1, 0, p2);
        match.newMove(1, 1, p1);
        match.newMove(1, 2, p2);
        match.newMove(2, 0, p1);
        // Leave 2,1 and 2,2 open

        // Should still return a valid move
        int[] move = match.generateMove(p2);

        assertNotNull("Should return move even on nearly full board", move);
        assertTrue("Move should be one of the two remaining cells",
                  (move[0] == 2 && move[1] == 1) || (move[0] == 2 && move[1] == 2));
    }

    @Test
    public void testHeuristicConsistencyAcrossMultipleCalls() throws Exception {
        // The heuristic should be deterministic for the same board state
        Match match1 = new Match(6, false);
        Match match2 = new Match(6, false);
        Player player = new Player(1, 1);

        // Set the same level for both
        match1.setLevel(1);
        match2.setLevel(1);

        int[] move1 = match1.generateMove(player);
        int[] move2 = match2.generateMove(player);

        // With the same board state and level, should suggest the same move
        assertArrayEquals("Same board state should produce same move", move1, move2);
    }

    @Test
    public void testHeuristicWithSwapRule() throws Exception {
        Match match = new Match(6, true);  // Enable swap rule
        Player p1 = new Player(1, 1);

        int[] move = match.generateMove(p1);

        assertNotNull("Should return move with swap rule enabled", move);
        assertTrue("Move should be in bounds",
                  move[0] >= 0 && move[0] < 6 && move[1] >= 0 && move[1] < 6);
    }

    @Test
    public void testHeuristicAfterSequenceOfMoves() throws Exception {
        Match match = new Match(6, false);
        Player p1 = new Player(1, 1);
        Player p2 = new Player(1, 0);

        // Simulate a short game
        for (int i = 0; i < 10; i++) {
            Player currentPlayer = (i % 2 == 0) ? p1 : p2;
            int[] move = match.generateMove(currentPlayer);

            assertNotNull("Should return valid move on turn " + i, move);

            // Apply the move
            Player winner = match.newMove(move[0], move[1], currentPlayer);

            // If someone won, stop
            if (winner != null) {
                break;
            }
        }

        // Test passed if we got here without exceptions
        assertTrue("Sequence of heuristic moves completed without crashing", true);
    }

    @Test
    public void testBorderNamesAreConsistent() throws Exception {
        // This tests that the 'O' vs 'W' bug doesn't reoccur
        // by verifying the heuristic works for the horizontal player
        Match match = new Match(6, false);
        Player horizontalPlayer = new Player(1, 0);  // Player 0 uses E-W borders

        // Make several moves and ensure no ArrayIndexOutOfBoundsException
        for (int i = 0; i < 5; i++) {
            int[] move = match.generateMove(horizontalPlayer);
            assertNotNull("Horizontal player should get valid moves", move);
            match.newMove(move[0], move[1], horizontalPlayer);
        }

        // If we get here, border names are consistent
        assertTrue("Border name consistency test passed", true);
    }

    @Test
    public void testHeuristicLevel1VsLevel2() throws Exception {
        Match match = new Match(6, false);
        Player player = new Player(1, 1);

        // Test level 1
        match.setLevel(1);
        int[] move1 = match.generateMove(player);
        assertNotNull("Level 1 should return a move", move1);

        // Test level 2
        match.setLevel(2);
        int[] move2 = match.generateMove(player);
        assertNotNull("Level 2 should return a move", move2);

        // Both levels should return valid moves
        assertTrue("Level 1 move should be in bounds",
                  move1[0] >= 0 && move1[0] < 6 && move1[1] >= 0 && move1[1] < 6);
        assertTrue("Level 2 move should be in bounds",
                  move2[0] >= 0 && move2[0] < 6 && move2[1] >= 0 && move2[1] < 6);
    }
}

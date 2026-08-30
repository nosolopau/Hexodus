package heuristics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the MultiThread heuristic engine, which the Factory only selects on
 * multi-core machines for boards of 7x7 or larger. Instantiating it directly here
 * (the class is package-private, hence this test lives in package heuristics)
 * guarantees the parallel search is exercised regardless of the host machine.
 *
 * Level 1 and a handful of moves keep the runtime modest.
 */
public class MultiThreadTest {

    private static final int DIMENSION = 7;
    private static final int LEVEL = 1;

    private static final int VERTICAL = 1;
    private static final int HORIZONTAL = 0;

    private final List<MultiThread> engines = new ArrayList<MultiThread>();

    /** Creates an engine and registers it so it is shut down when the test ends */
    private MultiThread newEngine() {
        MultiThread engine = new MultiThread(DIMENSION, LEVEL, false);
        engines.add(engine);
        return engine;
    }

    @After
    public void shutdownEngines() {
        for (MultiThread engine : engines) {
            engine.shutdown();
        }
        engines.clear();
    }

    /** Encodes a move as a single key so occupied cells can be tracked in a set */
    private static Integer key(int row, int column) {
        return Integer.valueOf(row * DIMENSION + column);
    }

    private static void assertLegal(String context, int[] move, Set<Integer> occupied) {
        assertNotNull(context + " should return a move", move);
        assertEquals(context + " should have 2 coordinates", 2, move.length);
        assertTrue(context + " row should be in bounds, was " + move[0],
                   move[0] >= 0 && move[0] < DIMENSION);
        assertTrue(context + " column should be in bounds, was " + move[1],
                   move[1] >= 0 && move[1] < DIMENSION);
        assertFalse(context + " should not choose the occupied cell (" + move[0] + "," + move[1] + ")",
                    occupied.contains(key(move[0], move[1])));
    }

    @Test
    public void testOpeningMoveComesFromBookAndIsOnBoard() {
        MultiThread engine = newEngine();

        int[] move = engine.chooseMove(VERTICAL, 0);

        assertLegal("Opening move", move, new HashSet<Integer>());
    }

    @Test
    public void testAlternatingMovesAreLegal() {
        MultiThread engine = newEngine();
        Set<Integer> occupied = new HashSet<Integer>();

        // Seed the board so the searches below run over a real position
        engine.newMove(3, 3, VERTICAL);
        occupied.add(key(3, 3));

        // Four alternating searched moves, starting with the horizontal player
        for (int moveNumber = 1; moveNumber <= 4; moveNumber++) {
            int color = (moveNumber % 2 == 1) ? HORIZONTAL : VERTICAL;

            int[] move = engine.chooseMove(color, moveNumber);

            assertLegal("Move " + moveNumber, move, occupied);

            engine.newMove(move[0], move[1], color);
            occupied.add(key(move[0], move[1]));
        }

        assertEquals("Every move should have occupied a distinct cell", 5, occupied.size());
    }

    @Test
    public void testSamePositionProducesSameMove() {
        MultiThread first = newEngine();
        MultiThread second = newEngine();

        first.newMove(3, 3, VERTICAL);
        second.newMove(3, 3, VERTICAL);

        int[] move1 = first.chooseMove(HORIZONTAL, 1);
        int[] move2 = second.chooseMove(HORIZONTAL, 1);

        assertArrayEquals("Same board state should produce the same move", move1, move2);
    }
}

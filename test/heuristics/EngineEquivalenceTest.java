package heuristics;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;

/**
 * The engine ships two path representations and an optimised OR rule, all
 * claiming to compute exactly the same thing. That claim is the basis of
 * the whole optimisation effort, and nothing else checks it.
 *
 * These tests evaluate real positions under every combination of the flags
 * and require the resulting scores to be identical to the last bit, not
 * merely close: a difference of any size would mean the engine's choice of
 * move could change with a flag.
 */
public class EngineEquivalenceTest {

    private boolean savedBitpath, savedLeanOr;

    @Before
    public void saveFlags() {
        savedBitpath = OptConfig.USE_BITPATH;
        savedLeanOr = OptConfig.USE_LEAN_OR;
    }

    @After
    public void restoreFlags() {
        OptConfig.USE_BITPATH = savedBitpath;
        OptConfig.USE_LEAN_OR = savedLeanOr;
    }

    /** Plays a fixed sequence of stones and records the score after each */
    private double[] evaluate(int dim, int[][] moves, boolean bitpath, boolean leanOr) {
        OptConfig.USE_BITPATH = bitpath;
        OptConfig.USE_LEAN_OR = leanOr;

        double[] scores = new double[moves.length];
        Simulation sim = new Simulation(dim);
        for (int i = 0; i < moves.length; i++) {
            sim = new Simulation(sim, moves[i][0], moves[i][1], i % 2 == 0 ? 1 : 0);
            scores[i] = sim.calculateValue();
        }
        return scores;
    }

    private static final int[][] OPENING = {
        {2, 2}, {1, 2}, {3, 3}, {2, 3}, {1, 1}, {3, 2}
    };

    @Test
    public void bitmaskPathsScoreIdenticallyToObjectPaths() {
        double[] objects = evaluate(5, OPENING, false, false);
        double[] bitmask = evaluate(5, OPENING, true, false);
        assertArrayEquals("bitmask paths must not change any evaluation",
            objects, bitmask, 0.0);
    }

    @Test
    public void leanOrRuleScoresIdenticallyToTheOriginal() {
        double[] original = evaluate(5, OPENING, false, false);
        double[] lean = evaluate(5, OPENING, false, true);
        assertArrayEquals("the lean OR rule must not change any evaluation",
            original, lean, 0.0);
    }

    @Test
    public void bothOptimisationsTogetherAreIdenticalToNeither() {
        double[] neither = evaluate(5, OPENING, false, false);
        double[] both = evaluate(5, OPENING, true, true);
        assertArrayEquals("the optimisations must not interact",
            neither, both, 0.0);
    }

    /** 8x8 puts the four board edges at ids 64..67, so the bitmask's high
     *  word carries them: the boundary the two-word representation exists
     *  for, and the one most likely to break silently. */
    @Test
    public void bitmaskHandlesBoardsUsingItsSecondWord() {
        int[][] moves = { {3, 3}, {3, 4}, {4, 3}, {2, 4} };
        double[] objects = evaluate(8, moves, false, true);
        double[] bitmask = evaluate(8, moves, true, true);
        assertArrayEquals("ids above 63 must be handled by the high mask word",
            objects, bitmask, 0.0);
    }

    @Test
    public void evaluationIsReproducible() {
        double[] first = evaluate(5, OPENING, true, true);
        double[] second = evaluate(5, OPENING, true, true);
        assertArrayEquals("the same position must always score the same",
            first, second, 0.0);
    }
}

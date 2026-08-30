package heuristics;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;

/**
 * Path carries the same set of cells in two very different ways: a list of
 * objects, or a 128-bit mask. Every set operation in the search runs
 * through here, so the two must agree exactly.
 *
 * Each test performs the same operations in both representations and
 * compares the answers, rather than asserting hand-written expectations
 * that could be wrong for both.
 */
public class PathRepresentationTest {

    private boolean saved;

    @Before
    public void saveFlag() {
        saved = OptConfig.USE_BITPATH;
    }

    @After
    public void restoreFlag() {
        OptConfig.USE_BITPATH = saved;
    }

    private static Path pathOf(Cell... cells) {
        Path p = new Path();
        for (Cell c : cells) p.add(c);
        return p;
    }

    private static Cell[] cells(int... ids) {
        Cell[] out = new Cell[ids.length];
        for (int i = 0; i < ids.length; i++) out[i] = new Cell(ids[i]);
        return out;
    }

    @Test
    public void lengthAgreesBetweenRepresentations() {
        Cell[] c = cells(0, 5, 17, 40);
        for (boolean bitmask : new boolean[]{false, true}) {
            OptConfig.USE_BITPATH = bitmask;
            assertEquals("length, bitmask=" + bitmask, 4, pathOf(c).getLength());
        }
    }

    /* The board holds exactly one Cell object per id, so a path only ever
     * sees the same instance repeated. (The two representations would
     * disagree on two distinct objects sharing an id: the list form
     * de-duplicates by object, the mask by id. That cannot arise here.) */
    @Test
    public void addingTheSameCellTwiceDoesNotGrowThePath() {
        for (boolean bitmask : new boolean[]{false, true}) {
            OptConfig.USE_BITPATH = bitmask;
            Cell c = new Cell(3);
            assertEquals("duplicates, bitmask=" + bitmask, 1, pathOf(c, c, c).getLength());
        }
    }

    @Test
    public void containsAgreesBetweenRepresentations() {
        for (boolean bitmask : new boolean[]{false, true}) {
            OptConfig.USE_BITPATH = bitmask;
            Cell[] c = cells(2, 9);
            Path p = pathOf(c);
            assertTrue("present, bitmask=" + bitmask, p.contains(c[0]));
            assertFalse("absent, bitmask=" + bitmask, p.contains(new Cell(11)));
        }
    }

    @Test
    public void unionAgreesBetweenRepresentations() {
        for (boolean bitmask : new boolean[]{false, true}) {
            OptConfig.USE_BITPATH = bitmask;
            Cell[] c = cells(1, 2, 3, 4);
            Path union = pathOf(c[0], c[1]).union(pathOf(c[1], c[2]));
            assertEquals("union size, bitmask=" + bitmask, 3, union.getLength());
            assertTrue(union.contains(c[0]));
            assertTrue(union.contains(c[1]));
            assertTrue(union.contains(c[2]));
            assertFalse(union.contains(c[3]));
        }
    }

    @Test
    public void unionThroughACellIncludesThatCell() {
        for (boolean bitmask : new boolean[]{false, true}) {
            OptConfig.USE_BITPATH = bitmask;
            Cell[] c = cells(1, 2, 7);
            Path union = pathOf(c[0]).union(pathOf(c[1]), c[2]);
            assertEquals("size, bitmask=" + bitmask, 3, union.getLength());
            assertTrue("pivot must be carried, bitmask=" + bitmask, union.contains(c[2]));
        }
    }

    @Test
    public void intersectionAgreesBetweenRepresentations() {
        for (boolean bitmask : new boolean[]{false, true}) {
            OptConfig.USE_BITPATH = bitmask;
            Cell[] c = cells(1, 2, 3);
            Path shared = pathOf(c[0], c[1]).intersection(pathOf(c[1], c[2]));
            assertEquals("size, bitmask=" + bitmask, 1, shared.getLength());
            assertTrue(shared.contains(c[1]));
        }
    }

    @Test
    public void emptyIntersectionIsDetected() {
        for (boolean bitmask : new boolean[]{false, true}) {
            OptConfig.USE_BITPATH = bitmask;
            Cell[] c = cells(1, 2, 3, 4);
            assertTrue("disjoint, bitmask=" + bitmask,
                pathOf(c[0], c[1]).hasEmptyIntersection(pathOf(c[2], c[3])));
            assertFalse("overlapping, bitmask=" + bitmask,
                pathOf(c[0], c[1]).hasEmptyIntersection(pathOf(c[1], c[3])));
        }
    }

    @Test
    public void equalityIgnoresInsertionOrder() {
        for (boolean bitmask : new boolean[]{false, true}) {
            OptConfig.USE_BITPATH = bitmask;
            Cell[] c = cells(4, 8, 15);
            assertTrue("order must not matter, bitmask=" + bitmask,
                pathOf(c[0], c[1], c[2]).equals(pathOf(c[2], c[0], c[1])));
            assertFalse("different contents, bitmask=" + bitmask,
                pathOf(c[0], c[1]).equals(pathOf(c[0], c[2])));
        }
    }

    /** Ids of 64 and above live in the mask's second word; a path spanning
     *  the boundary is where a one-word implementation would fail. */
    @Test
    public void cellsAboveTheWordBoundaryAreDistinct() {
        OptConfig.USE_BITPATH = true;

        // A path holding only id 69 must not appear to hold id 69-64 = 5
        Path high = pathOf(new Cell(69));
        assertEquals(1, high.getLength());
        assertTrue("id 69 belongs to the high word", high.contains(new Cell(69)));
        assertFalse("id 69 must not alias id 5", high.contains(new Cell(5)));

        // and a path spanning both words keeps them apart
        Path spanning = pathOf(new Cell(5), new Cell(69));
        assertEquals(2, spanning.getLength());
        assertFalse("unrelated high id", spanning.contains(new Cell(70)));
    }

    @Test
    public void aDirectPathIsEmptyAndEqualsAnotherDirectPath() {
        for (boolean bitmask : new boolean[]{false, true}) {
            OptConfig.USE_BITPATH = bitmask;
            Path a = new Path(), b = new Path();
            a.makeDirect();
            b.makeDirect();
            assertTrue("direct paths carry no cells, bitmask=" + bitmask, a.isEmpty());
            assertTrue("direct paths are interchangeable, bitmask=" + bitmask, a.equals(b));
        }
    }
}

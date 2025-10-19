package heuristics;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.util.Iterator;

/**
 * Test suite for the Path class.
 * Tests path operations including union, intersection, contains, and direct paths.
 */
public class PathTest {
    private Path path1;
    private Path path2;
    private Cell cell1;
    private Cell cell2;
    private Cell cell3;
    private Cell cell4;

    @Before
    public void setUp() {
        path1 = new Path();
        path2 = new Path();

        cell1 = new Cell(1);
        cell2 = new Cell(2);
        cell3 = new Cell(3);
        cell4 = new Cell(4);
    }

    @Test
    public void testPathCreation_EmptyPath() {
        // Newly created path should be empty
        assertTrue(path1.isEmpty());
        assertEquals(0, path1.getLength());
    }

    @Test
    public void testPathCreation_IsNew() {
        // Newly created path should be marked as new
        assertTrue(path1.isNew());
    }

    @Test
    public void testAdd_SingleCell() {
        // Add a cell to the path
        path1.add(cell1);
        assertEquals(1, path1.getLength());
        assertTrue(path1.contains(cell1));
    }

    @Test
    public void testAdd_MultipleCells() {
        // Add multiple cells to the path
        path1.add(cell1);
        path1.add(cell2);
        path1.add(cell3);

        assertEquals(3, path1.getLength());
        assertTrue(path1.contains(cell1));
        assertTrue(path1.contains(cell2));
        assertTrue(path1.contains(cell3));
    }

    @Test
    public void testAdd_DuplicateCell() {
        // Adding the same cell twice should not increase length
        path1.add(cell1);
        path1.add(cell1);

        assertEquals(1, path1.getLength());
    }

    @Test
    public void testContains_ExistingCell() {
        // Path should contain added cell
        path1.add(cell1);
        assertTrue(path1.contains(cell1));
    }

    @Test
    public void testContains_NonExistingCell() {
        // Path should not contain cell that wasn't added
        path1.add(cell1);
        assertFalse(path1.contains(cell2));
    }

    @Test
    public void testUnion_TwoPaths() {
        // Test union of two paths
        path1.add(cell1);
        path1.add(cell2);

        path2.add(cell3);
        path2.add(cell4);

        Path union = path1.union(path2);

        assertEquals(4, union.getLength());
        assertTrue(union.contains(cell1));
        assertTrue(union.contains(cell2));
        assertTrue(union.contains(cell3));
        assertTrue(union.contains(cell4));
    }

    @Test
    public void testUnion_OverlappingPaths() {
        // Test union of paths with overlapping cells
        path1.add(cell1);
        path1.add(cell2);

        path2.add(cell2);
        path2.add(cell3);

        Path union = path1.union(path2);

        assertEquals(3, union.getLength());
        assertTrue(union.contains(cell1));
        assertTrue(union.contains(cell2));
        assertTrue(union.contains(cell3));
    }

    @Test
    public void testUnion_WithCell() {
        // Test union with an intercalated cell
        path1.add(cell1);
        path1.add(cell2);

        path2.add(cell3);

        Path union = path1.union(path2, cell4);

        assertEquals(4, union.getLength());
        assertTrue(union.contains(cell1));
        assertTrue(union.contains(cell2));
        assertTrue(union.contains(cell3));
        assertTrue(union.contains(cell4));
    }

    @Test
    public void testIntersection_OverlappingPaths() {
        // Test intersection of paths with common cells
        path1.add(cell1);
        path1.add(cell2);
        path1.add(cell3);

        path2.add(cell2);
        path2.add(cell3);
        path2.add(cell4);

        Path intersection = path1.intersection(path2);

        assertEquals(2, intersection.getLength());
        assertTrue(intersection.contains(cell2));
        assertTrue(intersection.contains(cell3));
    }

    @Test
    public void testIntersection_DisjointPaths() {
        // Test intersection of paths with no common cells
        path1.add(cell1);
        path1.add(cell2);

        path2.add(cell3);
        path2.add(cell4);

        Path intersection = path1.intersection(path2);

        assertEquals(0, intersection.getLength());
        assertTrue(intersection.isEmpty());
    }

    @Test
    public void testIntersection_EmptyResult() {
        // Test intersection where one path is empty
        path1.add(cell1);

        Path intersection = path1.intersection(path2);

        assertTrue(intersection.isEmpty());
    }

    @Test
    public void testHasEmptyIntersection_True() {
        // Test paths with no overlap
        path1.add(cell1);
        path1.add(cell2);

        path2.add(cell3);
        path2.add(cell4);

        assertTrue(path1.hasEmptyIntersection(path2));
    }

    @Test
    public void testHasEmptyIntersection_False() {
        // Test paths with overlap
        path1.add(cell1);
        path1.add(cell2);

        path2.add(cell2);
        path2.add(cell3);

        assertFalse(path1.hasEmptyIntersection(path2));
    }

    @Test
    public void testEquals_SamePaths() {
        // Test equality of paths with same cells
        path1.add(cell1);
        path1.add(cell2);

        path2.add(cell2);
        path2.add(cell1);

        assertTrue(path1.equals(path2));
    }

    @Test
    public void testEquals_DifferentPaths() {
        // Test inequality of paths with different cells
        path1.add(cell1);
        path1.add(cell2);

        path2.add(cell3);
        path2.add(cell4);

        assertFalse(path1.equals(path2));
    }

    @Test
    public void testEquals_DifferentLengths() {
        // Test inequality of paths with different lengths
        path1.add(cell1);
        path1.add(cell2);

        path2.add(cell1);

        assertFalse(path1.equals(path2));
    }

    @Test
    public void testDirectPath_Creation() {
        // Test creating a direct path
        path1.makeDirect();

        assertTrue(path1.isDirect());
        assertTrue(path1.isEmpty());
    }

    @Test
    public void testDirectPath_Union() {
        // Test union of two direct paths
        path1.makeDirect();
        path2.makeDirect();

        Path union = path1.union(path2);

        assertTrue(union.isDirect());
    }

    @Test
    public void testDirectPath_Equals() {
        // Test equality of direct paths
        path1.makeDirect();
        path2.makeDirect();

        assertTrue(path1.equals(path2));
    }

    @Test
    public void testChangeNew_ToFalse() {
        // Test changing new attribute to false
        assertTrue(path1.isNew());

        path1.changeNew(false);

        assertFalse(path1.isNew());
    }

    @Test
    public void testChangeNew_ToTrue() {
        // Test changing new attribute to true
        path1.changeNew(false);
        assertFalse(path1.isNew());

        path1.changeNew(true);
        assertTrue(path1.isNew());
    }

    @Test
    public void testGetLength_EmptyPath() {
        // Test length of empty path
        assertEquals(0, path1.getLength());
    }

    @Test
    public void testGetLength_AfterAdding() {
        // Test length after adding cells
        path1.add(cell1);
        assertEquals(1, path1.getLength());

        path1.add(cell2);
        assertEquals(2, path1.getLength());

        path1.add(cell3);
        assertEquals(3, path1.getLength());
    }

    @Test
    public void testIterator_EmptyPath() {
        // Test iterator on empty path
        Iterator iter = path1.getIterator();
        assertNotNull(iter);
        assertFalse(iter.hasNext());
    }

    @Test
    public void testIterator_WithCells() {
        // Test iterator with cells
        path1.add(cell1);
        path1.add(cell2);

        Iterator iter = path1.getIterator();
        assertNotNull(iter);
        assertTrue(iter.hasNext());

        int count = 0;
        while (iter.hasNext()) {
            iter.next();
            count++;
        }
        assertEquals(2, count);
    }

    @Test
    public void testComplexUnion_MultiplePaths() {
        // Test complex union scenario
        path1.add(cell1);
        path1.add(cell2);

        Path path3 = new Path();
        path3.add(cell3);
        path3.add(cell4);

        Path union1 = path1.union(path2);
        Path union2 = union1.union(path3);

        assertTrue(union2.contains(cell1));
        assertTrue(union2.contains(cell2));
        assertTrue(union2.contains(cell3));
        assertTrue(union2.contains(cell4));
    }
}

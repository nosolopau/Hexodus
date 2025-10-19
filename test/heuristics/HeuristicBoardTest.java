package heuristics;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.util.ArrayList;

/**
 * Test suite for the heuristics.Board class.
 * Tests virtual board creation, neighbor detection, and connection management.
 */
public class HeuristicBoardTest {
    private Board board3;
    private Board board5;
    private Board board7;

    @Before
    public void setUp() {
        board3 = new Board(3);
        board5 = new Board(5);
        board7 = new Board(7);
    }

    @Test
    public void testBoardCreation_Dimension3() {
        // Test creating a 3x3 board
        assertNotNull(board3);
        assertEquals(3, board3.getDimension());
    }

    @Test
    public void testBoardCreation_Dimension5() {
        // Test creating a 5x5 board
        assertNotNull(board5);
        assertEquals(5, board5.getDimension());
    }

    @Test
    public void testBoardCreation_Dimension7() {
        // Test creating a 7x7 board
        assertNotNull(board7);
        assertEquals(7, board7.getDimension());
    }

    @Test
    public void testGetSquare_ValidPosition() {
        // Test getting a square at valid position
        Square square = board5.get(2, 2);
        assertNotNull(square);
        assertEquals(2, square.getRow());
        assertEquals(2, square.getColumn());
    }

    @Test
    public void testGetSquare_CornerPositions() {
        // Test getting squares at all corners
        Square topLeft = board5.get(0, 0);
        Square topRight = board5.get(0, 4);
        Square bottomLeft = board5.get(4, 0);
        Square bottomRight = board5.get(4, 4);

        assertNotNull(topLeft);
        assertNotNull(topRight);
        assertNotNull(bottomLeft);
        assertNotNull(bottomRight);

        assertEquals(0, topLeft.getRow());
        assertEquals(0, topLeft.getColumn());
    }

    @Test
    public void testGetSquare_EdgePositions() {
        // Test getting squares at edge positions
        Square topEdge = board5.get(0, 2);
        Square bottomEdge = board5.get(4, 2);
        Square leftEdge = board5.get(2, 0);
        Square rightEdge = board5.get(2, 4);

        assertNotNull(topEdge);
        assertNotNull(bottomEdge);
        assertNotNull(leftEdge);
        assertNotNull(rightEdge);
    }

    @Test
    public void testGetConnections_NotNull() {
        // Test that connections are initialized
        Connections connections = board5.getConnections();
        assertNotNull(connections);
    }

    @Test
    public void testGetExpireList_NotNull() {
        // Test that expire list is initialized
        ArrayList expireList = board5.getExpireList();
        assertNotNull(expireList);
    }

    @Test
    public void testGetExpireList_NotEmpty() {
        // Test that expire list contains paths for initialized boards
        ArrayList expireList = board3.getExpireList();
        assertNotNull(expireList);
        assertTrue(expireList.size() > 0);
    }

    @Test
    public void testGenerateG_Player1() {
        // Test generating G set for player 1
        ArrayList<Cell> g = board5.generateG(1);
        assertNotNull(g);
        assertTrue(g.size() > 0);
    }

    @Test
    public void testGenerateG_Player2() {
        // Test generating G set for player 2 (color 0)
        ArrayList<Cell> g = board5.generateG(0);
        assertNotNull(g);
        assertTrue(g.size() > 0);
    }

    @Test
    public void testGenerateG_EmptyBoard() {
        // On empty board, G should contain all squares plus borders
        ArrayList<Cell> g = board3.generateG(1);
        // 3x3 board = 9 squares + 2 borders (north and south for player 1)
        assertTrue(g.size() >= 11);
    }

    @Test
    public void testGenerateG_OccupiedSquares() {
        // Test G generation with occupied squares
        Square square = board5.get(2, 2);
        square.occupy(1); // Occupy for player 1

        ArrayList<Cell> g1 = board5.generateG(1);
        ArrayList<Cell> g2 = board5.generateG(0);

        // Player 1's G should include the occupied square
        assertTrue(g1.contains(square));
        // Player 2's G should not include the square occupied by player 1
        assertFalse(g2.contains(square));
    }

    @Test
    public void testGetFreeCells_EmptyBoard() {
        // Test getting free cells on empty board
        ArrayList<Square> freeCells = board5.getCellsLibres();
        assertNotNull(freeCells);
        assertEquals(25, freeCells.size()); // 5x5 = 25 squares
    }

    @Test
    public void testGetFreeCells_AfterOccupying() {
        // Test getting free cells after occupying some
        Square square1 = board5.get(2, 2);
        Square square2 = board5.get(3, 3);

        square1.occupy(1);
        square2.occupy(0);

        ArrayList<Square> freeCells = board5.getCellsLibres();
        assertEquals(23, freeCells.size()); // 25 - 2 = 23 free squares
    }

    @Test
    public void testSquareEquals_SamePosition() {
        // Test square equality
        Square square1 = board5.get(2, 2);
        Square square2 = new Square(2, 2, 100); // Different ID, same position

        assertTrue(square1.equals(square2));
    }

    @Test
    public void testSquareEquals_DifferentPosition() {
        // Test square inequality
        Square square1 = board5.get(2, 2);
        Square square2 = board5.get(3, 3);

        assertFalse(square1.equals(square2));
    }

    @Test
    public void testCellNeighbors() {
        // Test that cells have neighbors
        Square center = board5.get(2, 2);
        ArrayList<Cell> neighbors = center.getNeighborList();

        assertNotNull(neighbors);
        // Center square in hex board should have 6 neighbors
        assertEquals(6, neighbors.size());
    }

    @Test
    public void testCornerCellNeighbors() {
        // Test that corner cells have fewer neighbors
        Square corner = board5.get(0, 0);
        ArrayList<Cell> neighbors = corner.getNeighborList();

        assertNotNull(neighbors);
        // Corner square should have fewer neighbors (3 in hex)
        assertEquals(3, neighbors.size());
    }

    @Test
    public void testEdgeCellNeighbors() {
        // Test that edge cells have appropriate number of neighbors
        Square edge = board5.get(0, 2);
        ArrayList<Cell> neighbors = edge.getNeighborList();

        assertNotNull(neighbors);
        // Edge square (not corner) should have 4 neighbors
        assertEquals(4, neighbors.size());
    }

    @Test
    public void testBoardDimension_Consistency() {
        // Test dimension consistency
        assertEquals(3, board3.getDimension());
        assertEquals(5, board5.getDimension());
        assertEquals(7, board7.getDimension());
    }

    @Test
    public void testSmallBoard_Dimension3() {
        // Test all aspects of a 3x3 board
        assertEquals(3, board3.getDimension());

        ArrayList<Square> freeCells = board3.getCellsLibres();
        assertEquals(9, freeCells.size());

        Square center = board3.get(1, 1);
        assertNotNull(center);
        assertEquals(1, center.getRow());
        assertEquals(1, center.getColumn());
    }

    @Test
    public void testLargeBoard_Dimension7() {
        // Test all aspects of a 7x7 board
        assertEquals(7, board7.getDimension());

        ArrayList<Square> freeCells = board7.getCellsLibres();
        assertEquals(49, freeCells.size());

        Square center = board7.get(3, 3);
        assertNotNull(center);
    }
}

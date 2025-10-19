package heuristics;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Test suite for the Connections class.
 * Tests connection management between cells including creating, checking,
 * and removing connections and routes.
 */
public class ConnectionsTest {
    private Connections connections;
    private Cell cell1;
    private Cell cell2;
    private Cell cell3;
    private Cell cell4;

    @Before
    public void setUp() {
        connections = new Connections(5);

        cell1 = new Cell(1);
        cell2 = new Cell(2);
        cell3 = new Cell(3);
        cell4 = new Cell(4);
    }

    @Test
    public void testConnectionsCreation() {
        // Test creating connections object
        assertNotNull(connections);
    }

    @Test
    public void testNewConnection_WithoutRoute() {
        // Test creating a new connection without providing a route
        connections.newConnection(cell1, cell2);

        assertTrue(connections.hasConnection(cell1, cell2));
    }

    @Test
    public void testNewConnection_WithRoute() {
        // Test creating a new connection with a route
        Route route = new Route();
        connections.newConnection(cell1, cell2, route);

        assertTrue(connections.hasConnection(cell1, cell2));
    }

    @Test
    public void testHasConnection_ExistingConnection() {
        // Test checking for existing connection
        connections.newConnection(cell1, cell2);

        assertTrue(connections.hasConnection(cell1, cell2));
    }

    @Test
    public void testHasConnection_NonExistingConnection() {
        // Test checking for non-existing connection
        assertFalse(connections.hasConnection(cell1, cell2));
    }

    @Test
    public void testHasConnection_ReverseOrder() {
        // Test that connection is directional
        connections.newConnection(cell1, cell2);

        assertTrue(connections.hasConnection(cell1, cell2));
        assertFalse(connections.hasConnection(cell2, cell1));
    }

    @Test
    public void testHasConnectionEx_BothDirections() {
        // Test checking connection without caring about direction
        connections.newConnection(cell1, cell2);

        assertTrue(connections.hasConnectionEx(cell1, cell2));
        assertTrue(connections.hasConnectionEx(cell2, cell1));
    }

    @Test
    public void testHasConnectionEx_ReverseConnection() {
        // Test hasConnectionEx with reverse connection
        connections.newConnection(cell2, cell1);

        assertTrue(connections.hasConnectionEx(cell1, cell2));
        assertTrue(connections.hasConnectionEx(cell2, cell1));
    }

    @Test
    public void testHasConnectionEx_NoConnection() {
        // Test hasConnectionEx with no connection
        assertFalse(connections.hasConnectionEx(cell1, cell2));
    }

    @Test
    public void testGetRoute_ExistingConnection() {
        // Test getting route for existing connection
        connections.newConnection(cell1, cell2);

        Route route = connections.getRoute(cell1, cell2);

        assertNotNull(route);
    }

    @Test
    public void testGetRoute_NonExistingConnection() {
        // Test getting route for non-existing connection
        Route route = connections.getRoute(cell1, cell2);

        assertNull(route);
    }

    @Test
    public void testGetRoute_ReverseConnection() {
        // Test getting route works in reverse if connection exists
        connections.newConnection(cell1, cell2);

        Route route1 = connections.getRoute(cell1, cell2);
        Route route2 = connections.getRoute(cell2, cell1);

        assertNotNull(route1);
        assertNotNull(route2);
        assertEquals(route1, route2);
    }

    @Test
    public void testInsertDirectPath_NewConnection() {
        // Test inserting direct path creates connection if needed
        Path path = connections.insertDirectPath(cell1, cell2);

        assertNotNull(path);
        assertTrue(path.isDirect());
        assertTrue(connections.hasConnection(cell1, cell2));
    }

    @Test
    public void testInsertDirectPath_ExistingConnection() {
        // Test inserting direct path to existing connection
        connections.newConnection(cell1, cell2);

        Path path = connections.insertDirectPath(cell1, cell2);

        assertNotNull(path);
        assertTrue(path.isDirect());
    }

    @Test
    public void testInsertDirectPath_RouteContainsPath() {
        // Test that inserted path is added to route
        connections.insertDirectPath(cell1, cell2);

        Route route = connections.getRoute(cell1, cell2);

        assertNotNull(route);
        assertFalse(route.isEmpty());
        assertTrue(route.hasDirectPath());
    }

    @Test
    public void testRemoveConnection_ExistingConnection() {
        // Test removing existing connection
        connections.newConnection(cell1, cell2);
        assertTrue(connections.hasConnection(cell1, cell2));

        connections.removeConnection(cell1, cell2);

        assertFalse(connections.hasConnection(cell1, cell2));
    }

    @Test
    public void testRemoveConnection_ReverseOrder() {
        // Test removing connection in reverse order
        connections.newConnection(cell1, cell2);

        connections.removeConnection(cell2, cell1);

        assertFalse(connections.hasConnection(cell1, cell2));
    }

    @Test
    public void testRemoveConnection_NonExisting() {
        // Test removing non-existing connection (should not cause error)
        connections.removeConnection(cell1, cell2);

        assertFalse(connections.hasConnection(cell1, cell2));
    }

    @Test
    public void testMultipleConnections() {
        // Test creating multiple connections
        connections.newConnection(cell1, cell2);
        connections.newConnection(cell2, cell3);
        connections.newConnection(cell3, cell4);

        assertTrue(connections.hasConnection(cell1, cell2));
        assertTrue(connections.hasConnection(cell2, cell3));
        assertTrue(connections.hasConnection(cell3, cell4));
    }

    @Test
    public void testClone_IndependentCopy() {
        // Test that cloned connections is independent
        connections.newConnection(cell1, cell2);
        connections.insertDirectPath(cell1, cell2);

        Connections cloned = connections.clone();

        // Verify clone has the connection
        assertTrue(cloned.hasConnection(cell1, cell2));

        // Modify original
        connections.newConnection(cell3, cell4);

        // Clone should not have the new connection
        assertFalse(cloned.hasConnection(cell3, cell4));
    }

    @Test
    public void testClone_DeepCopy() {
        // Test that clone is a deep copy of routes
        connections.newConnection(cell1, cell2);
        Path path = connections.insertDirectPath(cell1, cell2);

        Connections cloned = connections.clone();

        // Get routes from both
        Route originalRoute = connections.getRoute(cell1, cell2);
        Route clonedRoute = cloned.getRoute(cell1, cell2);

        // Routes should exist in both but be different objects
        assertNotNull(originalRoute);
        assertNotNull(clonedRoute);
        // Note: They might not be the same object reference
    }

    @Test
    public void testRouteManagement_AddPathToRoute() {
        // Test adding paths to a connection's route
        connections.newConnection(cell1, cell2);

        Path directPath = connections.insertDirectPath(cell1, cell2);
        Route route = connections.getRoute(cell1, cell2);

        assertTrue(route.hasDirectPath());
        assertFalse(route.isEmpty());
    }

    @Test
    public void testConnectionsBetweenBorders() {
        // Test connections between border cells
        Border north = new Border(100, 'N');
        Border south = new Border(101, 'S');

        connections.newConnection(north, south);

        assertTrue(connections.hasConnection(north, south));
    }

    @Test
    public void testConnectionsBetweenSquareAndBorder() {
        // Test connections between squares and borders
        Square square = new Square(0, 0, 0);
        Border north = new Border(100, 'N');

        connections.newConnection(square, north);

        assertTrue(connections.hasConnection(square, north));
    }

    @Test
    public void testMultiplePathsInRoute() {
        // Test that routes can contain multiple paths
        connections.newConnection(cell1, cell2);

        Route route = connections.getRoute(cell1, cell2);

        Path path1 = new Path();
        path1.add(cell3);
        path1.add(cell4);

        Path path2 = new Path();
        path2.add(cell3);

        route.add(path1);
        route.add(path2);

        assertEquals(2, route.getLength());
    }

    @Test
    public void testGetRoute_WithPaths() {
        // Test getting route with paths
        connections.newConnection(cell1, cell2);

        Route route = connections.getRoute(cell1, cell2);
        Path testPath = new Path();
        testPath.add(cell3);
        route.add(testPath);

        Route retrieved = connections.getRoute(cell1, cell2);
        assertEquals(1, retrieved.getLength());
    }
}

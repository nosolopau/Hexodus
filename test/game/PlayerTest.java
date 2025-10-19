package game;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Test suite for the Player class.
 * Tests player creation, state management, and property access.
 */
public class PlayerTest {

    @Test
    public void testPlayerCreation_WithName() {
        // Test creating a player with name
        Player player = new Player("Alice", 0, 1);

        assertEquals("Alice", player.getName());
        assertEquals(0, player.getType());
        assertEquals(1, player.getPosition());
        assertEquals(1, player.getColor());
    }

    @Test
    public void testPlayerCreation_WithoutName() {
        // Test creating a player without name (default names)
        Player player1 = new Player(0, 1);
        Player player2 = new Player(0, 2);

        assertEquals("vertical player", player1.getName());
        assertEquals("horizontal player", player2.getName());
    }

    @Test
    public void testPlayerType_Human() {
        // Test human player type (type = 0)
        Player human = new Player("Human", 0, 1);

        assertEquals(0, human.getType());
        assertFalse(human.isComputer());
    }

    @Test
    public void testPlayerType_Computer() {
        // Test computer player type (type = 1)
        Player computer = new Player("Computer", 1, 1);

        assertEquals(1, computer.getType());
        assertTrue(computer.isComputer());
    }

    @Test
    public void testPlayerPosition_Vertical() {
        // Test player with vertical position (position = 1)
        Player player = new Player("Player1", 0, 1);

        assertEquals(1, player.getPosition());
        assertEquals(1, player.getColor());
    }

    @Test
    public void testPlayerPosition_Horizontal() {
        // Test player with horizontal position (position = 2)
        Player player = new Player("Player2", 0, 2);

        assertEquals(2, player.getPosition());
        assertEquals(2, player.getColor());
    }

    @Test
    public void testPlayerColor_Position1() {
        // Position 1 should give color 1
        Player player = new Player("Test", 0, 1);
        assertEquals(1, player.getColor());
    }

    @Test
    public void testPlayerColor_Position2() {
        // Position 2 should give color 2
        Player player = new Player("Test", 0, 2);
        assertEquals(2, player.getColor());
    }

    @Test
    public void testPlayerDefaultName_Vertical() {
        // Test default name for vertical player
        Player player = new Player(0, 1);
        assertEquals("vertical player", player.getName());
    }

    @Test
    public void testPlayerDefaultName_Horizontal() {
        // Test default name for horizontal player
        Player player = new Player(0, 2);
        assertEquals("horizontal player", player.getName());
    }

    @Test
    public void testMultiplePlayers_DifferentTypes() {
        // Test creating multiple players with different types
        Player human = new Player("Human", 0, 1);
        Player computer = new Player("Computer", 1, 2);

        assertFalse(human.isComputer());
        assertTrue(computer.isComputer());

        assertEquals(1, human.getPosition());
        assertEquals(2, computer.getPosition());
    }

    @Test
    public void testPlayerProperties_Consistency() {
        // Test that all player properties are consistent
        Player player = new Player("TestPlayer", 1, 1);

        assertEquals("TestPlayer", player.getName());
        assertEquals(1, player.getType());
        assertEquals(1, player.getPosition());
        assertEquals(1, player.getColor());
        assertTrue(player.isComputer());
    }

    @Test
    public void testTwoPlayers_DifferentColors() {
        // Test that two players have different colors based on position
        Player player1 = new Player("P1", 0, 1);
        Player player2 = new Player("P2", 0, 2);

        assertNotEquals(player1.getColor(), player2.getColor());
        assertEquals(1, player1.getColor());
        assertEquals(2, player2.getColor());
    }

    @Test
    public void testPlayerName_SpecialCharacters() {
        // Test player name with special characters
        Player player = new Player("Player-123_ABC", 0, 1);
        assertEquals("Player-123_ABC", player.getName());
    }

    @Test
    public void testPlayerName_EmptyString() {
        // Test player with empty name
        Player player = new Player("", 0, 1);
        assertEquals("", player.getName());
    }
}

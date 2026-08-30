package ui;

import java.util.prefs.Preferences;

/**
 *  Persists the last New Game selections (board size, difficulty,
 *  algorithm and swap rule) across sessions, using the platform
 *  user-preferences store (no configuration file involved).
 *
 *  @author Pau
 *  @version 1.0
 */
final class GamePrefs {
    private static final Preferences PREFS = Preferences.userNodeForPackage(GamePrefs.class);

    static final String DIMENSION  = "dimensionIndex";   // Index into Main.DIMENSIONS
    static final String DIFFICULTY = "difficultyIndex";  // Combo index: 0=Normal, 1=Expert, 2=Master
    static final String ALGORITHM  = "algorithmIndex";   // Combo index: 0=Object-Oriented, 1=Bitmask
    static final String SWAP       = "swapRule";
    static final String SHOW_THINKING = "showThinking";  // Analysis overlay on the board
    static final String SHOW_LINKS    = "showLinks";     // Connection skeleton on the board
    /* Player types are intentionally not persisted: the game always opens
     * on human vs computer so it never starts playing itself on launch. */

    static int get(String key, int def){
        return PREFS.getInt(key, def);
    }

    static boolean get(String key, boolean def){
        return PREFS.getBoolean(key, def);
    }

    static void put(String key, int value){
        PREFS.putInt(key, value);
    }

    static void put(String key, boolean value){
        PREFS.putBoolean(key, value);
    }

    private GamePrefs(){
    }
}

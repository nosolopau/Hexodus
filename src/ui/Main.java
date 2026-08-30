package ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.*;
import game.*;

/** Represents the application control module and the main window
 *  @author Pau
 *  @version 1.0
 */
public class Main{

    /** Board sizes offered in the New Game dialog, in menu order. Each one
     *  needs matching artwork at images/<dim>.png; the window layout is
     *  derived from the dimension in GameWindow.newGame, so no per-size
     *  layout entry is required. */
    static final int[] DIMENSIONS = {5, 6, 7, 8, 9};

    /** Window background, matching the margin colour baked into the board
     *  artwork so the board sits on one continuous surface rather than a
     *  visible rectangle on white. */
    static final Color BACKGROUND = new Color(0xF7, 0xF5, 0xF1);

    /** Index into DIMENSIONS used when nothing is remembered yet (6x6) */
    static final int DEFAULT_DIMENSION_INDEX = 1;

    /** Creates a new instance of the main class Main */
    public Main(){
    }
    
    /** Starts the program execution
     *  @param args Command line arguments */
    public static void main(String[] args){
        // Capture stack traces (including any thrown on the Swing thread)
        ErrorLog.install();

        // Fonts and colours for menus, dialogs and labels
        Theme.apply();

        /* The startup game honors the selections remembered from the last
         * New Game dialog (defaults: 6x6, human vs computer, swap on,
         * Normal difficulty, Bitmask H-Search). */
        heuristics.OptConfig.USE_BITPATH = (GamePrefs.get(GamePrefs.ALGORITHM, 1) == 1);

        int dimIndex = GamePrefs.get(GamePrefs.DIMENSION, DEFAULT_DIMENSION_INDEX);
        if(dimIndex < 0 || dimIndex >= DIMENSIONS.length) dimIndex = DEFAULT_DIMENSION_INDEX;
        int dim = DIMENSIONS[dimIndex];

        int difficulty = GamePrefs.get(GamePrefs.DIFFICULTY, 0) + 1;
        if(difficulty < 1 || difficulty > 3) difficulty = 1;

        /* Player types are deliberately NOT restored: launching into a
         * remembered computer-vs-computer setup would start a game playing
         * itself before the user has done anything. Startup is always
         * human (vertical) vs computer (horizontal). */
        GameWindow game = new GameWindow(dim, 0, 1,
            GamePrefs.get(GamePrefs.SWAP, true),
            difficulty);

        WindowCloseHandler window = new WindowCloseHandler();
        game.addWindowListener(window);
    }
}


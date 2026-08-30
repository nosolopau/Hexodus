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

/** Options dialog box for creating a new game */
class OptionsDialog extends JDialog{
    private JRadioButton v1, v2, h1, h2;    // References to the controls
    private JComboBox selDimension;
    private JComboBox selDifficulty;
    private JComboBox selAlgorithm;
    private JCheckBox enableSwap;
    private GameWindow game;

    /** Shows the dialog to create a new match
     *  @param principal    Reference to the main game window */
    public OptionsDialog(GameWindow principal){
        super(principal, "New Game", true);
        setResizable(false);

        game = principal;

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(Theme.BACKGROUND);
        content.setBorder(new EmptyBorder(18, 22, 16, 22));
        setContentPane(content);

        /* ---- controls ---- */

        selDimension = new JComboBox();
        for(int i = 0; i < Main.DIMENSIONS.length; i++)
            selDimension.addItem(Main.DIMENSIONS[i] + " x " + Main.DIMENSIONS[i]);
        selDimension.setToolTipText("<html>Thinking time grows steeply with board size.<br>"
            + "8x8 and 9x9 are comfortable on Normal, but take<br>"
            + "minutes per move on Expert and Master.</html>");
        selDimension.setSelectedIndex(restoreIndex(GamePrefs.DIMENSION,
            Main.DEFAULT_DIMENSION_INDEX, selDimension.getItemCount()));

        selDifficulty = new JComboBox();
        selDifficulty.addItem("Normal  \u00b7  searches 1 move ahead");
        selDifficulty.addItem("Expert  \u00b7  searches 2 moves ahead");
        selDifficulty.addItem("Master  \u00b7  searches 3 moves ahead");
        selDifficulty.setToolTipText("Deeper search plays better but takes longer.");
        selDifficulty.setSelectedIndex(restoreIndex(GamePrefs.DIFFICULTY, 0,
            selDifficulty.getItemCount()));

        selAlgorithm = new JComboBox();
        selAlgorithm.addItem("Object-Oriented H-Search");
        selAlgorithm.addItem("Bitmask H-Search");
        selAlgorithm.setToolTipText("<html>Both play identical moves.<br>"
            + "Bitmask H-Search analyses about 5x faster.</html>");
        selAlgorithm.setSelectedIndex(restoreIndex(GamePrefs.ALGORITHM, 1,
            selAlgorithm.getItemCount()));

        enableSwap = new JCheckBox("Enable swap rule");
        enableSwap.setOpaque(false);
        enableSwap.setToolTipText("<html>Lets the second player take over the first<br>"
            + "player's opening move, offsetting the advantage<br>of moving first.</html>");
        enableSwap.setSelected(GamePrefs.get(GamePrefs.SWAP, true));

        ButtonGroup verticalGroup = new ButtonGroup();
        ButtonGroup horizontalGroup = new ButtonGroup();
        v1 = radio("Human");
        v2 = radio("Computer");
        h1 = radio("Human");
        h2 = radio("Computer");
        /* Always opens on human vs computer: computer-vs-computer is a
         * demo mode, chosen deliberately rather than inherited from the
         * previous game. */
        v1.setSelected(true);
        h2.setSelected(true);
        verticalGroup.add(v1);
        verticalGroup.add(v2);
        horizontalGroup.add(h1);
        horizontalGroup.add(h2);

        /* ---- layout: label column, field column ---- */

        GridBagConstraints label = new GridBagConstraints();
        label.gridx = 0;
        label.anchor = GridBagConstraints.LINE_END;
        label.insets = new Insets(4, 0, 4, 10);

        GridBagConstraints field = new GridBagConstraints();
        field.gridx = 1;
        field.fill = GridBagConstraints.HORIZONTAL;
        field.weightx = 1.0;
        field.insets = new Insets(4, 0, 4, 0);

        GridBagConstraints wide = new GridBagConstraints();
        wide.gridx = 0;
        wide.gridwidth = 2;
        wide.fill = GridBagConstraints.HORIZONTAL;
        wide.anchor = GridBagConstraints.LINE_START;

        int row = 0;

        row = section(content, "Board", row, wide, true);
        label.gridy = field.gridy = row++;
        content.add(new JLabel("Size"), label);
        content.add(selDimension, field);
        label.gridy = field.gridy = row++;
        content.add(new JLabel("Difficulty"), label);
        content.add(selDifficulty, field);

        row = section(content, "Engine", row, wide, false);
        label.gridy = field.gridy = row++;
        content.add(new JLabel("Algorithm"), label);
        content.add(selAlgorithm, field);

        row = section(content, "Players", row, wide, false);
        wide.gridy = row++;
        wide.insets = new Insets(1, 0, 1, 0);
        content.add(playerRow(Theme.RED, "Vertical", v1, v2), wide);
        wide.gridy = row++;
        content.add(playerRow(Theme.BLUE, "Horizontal", h1, h2), wide);

        row = section(content, "Rules", row, wide, false);
        wide.gridy = row++;
        wide.insets = new Insets(2, 0, 2, 0);
        content.add(enableSwap, wide);

        /* ---- actions ---- */

        JButton start = new JButton("Start Game");
        start.addActionListener(new AcceptHandler());
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){ dispose(); }
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(cancel);
        buttons.add(start);

        wide.gridy = row++;
        wide.insets = new Insets(18, 0, 0, 0);
        wide.anchor = GridBagConstraints.LINE_END;
        content.add(buttons, wide);

        getRootPane().setDefaultButton(start);          // Enter starts the game
        getRootPane().registerKeyboardAction(new ActionListener(){   // Escape closes
                public void actionPerformed(ActionEvent e){ dispose(); }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);

        pack();
        setLocationRelativeTo(principal);
    }

    /** A radio button that lets the dialog background show through */
    private static JRadioButton radio(String text){
        JRadioButton b = new JRadioButton(text);
        b.setOpaque(false);
        return b;
    }

    /** Adds a small section heading with a hairline rule under it.
     *  @return The next free grid row */
    private static int section(JPanel panel, String title, int row,
            GridBagConstraints wide, boolean first){
        JLabel heading = new JLabel(title.toUpperCase());
        heading.setFont(Theme.SMALL.deriveFont(Font.BOLD));
        heading.setForeground(Theme.TEXT_MUTED);

        wide.gridy = row++;
        wide.insets = new Insets(first ? 0 : 16, 0, 2, 0);
        panel.add(heading, wide);

        JSeparator rule = new JSeparator();
        rule.setForeground(Theme.LINE);
        wide.gridy = row++;
        wide.insets = new Insets(0, 0, 8, 0);
        panel.add(rule, wide);

        wide.insets = new Insets(0, 0, 0, 0);
        return row;
    }

    /** One player's row: a dot in that player's colour, the side they are
     *  connecting, and the human/computer choice. */
    private static JPanel playerRow(final Color colour, String side,
            JRadioButton human, JRadioButton computer){
        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        rowPanel.setOpaque(false);

        JComponent dot = new JComponent(){
            public Dimension getPreferredSize(){ return new Dimension(10, 12); }
            protected void paintComponent(Graphics g){
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(colour);
                g2.fillOval(0, 2, 9, 9);
                g2.dispose();
            }
        };
        rowPanel.add(dot);

        JLabel name = new JLabel(side);
        name.setPreferredSize(new Dimension(72, name.getPreferredSize().height));
        rowPanel.add(name);
        rowPanel.add(human);
        rowPanel.add(computer);
        return rowPanel;
    }

    /** Returns a stored combo index, falling back to the default when the
     *  stored value does not fit the current item count
     *  @param key Preference key
     *  @param def Default index
     *  @param itemCount Number of items in the combo */
    private static int restoreIndex(String key, int def, int itemCount){
        int index = GamePrefs.get(key, def);
        if(index < 0 || index >= itemCount) return def;
        return index;
    }

    /** Class to listen for the accept button press */
    class AcceptHandler implements ActionListener{
        public void actionPerformed(ActionEvent e){
            int v, h;
            int dim;
            int difficulty;

            if(v1.isSelected()) v = 0;
            else v = 1;
            if(h1.isSelected()) h = 0;
            else h = 1;

            dim = Main.DIMENSIONS[selDimension.getSelectedIndex()];

            // Get difficulty level (1, 2, or 3)
            difficulty = selDifficulty.getSelectedIndex() + 1;

            /* Must be set before the new Match (and its Simulations/Paths)
             * is created; safe to toggle here because newGame builds a
             * fresh engine and board. */
            heuristics.OptConfig.USE_BITPATH = (selAlgorithm.getSelectedIndex() == 1);

            // Remember the selections for the next game and the next session
            GamePrefs.put(GamePrefs.DIMENSION, selDimension.getSelectedIndex());
            GamePrefs.put(GamePrefs.DIFFICULTY, selDifficulty.getSelectedIndex());
            GamePrefs.put(GamePrefs.ALGORITHM, selAlgorithm.getSelectedIndex());
            GamePrefs.put(GamePrefs.SWAP, enableSwap.isSelected());

            dispose();
            game.newGame(dim, v, h, enableSwap.isSelected(), difficulty);
        }
    }
}

/** Class to represent the About dialog box */
class AboutDialog extends JDialog{
    public AboutDialog(GameWindow principal){
        super(principal, "About Hexodus", true);
        setSize(300, 350);
        setResizable(false);
        
        Container panel = getContentPane();
        panel.setLayout(null);
        
        JPanel options = new JPanel(); 
        options.setBounds(10, 10, 280, 280);
        panel.add(options);
        options.setLayout(new FlowLayout());
        JLabel title = new JLabel("<html><br><br><center><font size=+4>" + 
                "<b>Hexodus</b></font><br>versin 1.0</center>");

        JLabel text = new JLabel("<html><br><center>Copyright © 2006 - 2008 " + 
                "Pablo Torrecilla<br>GNU General Public License." + "<br><br>" +
                "pau@nosololinux.com</center>");

        options.add(title);
        options.add(text);
        
        JButton Ok = new JButton();        
        Ok.addActionListener(new AcceptHandler());
        Ok.setBounds(190, 290, 100, 30);
        Ok.setText("Close");
        panel.add(Ok);
    }
    
    /** Listens for the "close" button event to close the window */
    class AcceptHandler implements ActionListener{
        public void actionPerformed(ActionEvent e){    
            dispose();
        }
    }
}

/** Class that represents a match and the window used to provide
 *  support for the new game, maintaining interaction with the user */
class GameWindow extends JFrame{
    private GameWindow window;
    private JMenu[] menus;
    private BoardPanel board;
    private StatusBar statusBar;
    private double layoutHOff, layoutVOff;   // Cell geometry, for the analysis overlay
    private boolean showConnections;         // Draw the connection skeleton
    private heuristics.Skeleton[] skeletons = new heuristics.Skeleton[2];

    /** Minimum gap between live repaints while the engine searches. Low
     *  enough to look continuous, high enough that drawing does not eat
     *  into the search. */
    private static final long PROGRESS_PAINT_MS = 80;
    private CellHandler g[][];
    private JButton b[][];
    private Icon red, blue, suggestion;
    private Icon redLast, blueLast;     // Darker variants marking the most recent stone
    private Icon ic_turn;
    private JButton lastStoneButton;    // Button holding the most recent stone
    private Icon lastStoneIcon;         // Normal-color icon to restore on that button
    private JButton swap;
    
    private Player turn;
    private Player playerOne;
    private Player playerTwo;
    private Match p;
    private int suggested[];
    private int Dimension;
    private int f;
    private int c;
    private boolean firstMove;
    
    /** Creates a new game window
     *  @param dim      Board dimension of the new game
     *  @param typeV    Type of the vertical player
     *  @param typeH    Type of the horizontal player
     *  @param swap     True if the swap rule is enabled, false otherwise
     *  @param difficulty Difficulty level (1 = Normal, 2 = Expert, 3 = Master) */
    public GameWindow(int dimension, int typeVertical, int typeHorizontal, boolean swap, int difficulty){
        Dimension = dimension;
        window = this;
        newGame(dimension, typeVertical, typeHorizontal, swap, difficulty);
        setResizable(false);
        setTitle("Hexodus");
    }
    
    /** Creates a new game
     *  @param dim      Board dimension of the new game
     *  @param typeV    Type of the vertical player
     *  @param typeH    Type of the horizontal player
     *  @param swap     True if the swap rule is enabled, false otherwise
     *  @param difficulty Difficulty level (1 = Normal, 2 = Expert, 3 = Master) */
    public void newGame(int dim, int typeV, int typeH, boolean swap, int difficulty){
        Dimension = dim;
        
        /* Board artwork is generated from one geometry (tools/BoardStyles.java):
         * pointy-top hexagons of radius R = 61/sqrt(3), horizontal pitch 61,
         * vertical pitch 52.5, each row shifted 30.5 to the left, and a 20px
         * margin around the board for the coloured frame. The window layout
         * therefore follows from the dimension instead of a per-size table,
         * so any board size lines up automatically. */
        final double CELL_R = 61.0 / Math.sqrt(3.0);   // 35.22
        final double ART_MARGIN = 20.0;
        final int CHROME_W = 88;   // window width beyond the artwork
        final int CHROME_H = 137;  // title bar + status bar + bottom margin
        final int ART_TOP = 50;    // where BoardPanel draws the artwork

        int artWidth  = (int)Math.round(91.5 * (dim - 1) + 61.0 + 2 * ART_MARGIN);
        int artHeight = (int)Math.round(52.5 * (dim - 1) + 2 * CELL_R + 2 * ART_MARGIN);

        int width  = artWidth + CHROME_W;
        int height = artHeight + CHROME_H;

        int widthSize = 50, heightSize = 45;          // clickable cell button
        double horizontalIncrement = 11, verticalIncrement = 7.5;  // pitch = size + increment
        double offset = 30.5;                          // per-row leftward shift

        /* Artwork is centred horizontally by BoardPanel, so its left edge sits
         * at CHROME_W / 2. Cell (0,0)'s centre is ART_MARGIN + 30.5*dim from
         * that edge; the button is positioned by its top-left corner. */
        double horizontalOffset = (CHROME_W / 2) + (30.5 * dim + ART_MARGIN) - (widthSize / 2.0);
        double verticalOffset = ART_TOP + (CELL_R + ART_MARGIN) - (heightSize / 2.0);

        setSize(width, height);
        
        firstMove = true;         
        board = new BoardPanel(Dimension, width);
        
        board.setLayout(null);
        board.setBackground(Main.BACKGROUND);
        setContentPane(board);
        
        layoutHOff = horizontalOffset;
        layoutVOff = verticalOffset;
        installAnalysisListener();

        statusBar = new StatusBar(width);
        statusBar.setBounds(0, 5, width, 34);
        board.add(statusBar);
        red = new ImageIcon(ClassLoader.getSystemResource("images/red.png"));
        blue = new ImageIcon(ClassLoader.getSystemResource("images/blue.png"));
        suggestion = new ImageIcon(ClassLoader.getSystemResource("images/sug.png"));
        redLast = darken(red);
        blueLast = darken(blue);
        lastStoneButton = null;
        lastStoneIcon = null;

        b = new JButton [Dimension][Dimension];

        g = new CellHandler [Dimension][Dimension];
        for(int i = 0; i < Dimension; i++){
            for(int j = 0; j < Dimension; j++){
                b[i][j] = new JButton();
                getContentPane().add(b[i][j]);

                b[i][j].setBounds((int)(horizontalOffset + (widthSize + horizontalIncrement) * i - (j * offset)), (int)(verticalOffset + (heightSize + verticalIncrement) * j), widthSize, heightSize);
                b[i][j].setContentAreaFilled(false);
                b[i][j].setBorderPainted(false);
                b[i][j].setFocusPainted(false);
                if(swap) g[i][j] = new CellHandlerSwap(i, j);
                else g[i][j] = new CellHandlerNormal(i, j);
                b[i][j].addActionListener(g[i][j]);
            }
        }
        
        menus = new JMenu[] {new JMenu("Game"), new JMenu("Hexodus"), new JMenu("Help")};
        JMenuItem[] gameMenu = {new JMenuItem("New Game...")};
        JCheckBoxMenuItem showThinking = new JCheckBoxMenuItem("Show AI Thinking");
        showThinking.setToolTipText("<html>Tints every move the engine scored, strongest for the<br>"
            + "ones it rated best. Untinted cells were pruned by the search.</html>");
        showThinking.setSelected(GamePrefs.get(GamePrefs.SHOW_THINKING, false));
        heuristics.Analysis.ENABLED = showThinking.isSelected();

        JCheckBoxMenuItem showLinks = new JCheckBoxMenuItem("Show Connections");
        showLinks.setToolTipText("<html>Marks stone groups that are already joined \u2014 even<br>"
            + "when not physically touching \u2014 and the empty cells<br>that keep the link alive.</html>");
        showLinks.setSelected(GamePrefs.get(GamePrefs.SHOW_LINKS, false));
        showConnections = showLinks.isSelected();

        JMenuItem[] hexodus = {new JMenuItem("Suggest Move"), new JRadioButtonMenuItem("Normal Mode"), new JRadioButtonMenuItem("Expert Mode"), new JRadioButtonMenuItem("Master Mode"), showThinking, showLinks};
        JMenuItem[] helpMenu = {new JMenuItem("About...")};

        ButtonGroup difficultyGroup = new ButtonGroup();
        difficultyGroup.add(hexodus[1]);
        difficultyGroup.add(hexodus[2]);
        difficultyGroup.add(hexodus[3]);
        
        for(int i = 0; i < gameMenu.length; i++){
            menus[0].add(gameMenu[i]);
            gameMenu[i].addActionListener(new MenuHandler(0, i));
        }
        for(int i = 0; i < hexodus.length; i++){
            menus[1].add(hexodus[i]);
            if(i == 0 || i == 3) menus[1].add(new JSeparator());
            hexodus[i].addActionListener(new MenuHandler(1, i));
        }
        for(int i = 0; i < helpMenu.length; i++){
            menus[2].add(helpMenu[i]);
            helpMenu[i].addActionListener(new MenuHandler(2, i));
        }
        JMenuBar statusBarMenu = new JMenuBar();
        for(int i = 0; i < menus.length; i++){
            statusBarMenu.add(menus[i]);
        }
        // Set the appropriate difficulty menu item based on selected difficulty
        hexodus[difficulty].setSelected(true);
        setJMenuBar(statusBarMenu);
        setVisible(true);
        
        playerOne = new Player(typeV, 1);
        playerTwo = new Player(typeH, 0);

        p = new Match(dim, swap);

        // Set the difficulty level for the match
        try {
            p.setLevel(difficulty);
        } catch (game.IncorrectLevel ex) {
            ex.printStackTrace();
        }

        // Forces the window to be redrawn
        Graphics gf = getGraphics();
        if (gf != null) paintComponents(gf);
        else repaint();
                
        ic_turn = red;  
        turn = playerOne;
        
        if((typeV == 1) && (typeH == 1)){
            demo();
        }
        else if(turn.getType() == 1){ // If the starting player is the computer
            changeStatus(0);
            if(swap){
                generateSwapMove(turn);
            }
            else{generateMove(turn);
            changeStatus(-1);
            }
        }
    }
    
    /** Disables controls that should be blocked at certain times... */
    public void disable(){
        menus[1].setEnabled(false);
    }
    
    /** And enables them again */
    public void enable(){
        menus[1].setEnabled(true);
    }
    
    /** Changes the status shown in the upper status bar text
     *  @param  id Identifier of the new status */
    public void changeStatus(int id){
        String status = "";
                
        switch(id){
            case 0:
                disable();
                status = "Analysing position\u2026";
                statusBar.setTurn(turn == playerOne, "Hexodus thinking");
                break;
            default:
                enable();
                /* Leaves the message alone: it usually holds the timing of
                 * the move just played, which is worth keeping on screen. */
                status = null;
                if(turn != null) statusBar.setTurn(turn == playerOne,
                    turn.getType() == 0 ? "Your turn" : "Hexodus");
        }
        if(status != null) statusBar.setMessage(status);
        
        Graphics gf = getGraphics();
        if (gf != null) paintComponents(gf);
        else repaint();
    }
    
    /** Changes the status shown in the upper status bar text
     *  @param  status String with the new status */
    public void changeStatus(String status){
        statusBar.setMessage(status);
        
        Graphics gf = getGraphics();
        if (gf != null) paintComponents(gf);
        else repaint();
    }
    
    /** Requests the match object to suggest a move and displays it on
     *  the board
     *  @param t player for whom the move is suggested */
    public void suggestMove(Player t){
        changeStatus(0);
        long startTime = System.currentTimeMillis();
        suggested = p.generateMove(t);
        long thinkingTime = System.currentTimeMillis() - startTime;

        b[suggested[1]][suggested[0]].setIcon(suggestion);
        changeStatus(-1);
        changeStatus("Suggested " + cellName(suggested[0], suggested[1]) + " in " + thinkingTime + "ms");
        if(board != null) board.repaint();   // show the reasoning behind the suggestion
    }
    
    /** Removes the last suggested move from the board */
    public void removeSuggestedMove(){
        if(suggested != null)
            b[suggested[1]][suggested[0]].setIcon(null);
        suggested = null;
    }
    
    /** Recomputes the connection skeleton for both players.
     *
     *  The connections belong to whatever position the engine last
     *  evaluated, so they are refreshed from the current position before
     *  being drawn; one evaluation is cheap next to a move search. */
    private void refreshConnections(){
        if(!showConnections || p == null){
            skeletons[0] = skeletons[1] = null;
            return;
        }
        try {
            heuristics.Simulation position = p.getCurrentPosition();
            position.calculateValue();
            skeletons[0] = heuristics.Skeleton.of(position, 0);
            skeletons[1] = heuristics.Skeleton.of(position, 1);
        } catch (RuntimeException ex) {
            skeletons[0] = skeletons[1] = null;   // never break the game over a decoration
        }
    }

    /** Makes the board follow the engine's search as it happens.
     *
     *  The search runs on the event thread, so ordinary repaint requests
     *  would only be serviced once it finished and the board would sit
     *  frozen. Painting synchronously from the engine's own progress
     *  callback draws each update immediately instead. Repaints are
     *  throttled so the drawing does not noticeably slow the search. */
    private void installAnalysisListener(){
        heuristics.Analysis.setListener(new Runnable(){
            private long lastPaint = 0;

            public void run(){
                if(board == null || !board.isShowing()) return;
                if(!SwingUtilities.isEventDispatchThread()) return;  // worker thread: skip

                long now = System.currentTimeMillis();
                boolean done = !heuristics.Analysis.isSearching();
                if(!done && now - lastPaint < PROGRESS_PAINT_MS) return;
                lastPaint = now;

                if(heuristics.Analysis.isSearching()){
                    statusBar.setMessage("Analysing position… " +
                        heuristics.Analysis.getEvaluated() + " of " +
                        heuristics.Analysis.getCandidates() + " moves examined");
                    statusBar.refreshNow();
                }
                board.paintImmediately(0, 0, board.getWidth(), board.getHeight());
            }
        });
    }

    /** Formats a cell in the notation normally used for Hex: a letter for
     *  the column and a number for the row, counting from 1. Board indices
     *  are zero-based internally, which is not something a player should
     *  have to know, so (0,0) reads as "a1".
     *  @param row Zero-based row
     *  @param column Zero-based column
     *  @return The cell name, e.g. "d4" */
    private static String cellName(int row, int column){
        return (char)('a' + column) + Integer.toString(row + 1);
    }

    /** Returns a slightly darker copy of an icon, used to highlight the
     *  most recently placed stone.
     *  @param src Source icon
     *  @return Darkened copy */
    private Icon darken(Icon src){
        BufferedImage img = new BufferedImage(src.getIconWidth(), src.getIconHeight(),
            BufferedImage.TYPE_INT_ARGB);
        Graphics2D gr = img.createGraphics();
        src.paintIcon(null, gr, 0, 0);
        gr.dispose();

        /* Scale the darkening by how far each pixel is from white, so the
         * stone darkens fully while anything near-white is left alone.
         * The stone images are transparent (tools/StoneGen.java), so this
         * mainly keeps the anti-aliased rim smooth; it also means the
         * effect stays correct if an opaque-background icon is ever used
         * again, which would otherwise gain a grey box. */
        final double factor = 0.72;
        for(int y = 0; y < img.getHeight(); y++){
            for(int x = 0; x < img.getWidth(); x++){
                int argb = img.getRGB(x, y);
                int a = (argb >>> 24);
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                int min = Math.min(r, Math.min(g, b));
                double weight = (255 - min) / 255.0;     // 0 for white, ~1 for the stone
                double f = 1.0 - (1.0 - factor) * weight;

                img.setRGB(x, y, (a << 24) | ((int)(r * f) << 16)
                    | ((int)(g * f) << 8) | (int)(b * f));
            }
        }
        return new ImageIcon(img);
    }

    /** Places a stone of the current turn's color on the board, marking it
     *  with the darker "last move" variant and restoring the previous last
     *  stone to its normal color.
     *  @param col Button column index (first index into b)
     *  @param row Button row index (second index into b) */
    private void placeStone(int col, int row){
        if(lastStoneButton != null)
            lastStoneButton.setIcon(lastStoneIcon);

        b[col][row].setIcon(ic_turn == red ? redLast : blueLast);
        lastStoneButton = b[col][row];
        lastStoneIcon = ic_turn;
    }

    /** Generates a move if the swap rule is enabled
     *  @param turn Player in possession of the turn */
    public void generateSwapMove(Player turn){
        int [] move = new int[2];
        changeStatus(0);
        long startTime = System.currentTimeMillis();
        move = p.generateMove(turn);
        long thinkingTime = System.currentTimeMillis() - startTime;

        placeStone(move[1], move[0]);
        changeStatus("First move " + cellName(move[0], move[1]) + " calculated in " + thinkingTime + "ms");

        swap = new JButton();
        swap.setContentAreaFilled(false);
        swap.setText("Swap Move");
        swap.addActionListener(new SwapButtonHandler());
        statusBar.addControl(swap);

        f = move[0];
        c = move[1];
        firstMove = false;
        disable();
        Graphics gf = getGraphics();
        if (gf != null) paintComponents(gf);
        else repaint();
    }
    
    /** Requests the match object to generate a move
     *  @param turn Player in possession of the turn
     *  @return Player who wins the match with this move, if applicable */
    public Player generateMove(Player turn){
        Player winner = null;
        int [] move = new int[2];

        changeStatus(0);
        long startTime = System.currentTimeMillis();
        move = p.generateMove(turn);
        long thinkingTime = System.currentTimeMillis() - startTime;

        refreshConnections();
        if(board != null) board.repaint();   // refresh the overlays

        // Show timing in status bar
        changeStatus("Move " + cellName(move[0], move[1]) + " calculated in " + thinkingTime + "ms");

        placeStone(move[1], move[0]);
        g[move[1]][move[0]].setAllowed(false);
        try {
            winner = p.newMove(move[0], move[1], turn);
        } catch (OccupiedSquare ex) {
            ex.printStackTrace();
        } catch (NonexistentSquare ex) {
            ex.printStackTrace();
        }
        changeTurn();

        // Clear status after a short delay (in a real app, would use Timer)
        try {
            Thread.sleep(800);  // Show timing for 800ms
        } catch (InterruptedException e) {}
        changeStatus(-1);

        return winner;
    }
    
    /** Exchanges the turn between both players */
    public void changeTurn(){
        if(turn == playerOne) turn = playerTwo;
        else turn = playerOne;
        if(ic_turn == blue) ic_turn = red;
        else ic_turn = blue;
    }
    
    /** Returns the next player to have the turn */
    public Player getNextTurn(){
        if(turn == playerOne) return playerTwo;
        else return playerOne;
    }
    
    /** Updates the interface when the current match ends, showing the
     *  status in the box */
    public void finish(Player winner){
        for(int i = 0; i < Dimension; i++){
            for(int j = 0; j < Dimension; j++){
                b[i][j].removeActionListener(g[i][j]);
            }
        }
        disable();
        statusBar.setMessage("");
        statusBar.clearTurn(winner.getName() + " wins");
    }
    
    /** Removes the special handlers for the swap move and
     *  replaces them with the normal ones for the game */
    public void updateSwapHandlers(){
        for(int i = 0; i < Dimension; i++){
            for(int j = 0; j < Dimension; j++){
                boolean bloq = g[i][j].getAllowed();
                b[i][j].removeActionListener(g[i][j]);
                g[i][j] = new CellHandlerNormal(i, j, bloq);
                b[i][j].addActionListener(g[i][j]);
            }
        }
    }
    
    /** Executes demonstration mode */
    public void demo(){
        disable();
        
        for(int i = 0; i < Dimension; i++){
            for(int j = 0; j < Dimension; j++){
                b[i][j].removeActionListener(g[i][j]);
            }
        }       
        Player winner = null;
        int [] move = new int[2];
        do{
            winner = generateMove(turn);

            Graphics gf = getGraphics();
            if (gf != null) paintComponents(gf);
            else repaint();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        } while(winner == null);
        
        disable();
        statusBar.setMessage("");
        statusBar.clearTurn(winner.getName() + " wins");
    }
    
    /** Subclass to represent the panel with the board */
    class BoardPanel extends JPanel{ 
        Image backgroundImage = null;
        int width;
        
        public BoardPanel(int dimension, int width){
            this.width = width;
            String imagePath = "images/" + dimension + ".png";
            MediaTracker mt = new MediaTracker(this);
            backgroundImage = Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource(imagePath));

            //backgroundImage = Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource(imagePath));

            
            mt.addImage(backgroundImage, 0);
            try{
                mt.waitForAll();
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
        }    
        
        protected void paintComponent(Graphics g){
            super.paintComponent(g);
        
            if(backgroundImage != null){
                int imwidth = backgroundImage.getWidth(null);
                int imheight = backgroundImage.getHeight(null);
                
                // displays the centered board image.
                if((imwidth > 0) && (imheight > 0)){
                    g.drawImage(backgroundImage, width / 2 - imwidth / 2, 50, null);
                }
            }

            if(showConnections) paintConnections((Graphics2D) g);
            if(heuristics.Analysis.isAvailable()) paintAnalysis((Graphics2D) g);
        }

        /** Draws each player's connective structure: groups already bound
         *  together by a virtual connection, the edges they reach, and the
         *  empty cells those links depend on. */
        private void paintConnections(Graphics2D g2){
            Graphics2D g = (Graphics2D) g2.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for(int color = 0; color <= 1; color++){
                heuristics.Skeleton skeleton = skeletons[color];
                if(skeleton == null) continue;
                Color tint = (color == 1) ? Theme.RED : Theme.BLUE;

                for(heuristics.Skeleton.Link link : skeleton.getLinks()){
                    // the empty cells holding the link together
                    g.setColor(new Color(tint.getRed(), tint.getGreen(), tint.getBlue(), 38));
                    for(heuristics.Square carrier : link.carrier)
                        g.fill(cellShape(carrier.getRow(), carrier.getColumn(), 25));

                    double x1 = cellX(link.from.getRow(), link.from.getColumn());
                    double y1 = cellY(link.from.getRow());
                    double x2, y2;
                    if(link.toEdge){
                        double[] end = edgePoint(color, link.farEdge, link.from);
                        x2 = end[0]; y2 = end[1];
                    }
                    else {
                        x2 = cellX(link.to.getRow(), link.to.getColumn());
                        y2 = cellY(link.to.getRow());
                    }

                    g.setColor(new Color(tint.getRed(), tint.getGreen(), tint.getBlue(), 205));
                    g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                        10f, new float[]{8f, 6f}, 0f));
                    g.draw(new java.awt.geom.Line2D.Double(x1, y1, x2, y2));
                }
            }
            g.dispose();
        }

        /** Where a group-to-edge link should terminate: just outside the
         *  board on the side that player is trying to reach. */
        private double[] edgePoint(int color, boolean far, heuristics.Square from){
            int row = from.getRow(), col = from.getColumn();
            double margin = 36;
            if(color == 1)   // vertical player: north at the top, south below
                return new double[]{ cellX(row, col),
                    far ? cellY(Dimension - 1) + margin : cellY(0) - margin };
            // horizontal player: east to the right, west to the left
            return new double[]{ far ? cellX(row, 0) - margin : cellX(row, Dimension - 1) + margin,
                cellY(row) };
        }

        private double cellX(int row, int col){ return layoutHOff + 61.0 * col - 30.5 * row + 25; }
        private double cellY(int row){ return layoutVOff + 52.5 * row + 22.5; }

        /** Paints the engine's reasoning over the board: every candidate the
         *  search actually scored is tinted in the moving player's colour,
         *  strongest for the moves it liked best. Cells left untinted were
         *  pruned by alpha-beta — the search never looked at them. The move
         *  finally played is ringed. */
        private void paintAnalysis(Graphics2D g2){
            double[][] scores = heuristics.Analysis.getScores();
            int n = scores.length;
            if(n != Dimension) return;

            boolean maximizing = (heuristics.Analysis.getMover() == 1);
            /* The soft border hues rather than the stone colours: this is a
             * wash over the board and must stay behind the pieces. */
            Color tint = maximizing ? Theme.RED_SOFT : Theme.BLUE_SOFT;

            // Rank the examined scores; ranking survives the 0 / infinity extremes
            java.util.ArrayList<Double> seen = new java.util.ArrayList<Double>();
            for(int i = 0; i < n; i++)
                for(int j = 0; j < n; j++)
                    if(!Double.isNaN(scores[i][j])) seen.add(Double.valueOf(scores[i][j]));
            if(seen.isEmpty()) return;
            java.util.Collections.sort(seen);

            Graphics2D g = (Graphics2D) g2.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for(int row = 0; row < n; row++){
                for(int col = 0; col < n; col++){
                    double s = scores[row][col];
                    if(Double.isNaN(s)) continue;

                    // Fraction of examined moves this one is better than
                    int rank = java.util.Collections.binarySearch(seen, Double.valueOf(s));
                    if(rank < 0) rank = -rank - 1;
                    double t = (seen.size() == 1) ? 1.0 : rank / (double)(seen.size() - 1);
                    if(!maximizing) t = 1.0 - t;   // Minimising player prefers low scores

                    int alpha = (int)Math.round(10 + 72 * t * t);
                    g.setColor(new Color(tint.getRed(), tint.getGreen(), tint.getBlue(), alpha));
                    g.fill(cellShape(row, col, 25));
                }
            }

            /* Ring the leading move: dashed while the search is still
             * running (it may yet be beaten), solid once decided. */
            int br = heuristics.Analysis.getBestRow(), bc = heuristics.Analysis.getBestColumn();
            if(br >= 0 && bc >= 0){
                g.setColor(new Color(tint.getRed(), tint.getGreen(), tint.getBlue(), 190));
                if(heuristics.Analysis.isSearching())
                    g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
                        10f, new float[]{5f, 4f}, 0f));
                else
                    g.setStroke(new BasicStroke(2.0f));
                g.draw(cellShape(br, bc, 26));
            }
            g.dispose();
        }

        /** Hexagon outline of one cell, in panel coordinates */
        private Shape cellShape(int row, int col, double radius){
            double cx = layoutHOff + 61.0 * col - 30.5 * row + 25;
            double cy = layoutVOff + 52.5 * row + 22.5;
            java.awt.geom.Path2D p = new java.awt.geom.Path2D.Double();
            for(int k = 0; k < 6; k++){
                double ang = Math.toRadians(-90 + 60 * k);
                double x = cx + radius * Math.cos(ang), y = cy + radius * Math.sin(ang);
                if(k == 0) p.moveTo(x, y); else p.lineTo(x, y);
            }
            p.closePath();
            return p;
        }
    }
    
    /** Subclass to manage the application menu */
    class MenuHandler implements ActionListener{
        private int index;
        private int parent;
        
        public MenuHandler(int parentId, int indexId){
            index = indexId;
            parent = parentId;
        }
        
        public void actionPerformed(ActionEvent e){
            switch(parent){
            case 0:
                switch(index){
                case 0:
                    OptionsDialog dialog = new OptionsDialog(window);
                    dialog.setVisible(true);
                    break;
                }
                break;
            case 1:
                switch(index){
                case 0:
                    suggestMove(turn);
                    break;
                case 1:
                    try {
                        p.setLevel(1);
                    } catch (IncorrectLevel ex) {
                        ex.printStackTrace();
                    }
                    break;
                case 2:
                    try {
                        p.setLevel(2);
                    } catch (IncorrectLevel ex) {
                        ex.printStackTrace();
                    }
                    break;
                case 3:
                    try {
                        p.setLevel(3);
                    } catch (IncorrectLevel ex) {
                        ex.printStackTrace();
                    }
                    break;
                case 5:
                    showConnections = ((JCheckBoxMenuItem) e.getSource()).isSelected();
                    GamePrefs.put(GamePrefs.SHOW_LINKS, showConnections);
                    refreshConnections();
                    repaint();
                    break;
                case 4:
                    /* Show AI Thinking: the engine only records its reasoning
                     * while this is on, so the overlay appears from the next
                     * computed move onward. */
                    boolean on = ((JCheckBoxMenuItem) e.getSource()).isSelected();
                    heuristics.Analysis.ENABLED = on;
                    GamePrefs.put(GamePrefs.SHOW_THINKING, on);
                    if(!on) heuristics.Analysis.clear();
                    repaint();
                    break;
                }
                break;
            case 2:
                switch(index){
                case 0:
                    AboutDialog acerca = new AboutDialog(window);
                    acerca.setVisible(true);
                    break;
                }
                break;
            }
            
        }
    }
    
    /** Subclass to handle the different events related to
     *  squares of the game board.
     *  It is abstract because the square handlers have to control
     *  the application of the swap rule in the first move if that option
     *  is enabled. To save checks, polymorphism is used and
     *  one handler is used in the first move and another for the rest of the
     *  match. */
    abstract class CellHandler implements ActionListener{
        protected int row;             // row of the square
        protected int column;          // column of the square
        protected boolean allowed;    // If that square can be clicked again
        
        public CellHandler(int column, int row){
            this.column = column;
            this.row = row;
            allowed = true;
        }
        
        abstract public void actionPerformed(ActionEvent e);
        
        public boolean getAllowed(){
            return allowed;
        }
        
        public void setAllowed(boolean isAllowed){
            allowed = isAllowed;
        }
    }
    
    /** Normal handler for the complete game after the first move */
    class CellHandlerNormal extends CellHandler{ 
        public CellHandlerNormal(int column, int row){ 
            super(column, row);
        }
        
        public CellHandlerNormal(int column, int row, boolean isAllowed){
            super(column, row);
            allowed = isAllowed;
        }
 
        public void actionPerformed(ActionEvent e){
            Player h;
            Player m;
            int [] move = new int[2];
            
            if(allowed){
                removeSuggestedMove();
                allowed = false;
                /* The recorded analysis describes the position before this
                 * move, so drop it rather than leave a stale overlay. */
                heuristics.Analysis.clear();
                placeStone(column, row);
                refreshConnections();

                // Forces the redraw
                Graphics gf = getGraphics();
                if (gf != null) paintComponents(gf);
                else repaint();
                               
                try {
                    h = p.newMove(row,column,turn);
                    if(h == null){
                        changeTurn();    
                        if(turn.isComputer()){
                            m = generateMove(turn);
                            if(m != null) finish(m);
                        }
                    } 
                    else{
                        finish(h);
                    }
                }catch(OccupiedSquare ex){
                    System.out.println("Occupied");
                }catch(NonexistentSquare ex){
                    System.out.println("Out of range");
                }
            }
        }
    }
    
    /** Special handler for the first move if the swap rule is enabled */
    class CellHandlerSwap extends CellHandler{    
        public CellHandlerSwap(int column, int row){ 
            super(column, row);
        }
 
        public void actionPerformed(ActionEvent e){
            Player h;
            Player m;
            int [] move = new int[2];
            
            if(allowed){
                allowed = false;
                removeSuggestedMove();
                if(firstMove){    // It is the first time this handler is used
                    disable();
                    placeStone(column, row);
                    allowed = false;

                    f = row;
                    c = column;
                    firstMove = false;
                    
                    // If the opponent is the computer, ask if it swaps
                    if(getNextTurn().isComputer()) {  
                        if(p.offerSwap(f, c)){ // The computer accepts the swap
                            Graphics gf = getGraphics();
                            if (gf != null) paintComponents(gf);
                            else repaint();                 
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                            changeStatus("Hexodus has swapped the move");
                            changeTurn();
                            placeStone(c, f);

                            try {
                                p.newMove(f, c, turn);
                            } catch (NonexistentSquare ex) {
                                ex.printStackTrace();
                            } catch (OccupiedSquare ex) {
                                ex.printStackTrace();
                            }
                            changeTurn();
                        }
                        else{ // The computer rejects the swap
                            try {
                                p.newMove(f, c, turn);
                                changeTurn();
                            } catch (NonexistentSquare ex) {
                                ex.printStackTrace();
                            } catch (OccupiedSquare ex) {
                                ex.printStackTrace();
                            }

                            generateMove(turn);
                            
                            Graphics gf = getGraphics();
                            if (gf != null) paintComponents(gf);
                            else repaint();                           
                        }
                        updateSwapHandlers();
                    }   // If the opponent is a human, show the button
                    else{
                        swap = new JButton();
                        swap.setContentAreaFilled(false);
                        swap.setText("Swap Move");
                        swap.addActionListener(new SwapButtonHandler());
                        statusBar.addControl(swap);
                    }
                }  
                else{ /* If it is not the first time the handler is executed, it means
                       * that the human opponent rejects the swap */
                    try {
                        p.newMove(f, c, turn);
                        changeTurn();
                        placeStone(column, row);
                        p.newMove(row, column, turn);
                    }catch (OccupiedSquare ex) {
                        System.out.println("Occupied");
                    }catch (NonexistentSquare ex) {
                        System.out.println("Out of range");
                    }
                    changeTurn();
                    swap.setVisible(false);

                    Graphics gf = getGraphics();
                    if (gf != null) paintComponents(gf);
                    else repaint();

                    if(turn.isComputer()) generateMove(turn);
                    updateSwapHandlers();
                    enable();
                }        
            }
        }
    }
    
    /** Controls the swap button press when this
     *  possibility is offered to the user */
    class SwapButtonHandler implements ActionListener{
        public void actionPerformed(ActionEvent e){
            changeTurn();
            placeStone(c, f);

            try{
                p.newMove(f, c, turn);
            }catch (NonexistentSquare ex){
                ex.printStackTrace();
            }catch (OccupiedSquare ex){
                ex.printStackTrace();
            }
            changeTurn();

            updateSwapHandlers();
            swap.setVisible(false);
            if(turn.isComputer()) generateMove(turn);
            enable();
        }
    }       
}


/** Terminate the process when the window is closed */
class WindowCloseHandler extends WindowAdapter {
    public void windowClosing(WindowEvent e) {
        System.exit(0);
    }
}

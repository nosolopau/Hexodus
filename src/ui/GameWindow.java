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


/** Class that represents a match and the window used to provide
 *  support for the new game, maintaining interaction with the user */
class GameWindow extends JFrame{
    private GameWindow window;
    private JMenu[] menus;
    private BoardPanel board;
    private StatusBar statusBar;
    private double layoutHOff, layoutVOff;   // Cell geometry, for the analysis overlay

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

        JMenuItem[] hexodus = {new JMenuItem("Suggest Move"), new JRadioButtonMenuItem("Normal Mode"), new JRadioButtonMenuItem("Expert Mode"), new JRadioButtonMenuItem("Master Mode"), showThinking};
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

        if(board != null) board.repaint();   // refresh the analysis overlay

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

            if(heuristics.Analysis.isAvailable()) paintAnalysis((Graphics2D) g);
        }

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

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
   
    /** Creates a new instance of the main class Main */
    public Main(){
    }
    
    /** Starts the program execution
     *  @param args Command line arguments */
    public static void main(String[] args){        
        GameWindow juego = new GameWindow(6, 0, 1, true);

        WindowCloseHandler window = new WindowCloseHandler();
        juego.addWindowListener(window);
    }
}

/** Options dialog box for creating a new game */
class OptionsDialog extends JDialog{
    private JRadioButton v1, v2, h1, h2;    // References to the controls
    private JComboBox selDimension;
    private JCheckBox activarSwap;
    private GameWindow juego;
    
    /** Shows the dialog to create a new match
     *  @param principal    Reference to the main game window */
    public OptionsDialog(GameWindow principal){
        super(principal, "New Game", true);
        setSize(300, 350);
        setResizable(false);
        
        juego = principal;
        
        Container panel = getContentPane();
        panel.setLayout(null);
        
        JPanel opciones = new JPanel();        
        JPanel vertical = new JPanel();
        JPanel horizontal = new JPanel();
        opciones.setBorder(new TitledBorder("Game Options"));
        vertical.setBorder(new TitledBorder("Player 1 (vertical)"));
        horizontal.setBorder(new TitledBorder("Player 2 (horizontal)"));
        opciones.setBounds(10, 10, 280, 100);
        vertical.setBounds(10, 120, 280, 80);
        horizontal.setBounds(10, 210, 280, 80); 
        panel.add(opciones);
        panel.add(vertical);
        panel.add(horizontal);
        
        JButton Ok = new JButton();        
        Ok.addActionListener(new AcceptHandler());
        Ok.setBounds(190, 290, 100, 30);
        Ok.setText("Accept");
        panel.add(Ok);
              
        ButtonGroup botonesVertical = new ButtonGroup();
        ButtonGroup botonesHorizontal = new ButtonGroup();
        v1 = new JRadioButton("Human"); 
        v2 = new JRadioButton("Computer");
        h1 = new JRadioButton("Human"); 
        h2 = new JRadioButton("Computer");
        v1.setSelected(true);
        h2.setSelected(true);
        botonesVertical.add(v1);  
        botonesVertical.add(v2);
        botonesHorizontal.add(h1);
        botonesHorizontal.add(h2);
        
        selDimension = new JComboBox();
        selDimension.addItem("5 x 5");
        selDimension.addItem("6 x 6");
        selDimension.addItem("7 x 7");
        
        JLabel etiDimension = new JLabel("Dimension: ");
        activarSwap = new JCheckBox("Enable swap rule");
        activarSwap.setSelected(true);
        
        opciones.add(etiDimension);
        opciones.add(selDimension);
        opciones.add(activarSwap);
        vertical.add(v1); 
        vertical.add(v2);
        horizontal.add(h1);
        horizontal.add(h2);
    }
    
    /** Class to listen for the accept button press */
    class AcceptHandler implements ActionListener{
        public void actionPerformed(ActionEvent e){
            int v, h;
            int dim;
            
            if(v1.isSelected()) v = 0;
            else v = 1;
            if(h1.isSelected()) h = 0;
            else h = 1;
            
            switch(selDimension.getSelectedIndex()){
            case 0:
                dim = 5;
                break;
            case 1:
                dim = 6;
                break;
            default:
                dim = 7;
            }
            dispose();      
            juego.newGame(dim, v, h, activarSwap.isSelected());
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
        
        JPanel opciones = new JPanel(); 
        opciones.setBounds(10, 10, 280, 280);
        panel.add(opciones);
        opciones.setLayout(new FlowLayout());
        JLabel titulo = new JLabel("<html><br><br><center><font size=+4>" + 
                "<b>Hexodus</b></font><br>versin 1.0</center>");

        JLabel text = new JLabel("<html><br><center>Copyright © 2006 - 2008 " + 
                "Pablo Torrecilla<br>GNU General Public License." + "<br><br>" +
                "pau@nosololinux.com</center>");

        opciones.add(titulo);
        opciones.add(text);
        
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
    private GameWindow yo;
    private JMenu[] menus;
    private BoardPanel board;
    private JPanel statusBar;
    private CellHandler g[][];
    private JButton b[][];
    private Icon red, blue, suggestion;
    private Icon ic_turn;
    private JLabel text;
    private JButton swap;
    
    private Player turn;
    private Player uno;
    private Player dos;
    private Match p;
    private int suggested[];
    private int Dimension;
    private int f;
    private int c;
    private boolean firstMove;
    
    /** Creates a new game window
     *  @param dim      Board dimension of the new game
     *  @param tipoV    Type of the vertical player
     *  @param tipoH    Type of the horizontal player
     *  @param swap     True if the swap rule is enabled, false otherwise */
    public GameWindow(int dimension, int tipoVertical, int tipoHorizontal, boolean swap){
        Dimension = dimension;
        yo = this;
        newGame(dimension, tipoVertical, tipoHorizontal, swap);
        setResizable(false);          
        setTitle("Hexodus");
    }
    
    /** Creates a new game
     *  @param dim      Board dimension of the new game
     *  @param tipoV    Type of the vertical player
     *  @param tipoH    Type of the horizontal player
     *  @param swap     True if the swap rule is enabled, false otherwise */
    public void newGame(int dim, int tipoV, int tipoH, boolean swap){
        Dimension = dim;
        
        int width, height;
        int horizontalOffset, verticalOffset;
        int widthSize, heightSize;
        double horizontalIncrement, verticalIncrement;
        double offset;
        
        switch(Dimension){
            case 5:
                width = 529;
                height = 423;
                horizontalOffset = 178;
                verticalOffset = 66;
                widthSize = 50;
                heightSize = 45;
                horizontalIncrement = 11;
                verticalIncrement = 7;
                offset = 30.5;
                break;
            case 6:
                width = 620;
                height = 480;
                horizontalOffset = 210;
                verticalOffset = 67;
                widthSize = 50;
                heightSize = 45;
                horizontalIncrement = 11;
                verticalIncrement = 7.5;
                offset = 30.5;
                break;                
            default:
                width = 710;
                height = 530;
                horizontalOffset = 240;
                verticalOffset = 68;
                widthSize = 50;
                heightSize = 45;                
                horizontalIncrement = 11;
                verticalIncrement = 7.5;                
                offset = 30.5;
        }
        setSize(width, height);
        
        firstMove = true;         
        board = new BoardPanel(Dimension, width);
        
        board.setLayout(null);
        board.setBackground(Color.white);
        setContentPane(board);
        
        statusBar = new JPanel();
        statusBar.setBackground(Color.white);
        statusBar.setBounds(0, 5, width, 40);

        board.add(statusBar);
        
        text = new JLabel();
        statusBar.add(text);
        red = new ImageIcon(ClassLoader.getSystemResource("images/red.png"));
        blue = new ImageIcon(ClassLoader.getSystemResource("images/blue.png"));
        suggestion = new ImageIcon(ClassLoader.getSystemResource("images/sug.png"));

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
        JMenuItem[] juego = {new JMenuItem("New Game...")};
        JMenuItem[] hexodus = {new JMenuItem("Suggest Move"), new JRadioButtonMenuItem("Normal Mode"), new JRadioButtonMenuItem("Expert Mode"), new JRadioButtonMenuItem("Master Mode")};
        JMenuItem[] ayuda = {new JMenuItem("About...")};

        ButtonGroup nivel = new ButtonGroup();
        nivel.add(hexodus[1]);
        nivel.add(hexodus[2]);
        nivel.add(hexodus[3]);
        
        for(int i = 0; i < juego.length; i++){
            menus[0].add(juego[i]);
            juego[i].addActionListener(new MenuHandler(0, i));
        }
        for(int i = 0; i < hexodus.length; i++){
            menus[1].add(hexodus[i]);
            if(i == 0)menus[1].add(new JSeparator());
            hexodus[i].addActionListener(new MenuHandler(1, i));
        }
        for(int i = 0; i < ayuda.length; i++){
            menus[2].add(ayuda[i]);
            ayuda[i].addActionListener(new MenuHandler(2, i));
        }
        JMenuBar statusBarMenu = new JMenuBar();
        for(int i = 0; i < menus.length; i++){
            statusBarMenu.add(menus[i]);
        }
        hexodus[1].setSelected(true);
        setJMenuBar(statusBarMenu);  
        setVisible(true);
        
        uno = new Player(tipoV, 1);
        dos = new Player(tipoH, 0);
        
        p = new Match(dim, swap);
        
        // Forces the window to be redrawn
        Graphics gf = getGraphics();
        if (gf != null) paintComponents(gf);
        else repaint();
                
        ic_turn = red;  
        turn = uno;
        
        if((tipoV == 1) && (tipoH == 1)){
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
                status = "Hexodus is thinking...";
                break;
            default:
                enable();
                status = "";
        }
        text.setText(status);
        
        Graphics gf = getGraphics();
        if (gf != null) paintComponents(gf);
        else repaint();
    }
    
    /** Changes the status shown in the upper status bar text
     *  @param  status String with the new status */
    public void changeStatus(String status){
        text.setText(status);
        
        Graphics gf = getGraphics();
        if (gf != null) paintComponents(gf);
        else repaint();
    }
    
    /** Requests the match object to suggest a move and displays it on
     *  the board
     *  @param t player for whom the move is suggested */
    public void suggestMove(Player t){
        changeStatus(0);
        suggested = p.generateMove(t);

        b[suggested[1]][suggested[0]].setIcon(suggestion);
        changeStatus(-1);
    }
    
    /** Removes the last suggested move from the board */
    public void removeSuggestedMove(){
        if(suggested != null)
            b[suggested[1]][suggested[0]].setIcon(null);
        suggested = null;
    }
    
    /** Generates a move if the swap rule is enabled
     *  @param turn Player in possession of the turn */
    public void generateSwapMove(Player turn){
        int [] move = new int[2];
        changeStatus(0);
        move = p.generateMove(turn);
        b[move[1]][move[0]].setIcon(ic_turn);
        changeStatus(-1);

        swap = new JButton();
        swap.setContentAreaFilled(false);
        swap.setText("Swap Move");
        swap.addActionListener(new SwapButtonHandler());
        statusBar.add(swap);

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
        move = p.generateMove(turn);
        b[move[1]][move[0]].setIcon(ic_turn);
        g[move[1]][move[0]].setAllowed(false);
        try {
            winner = p.newMove(move[0], move[1], turn);
        } catch (OccupiedSquare ex) {
            ex.printStackTrace();
        } catch (NonexistentSquare ex) {
            ex.printStackTrace();
        }
        changeTurn();
        changeStatus(-1);
        
        return winner;
    }
    
    /** Exchanges the turn between both players */
    public void changeTurn(){
        if(turn == uno) turn = dos;
        else turn = uno;
        if(ic_turn == blue) ic_turn = red;
        else ic_turn = blue;
    }
    
    /** Returns the next player to have the turn */
    public Player ObtenernextTurno(){
        if(turn == uno) return dos;
        else return uno;
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
        text.setText("Winner: " + winner.getName());
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
        text.setText("Winner: " + winner.getName());
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
        }
    }
    
    /** Subclass to manage the application menu */
    class MenuHandler implements ActionListener{
        private int index;
        private int parent;
        
        public MenuHandler(int padre, int indice){
            index = indice;
            parent = padre;
        }
        
        public void actionPerformed(ActionEvent e){
            switch(parent){
            case 0:
                switch(index){
                case 0:
                    OptionsDialog dialog = new OptionsDialog(yo);
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
                }
                break;
            case 2:
                switch(index){
                case 0:
                    AboutDialog acerca = new AboutDialog(yo);
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
        
        public void setAllowed(boolean permitido){
            allowed = permitido;
        }
    }
    
    /** Normal handler for the complete game after the first move */
    class CellHandlerNormal extends CellHandler{ 
        public CellHandlerNormal(int column, int row){ 
            super(column, row);
        }
        
        public CellHandlerNormal(int column, int row, boolean permitido){ 
            super(column, row);
            allowed = permitido;
        }
 
        public void actionPerformed(ActionEvent e){
            Player h;
            Player m;
            int [] move = new int[2];
            
            if(allowed){
                removeSuggestedMove();
                allowed = false;
                b[column][row].setIcon(ic_turn);

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
                    b[column][row].setIcon(ic_turn);
                    allowed = false;

                    f = row;
                    c = column;
                    firstMove = false;
                    
                    // If the opponent is the computer, ask if it swaps
                    if(ObtenernextTurno().isComputer()) {  
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
                            b[c][f].setIcon(ic_turn);

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
                        statusBar.add(swap);
                    }
                }  
                else{ /* If it is not the first time the handler is executed, it means
                       * that the human opponent rejects the swap */
                    try {
                        p.newMove(f, c, turn);
                        changeTurn();
                        b[column][row].setIcon(ic_turn);
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
            b[c][f].setIcon(ic_turn);

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

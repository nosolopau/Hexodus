/*
 * Hexodus >> Main.java
 *
 * Creado el 10 de mayo de 2007 a las 11:16
 *
 * Copyright (C) 2006 - 2008 Pablo Torrecilla
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301, USA. */

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

/** Representa el mdulo de control de la aplicacin y la ventana principal
 *  @author Pau
 *  @version 1.0
 */
public class Main{
   
    /** Crea una nueva instancia de la clase principal Main */
    public Main(){
    }
    
    /** Comienza la ejecucin del programa
     *  @param args Argumentos de la lnea de comandos */
    public static void main(String[] args){        
        GameWindow juego = new GameWindow(6, 0, 1, true);

        WindowCloseHandler ventana = new WindowCloseHandler();
        juego.addWindowListener(ventana);
    }
}

/** Cuadro de dilogo de opciones para crear un nuevo juego */
class OptionsDialog extends JDialog{
    private JRadioButton v1, v2, h1, h2;    // Referencias a los controles
    private JComboBox selDimension;
    private JCheckBox activarSwap;
    private GameWindow juego;
    
    /** Muestra el cuadro para crear una nueva match
     *  @param principal    Regerencia a la ventaja principal del juego */
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
    
    /** Clase para escuchar la pulsacin del botn aceptar */
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

/** Clase para representar el cuadro de dilogo Acerca de */
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
    
    /** Escucha el evento del botn "cerrar" para cerrar la ventana */
    class AcceptHandler implements ActionListener{
        public void actionPerformed(ActionEvent e){    
            dispose();
        }
    }
}

/** Clase que representa una match y la ventana que se utiliza para dar
 *  soporte al nuevo juego, manteniendo la interaccin con el usuario */
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
    private boolean Primera;
    
    /** Crea una nueva ventana de juego
     *  @param dim      Dimensin del board del nuevo juego
     *  @param tipoV    Tipo del player vertical
     *  @param tipoH    Tipo del player horizontal
     *  @param swap     Verdadero si se habilita la regla swap, falso en otro caso */
    public GameWindow(int dimension, int tipoVertical, int tipoHorizontal, boolean swap){
        Dimension = dimension;
        yo = this;
        newGame(dimension, tipoVertical, tipoHorizontal, swap);
        setResizable(false);          
        setTitle("Hexodus");
    }
    
    /** Crea un nuevo juego
     *  @param dim      Dimensin del board del nuevo juego
     *  @param tipoV    Tipo del player vertical
     *  @param tipoH    Tipo del player horizontal
     *  @param swap     Verdadero si se habilita la regla swap, falso en otro caso */
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
        
        Primera = true;         
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
        red = new ImageIcon(ClassLoader.getSystemResource("images/rojo.png"));
        blue = new ImageIcon(ClassLoader.getSystemResource("images/azul.png"));
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
        JMenuItem[] hexodus = {new JMenuItem("Suggest Move"), new JRadioButtonMenuItem("Normal Mode"), new JRadioButtonMenuItem("Expert Mode")};
        JMenuItem[] ayuda = {new JMenuItem("About...")};
        
        ButtonGroup nivel = new ButtonGroup();
        nivel.add(hexodus[1]);
        nivel.add(hexodus[2]);
        
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
        
        // Fuerza el redibujado de la ventana
        Graphics gf = getGraphics();
        if (gf != null) paintComponents(gf);
        else repaint();
                
        ic_turn = red;  
        turn = uno;
        
        if((tipoV == 1) && (tipoH == 1)){
            demo();
        }
        else if(turn.getType() == 1){ // Si el player que comienza es el ordenador
            changeStatus(0);
            if(swap){
                GeneraMoveSwap(turn);
            }
            else{GeneraMove(turn);
            changeStatus(-1);
            }
        }
    }
    
    /** Inhabilita los controles que deben bloquearse en ciertos momentos... */
    public void Inhabilitar(){
        menus[1].setEnabled(false);
    }
    
    /** Y vuelve a habilitarlos */
    public void Habilitar(){
        menus[1].setEnabled(true);
    }
    
    /** Cambia el status que se muestra en la statusBar superior de text
     *  @param  id Identificador del nuevo status */
    public void changeStatus(int id){
        String status = "";
                
        switch(id){
            case 0:
                Inhabilitar();
                status = "Hexodus est pensando...";
                break;
            default:
                Habilitar();
                status = "";
        }
        text.setText(status);
        
        Graphics gf = getGraphics();
        if (gf != null) paintComponents(gf);
        else repaint();
    }
    
    /** Cambia el status que se muestra en la statusBar superior de text
     *  @param  status Cadena con el nuevo status */
    public void changeStatus(String status){
        text.setText(status);
        
        Graphics gf = getGraphics();
        if (gf != null) paintComponents(gf);
        else repaint();
    }
    
    /** Solicita al objeto match que sugiera un movimiento y lo muestra sobre
     *  el board
     *  @param t player para el que se sugiere el movimiento */
    public void SugiereMove(Player t){
        changeStatus(0);
        suggested = p.generateMove(t);

        b[suggested[1]][suggested[0]].setIcon(suggestion);
        changeStatus(-1);
    }
    
    /** Borra del board el ltimo movimiento suggested */
    public void EliminaMoveSugerido(){
        if(suggested != null)
            b[suggested[1]][suggested[0]].setIcon(null);
        suggested = null;
    }
    
    /** Genera un movimiento si la regla de intercambio est activada
     *  @param turn Player en posesin del turn */
    public void GeneraMoveSwap(Player turn){
        int [] jugada = new int[2];
        changeStatus(0);
        jugada = p.generateMove(turn);
        b[jugada[1]][jugada[0]].setIcon(ic_turn);
        changeStatus(-1);

        swap = new JButton();
        swap.setContentAreaFilled(false);
        swap.setText("Swap Move");
        swap.addActionListener(new SwapButtonHandler());
        statusBar.add(swap);

        f = jugada[0];
        c = jugada[1];
        Primera = false;
        Inhabilitar();
        Graphics gf = getGraphics();
        if (gf != null) paintComponents(gf);
        else repaint();
    }
    
    /** Solicita al objeto match que genere un movimiento
     *  @param turn Player en posesin del turn
     *  @return Player que gana la match con este movimiento, si procede */
    public Player GeneraMove(Player turn){
        Player winner = null;
        int [] jugada = new int[2];
        
        changeStatus(0);
        jugada = p.generateMove(turn);
        b[jugada[1]][jugada[0]].setIcon(ic_turn);
        g[jugada[1]][jugada[0]].SeleccionarPermitido(false);
        try {
            winner = p.newMove(jugada[0], jugada[1], turn);
        } catch (OccupiedSquare ex) {
            ex.printStackTrace();
        } catch (NonexistentSquare ex) {
            ex.printStackTrace();
        }
        changeTurn();
        changeStatus(-1);
        
        return winner;
    }
    
    /** Intercambia el turn entre ambos playeres */
    public void changeTurn(){
        if(turn == uno) turn = dos;
        else turn = uno;
        if(ic_turn == blue) ic_turn = red;
        else ic_turn = blue;
    }
    
    /** Devuelve el siguiente player en tener el turn */
    public Player ObtenerSiguienteTurno(){
        if(turn == uno) return dos;
        else return uno;
    }
    
    /** Actualiza la interfaz cuando finaliza la match en curso, mostrando el
     *  status en el cuadro */
    public void finish(Player winner){
        for(int i = 0; i < Dimension; i++){
            for(int j = 0; j < Dimension; j++){
                b[i][j].removeActionListener(g[i][j]);
            }
        }
        Inhabilitar();
        text.setText("Winner: " + winner.getName());
    }
    
    /** Elimina los manejadores especiales para la jugada de intercambio y los
     *  reemplaza por los normales para el juego */
    public void updateSwapHandlers(){
        for(int i = 0; i < Dimension; i++){
            for(int j = 0; j < Dimension; j++){
                boolean bloq = g[i][j].ObtenerPermitido();
                b[i][j].removeActionListener(g[i][j]);
                g[i][j] = new CellHandlerNormal(i, j, bloq);
                b[i][j].addActionListener(g[i][j]);
            }
        }
    }
    
    /** Ejecuta el modo de demostracin */
    public void demo(){
        Inhabilitar();
        
        for(int i = 0; i < Dimension; i++){
            for(int j = 0; j < Dimension; j++){
                b[i][j].removeActionListener(g[i][j]);
            }
        }       
        Player winner = null;
        int [] jugada = new int[2];
        do{
            winner = GeneraMove(turn);

            Graphics gf = getGraphics();
            if (gf != null) paintComponents(gf);
            else repaint();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        } while(winner == null);
        
        Inhabilitar();
        text.setText("Winner: " + winner.getName());
    }
    
    /** Subclase para representar el panel con el board */
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
                
                // muestra la imagen del board centrada.
                if((imwidth > 0) && (imheight > 0)){                
                    g.drawImage(backgroundImage, width / 2 - imwidth / 2, 50, null);
                }
            }
        }
    }
    
    /** Subclase para gestionar el men de la aplicacin */
    class MenuHandler implements ActionListener{
        private int Indice;
        private int Padre;
        
        public MenuHandler(int padre, int indice){
            Indice = indice;
            Padre = padre;
        }
        
        public void actionPerformed(ActionEvent e){
            switch(Padre){
            case 0:
                switch(Indice){
                case 0:
                    OptionsDialog nuevo = new OptionsDialog(yo);
                    nuevo.setVisible(true);
                    break;
                }
                break;
            case 1:
                switch(Indice){
                case 0:
                    SugiereMove(turn);
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
                }
                break;
            case 2:
                switch(Indice){
                case 0:
                    AboutDialog acerca = new AboutDialog(yo);
                    acerca.setVisible(true);
                    break;
                }
                break;
            }
            
        }
    }
    
    /** Subclase para tratar los diferentes eventos relacionados con las
     *  casillas del board de juego.
     *  Es abstracta porque los manejadores de las casillas tienen que controlar
     *  la aplicacin de la regla intercambio en la primera jugada si esa opcin
     *  est habilitada. Para ahorrar comprobaciones, se usa el polimorfismo y se
     *  utiliza un manejador en el primer movimiento y otro para el resto de la
     *  match. */
    abstract class CellHandler implements ActionListener{
        protected int row;             // row de la casilla
        protected int column;          // column de la casilla
        protected boolean allowed;    // Si se puede volver a hacer clic sobre esa casilla
        
        public CellHandler(int column, int row){
            column = column;
            row = row;
            allowed = true;
        }
        
        abstract public void actionPerformed(ActionEvent e);
        
        public boolean ObtenerPermitido(){
            return allowed;
        }
        
        public void SeleccionarPermitido(boolean permitido){
            allowed = permitido;
        }
    }
    
    /** Manejador normal para el juego completo despus de la primera jugada */
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
            int [] jugada = new int[2];
            
            if(allowed){
                EliminaMoveSugerido();
                allowed = false;
                b[column][row].setIcon(ic_turn);

                // Fuerza el redibujado
                Graphics gf = getGraphics();
                if (gf != null) paintComponents(gf);
                else repaint();
                               
                try {
                    h = p.newMove(row,column,turn);
                    if(h == null){
                        changeTurn();    
                        if(turn.isComputer()){
                            m = GeneraMove(turn);
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
    
    /** Manejador especial para la primera jugada si se habilita la regla de intercambio */
    class CellHandlerSwap extends CellHandler{    
        public CellHandlerSwap(int column, int row){ 
            super(column, row);
        }
 
        public void actionPerformed(ActionEvent e){
            Player h;
            Player m;
            int [] jugada = new int[2];
            
            if(allowed){
                allowed = false;
                EliminaMoveSugerido();
                if(Primera){    // Es la primera vez que se utiliza este manejador
                    Inhabilitar();
                    b[column][row].setIcon(ic_turn);
                    allowed = false;

                    f = row;
                    c = column;
                    Primera = false;
                    
                    // Si el contrario es el ordenador, preguntar si se intercambia
                    if(ObtenerSiguienteTurno().isComputer()) {  
                        if(p.offerSwap(f, c)){ // El ordenador acepta el intercambio
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
                        else{ // El ordenador rechaza el intercambio
                            try {
                                p.newMove(f, c, turn);
                                changeTurn();
                            } catch (NonexistentSquare ex) {
                                ex.printStackTrace();
                            } catch (OccupiedSquare ex) {
                                ex.printStackTrace();
                            }

                            GeneraMove(turn);
                            
                            Graphics gf = getGraphics();
                            if (gf != null) paintComponents(gf);
                            else repaint();                           
                        }
                        updateSwapHandlers();
                    }   // Si el contrario es un humano, mostrar el botn
                    else{
                        swap = new JButton();
                        swap.setContentAreaFilled(false);
                        swap.setText("Swap Move");
                        swap.addActionListener(new SwapButtonHandler());
                        statusBar.add(swap);
                    }
                }  
                else{ /* Si no es la primera vez que se ejecuta el manejador, quiere
                       * decir que el contrincante humano y rechaza el intercambio */
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

                    if(turn.isComputer()) GeneraMove(turn);
                    updateSwapHandlers();
                    Habilitar();
                }        
            }
        }
    }
    
    /** Controla la pulsacin del botn de intercambio cuando se ofrece esta
     *  posibilidad al usuario */
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
            if(turn.isComputer()) GeneraMove(turn);
            Habilitar();
        }
    }       
}


/** Terminar el proceso cuando se cierre la ventana */
class WindowCloseHandler extends WindowAdapter {
    public void windowClosing(WindowEvent e) {
        System.exit(0);
    }
}

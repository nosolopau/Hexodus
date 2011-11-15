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

package interfaz;

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
import juego.*;

/** Representa el módulo de control de la aplicación y la ventana principal
 *  @author Pau
 *  @version 1.0
 */
public class Main{
   
    /** Crea una nueva instancia de la clase principal Main */
    public Main(){
    }
    
    /** Comienza la ejecución del programa
     *  @param args Argumentos de la línea de comandos */
    public static void main(String[] args){        
        Juego juego = new Juego(6, 0, 1, true);
        
        GestorBotonCerrar ventana = new GestorBotonCerrar();
        juego.addWindowListener(ventana);
    }
}

/** Cuadro de diálogo de opciones para crear un nuevo juego */
class Opciones extends JDialog{
    private JRadioButton v1, v2, h1, h2;    // Referencias a los controles
    private JComboBox selDimension;
    private JCheckBox activarSwap;
    private Juego juego;
    
    /** Muestra el cuadro para crear una nueva partida
     *  @param principal    Regerencia a la ventaja principal del juego */
    public Opciones(Juego principal){
        super(principal, "Nuevo juego", true);
        setSize(300, 350);
        setResizable(false);
        
        juego = principal;
        
        Container panel = getContentPane();
        panel.setLayout(null);
        
        JPanel opciones = new JPanel();        
        JPanel vertical = new JPanel();
        JPanel horizontal = new JPanel();
        opciones.setBorder(new TitledBorder("Opciones del juego"));
        vertical.setBorder(new TitledBorder("Jugador 1 (vertical)"));
        horizontal.setBorder(new TitledBorder("Jugador 2 (horizontal)"));
        opciones.setBounds(10, 10, 280, 100);
        vertical.setBounds(10, 120, 280, 80);
        horizontal.setBounds(10, 210, 280, 80); 
        panel.add(opciones);
        panel.add(vertical);
        panel.add(horizontal);
        
        JButton Ok = new JButton();        
        Ok.addActionListener(new Aceptar());
        Ok.setBounds(190, 290, 100, 30);
        Ok.setText("Aceptar");
        panel.add(Ok);
              
        ButtonGroup botonesVertical = new ButtonGroup();
        ButtonGroup botonesHorizontal = new ButtonGroup();
        v1 = new JRadioButton("Humano"); 
        v2 = new JRadioButton("Ordenador");
        h1 = new JRadioButton("Humano"); 
        h2 = new JRadioButton("Ordenador");
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
        activarSwap = new JCheckBox("Activar regla de intercambio");
        activarSwap.setSelected(true);
        
        opciones.add(etiDimension);
        opciones.add(selDimension);
        opciones.add(activarSwap);
        vertical.add(v1); 
        vertical.add(v2);
        horizontal.add(h1);
        horizontal.add(h2);
    }
    
    /** Clase para escuchar la pulsación del botón aceptar */
    class Aceptar implements ActionListener{
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
            juego.NuevoJuego(dim, v, h, activarSwap.isSelected());
        }
    }
}

/** Clase para representar el cuadro de diálogo Acerca de */
class AcercaDe extends JDialog{
    public AcercaDe(Juego principal){
        super(principal, "Acerca de Hexodus", true);
        setSize(300, 350);
        setResizable(false);
        
        Container panel = getContentPane();
        panel.setLayout(null);
        
        JPanel opciones = new JPanel(); 
        opciones.setBounds(10, 10, 280, 280);
        panel.add(opciones);
        opciones.setLayout(new FlowLayout());
        JLabel titulo = new JLabel("<html><br><br><center><font size=+4>" + 
                "<b>Hexodus</b></font><br>versión 1.0</center>");

        JLabel texto = new JLabel("<html><br><center>Copyright © 2006 - 2008 " + 
                "Pablo Torrecilla<br>GNU General Public License." + "<br><br>" +
                "pau@nosololinux.com</center>");

        opciones.add(titulo);
        opciones.add(texto);
        
        JButton Ok = new JButton();        
        Ok.addActionListener(new Aceptar());
        Ok.setBounds(190, 290, 100, 30);
        Ok.setText("Cerrar");
        panel.add(Ok);
    }
    
    /** Escucha el evento del botón "cerrar" para cerrar la ventana */
    class Aceptar implements ActionListener{
        public void actionPerformed(ActionEvent e){    
            dispose();
        }
    }
}

/** Clase que representa una partida y la ventana que se utiliza para dar
 *  soporte al nuevo juego, manteniendo la interacción con el usuario */
class Juego extends JFrame{
    private Juego yo;    
    private JMenu[] menus;
    private Panel tablero;
    private JPanel barra;
    private GestorCasilla g[][];
    private JButton b[][];
    private Icon rojo, azul, sugerencia;
    private Icon ic_turno;
    private JLabel texto;
    private JButton swap;
    
    private Jugador turno;
    private Jugador uno;
    private Jugador dos;
    private Partida p;
    private int sugerido[];
    private int Dimension;
    private int f;
    private int c;
    private boolean Primera;
    
    /** Crea una nueva ventana de juego
     *  @param dim      Dimensión del tablero del nuevo juego
     *  @param tipoV    Tipo del jugador vertical
     *  @param tipoH    Tipo del jugador horizontal
     *  @param swap     Verdadero si se habilita la regla swap, falso en otro caso */
    public Juego(int dimension, int tipoVertical, int tipoHorizontal, boolean swap){
        Dimension = dimension;
        yo = this;
        NuevoJuego(dimension, tipoVertical, tipoHorizontal, swap);
        setResizable(false);          
        setTitle("Hexodus");
    }
    
    /** Crea un nuevo juego
     *  @param dim      Dimensión del tablero del nuevo juego
     *  @param tipoV    Tipo del jugador vertical
     *  @param tipoH    Tipo del jugador horizontal
     *  @param swap     Verdadero si se habilita la regla swap, falso en otro caso */
    public void NuevoJuego(int dim, int tipoV, int tipoH, boolean swap){
        Dimension = dim;
        
        int ancho, alto;
        int desplHoriz, desplVert;
        int tamH, tamV;
        double incrH, incrV;
        double despl;
        
        switch(Dimension){
            case 5:
                ancho = 529;
                alto = 423;
                desplHoriz = 178;
                desplVert = 66;
                tamH = 50;
                tamV = 45;
                incrH = 11;
                incrV = 7;
                despl = 30.5;
                break;
            case 6:
                ancho = 620;
                alto = 480;
                desplHoriz = 210;
                desplVert = 67;
                tamH = 50;
                tamV = 45;
                incrH = 11;
                incrV = 7.5;
                despl = 30.5;
                break;                
            default:
                ancho = 710;
                alto = 530;
                desplHoriz = 240;
                desplVert = 68;
                tamH = 50;
                tamV = 45;                
                incrH = 11;
                incrV = 7.5;                
                despl = 30.5;
        }
        setSize(ancho, alto);
        
        Primera = true;         
        tablero = new Panel(Dimension, ancho);
        
        tablero.setLayout(null);
        tablero.setBackground(Color.white);
        setContentPane(tablero);
        
        barra = new JPanel();
        barra.setBackground(Color.white);
        barra.setBounds(0, 5, ancho, 40);

        tablero.add(barra);
        
        texto = new JLabel();
        barra.add(texto);
        rojo = new ImageIcon(ClassLoader.getSystemResource("imagenes/rojo.png"));
        azul = new ImageIcon(ClassLoader.getSystemResource("imagenes/azul.png"));
        sugerencia = new ImageIcon(ClassLoader.getSystemResource("imagenes/sug.png"));

        b = new JButton [Dimension][Dimension];

        g = new GestorCasilla [Dimension][Dimension];
        for(int i = 0; i < Dimension; i++){
            for(int j = 0; j < Dimension; j++){
                b[i][j] = new JButton();
                getContentPane().add(b[i][j]);

                b[i][j].setBounds((int)(desplHoriz + (tamH + incrH) * i - (j * despl)), (int)(desplVert + (tamV + incrV) * j), tamH, tamV);
                b[i][j].setContentAreaFilled(false);
                b[i][j].setBorderPainted(false);
                b[i][j].setFocusPainted(false);
                if(swap) g[i][j] = new GestorCasillaSwap(i, j);
                else g[i][j] = new GestorCasillaNormal(i, j);
                b[i][j].addActionListener(g[i][j]);
            }
        }
        
        menus = new JMenu[] {new JMenu("Juego"), new JMenu("Hexodus"), new JMenu("Ayuda")};
        JMenuItem[] juego = {new JMenuItem("Nuevo juego...")};
        JMenuItem[] hexodus = {new JMenuItem("Sugerir jugada"), new JRadioButtonMenuItem("Modo normal"), new JRadioButtonMenuItem("Modo experto")};
        JMenuItem[] ayuda = {new JMenuItem("Acerca de...")};
        
        ButtonGroup nivel = new ButtonGroup();
        nivel.add(hexodus[1]);
        nivel.add(hexodus[2]);
        
        for(int i = 0; i < juego.length; i++){
            menus[0].add(juego[i]);
            juego[i].addActionListener(new GestorMenu(0, i));
        }
        for(int i = 0; i < hexodus.length; i++){
            menus[1].add(hexodus[i]);
            if(i == 0)menus[1].add(new JSeparator());
            hexodus[i].addActionListener(new GestorMenu(1, i));
        }
        for(int i = 0; i < ayuda.length; i++){
            menus[2].add(ayuda[i]);
            ayuda[i].addActionListener(new GestorMenu(2, i));
        }
        JMenuBar barraMenu = new JMenuBar();
        for(int i = 0; i < menus.length; i++){
            barraMenu.add(menus[i]);
        }
        hexodus[1].setSelected(true);
        setJMenuBar(barraMenu);  
        setVisible(true);
        
        uno = new Jugador(tipoV, 1);
        dos = new Jugador(tipoH, 0);
        
        p = new Partida(dim, swap);
        
        // Fuerza el redibujado de la ventana
        Graphics gf = getGraphics();
        if (gf != null) paintComponents(gf);
        else repaint();
                
        ic_turno = rojo;  
        turno = uno;
        
        if((tipoV == 1) && (tipoH == 1)){
            Demo();
        }
        else if(turno.ObtenerTipo() == 1){ // Si el jugador que comienza es el ordenador
            CambiaEstado(0);
            if(swap){
                GeneraMovimientoSwap(turno);
            }
            else{GeneraMovimiento(turno);
            CambiaEstado(-1);
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
    
    /** Cambia el estado que se muestra en la barra superior de texto
     *  @param  id Identificador del nuevo estado */
    public void CambiaEstado(int id){
        String estado = "";
                
        switch(id){
            case 0:
                Inhabilitar();
                estado = "Hexodus está pensando...";
                break;
            default:
                Habilitar();
                estado = "";
        }
        texto.setText(estado);
        
        Graphics gf = getGraphics();
        if (gf != null) paintComponents(gf);
        else repaint();
    }
    
    /** Cambia el estado que se muestra en la barra superior de texto
     *  @param  estado Cadena con el nuevo estado */
    public void CambiaEstado(String estado){
        texto.setText(estado);
        
        Graphics gf = getGraphics();
        if (gf != null) paintComponents(gf);
        else repaint();
    }
    
    /** Solicita al objeto partida que sugiera un movimiento y lo muestra sobre
     *  el tablero
     *  @param t jugador para el que se sugiere el movimiento */
    public void SugiereMovimiento(Jugador t){
        CambiaEstado(0);
        sugerido = p.GeneraMovimiento(t);

        b[sugerido[1]][sugerido[0]].setIcon(sugerencia);
        CambiaEstado(-1);
    }
    
    /** Borra del tablero el último movimiento sugerido */
    public void EliminaMovimientoSugerido(){
        if(sugerido != null)
            b[sugerido[1]][sugerido[0]].setIcon(null);
        sugerido = null;
    }
    
    /** Genera un movimiento si la regla de intercambio está activada
     *  @param turno Jugador en posesión del turno */
    public void GeneraMovimientoSwap(Jugador turno){
        int [] jugada = new int[2];
        CambiaEstado(0);
        jugada = p.GeneraMovimiento(turno);
        b[jugada[1]][jugada[0]].setIcon(ic_turno);
        CambiaEstado(-1);

        swap = new JButton();
        swap.setContentAreaFilled(false);
        swap.setText("Intercambiar jugada");
        swap.addActionListener(new GestorBotonSwap());
        barra.add(swap);

        f = jugada[0];
        c = jugada[1];
        Primera = false;
        Inhabilitar();
        Graphics gf = getGraphics();
        if (gf != null) paintComponents(gf);
        else repaint();
    }
    
    /** Solicita al objeto partida que genere un movimiento
     *  @param turno Jugador en posesión del turno
     *  @return Jugador que gana la partida con este movimiento, si procede */
    public Jugador GeneraMovimiento(Jugador turno){
        Jugador ganador = null;
        int [] jugada = new int[2];
        
        CambiaEstado(0);
        jugada = p.GeneraMovimiento(turno);
        b[jugada[1]][jugada[0]].setIcon(ic_turno);
        g[jugada[1]][jugada[0]].SeleccionarPermitido(false);
        try {
            ganador = p.NuevoMovimiento(jugada[0], jugada[1], turno);
        } catch (CasillaOcupada ex) {
            ex.printStackTrace();
        } catch (CasillaInexistente ex) {
            ex.printStackTrace();
        }
        CambiaTurno();
        CambiaEstado(-1);
        
        return ganador;
    }
    
    /** Intercambia el turno entre ambos jugadores */
    public void CambiaTurno(){
        if(turno == uno) turno = dos;
        else turno = uno;
        if(ic_turno == azul) ic_turno = rojo;
        else ic_turno = azul;
    }
    
    /** Devuelve el siguiente jugador en tener el turno */
    public Jugador ObtenerSiguienteTurno(){
        if(turno == uno) return dos;
        else return uno;
    }
    
    /** Actualiza la interfaz cuando finaliza la partida en curso, mostrando el
     *  estado en el cuadro */
    public void Finalizar(Jugador ganador){
        for(int i = 0; i < Dimension; i++){
            for(int j = 0; j < Dimension; j++){
                b[i][j].removeActionListener(g[i][j]);
            }
        }
        Inhabilitar();
        texto.setText("Gana el " + ganador.ObtenerNombre());
    }
    
    /** Elimina los manejadores especiales para la jugada de intercambio y los
     *  reemplaza por los normales para el juego */
    public void ActualizaManejadoresSwap(){
        for(int i = 0; i < Dimension; i++){
            for(int j = 0; j < Dimension; j++){
                boolean bloq = g[i][j].ObtenerPermitido();
                b[i][j].removeActionListener(g[i][j]);
                g[i][j] = new GestorCasillaNormal(i, j, bloq);
                b[i][j].addActionListener(g[i][j]);
            }
        }
    }
    
    /** Ejecuta el modo de demostración */
    public void Demo(){
        Inhabilitar();
        
        for(int i = 0; i < Dimension; i++){
            for(int j = 0; j < Dimension; j++){
                b[i][j].removeActionListener(g[i][j]);
            }
        }       
        Jugador ganador = null;
        int [] jugada = new int[2];
        do{
            ganador = GeneraMovimiento(turno);

            Graphics gf = getGraphics();
            if (gf != null) paintComponents(gf);
            else repaint();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        } while(ganador == null);
        
        Inhabilitar();
        texto.setText("Gana el " + ganador.ObtenerNombre());
    }
    
    /** Subclase para representar el panel con el tablero */
    class Panel extends JPanel{ 
        Image bgimage = null;
        int ancho;
        
        public Panel(int dimension, int ancho){
            this.ancho = ancho;
            String rutaImagen = "imagenes/" + dimension + ".png";
            MediaTracker mt = new MediaTracker(this);
            bgimage = Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource(rutaImagen));

            //bgimage = Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource(rutaImagen));

            
            mt.addImage(bgimage, 0);
            try{
                mt.waitForAll();
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
        }    
        
        protected void paintComponent(Graphics g){
            super.paintComponent(g);
        
            if(bgimage != null){
                int imwidth = bgimage.getWidth(null);
                int imheight = bgimage.getHeight(null);
                
                // muestra la imagen del tablero centrada.
                if((imwidth > 0) && (imheight > 0)){                
                    g.drawImage(bgimage, ancho / 2 - imwidth / 2, 50, null);
                }
            }
        }
    }
    
    /** Subclase para gestionar el menú de la aplicación */
    class GestorMenu implements ActionListener{
        private int Indice;
        private int Padre;
        
        public GestorMenu(int padre, int indice){
            Indice = indice;
            Padre = padre;
        }
        
        public void actionPerformed(ActionEvent e){
            switch(Padre){
            case 0:
                switch(Indice){
                case 0:
                    Opciones nuevo = new Opciones(yo);
                    nuevo.setVisible(true);
                    break;
                }
                break;
            case 1:
                switch(Indice){
                case 0:
                    SugiereMovimiento(turno);
                    break;
                case 1:
                    try {
                        p.EstableceNivel(1);
                    } catch (NivelIncorrecto ex) {
                        ex.printStackTrace();
                    }
                    break;
                case 2:
                    try {
                        p.EstableceNivel(2);
                    } catch (NivelIncorrecto ex) {
                        ex.printStackTrace();
                    }
                    break;
                }
                break;
            case 2:
                switch(Indice){
                case 0:
                    AcercaDe acerca = new AcercaDe(yo);
                    acerca.setVisible(true);
                    break;
                }
                break;
            }
            
        }
    }
    
    /** Subclase para tratar los diferentes eventos relacionados con las
     *  casillas del tablero de juego.
     *  Es abstracta porque los manejadores de las casillas tienen que controlar
     *  la aplicación de la regla intercambio en la primera jugada si esa opción
     *  está habilitada. Para ahorrar comprobaciones, se usa el polimorfismo y se
     *  utiliza un manejador en el primer movimiento y otro para el resto de la
     *  partida. */
    abstract class GestorCasilla implements ActionListener{
        protected int Fila;             // Fila de la casilla
        protected int Columna;          // Columna de la casilla
        protected boolean Permitido;    // Si se puede volver a hacer clic sobre esa casilla
        
        public GestorCasilla(int columna, int fila){
            Columna = columna;
            Fila = fila;
            Permitido = true;
        }
        
        abstract public void actionPerformed(ActionEvent e);
        
        public boolean ObtenerPermitido(){
            return Permitido;
        }
        
        public void SeleccionarPermitido(boolean permitido){
            Permitido = permitido;
        }
    }
    
    /** Manejador normal para el juego completo después de la primera jugada */
    class GestorCasillaNormal extends GestorCasilla{ 
        public GestorCasillaNormal(int columna, int fila){ 
            super(columna, fila);
        }
        
        public GestorCasillaNormal(int columna, int fila, boolean permitido){ 
            super(columna, fila);
            Permitido = permitido;
        }
 
        public void actionPerformed(ActionEvent e){
            Jugador h;
            Jugador m;
            int [] jugada = new int[2];
            
            if(Permitido){
                EliminaMovimientoSugerido();
                Permitido = false;
                b[Columna][Fila].setIcon(ic_turno);

                // Fuerza el redibujado
                Graphics gf = getGraphics();
                if (gf != null) paintComponents(gf);
                else repaint();
                               
                try {
                    h = p.NuevoMovimiento(Fila,Columna,turno);
                    if(h == null){
                        CambiaTurno();    
                        if(turno.EsOrdenador()){
                            m = GeneraMovimiento(turno);
                            if(m != null) Finalizar(m);
                        }
                    } 
                    else{
                        Finalizar(h);
                    }
                }catch(CasillaOcupada ex){
                    System.out.println("Ocupada");
                }catch(CasillaInexistente ex){
                    System.out.println("Fuera de rango");
                }
            }
        }
    }
    
    /** Manejador especial para la primera jugada si se habilita la regla de intercambio */
    class GestorCasillaSwap extends GestorCasilla{    
        public GestorCasillaSwap(int columna, int fila){ 
            super(columna, fila);
        }
 
        public void actionPerformed(ActionEvent e){
            Jugador h;
            Jugador m;
            int [] jugada = new int[2];
            
            if(Permitido){
                Permitido = false;
                EliminaMovimientoSugerido();
                if(Primera){    // Es la primera vez que se utiliza este manejador
                    Inhabilitar();
                    b[Columna][Fila].setIcon(ic_turno);
                    Permitido = false;

                    f = Fila;
                    c = Columna;
                    Primera = false;
                    
                    // Si el contrario es el ordenador, preguntar si se intercambia
                    if(ObtenerSiguienteTurno().EsOrdenador()) {  
                        if(p.OfrecerIntercambio(f, c)){ // El ordenador acepta el intercambio
                            Graphics gf = getGraphics();
                            if (gf != null) paintComponents(gf);
                            else repaint();                 
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                            CambiaEstado("Hexodus ha intercambiado la jugada");
                            CambiaTurno();
                            b[c][f].setIcon(ic_turno);

                            try {
                                p.NuevoMovimiento(f, c, turno);
                            } catch (CasillaInexistente ex) {
                                ex.printStackTrace();
                            } catch (CasillaOcupada ex) {
                                ex.printStackTrace();
                            }
                            CambiaTurno();
                        }
                        else{ // El ordenador rechaza el intercambio
                            try {
                                p.NuevoMovimiento(f, c, turno);
                                CambiaTurno();
                            } catch (CasillaInexistente ex) {
                                ex.printStackTrace();
                            } catch (CasillaOcupada ex) {
                                ex.printStackTrace();
                            }

                            GeneraMovimiento(turno);
                            
                            Graphics gf = getGraphics();
                            if (gf != null) paintComponents(gf);
                            else repaint();                           
                        }
                        ActualizaManejadoresSwap();
                    }   // Si el contrario es un humano, mostrar el botón
                    else{
                        swap = new JButton();
                        swap.setContentAreaFilled(false);
                        swap.setText("Intercambiar jugada");
                        swap.addActionListener(new GestorBotonSwap());
                        barra.add(swap);
                    }
                }  
                else{ /* Si no es la primera vez que se ejecuta el manejador, quiere
                       * decir que el contrincante humano y rechaza el intercambio */
                    try {
                        p.NuevoMovimiento(f, c, turno);
                        CambiaTurno();
                        b[Columna][Fila].setIcon(ic_turno);
                        p.NuevoMovimiento(Fila, Columna, turno);
                    }catch (CasillaOcupada ex) {
                        System.out.println("Ocupada");
                    }catch (CasillaInexistente ex) {
                        System.out.println("Fuera de rango");
                    }
                    CambiaTurno();
                    swap.setVisible(false);

                    Graphics gf = getGraphics();
                    if (gf != null) paintComponents(gf);
                    else repaint();

                    if(turno.EsOrdenador()) GeneraMovimiento(turno);
                    ActualizaManejadoresSwap();
                    Habilitar();
                }        
            }
        }
    }
    
    /** Controla la pulsación del botón de intercambio cuando se ofrece esta
     *  posibilidad al usuario */
    class GestorBotonSwap implements ActionListener{
        public void actionPerformed(ActionEvent e){
            CambiaTurno();
            b[c][f].setIcon(ic_turno);

            try{
                p.NuevoMovimiento(f, c, turno);
            }catch (CasillaInexistente ex){
                ex.printStackTrace();
            }catch (CasillaOcupada ex){
                ex.printStackTrace();
            }
            CambiaTurno();

            ActualizaManejadoresSwap();
            swap.setVisible(false);
            if(turno.EsOrdenador()) GeneraMovimiento(turno);
            Habilitar();
        }
    }       
}


/** Terminar el proceso cuando se cierre la ventana */
class GestorBotonCerrar extends WindowAdapter {
    public void windowClosing(WindowEvent e) {
        System.exit(0);
    }
}

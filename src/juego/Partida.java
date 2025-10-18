/*
 * Hexodus >> Partida.java
 *
 * Creado el 21 de junio de 2006 a las 18:39
 *
 * Copyright (C) 2006 - 2008 Pablo Torrecilla. GNU General Public License.
 */

package juego;
import heuristica.Factoria;
import heuristica.Heuristica;
import java.util.*;

/** Representa una partida del juego.
 *  @author Pau
 *  @version 1.0
 */
public class Partida{
    private int NumeroMovimientos;  // N�mero de movimientos
    private Tablero Tab;            // Referencia al tablero de juego
    private Movimiento [] Mov;      // Vector de movimientos
    private Heuristica Motor;
    
    /** Crea una nueva partida (representada en un objeto tipo Partida)
     *  @param dim  Dimension del tablero de la partida
     *  @param swap Valor l�gico que indica si est� habilitada la jugada swap */
    public Partida(int dim, boolean swap){
        NumeroMovimientos = 0;
        Tab = new Tablero(dim);
        Mov = new Movimiento[dim*dim];
        
        Factoria Fact = new Factoria();
        Motor = Fact.NuevaHeuristica(dim, 1, swap);
    }
    
    /** Pregunta a la heur�stica si debe intercambiarse la primera jugada
     *  @param f    Fila de la primera jugada
     *  @param c    Columna de la primera jugada  
     *  @return     Verdadero si debe intercambiarse y falso en otro caso */
    public boolean OfrecerIntercambio(int f, int c){
        return Motor.DecidirIntercambio(f, c);
    }
    
    /** Establece el nivel de la heur�stica
     *  @param nivel    El nuevo nivel de la heur�stica */
    public void EstableceNivel(int nivel) throws NivelIncorrecto{
        if((nivel == 1) || (nivel == 2))
            Motor.EstablecerNivel(nivel);
        else throw new NivelIncorrecto();
    }
    
    /** Pide a la heur�stica que devuelva el mejor movimiento disponible para
     *  el jugador que se pase como argumento
     *  @param j    Jugador para el que calcular la jugada
     *  @return     Un array de dos enteros que representan la fila y la columna respectivamente */
    public int [] GeneraMovimiento(Jugador j){
        int [] m = new int[2];
        m = Motor.ElegirMovimiento(j.ObtenerPosicion(), NumeroMovimientos);
        
        return m;
    }
    
    /** Crea un nuevo movimiento en la partida
     *  @param f    Fila del movimiento
     *  @param c    Columna del movimiento
     *  @param j    Jugador que realia el movimiento
     *  @return Una referencia a un eventual jugador ganador o
     *          una referencia igual a null si no hay ninguno ganador en este turno
     *  @throws CasillaOcupada      Si la casilla objetivo esta ocupada por otra
     *  @throws CasillaInexistente  Si la casilla objetivo esta fuera del rango del tablero
     */
    public Jugador NuevoMovimiento(int f, int c, Jugador j) throws CasillaOcupada, CasillaInexistente{
        Jugador aux = null;
        aux = Tab.Ocupar(f,c,j);

        Mov[NumeroMovimientos] = new Movimiento(NumeroMovimientos, f, c, j);
        
        Motor.NuevoMovimiento(f, c, j.ObtenerPosicion());
        
        NumeroMovimientos++;
        return aux;
    }
}

/** Representa el tablero de juego
 *  @author Pau
 */
class Tablero {
    static int J1 = 1;      // Macros para representar el estado de las casillas
    static int J2 = 2;
    static int NINGUNO = 0;
    private int Dimension;  // Dimensi�n del tablero
    private Casilla [][]c;  // Vector de casillas que forman el tablero
    private Borde N;        // Bordes del tablero para conectar las casillas
    private Borde S;
    private Borde E;
    private Borde O;
    
    /** Crea un nuevo tablero */
    public Tablero(int dim) {
        c = new Casilla[dim][dim];
        Dimension = dim;
        
        for(int i=0; i<dim; i++)
            for(int j=0; j<dim; j++)
                c[i][j] = new Casilla(i, j, i*dim + j);
        
        N = new Borde('N');
        S = new Borde('S');
        E = new Borde('E');
        O = new Borde('O');
    }
    
    /** Ocupa una casilla del tablero que se pase con los argumentos
     *  @param fl   Fila del tablero que se ocupa
     *  @param cl   Columna del tablero que se ocupa
     *  @param j    Jugador que ocupa la fila y la columna
     *  @return Una referencia al jugador que gana la partida o a null si no gana nadie
     *  @throws CasillaOcupada      Si la casilla objetivo esta ocupada por otra
     *  @throws CasillaInexistente  Si la casilla objetivo esta fuera del rango del tablero */
    public Jugador Ocupar(int fl, int cl, Jugador j) throws CasillaOcupada, CasillaInexistente{
        // Excepciones que lanza Tablero: en caso de casilla ocupada y en caso de fila y columna fuera de rango
        if((fl >= Dimension) || (cl >= Dimension) || (cl < 0) || (fl < 0)) throw new CasillaInexistente();
        if(c[fl][cl].ObtenerOcupada() == true) throw new CasillaOcupada();
        
        // Si todo va bien, devuelve el jugador que gana o un puntero a null si no gana nadie
	c[fl][cl].Ocupar(j);

	return(Unir(fl, cl, j));
    }
    
    /** Devuelve verdadero si las filas y las columnas que se le pasan como argumento pertenecen
     *  a dos casillas vecinas. Para ello, rellena una lista con las vecinas de la primera casilla
     *  y posteriormente busca la segunda casilla en esa lista.
     *  @param f1   Fila de la primera casilla
     *  @param c1   Columna de la primera casilla
     *  @param f2   Fila de la segunda casilla
     *  @param c2   Columna de la segunda casilla
     *  @return     Devuelve verdadero si las casillas son vecinas
     *  @throws CasillaInexistente  Si la casilla objetivo esta fuera del rango del tablero */
    public boolean SonVecinas(int f1, int c1, int f2, int c2) throws CasillaInexistente{
        Casilla [] lista;
        Casilla b = c[f2][c2];

        if((f1 >= Dimension) || (c1 >= Dimension) || (c1 < 0) || (f1 < 0) || (f2 >= Dimension) || (c2 >= Dimension) || (c2 < 0) || (f2 < 0))
            throw new CasillaInexistente();
        
        if((c1 == 0) && (f1 != 0) && (f1 != Dimension-1)){ // Columna izda excepto esquinas
            lista = new Casilla[4];
            lista[0]=c[f1][c1+1];
            lista[1]=c[f1+1][c1+1];
            lista[2]=c[f1-1][c1];
            lista[3]=c[f1+1][c1];
        }
        else if((c1 == Dimension-1) && (f1 != 0) && (f1 != Dimension-1)){ // Col dcha salvo esquinas
            lista = new Casilla[4];
            lista[0]=c[f1][c1-1];
            lista[1]=c[f1-1][c1-1];
            lista[2]=c[f1-1][c1];
            lista[3]=c[f1+1][c1];
        }
        else if((f1 == 0) && (c1 != 0) && (c1 != Dimension-1)){
            lista = new Casilla[4];
            lista[0]=c[f1+1][c1];
            lista[1]=c[f1+1][c1+1];
            lista[2]=c[f1][c1+1];
            lista[3]=c[f1][c1-1];
        }
        else if((f1 == Dimension-1) && (c1 != 0) && (c1 != Dimension-1)){
            lista = new Casilla[4];
            lista[0]=c[f1][c1-1];
            lista[1]=c[f1][c1+1];
            lista[2]=c[f1-1][c1-1];
            lista[3]=c[f1-1][c1];
        }
        else if((c1 == 0) && (f1 == 0)){
            lista = new Casilla[3];
            lista[0]=c[f1][c1+1];
            lista[1]=c[f1+1][c1];
            lista[2]=c[f1+1][c1+1];
        }
        else if((c1 == 0) && (f1 == Dimension-1)){
            lista = new Casilla[2];
            lista[0]=c[f1][c1+1];
            lista[1]=c[f1-1][c1];
        }
        else if((c1 == Dimension-1) && (f1 == Dimension-1)){
            lista = new Casilla[3];
            lista[0]=c[f1][c1-1];
            lista[1]=c[f1-1][c1];
            lista[2]=c[f1-1][c1-1];
        }
        else if((c1 == Dimension-1) && (f1 == 0)){
            lista = new Casilla[2];
            lista[0]=c[f1][c1-1];
            lista[1]=c[f1+1][c1];				
        }
        else{
            lista = new Casilla[6];
            lista[0]=c[f1-1][c1-1];
            lista[1]=c[f1-1][c1];
            lista[2]=c[f1][c1-1];
            lista[3]=c[f1][c1+1];
            lista[4]=c[f1+1][c1];
            lista[5]=c[f1+1][c1+1];
        }
        
        for(int i = 0; i < lista.length; i++)
            if(lista[i] == b) return true;
        
        return false;
    }
    
    /** Une la casilla que se pasa por argumento con los bordes del jugador, si procede
     *  @param fl   Fila de la casilla
     *  @param cl   Columna de la casilla
     *  @param j    Jugador
     *  @return     Una referencia a un jugador si este resulta ganador */
    public Jugador Unir(int fl, int cl, Jugador j){
        Casilla [] lista;
	int max = 0;
	Borde unido = null;
	
	boolean uniones = false;
        
	lista =  new Casilla[7];
	if(c[fl][cl].ObtenerConectada() != null)
            return null;
		
        if(j.ObtenerColor() == J1){
            if(fl == 0){
                c[fl][cl].Unir(N);
                unido = N;
                uniones = true;
            }
            else if(fl == Dimension-1){
                c[fl][cl].Unir(S);
                unido = S;
                uniones = true;
            }

            if((cl == 0) && (fl != 0) && (fl != Dimension-1)){ // Columna izda excepto esquinas
                max = 4;
                lista[0]=c[fl][cl+1];
                lista[1]=c[fl+1][cl+1];
                lista[2]=c[fl-1][cl];
                lista[3]=c[fl+1][cl];
            }
            else if((cl == Dimension-1) && (fl != 0) && (fl != Dimension-1)){ // Col dcha salvo esquinas
                max = 4;
               lista[0]=c[fl][cl-1];
                lista[1]=c[fl-1][cl-1];
                lista[2]=c[fl-1][cl];
                lista[3]=c[fl+1][cl];
            }
            else if((fl == 0) && (cl != 0) && (cl != Dimension-1)){
                max = 4;
                lista[0]=c[fl+1][cl];
                lista[1]=c[fl+1][cl+1];
                lista[2]=c[fl][cl+1];
                lista[3]=c[fl][cl-1];
            }
            else if((fl == Dimension-1) && (cl != 0) && (cl != Dimension-1)){
                max = 4;
                lista[0]=c[fl][cl-1];
                lista[1]=c[fl][cl+1];
                lista[2]=c[fl-1][cl-1];
                lista[3]=c[fl-1][cl];
            }
            else if((cl == 0) && (fl == 0)){
                max = 3;
                lista[0]=c[fl][cl+1];
                lista[1]=c[fl+1][cl];
                lista[2]=c[fl+1][cl+1];
            }
            else if((cl == 0) && (fl == Dimension-1)){
                max = 2;
                lista[0]=c[fl][cl+1];
                lista[1]=c[fl-1][cl];
            }
            else if((cl == Dimension-1) && (fl == Dimension-1)){
                max = 3;
                lista[0]=c[fl][cl-1];
                lista[1]=c[fl-1][cl];
                lista[2]=c[fl-1][cl-1];
            }
            else if((cl == Dimension-1) && (fl == 0)){
                max = 2;
                lista[0]=c[fl][cl-1];
                lista[1]=c[fl+1][cl];				
            }
            else{
                max = 6;
                lista[0]=c[fl-1][cl-1];
                lista[1]=c[fl-1][cl];
                lista[2]=c[fl][cl-1];
                lista[3]=c[fl][cl+1];
                lista[4]=c[fl+1][cl];
                lista[5]=c[fl+1][cl+1];
            }
	}
        else if(j.ObtenerColor() == J2){
            if(cl == 0){
                c[fl][cl].Unir(O);
                unido = O;
                uniones = true;
            }
            else if(cl == Dimension-1){
                c[fl][cl].Unir(E);
                unido = E;
                uniones = true;
            }

            if((fl == 0) && (cl != 0) && (cl != Dimension-1)){ // Fila norte excepto esquinas
                max = 4;	
                lista[0]=c[fl+1][cl];
                lista[1]=c[fl+1][cl+1];
                lista[2]=c[fl][cl-1];
                lista[3]=c[fl][cl+1];
            }
            else if((fl == Dimension-1) && (cl != 0) && (cl != Dimension-1)){ // Fila sur salvo esquinas
                max = 4;
                lista[0]=c[fl-1][cl];
                lista[1]=c[fl-1][cl-1];
                lista[2]=c[fl][cl-1];
                lista[3]=c[fl][cl+1];
            }
            else if((cl == 0) && (fl != 0) && (fl != Dimension-1)){ // Columna Oeste excepto esquinas
                max = 4;
                lista[0]=c[fl][cl+1];
                lista[1]=c[fl+1][cl+1];
                lista[2]=c[fl+1][cl];
                lista[3]=c[fl-1][cl];
            }
            else if((cl == Dimension-1) && (fl != 0) && (fl != Dimension-1)){ // Columna Este excepto esquinas
                max = 4;
                lista[0]=c[fl-1][cl];
                lista[1]=c[fl+1][cl];
                lista[2]=c[fl-1][cl-1];
                lista[3]=c[fl][cl-1];
            }
            else if((fl == 0) && (cl == 0)){ //
                max = 3;
                lista[0]=c[fl+1][cl];
                lista[1]=c[fl][cl+1];
                lista[2]=c[fl+1][cl+1];
            }
            else if((fl == 0) && (cl == Dimension-1)){ //
                max = 2;
                lista[0]=c[fl+1][cl];
                lista[1]=c[fl][cl-1];
            }
            else if((fl == Dimension-1) && (cl == Dimension-1)){
                max = 3;
                lista[0]=c[fl-1][cl];
                lista[1]=c[fl][cl-1];
                lista[2]=c[fl-1][cl-1];
            }
            else if((fl == Dimension-1) && (cl == 0)){
                max = 2;
                lista[0]=c[fl-1][cl];
                lista[1]=c[fl][cl+1];			
            }
            else{
                max = 6;
                lista[0]=c[fl-1][cl-1];
                lista[1]=c[fl-1][cl];
                lista[2]=c[fl][cl-1];
                lista[3]=c[fl][cl+1];
                lista[4]=c[fl+1][cl];
                lista[5]=c[fl+1][cl+1];
            }
	}
	
        // Declaraciones previas
	boolean conexiones = false;
	Borde flag = null;
        Borde con = null;
				
	// Primera pasada: observa si alg�n vecino est� unido con un borde
	for(int i=0; i<max; i++){
            if(lista[i].ObtenerOcupante() == j){    // Si el vecino es una ficha de su mismo color
                con = lista[i].ObtenerConectada();	
                if(con != null){                    // Si ese vecino de su color est� conectado a alg�n borde...
                    c[fl][cl].Unir(con);            // ...se conecta al mismo borde
                    uniones = true;                 // Flag para se�alar que se han realizado uniones en esta pasada
                    if((unido != null) && (con != unido)) return j;     // Se uni� a dos bordes distintos en dos pasadas diferentes: GANADOR
                    if(conexiones){                                     // Otro flag para detectar si se hicieron uniones entre dos casillas unidas a dos bordes diferentes
                        if(flag != lista[i].ObtenerConectada())
                            return j;                                   // Se uni� en una pasada anterior a un borde distinto del actual: GANADOR
                    }
                    else{
                        flag = lista[i].ObtenerConectada();
                    }
                    conexiones = true;
                }
            }
	}
	// Segunda pasada para revisar los enlaces una vez se han realizado uniones
	for(int m=0; m<max; m++)
            if((lista[m].ObtenerOcupante() == j) && uniones)
                Unir(lista[m].ObtenerFila(), lista[m].ObtenerColumna(), j);
	
	return null;
    }

    /** Representa cada uno de los bordes del tablero
     */
    class Borde {
        private char Nombre;

        public Borde(char nombre){
            Nombre = nombre;
        }
        public char ObtenerNombre(){
            return Nombre;
        }
    }

    /** Representa una casilla del tablero
     */
    class Casilla{
        private int Fila;
        private int Columna;
        private int Identificador;  // Identificador �nico de la casilla
        private boolean Ocupada;    // Verdadero si est� ocupada, Falso en caso contrario
        private Jugador Ocupante;
        private Borde Conectada;    // Referencia del borde al que est� conectada

        public Casilla(int fl, int cl, int id){
            Fila = fl;
            Columna = cl;
            Ocupante = null;
            Ocupada = false;
            Conectada = null;
            Identificador = id;
        }

        public void Ocupar(Jugador j){
            Ocupada = true;
            Ocupante = j;
        }
        public Jugador ObtenerOcupante(){
            return Ocupante;
        }
        public boolean ObtenerOcupada(){
            return Ocupada;
        }
        public Borde ObtenerConectada(){
            return Conectada;
        }
        public void Unir(Borde b){
            Conectada = b;
        }
        public int ObtenerFila(){
            return Fila;
        }
        public int ObtenerColumna(){
            return Columna;
        }

        /** Muestra la casilla por la salida est�ndar */
        public void Mostrar(){
            System.out.println("(" + Fila + ", " + Columna + ")");
        }
    }
}
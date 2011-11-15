/*
 * Hexodus >> Heuristica.java
 *
 * Creado el 25 de noviembre de 2006 a las 23:08
 *
 * Copyright (C) 2006 - 2008 Pablo Torrecilla. GNU General Public License
 */

package heuristica;
import java.util.*;


/**
 *  Representa el motor de juego del programa, encargado de calcular el valor numérico
 *  de cada jugada simulada, construir el árbol de jugadas y devolver la mejor opción
 *  disponible.
 *  Es una clase abstracta para soportar dos posibles implementaciones, ligadas
 *  dinámicamente: una con varios hilos preparada para varios procesadores y otra
 *  monohilo para sistemas de un único procesador.
 *
 *  @author Pau
 *  @version 1.0
 */
public abstract class Heuristica {
    protected int Dimension;    // Dimensión del tablero sobre el que se aplica la heurística
    protected int ProfMax;      // Profundidad máxima del árbol de búsqueda
    protected boolean Swap;     // Es verdadero si el intercambio está activado
    protected int [][][] Favoritos;
    
    /** Crea una nueva instancia de Heuristica. Inicializa las listas y estructuras
     *  necesarias y define las conexiones entre bordes y el resto del tablero
     *  @param dimension Dimensión del tablero de juego
     *  @param nivel Nivel de juego
     *  @param swap Verdadero si la regla swap está activada */
    public Heuristica(int dimension, int nivel, boolean swap) {
        Dimension = dimension;
        ProfMax = nivel;
        Swap = swap;
        // Las mejores jugadas para empezar:                1                2                3
        Favoritos = new int [][][] {{{0, 0},{0, 0}}, {{0, 0},{0, 0}}, {{1, 1},{0, 0}}, {{1, 1},{1, 2}},
        //         4                5                6                7                8                9
            {{2, 2},{1, 2}}, {{2, 2},{2, 3}}, {{3, 3},{3, 2}}, {{3, 3},{4, 5}}, {{4, 4},{5, 4}}, {{4, 4},{4, 5}},
        //         10               11
            {{5, 5},{6, 6}}, {{5, 5},{8, 9}}};
    }
    
    /** Modifica el nivel de juego del sistema
     *  @param nivel La profundidad del árbol de búsqueda de jugadas */
    public void EstablecerNivel(int nivel){
        ProfMax = nivel;
    }
    
    /** Informa de un nuevo movimiento y crea las estructuras asociadas
     *  @param fila La fila del movimiento
     *  @param columna La columna del movimiento
     *  @param color El color del jugador que ejecuta el movimiento */
    public abstract void NuevoMovimiento(int fila, int columna, int color);
    
    /** Elige el mejor movimiento para un jugador en el tablero actual 
     *  @param color Color del jugador para el que se desea obtener el movimiento
     *  @return Array de dos enteros que denotan fila y columna del mejor movimiento */
    public abstract int[] ElegirMovimiento(int color, int movimiento);
    protected abstract double AlfaBetaMin(Simulacion s, int prof, double alpha, double beta) throws CasillaInexistente;
    protected abstract double AlfaBetaMax(Simulacion s, int prof, double alpha, double beta) throws CasillaInexistente;
    
    public boolean DecidirIntercambio(int f, int c){
        if((f == Favoritos[Dimension][0][0]) && (c == Favoritos[Dimension][0][1]))
            return true;
        else return false;
    }
}

/**
 *  Heurística adaptada a sistemas de un único procesador
 */
class Monohilo extends Heuristica{
    private Simulacion Base;  // Simulación sobre la que se ejecutan las nuevas jugadas
    private Casilla MejorMax;
    private Casilla MejorMin;
    
    public Monohilo(int dim, int prof, boolean swap) {
        super(dim, prof, swap);
        
        MejorMax = null;
        MejorMin = null;
        Base = new Simulacion(Dimension);
    }
   
    public void NuevoMovimiento(int fila, int columna, int color){
        Simulacion nueva = new Simulacion(Base, fila, columna, color);
        Base = nueva;
    }
    
    /** Genera un movimiento legal al azar entre los disponibles
     *  @return La casilla sobre la que se ejecuta el movimiento aleatorio */
    private Casilla GenerarMovimientoAleatorio(){
        ArrayList<Casilla> Libres = Base.ObtenerCeldasLibres();
        int numeroAleatorio = (int)(Math.random()*Libres.size());
        return Libres.get(numeroAleatorio);
    }
    
    public int [] ElegirMovimiento(int color, int movimiento){
        int [] vector = new int [2];
        if(movimiento == 0){
            int s = 0;
            if(Swap) s = 1;
            vector[0] = Favoritos[Dimension][s][0];
            vector[1] = Favoritos[Dimension][s][1];
            return vector;
        }
                
        Casilla Mejor = null;
        MejorMax = null;
        MejorMin = null;
        
        double rs;
        
        if(color == 1){
            rs = AlfaBetaMax(Base, ProfMax, 0.0, Double.POSITIVE_INFINITY);
            Mejor = MejorMax;
        }
        else{
            rs = AlfaBetaMin(Base, ProfMax, 0.0, Double.POSITIVE_INFINITY);
            Mejor = MejorMin;
        }
        if(Mejor == null){
            System.out.println("Azar");
            Mejor = GenerarMovimientoAleatorio();
        }

        vector[0] = Mejor.ObtenerFila();
        vector[1] = Mejor.ObtenerColumna();
        return vector;
    }    
    
    public double AlfaBetaMax(Simulacion s, int nivel, double alfa, double beta) { 
        if(nivel == 0){
            return s.CalcularValor();
        }
        
        ArrayList <Casilla> Libres = s.ObtenerCeldasLibres();
        Iterator <Casilla> lib = Libres.iterator();
        while(lib.hasNext()){ 
            Casilla c = lib.next();
            Simulacion n = new Simulacion(s, c, 1);
          
            double valor = AlfaBetaMin(n, nivel - 1, alfa, beta);
            
            if(alfa < valor){
                alfa = valor;
                if(nivel == ProfMax) // Garantiza que la mejor jugada se genere en el último nivel
                    MejorMax = c;
            }

            n.Restaurar();
            
            if( alfa >= beta ){
                return alfa;
            }
        }
        return alfa;
    }
    
    public double AlfaBetaMin(Simulacion s, int nivel, double alfa, double beta) { 
        if(nivel == 0){
            return s.CalcularValor();
        }
        
        ArrayList <Casilla> Libres = s.ObtenerCeldasLibres();
        Iterator <Casilla> lib = Libres.iterator();
        while(lib.hasNext()){ 
            Casilla c = (Casilla)lib.next();
            Simulacion n = new Simulacion(s, c, 0);
          
            double valor = AlfaBetaMax(n, nivel - 1, alfa, beta);

            if(valor < beta){
                beta = valor;
                if(nivel == ProfMax)
                    MejorMin = c;
            }
            
            n.Restaurar();
            
            if( alfa >= beta ){
                return beta;
            }
        } 
        return beta;
    }
}

/**
 *  Heurística adaptada a sistemas de varios procesadores
 */
class Multihilo extends Heuristica{
    private Casilla Mejor;
    private Simulacion [] Base;
    
    public Multihilo(int dim, int prof, boolean swap) {
        super(dim, prof, swap);
        Mejor = null;

        Base = new Simulacion [Dimension*Dimension];
        for(int i = 0; i < Dimension*Dimension; i++){
            Base[i] = new Simulacion(Dimension);
        }
    }
    
    public void NuevoMovimiento(int fila, int columna, int color){
        Simulacion nueva = null;
        Mejor = null;
        
        for(int i = 0; i < Dimension*Dimension; i++){
            nueva = new Simulacion(Base[i], fila, columna, color);
            Base[i] = nueva;
        }
    }
    
    public int [] ElegirMovimiento(int color, int movimiento){
        int [] vector = new int [2];
        if(movimiento == 0){
            int s = 0;
            if(Swap) s = 1;
            vector[0] = Favoritos[Dimension][s][0];
            vector[1] = Favoritos[Dimension][s][1];
            return vector;
        }
        
        Mejor = null;
        Hilo [] h = new Hilo [Dimension*Dimension];
        double [] r = new double [Dimension*Dimension];
        Celda [] cas = new Celda [Dimension*Dimension];
        
        /* Para cada posible jugada, crea un hilo que la evalúa junto con todas
         * las posibilidades que la siguen */
        ArrayList <Casilla> Libres = Base[0].ObtenerCeldasLibres();
        Iterator <Casilla> lib = Libres.iterator();
        int i = 0;  // Número de hilos que crea el sistema
        while(lib.hasNext()){
            Casilla c1 = lib.next();
            h[i] = new Hilo(Base[i], c1, color);
            h[i].start();
            cas[i] = c1;            
            i++;
        }
        
        for(int k = 0; k < i; k++){
            try {
                h[k].join();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        /* Según los valores devueltos por los hilos, y dependiendo de si es
         * el jugador que minimiza o maximiza, busca el mejor valor disponible
         * y de vuelve la jugada que lo lleva asociado */
        int indiceMejor = 0;
        double tmp = 0;
        double valorMejor;
        switch(color){
        case 0:
            valorMejor = Double.POSITIVE_INFINITY;
            for(int k = 0; k < i; k++){
                tmp = h[k].ObtenerValor();
                if(tmp < valorMejor){
                    valorMejor = tmp;
                    indiceMejor = k;
                }
            }
            break;
        case 1:
            valorMejor = Double.NEGATIVE_INFINITY;
            for(int k = 0; k < i; k++){
                tmp = h[k].ObtenerValor();
                if(tmp > valorMejor){
                    valorMejor = tmp;
                    indiceMejor = k;
                }
            }
            break;        
        }

        vector[0] = h[indiceMejor].ObtenerCelda().ObtenerFila(); 
        vector[1] = h[indiceMejor].ObtenerCelda().ObtenerColumna();
        
        return vector;
    }    
    
    public double AlfaBetaMax(Simulacion s, int nivel, double alfa, double beta){ 
        if(nivel == 0){
            double v = s.CalcularValor();
            return v;
        }
        
        ArrayList <Casilla> Libres = s.ObtenerCeldasLibres();
        Iterator <Casilla> lib = Libres.iterator();
        while(lib.hasNext()){ 
            Casilla c = lib.next();
            Simulacion n = new Simulacion(s, c, 1);
          
            double score = AlfaBetaMin(n, nivel - 1, alfa, beta);
            
            if(alfa < score){
                alfa = score;
            }

            n.Restaurar();
            
            if( alfa >= beta ){
                return alfa;
            }
        }
        return alfa;
    }
    
    public double AlfaBetaMin(Simulacion s, int nivel, double alfa, double beta){ 
        if(nivel == 0){
            double v = s.CalcularValor();
            return v;
        }
        
        ArrayList <Casilla> Libres = s.ObtenerCeldasLibres();
        Iterator <Casilla> lib = Libres.iterator();
        while(lib.hasNext()){ 
            Casilla c = (Casilla)lib.next();
            Simulacion n = new Simulacion(s, c, 0);
          
            double score = AlfaBetaMax(n, nivel - 1, alfa, beta);

            if(score < beta){
                beta = score;
            }
            
            n.Restaurar();
            
            if( alfa >= beta ){
                return beta;
            }
        } 
        return beta;
    }
    
    /**
     *  Hilo que ejecuta una búsqueda en el árbol siguiente al movimiento que
     *  se le pase en el constructor y devuelve el valor de su rama al procedimiento
     *  que lo invoque. */
    class Hilo extends Thread{
        private Simulacion Base;    // Simulación de la que parte el hilo
        private int color;          // Color del jugador que ejecuta el movimiento
        private double valor;       // Valor que se asocia a la jugada del hilo

        /** Crea un nuevo hilo para simular la casilla c sobre la simulacion s */
        public Hilo(Simulacion s, Casilla c, int color){ 
            Base = s;
            Simulacion Sim = new Simulacion(Base, c, color);
            this.color = color;
            Base = Sim;
        } 
        
        /** Ejecuta el hilo, que realiza una búsqueda alfa-beta */
        public void run() {
            switch(color){
            case 1:
                valor = AlfaBetaMin(Base, ProfMax - 1, 0.0, Double.POSITIVE_INFINITY);
                break;
            case 0:
                valor = AlfaBetaMax(Base, ProfMax - 1, 0.0, Double.POSITIVE_INFINITY);
            }
            
            Base.Restaurar();
        }
        
        /** Devuelve el valor de la jugada */
        public double ObtenerValor(){
            return valor;
        }
        
        public Casilla ObtenerCelda(){
            return Base.ObtenerCeldaObjetivo();
        }
    }
}
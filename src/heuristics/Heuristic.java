package heuristics;
import java.util.*;


/**
 *  Represents the program's game engine, responsible for calculating the numerical
 *  value of each simulated move, building the move tree and returning the best available
 *  option.
 *  It is an abstract class to support two possible implementations, dynamically
 *  linked: one with multiple threads prepared for multiple processors and another
 *  single-threaded for single-processor systems.
 *
 *  @author Pau
 *  @version 1.0
 */
public abstract class Heuristic {
    protected int dimension;    // Board dimension on which the heuristic is applied
    protected int maxDepth;     // Maximum search tree depth
    protected boolean swap;     // True if swap is activated
    protected int [][][] favorites;

    /** Creates a new Heuristic instance. Initializes the necessary lists and structures
     *  and defines connections between borders and the rest of the board
     *  @param dimension Game board dimension
     *  @param level Game level
     *  @param swap True if swap rule is activated */
    public Heuristic(int dimension, int level, boolean swap) {
        this.dimension = dimension;
        this.maxDepth = level;
        this.swap = swap;
        // Best opening moves:              1                2                3
        favorites = new int [][][] {{{0, 0},{0, 0}}, {{0, 0},{0, 0}}, {{1, 1},{0, 0}}, {{1, 1},{1, 2}},
        //         4                5                6                7                8                9
            {{2, 2},{1, 2}}, {{2, 2},{2, 3}}, {{3, 3},{3, 2}}, {{3, 3},{4, 5}}, {{4, 4},{5, 4}}, {{4, 4},{4, 5}},
        //         10               11
            {{5, 5},{6, 6}}, {{5, 5},{8, 9}}};
    }

    /** Modifies the system's game level
     *  @param level The move search tree depth */
    public void setLevel(int level){
        maxDepth = level;
    }

    /** Informs of a new move and creates associated structures
     *  @param row Move row
     *  @param column Move column
     *  @param color Color of the player executing the move */
    public abstract void newMove(int row, int column, int color);

    /** Chooses the best move for a player on the current board
     *  @param color Player color for which to get the move
     *  @return Array of two integers denoting row and column of the best move */
    public abstract int[] chooseMove(int color, int moveNumber);
    protected abstract double alphaBetaMin(Simulation s, int depth, double alpha, double beta) throws NonexistentSquare;
    protected abstract double alphaBetaMax(Simulation s, int depth, double alpha, double beta) throws NonexistentSquare;

    public boolean decideSwap(int row, int col){
        if((row == favorites[dimension][0][0]) && (col == favorites[dimension][0][1]))
            return true;
        else return false;
    }
}

/**
 *  Heuristic adapted to single-processor systems
 */
class SingleThread extends Heuristic{
    private Simulation base;  // Simulation on which new moves are executed
    private Square bestMax;
    private Square bestMin;

    public SingleThread(int dim, int depth, boolean swap) {
        super(dim, depth, swap);

        bestMax = null;
        bestMin = null;
        base = new Simulation(dimension);
    }

    public void newMove(int row, int column, int color){
        Simulation newSim = new Simulation(base, row, column, color);
        base = newSim;
    }

    /** Generates a random legal move among available ones
     *  @return The square on which the random move is executed */
    private Square generateRandomMove(){
        ArrayList<Square> free = base.getFreeCells();
        int randomNumber = (int)(Math.random()*free.size());
        return free.get(randomNumber);
    }

    public int [] chooseMove(int color, int moveNumber){
        int [] vector = new int [2];
        if(moveNumber == 0){
            int s = 0;
            if(swap) s = 1;
            vector[0] = favorites[dimension][s][0];
            vector[1] = favorites[dimension][s][1];
            return vector;
        }

        Square best = null;
        bestMax = null;
        bestMin = null;

        double rs;

        if(color == 1){
            rs = alphaBetaMax(base, maxDepth, 0.0, Double.POSITIVE_INFINITY);
            best = bestMax;
        }
        else{
            rs = alphaBetaMin(base, maxDepth, 0.0, Double.POSITIVE_INFINITY);
            best = bestMin;
        }
        if(best == null){
            System.out.println("Random");
            best = generateRandomMove();
        }

        vector[0] = best.getRow();
        vector[1] = best.getColumn();
        return vector;
    }

    public double alphaBetaMax(Simulation s, int level, double alpha, double beta) {
        if(level == 0){
            return s.calculateValue();
        }

        ArrayList <Square> free = s.getFreeCells();
        Iterator <Square> iterator = free.iterator();
        while(iterator.hasNext()){
            Square c = iterator.next();
            Simulation n = new Simulation(s, c, 1);

            double value = alphaBetaMin(n, level - 1, alpha, beta);

            if(alpha < value){
                alpha = value;
                if(level == maxDepth) // Ensures best move is generated at last level
                    bestMax = c;
            }

            n.restore();

            if( alpha >= beta ){
                return alpha;
            }
        }
        return alpha;
    }

    public double alphaBetaMin(Simulation s, int level, double alpha, double beta) {
        if(level == 0){
            return s.calculateValue();
        }

        ArrayList <Square> free = s.getFreeCells();
        Iterator <Square> iterator = free.iterator();
        while(iterator.hasNext()){
            Square c = (Square)iterator.next();
            Simulation n = new Simulation(s, c, 0);

            double value = alphaBetaMax(n, level - 1, alpha, beta);

            if(value < beta){
                beta = value;
                if(level == maxDepth)
                    bestMin = c;
            }

            n.restore();

            if( alpha >= beta ){
                return beta;
            }
        }
        return beta;
    }
}

/**
 *  Heuristic adapted to multi-processor systems
 */
class MultiThread extends Heuristic{
    private Square best;
    private Simulation [] base;

    public MultiThread(int dim, int depth, boolean swap) {
        super(dim, depth, swap);
        best = null;

        // Create all needed simulations upfront (needed for first move)
        base = new Simulation [dimension*dimension];
        for(int i = 0; i < dimension*dimension; i++){
            base[i] = new Simulation(dimension);
        }
    }

    public void newMove(int row, int column, int color){
        Simulation newSim = null;
        best = null;

        // Update all simulations with the new move
        for(int i = 0; i < dimension*dimension; i++){
            newSim = new Simulation(base[i], row, column, color);
            base[i] = newSim;
        }
    }

    public int [] chooseMove(int color, int moveNumber){
        int [] vector = new int [2];
        if(moveNumber == 0){
            int s = 0;
            if(swap) s = 1;
            vector[0] = favorites[dimension][s][0];
            vector[1] = favorites[dimension][s][1];
            return vector;
        }

        best = null;

        /* For each possible move, creates a thread that evaluates it along with all
         * possibilities that follow */
        ArrayList <Square> free = base[0].getFreeCells();
        int numFreeCells = free.size();

        // Optimization: Allocate thread arrays sized exactly to the number of free cells
        // instead of dimension² (saves memory especially in late game)
        GameThread [] threads = new GameThread [numFreeCells];
        double [] r = new double [numFreeCells];
        Cell [] cells = new Cell [numFreeCells];

        Iterator <Square> iterator = free.iterator();
        int i = 0;  // Number of threads created by the system
        while(iterator.hasNext()){
            Square c1 = iterator.next();
            threads[i] = new GameThread(base[i], c1, color);
            threads[i].start();
            cells[i] = c1;
            i++;
        }

        for(int k = 0; k < i; k++){
            try {
                threads[k].join();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        /* According to values returned by threads, and depending on whether it's
         * the minimizing or maximizing player, searches for the best available value
         * and returns the move associated with it */
        int bestIndex = 0;
        double tmp = 0;
        double bestValue;
        switch(color){
        case 0:
            bestValue = Double.POSITIVE_INFINITY;
            for(int k = 0; k < i; k++){
                tmp = threads[k].getValue();
                if(tmp < bestValue){
                    bestValue = tmp;
                    bestIndex = k;
                }
            }
            break;
        case 1:
            bestValue = Double.NEGATIVE_INFINITY;
            for(int k = 0; k < i; k++){
                tmp = threads[k].getValue();
                if(tmp > bestValue){
                    bestValue = tmp;
                    bestIndex = k;
                }
            }
            break;
        }

        vector[0] = threads[bestIndex].getCell().getRow();
        vector[1] = threads[bestIndex].getCell().getColumn();

        return vector;
    }

    public double alphaBetaMax(Simulation s, int level, double alpha, double beta){
        if(level == 0){
            double v = s.calculateValue();
            return v;
        }

        ArrayList <Square> free = s.getFreeCells();
        Iterator <Square> iterator = free.iterator();
        while(iterator.hasNext()){
            Square c = iterator.next();
            Simulation n = new Simulation(s, c, 1);

            double score = alphaBetaMin(n, level - 1, alpha, beta);

            if(alpha < score){
                alpha = score;
            }

            n.restore();

            if( alpha >= beta ){
                return alpha;
            }
        }
        return alpha;
    }

    public double alphaBetaMin(Simulation s, int level, double alpha, double beta){
        if(level == 0){
            double v = s.calculateValue();
            return v;
        }

        ArrayList <Square> free = s.getFreeCells();
        Iterator <Square> iterator = free.iterator();
        while(iterator.hasNext()){
            Square c = (Square)iterator.next();
            Simulation n = new Simulation(s, c, 0);

            double score = alphaBetaMax(n, level - 1, alpha, beta);

            if(score < beta){
                beta = score;
            }

            n.restore();

            if( alpha >= beta ){
                return beta;
            }
        }
        return beta;
    }

    /**
     *  Thread that executes a search in the tree following the move passed
     *  in the constructor and returns the value of its branch to the procedure
     *  that invokes it. */
    class GameThread extends Thread{
        private Simulation base;    // Simulation from which the thread starts
        private int color;          // Color of the player executing the move
        private double value;       // Value associated with the thread's move

        /** Creates a new thread to simulate square c on simulation s */
        public GameThread(Simulation s, Square c, int color){
            base = s;
            Simulation sim = new Simulation(base, c, color);
            this.color = color;
            base = sim;
        }

        /** Executes the thread, which performs an alpha-beta search */
        public void run() {
            switch(color){
            case 1:
                value = alphaBetaMin(base, maxDepth - 1, 0.0, Double.POSITIVE_INFINITY);
                break;
            case 0:
                value = alphaBetaMax(base, maxDepth - 1, 0.0, Double.POSITIVE_INFINITY);
            }

            base.restore();
        }

        /** Returns the move's value */
        public double getValue(){
            return value;
        }

        public Square getCell(){
            return base.getTargetCell();
        }
    }
}

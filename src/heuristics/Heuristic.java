package heuristics;
import java.util.*;
import java.util.concurrent.*;


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

    /** Sorts moves by proximity to a target square for better alpha-beta pruning.
     *  Moves closer to the target are evaluated first, improving cutoff rates.
     *  @param moves List of candidate moves to sort
     *  @param target The reference square (typically the last move played) */
    protected void sortByProximity(ArrayList<Square> moves, Square target) {
        sortByProximityAndKillers(moves, target, null);
    }

    /** Sorts moves by killer moves first, then proximity to target.
     *  @param moves List of candidate moves to sort
     *  @param target The reference square for proximity sorting
     *  @param killers Array of killer moves to prioritize [0=primary, 1=secondary] */
    protected void sortByProximityAndKillers(ArrayList<Square> moves, Square target, final Square[] killers) {
        final int targetRow = (target != null) ? target.getRow() : -1;
        final int targetCol = (target != null) ? target.getColumn() : -1;

        Collections.sort(moves, new Comparator<Square>() {
            public int compare(Square s1, Square s2) {
                // Killer moves have highest priority
                if (killers != null) {
                    boolean s1IsKiller = (s1.equals(killers[0]) || s1.equals(killers[1]));
                    boolean s2IsKiller = (s2.equals(killers[0]) || s2.equals(killers[1]));

                    if (s1IsKiller && !s2IsKiller) return -1;  // s1 first
                    if (s2IsKiller && !s1IsKiller) return 1;   // s2 first

                    // Both killers: primary before secondary
                    if (s1IsKiller && s2IsKiller) {
                        if (s1.equals(killers[0])) return -1;
                        if (s2.equals(killers[0])) return 1;
                        return 0;
                    }
                }

                // No killer advantage: sort by proximity
                if (target != null) {
                    int dist1 = Math.abs(s1.getRow() - targetRow) + Math.abs(s1.getColumn() - targetCol);
                    int dist2 = Math.abs(s2.getRow() - targetRow) + Math.abs(s2.getColumn() - targetCol);
                    return Integer.compare(dist1, dist2);
                }

                return 0;  // No sorting criteria
            }
        });
    }
}

/**
 *  Heuristic adapted to single-processor systems
 */
class SingleThread extends Heuristic{
    private Simulation base;  // Simulation on which new moves are executed
    private Square bestMax;
    private Square bestMin;
    private Square[][] killerMoves;  // Killer moves for alpha-beta pruning [depth][0=primary, 1=secondary]
    private ArrayList<Square>[] freeBuffers;  // Reusable buffers for each recursion level

    public SingleThread(int dim, int depth, boolean swap) {
        super(dim, depth, swap);

        bestMax = null;
        bestMin = null;
        base = new Simulation(dimension);
        killerMoves = new Square[10][2];  // Support up to depth 9 (larger than any reasonable level)

        // Pre-allocate ArrayList buffers for each recursion level to avoid creating new ones
        freeBuffers = new ArrayList[10];
        for (int i = 0; i < 10; i++) {
            freeBuffers[i] = new ArrayList<Square>();
        }
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

        // Reuse buffer instead of creating new ArrayList
        ArrayList <Square> free = freeBuffers[level];
        free.clear();
        s.getFreeCellsInto(free);  // Populate buffer
        sortByProximityAndKillers(free, s.getTargetCell(), killerMoves[level]);  // Order by killers then proximity
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
                // Beta cutoff: store this move as a killer
                if (killerMoves[level][0] == null || !c.equals(killerMoves[level][0])) {
                    killerMoves[level][1] = killerMoves[level][0];  // Shift secondary
                    killerMoves[level][0] = c;  // New primary killer
                }
                return alpha;
            }
        }
        return alpha;
    }

    public double alphaBetaMin(Simulation s, int level, double alpha, double beta) {
        if(level == 0){
            return s.calculateValue();
        }

        // Reuse buffer instead of creating new ArrayList
        ArrayList <Square> free = freeBuffers[level];
        free.clear();
        s.getFreeCellsInto(free);  // Populate buffer
        sortByProximityAndKillers(free, s.getTargetCell(), killerMoves[level]);  // Order by killers then proximity
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
                // Alpha cutoff: store this move as a killer
                if (killerMoves[level][0] == null || !c.equals(killerMoves[level][0])) {
                    killerMoves[level][1] = killerMoves[level][0];  // Shift secondary
                    killerMoves[level][0] = c;  // New primary killer
                }
                return beta;
            }
        }
        return beta;
    }
}

/**
 *  Heuristic adapted to multi-processor systems using thread pooling
 */
class MultiThread extends Heuristic{
    private Square best;
    private Simulation [] base;
    private ExecutorService executor;  // Thread pool for move evaluation
    private Square[][] killerMoves;  // Killer moves for alpha-beta pruning [depth][0=primary, 1=secondary]
    private ThreadLocal<ArrayList<Square>[]> threadLocalBuffers;  // Thread-local reusable buffers

    public MultiThread(int dim, int depth, boolean swap) {
        super(dim, depth, swap);
        best = null;

        // Create all needed simulations upfront (needed for first move)
        base = new Simulation [dimension*dimension];
        for(int i = 0; i < dimension*dimension; i++){
            base[i] = new Simulation(dimension);
        }

        // Create thread pool sized to available processors
        // This pool will be reused across all move evaluations
        int numThreads = Runtime.getRuntime().availableProcessors();
        executor = Executors.newFixedThreadPool(numThreads);
        killerMoves = new Square[10][2];  // Support up to depth 9 (larger than any reasonable level)

        // Pre-allocate thread-local ArrayList buffers for each recursion level
        threadLocalBuffers = new ThreadLocal<ArrayList<Square>[]>() {
            @Override
            protected ArrayList<Square>[] initialValue() {
                ArrayList<Square>[] buffers = new ArrayList[10];
                for (int i = 0; i < 10; i++) {
                    buffers[i] = new ArrayList<Square>();
                }
                return buffers;
            }
        };
    }

    public void newMove(int row, int column, int color){
        Simulation newSim = null;
        best = null;

        // Clear killer moves for the new position
        for(int i = 0; i < killerMoves.length; i++){
            killerMoves[i][0] = null;
            killerMoves[i][1] = null;
        }

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

        /* For each possible move, submit a task to the thread pool that evaluates it
         * along with all possibilities that follow */
        ArrayList <Square> free = base[0].getFreeCells();
        int numFreeCells = free.size();

        // Submit all evaluation tasks to the thread pool
        List<Future<MoveEvaluation>> futures = new ArrayList<>(numFreeCells);
        for(int i = 0; i < numFreeCells; i++){
            Square c1 = free.get(i);
            MoveEvaluationTask task = new MoveEvaluationTask(base[i], c1, color);
            futures.add(executor.submit(task));
        }

        // Wait for all tasks to complete and collect results
        List<MoveEvaluation> results = new ArrayList<>(numFreeCells);
        for(Future<MoveEvaluation> future : futures){
            try {
                results.add(future.get());
            } catch (InterruptedException | ExecutionException ex) {
                ex.printStackTrace();
                // If a task fails, use a default poor evaluation
                results.add(new MoveEvaluation(0.0, null));
            }
        }

        /* According to values returned by tasks, and depending on whether it's
         * the minimizing or maximizing player, find the best available value
         * and return the move associated with it */
        int bestIndex = 0;
        double bestValue;
        switch(color){
        case 0:
            bestValue = Double.POSITIVE_INFINITY;
            for(int k = 0; k < results.size(); k++){
                double tmp = results.get(k).value;
                if(tmp < bestValue){
                    bestValue = tmp;
                    bestIndex = k;
                }
            }
            break;
        case 1:
            bestValue = Double.NEGATIVE_INFINITY;
            for(int k = 0; k < results.size(); k++){
                double tmp = results.get(k).value;
                if(tmp > bestValue){
                    bestValue = tmp;
                    bestIndex = k;
                }
            }
            break;
        }

        Square bestCell = results.get(bestIndex).cell;
        vector[0] = bestCell.getRow();
        vector[1] = bestCell.getColumn();

        return vector;
    }

    public double alphaBetaMax(Simulation s, int level, double alpha, double beta){
        if(level == 0){
            double v = s.calculateValue();
            return v;
        }

        // Reuse thread-local buffer instead of creating new ArrayList
        ArrayList<Square>[] buffers = threadLocalBuffers.get();
        ArrayList <Square> free = buffers[level];
        free.clear();
        s.getFreeCellsInto(free);  // Populate buffer
        sortByProximityAndKillers(free, s.getTargetCell(), killerMoves[level]);  // Order by killers then proximity
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
                // Beta cutoff: store this move as a killer
                if (killerMoves[level][0] == null || !c.equals(killerMoves[level][0])) {
                    killerMoves[level][1] = killerMoves[level][0];  // Shift secondary
                    killerMoves[level][0] = c;  // New primary killer
                }
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

        // Reuse thread-local buffer instead of creating new ArrayList
        ArrayList<Square>[] buffers = threadLocalBuffers.get();
        ArrayList <Square> free = buffers[level];
        free.clear();
        s.getFreeCellsInto(free);  // Populate buffer
        sortByProximityAndKillers(free, s.getTargetCell(), killerMoves[level]);  // Order by killers then proximity
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
                // Alpha cutoff: store this move as a killer
                if (killerMoves[level][0] == null || !c.equals(killerMoves[level][0])) {
                    killerMoves[level][1] = killerMoves[level][0];  // Shift secondary
                    killerMoves[level][0] = c;  // New primary killer
                }
                return beta;
            }
        }
        return beta;
    }

    /**
     * Result of evaluating a single move
     */
    private static class MoveEvaluation {
        final double value;
        final Square cell;

        MoveEvaluation(double value, Square cell) {
            this.value = value;
            this.cell = cell;
        }
    }

    /**
     *  Task that executes a search in the tree following the move passed
     *  in the constructor and returns the value and cell.
     *  Implements Callable for use with ExecutorService thread pool. */
    class MoveEvaluationTask implements Callable<MoveEvaluation>{
        private Simulation base;    // Simulation from which the task starts
        private int color;          // Color of the player executing the move

        /** Creates a new task to simulate square c on simulation s */
        public MoveEvaluationTask(Simulation s, Square c, int color){
            base = s;
            Simulation sim = new Simulation(base, c, color);
            this.color = color;
            base = sim;
        }

        /** Executes the task, which performs an alpha-beta search */
        public MoveEvaluation call() {
            double value;
            switch(color){
            case 1:
                value = alphaBetaMin(base, maxDepth - 1, 0.0, Double.POSITIVE_INFINITY);
                break;
            case 0:
                value = alphaBetaMax(base, maxDepth - 1, 0.0, Double.POSITIVE_INFINITY);
                break;
            default:
                value = 0.0;
            }

            Square cell = base.getTargetCell();
            base.restore();

            return new MoveEvaluation(value, cell);
        }
    }

    /**
     * Cleanup method to shutdown the executor service
     */
    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
    }
}

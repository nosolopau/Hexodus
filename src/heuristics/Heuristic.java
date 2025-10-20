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

        long startTime = System.currentTimeMillis();
        System.out.println("[AI] Starting move calculation (depth=" + maxDepth +
            ", color=" + (color == 1 ? "VERTICAL" : "HORIZONTAL") +
            ", free cells=" + base.getFreeCells().size() + ")");

        Square best = null;
        bestMax = null;
        bestMin = null;

        double rs;

        long searchStart = System.currentTimeMillis();
        if(color == 1){
            rs = alphaBetaMax(base, maxDepth, 0.0, Double.POSITIVE_INFINITY);
            best = bestMax;
        }
        else{
            rs = alphaBetaMin(base, maxDepth, 0.0, Double.POSITIVE_INFINITY);
            best = bestMin;
        }
        long searchTime = System.currentTimeMillis() - searchStart;
        System.out.println("[AI] Alpha-beta search completed in " + searchTime + "ms (score=" + String.format("%.2f", rs) + ")");

        if(best == null){
            System.out.println("[AI] No best move found, using random selection");
            best = generateRandomMove();
        }

        vector[0] = best.getRow();
        vector[1] = best.getColumn();

        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println("[AI] Move calculation complete: (" + vector[0] + "," + vector[1] +
            ") in " + totalTime + "ms\n");

        return vector;
    }

    public double alphaBetaMax(Simulation s, int level, double alpha, double beta) {
        if(level == 0){
            long evalStart = System.currentTimeMillis();
            double value = s.calculateValue();
            long evalTime = System.currentTimeMillis() - evalStart;
            if(evalTime > 10) {  // Only log if position evaluation takes >10ms
                Square target = s.getTargetCell();
                if(target != null){
                    System.out.println("[AI]   Leaf position evaluated in " + evalTime + "ms for (" +
                        target.getRow() + "," + target.getColumn() + ")");
                } else {
                    System.out.println("[AI]   Leaf position evaluated in " + evalTime + "ms");
                }
            }
            return value;
        }

        // Reuse buffer instead of creating new ArrayList
        ArrayList <Square> free = freeBuffers[level];
        free.clear();
        s.getFreeCellsInto(free);  // Populate buffer

        if(level == maxDepth) {
            System.out.println("[AI] Evaluating " + free.size() + " candidate moves at root level...");
        }

        sortByProximityAndKillers(free, s.getTargetCell(), killerMoves[level]);  // Order by killers then proximity

        int movesEvaluated = 0;
        int cutoffs = 0;
        Iterator <Square> iterator = free.iterator();
        while(iterator.hasNext()){
            Square c = iterator.next();
            Simulation n = new Simulation(s, c, 1);

            double value = alphaBetaMin(n, level - 1, alpha, beta);
            movesEvaluated++;

            if(alpha < value){
                alpha = value;
                if(level == maxDepth) { // Ensures best move is generated at last level
                    bestMax = c;
                    System.out.println("[AI]   New best move: (" + c.getRow() + "," + c.getColumn() +
                        ") score=" + String.format("%.2f", value) + " [" + movesEvaluated + "/" + free.size() + " moves evaluated]");
                }
            }

            n.restore();

            if( alpha >= beta ){
                // Beta cutoff: store this move as a killer
                if (killerMoves[level][0] == null || !c.equals(killerMoves[level][0])) {
                    killerMoves[level][1] = killerMoves[level][0];  // Shift secondary
                    killerMoves[level][0] = c;  // New primary killer
                }
                cutoffs++;
                if(level == maxDepth) {
                    System.out.println("[AI] Beta cutoff - pruned " + (free.size() - movesEvaluated) + " remaining moves");
                }
                return alpha;
            }
        }
        return alpha;
    }

    public double alphaBetaMin(Simulation s, int level, double alpha, double beta) {
        if(level == 0){
            long evalStart = System.currentTimeMillis();
            double value = s.calculateValue();
            long evalTime = System.currentTimeMillis() - evalStart;
            if(evalTime > 10) {  // Only log if position evaluation takes >10ms
                Square target = s.getTargetCell();
                if(target != null){
                    System.out.println("[AI]   Leaf position evaluated in " + evalTime + "ms for (" +
                        target.getRow() + "," + target.getColumn() + ")");
                } else {
                    System.out.println("[AI]   Leaf position evaluated in " + evalTime + "ms");
                }
            }
            return value;
        }

        // Reuse buffer instead of creating new ArrayList
        ArrayList <Square> free = freeBuffers[level];
        free.clear();
        s.getFreeCellsInto(free);  // Populate buffer

        if(level == maxDepth) {
            System.out.println("[AI] Evaluating " + free.size() + " candidate moves at root level...");
        }

        sortByProximityAndKillers(free, s.getTargetCell(), killerMoves[level]);  // Order by killers then proximity

        int movesEvaluated = 0;
        int cutoffs = 0;
        Iterator <Square> iterator = free.iterator();
        while(iterator.hasNext()){
            Square c = (Square)iterator.next();
            Simulation n = new Simulation(s, c, 0);

            double value = alphaBetaMax(n, level - 1, alpha, beta);
            movesEvaluated++;

            if(value < beta){
                beta = value;
                if(level == maxDepth) {
                    bestMin = c;
                    System.out.println("[AI]   New best move: (" + c.getRow() + "," + c.getColumn() +
                        ") score=" + String.format("%.2f", value) + " [" + movesEvaluated + "/" + free.size() + " moves evaluated]");
                }
            }

            n.restore();

            if( alpha >= beta ){
                // Alpha cutoff: store this move as a killer
                if (killerMoves[level][0] == null || !c.equals(killerMoves[level][0])) {
                    killerMoves[level][1] = killerMoves[level][0];  // Shift secondary
                    killerMoves[level][0] = c;  // New primary killer
                }
                cutoffs++;
                if(level == maxDepth) {
                    System.out.println("[AI] Alpha cutoff - pruned " + (free.size() - movesEvaluated) + " remaining moves");
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

        long startTime = System.currentTimeMillis();
        best = null;

        /* For each possible move, submit a task to the thread pool that evaluates it
         * along with all possibilities that follow */
        ArrayList <Square> free = base[0].getFreeCells();
        int numFreeCells = free.size();

        System.out.println("[AI] Starting move calculation (depth=" + maxDepth +
            ", color=" + (color == 1 ? "VERTICAL" : "HORIZONTAL") +
            ", free cells=" + numFreeCells + ")");
        System.out.println("[AI] Submitting " + numFreeCells + " parallel evaluation tasks to thread pool...");

        // Use CompletionService to process results as they complete
        CompletionService<MoveEvaluation> completionService = new ExecutorCompletionService<>(executor);

        // Submit all evaluation tasks to the thread pool
        long submitStart = System.currentTimeMillis();
        List<Future<MoveEvaluation>> futures = new ArrayList<>(numFreeCells);
        for(int i = 0; i < numFreeCells; i++){
            Square c1 = free.get(i);
            MoveEvaluationTask task = new MoveEvaluationTask(base[i], c1, color);
            futures.add(completionService.submit(task));
        }
        long submitTime = System.currentTimeMillis() - submitStart;
        System.out.println("[AI] All tasks submitted in " + submitTime + "ms, processing as they complete...");

        // Process results as they complete, with early termination
        long waitStart = System.currentTimeMillis();
        Square bestCell = null;
        double bestValue = (color == 1) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        int completed = 0;
        int cancelled = 0;

        try {
            for(int i = 0; i < numFreeCells; i++){
                Future<MoveEvaluation> completedFuture = completionService.take();
                MoveEvaluation result = completedFuture.get();
                completed++;

                boolean improved = false;
                if(color == 1 && result.value > bestValue){
                    bestValue = result.value;
                    bestCell = result.cell;
                    improved = true;
                } else if(color == 0 && result.value < bestValue){
                    bestValue = result.value;
                    bestCell = result.cell;
                    improved = true;
                }

                if(improved){
                    System.out.println("[AI]   New best: (" + bestCell.getRow() + "," + bestCell.getColumn() +
                        ") score=" + String.format("%.2f", bestValue) + " [" + completed + "/" + numFreeCells + " complete]");
                }

                // Check if we can prune remaining moves
                // For maximizing: if we found a win (Infinity) or very good move
                // For minimizing: if we found a win (Infinity) or very good move
                boolean canPrune = false;
                if(color == 1 && bestValue == Double.POSITIVE_INFINITY) canPrune = true;
                if(color == 0 && bestValue == Double.NEGATIVE_INFINITY) canPrune = true;

                if(canPrune && i < numFreeCells - 1){
                    // Cancel remaining futures
                    for(Future<MoveEvaluation> f : futures){
                        if(!f.isDone()){
                            if(f.cancel(true)){
                                cancelled++;
                            }
                        }
                    }
                    System.out.println("[AI] Early termination: found winning move, cancelled " +
                        cancelled + " remaining evaluations");
                    break;
                }
            }
        } catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
        }

        long waitTime = System.currentTimeMillis() - waitStart;
        System.out.println("[AI] Completed " + completed + " evaluations in " + waitTime + "ms" +
            (cancelled > 0 ? " (saved " + cancelled + " evaluations)" : ""));

        if(bestCell == null){
            bestCell = free.get(0);  // Fallback
        }

        vector[0] = bestCell.getRow();
        vector[1] = bestCell.getColumn();

        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println("[AI] Move calculation complete: (" + vector[0] + "," + vector[1] +
            ") score=" + String.format("%.2f", bestValue) + " in " + totalTime + "ms\n");

        return vector;
    }

    public double alphaBetaMax(Simulation s, int level, double alpha, double beta){
        if(level == 0){
            long evalStart = System.currentTimeMillis();
            double v = s.calculateValue();
            long evalTime = System.currentTimeMillis() - evalStart;
            if(evalTime > 50) {  // Only log slow evaluations to avoid clutter
                Square target = s.getTargetCell();
                if(target != null){
                    System.out.println("[AI]     Position evaluation took " + evalTime + "ms for (" +
                        target.getRow() + "," + target.getColumn() + ")");
                } else {
                    System.out.println("[AI]     Position evaluation took " + evalTime + "ms");
                }
            }
            return v;
        }

        // Reuse thread-local buffer instead of creating new ArrayList
        ArrayList<Square>[] buffers = threadLocalBuffers.get();
        ArrayList <Square> free = buffers[level];
        free.clear();
        s.getFreeCellsInto(free);  // Populate buffer

        if(level == maxDepth - 1) {  // Log at depth 1 (one level below root)
            System.out.println("[AI]   Evaluating " + free.size() + " moves at depth " + level);
        }

        sortByProximityAndKillers(free, s.getTargetCell(), killerMoves[level]);  // Order by killers then proximity

        int movesEvaluated = 0;
        int cutoffs = 0;
        long startTime = System.currentTimeMillis();

        Iterator <Square> iterator = free.iterator();
        while(iterator.hasNext()){
            Square c = iterator.next();
            Simulation n = new Simulation(s, c, 1);

            double score = alphaBetaMin(n, level - 1, alpha, beta);
            movesEvaluated++;

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
                cutoffs++;
                if(level == maxDepth - 1) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    System.out.println("[AI]   Cutoff at depth " + level + " after " + movesEvaluated + "/" + free.size() +
                        " moves (" + elapsed + "ms)");
                }
                return alpha;
            }
        }

        if(level == maxDepth - 1) {
            long elapsed = System.currentTimeMillis() - startTime;
            System.out.println("[AI]   Completed depth " + level + ": " + movesEvaluated + " moves in " + elapsed + "ms");
        }

        return alpha;
    }

    public double alphaBetaMin(Simulation s, int level, double alpha, double beta){
        if(level == 0){
            long evalStart = System.currentTimeMillis();
            double v = s.calculateValue();
            long evalTime = System.currentTimeMillis() - evalStart;
            if(evalTime > 50) {  // Only log slow evaluations to avoid clutter
                Square target = s.getTargetCell();
                if(target != null){
                    System.out.println("[AI]     Position evaluation took " + evalTime + "ms for (" +
                        target.getRow() + "," + target.getColumn() + ")");
                } else {
                    System.out.println("[AI]     Position evaluation took " + evalTime + "ms");
                }
            }
            return v;
        }

        // Reuse thread-local buffer instead of creating new ArrayList
        ArrayList<Square>[] buffers = threadLocalBuffers.get();
        ArrayList <Square> free = buffers[level];
        free.clear();
        s.getFreeCellsInto(free);  // Populate buffer

        if(level == maxDepth - 1) {  // Log at depth 1 (one level below root)
            System.out.println("[AI]   Evaluating " + free.size() + " moves at depth " + level);
        }

        sortByProximityAndKillers(free, s.getTargetCell(), killerMoves[level]);  // Order by killers then proximity

        int movesEvaluated = 0;
        int cutoffs = 0;
        long startTime = System.currentTimeMillis();

        Iterator <Square> iterator = free.iterator();
        while(iterator.hasNext()){
            Square c = (Square)iterator.next();
            Simulation n = new Simulation(s, c, 0);

            double score = alphaBetaMax(n, level - 1, alpha, beta);
            movesEvaluated++;

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
                cutoffs++;
                if(level == maxDepth - 1) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    System.out.println("[AI]   Cutoff at depth " + level + " after " + movesEvaluated + "/" + free.size() +
                        " moves (" + elapsed + "ms)");
                }
                return beta;
            }
        }

        if(level == maxDepth - 1) {
            long elapsed = System.currentTimeMillis() - startTime;
            System.out.println("[AI]   Completed depth " + level + ": " + movesEvaluated + " moves in " + elapsed + "ms");
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

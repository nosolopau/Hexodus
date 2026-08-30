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

    /** Search tracing, silent unless OptConfig.VERBOSE is set */
    protected static void log(String message){
        if(OptConfig.VERBOSE) System.out.println(message);
    }

    public boolean decideSwap(int row, int col){
        if((row == favorites[dimension][0][0]) && (col == favorites[dimension][0][1]))
            return true;
        else return false;
    }

    /** Best-effort move for decided positions: when no move improves the
     *  search bounds (the opponent's win is proven, every move evaluates
     *  the same), rank the free cells by the player's own resistance and
     *  play the most connective one — the losing side keeps building its
     *  best threat instead of playing randomly.
     *  @param base Simulation of the current position
     *  @param color Player to move
     *  @return The free square minimizing the player's own resistance,
     *  or null if there are no free squares */
    protected Square bestFallbackMove(Simulation base, int color){
        ArrayList<Square> free = base.getFreeCells();
        Square best = null;
        double bestR = Double.POSITIVE_INFINITY;

        for(int i = 0; i < free.size(); i++){
            Square c = free.get(i);
            Simulation n = new Simulation(base, c, color);
            double r = n.calculateOwnResistance(color);
            n.restore();
            if(best == null || r < bestR){
                best = c;
                bestR = r;
            }
        }
        return best;
    }


    /** Hex distance between two squares on this board's adjacency
     *  (neighbors: row/column steps plus the (+1,+1)/(-1,-1) diagonal) */
    private static int hexDistance(int r1, int c1, int r2, int c2){
        int dr = r1 - r2;
        int dc = c1 - c2;
        if((dr >= 0) == (dc >= 0))
            return Math.max(Math.abs(dr), Math.abs(dc));
        return Math.abs(dr) + Math.abs(dc);
    }

    /** Sorts moves by killers first, then by distance to the nearest
     *  occupied square (locality), with distance to the last move as
     *  tiebreak. Every move stays in the list — ordering only — so the
     *  search result is unchanged; good moves in hex cluster around the
     *  existing stones, which improves alpha-beta cutoffs.
     *  @param moves List of candidate moves to sort
     *  @param s Simulation providing the board (for stone positions)
     *  @param killers Array of killer moves to prioritize [0=primary, 1=secondary] */
    protected void sortByLocalityAndKillers(ArrayList<Square> moves, Simulation s, final Square[] killers) {
        ArrayList<Square> stones = new ArrayList<Square>();
        s.getBoard().getOccupiedInto(stones);
        Square target = s.getTargetCell();
        final int targetRow = (target != null) ? target.getRow() : -1;
        final int targetCol = (target != null) ? target.getColumn() : -1;

        /* Precompute a composite key per candidate: distance to the nearest
         * stone (dominant) and distance to the last move (tiebreak) */
        final HashMap<Square, Integer> rank = new HashMap<Square, Integer>();
        for(int i = 0; i < moves.size(); i++){
            Square m = moves.get(i);
            int nearest = Integer.MAX_VALUE;
            for(int j = 0; j < stones.size(); j++){
                Square st = stones.get(j);
                int d = hexDistance(m.getRow(), m.getColumn(), st.getRow(), st.getColumn());
                if(d < nearest) nearest = d;
            }
            if(nearest == Integer.MAX_VALUE) nearest = 0;  // Empty board: all equal
            int tiebreak = (target != null)
                ? hexDistance(m.getRow(), m.getColumn(), targetRow, targetCol) : 0;
            rank.put(m, Integer.valueOf(nearest * 1000 + tiebreak));
        }

        Collections.sort(moves, new Comparator<Square>() {
            public int compare(Square s1, Square s2) {
                if (killers != null) {
                    boolean s1IsKiller = (s1.equals(killers[0]) || s1.equals(killers[1]));
                    boolean s2IsKiller = (s2.equals(killers[0]) || s2.equals(killers[1]));

                    if (s1IsKiller && !s2IsKiller) return -1;
                    if (s2IsKiller && !s1IsKiller) return 1;
                    if (s1IsKiller && s2IsKiller) {
                        if (s1.equals(killers[0])) return -1;
                        if (s2.equals(killers[0])) return 1;
                        return 0;
                    }
                }
                return rank.get(s1).intValue() - rank.get(s2).intValue();
            }
        });
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

    /** Orders root moves by a shallow (depth-0) evaluation so the most
     *  promising options are searched first, improving alpha-beta cutoffs.
     *  Used instead of proximity ordering when OptConfig.USE_ROOT_PRESORT
     *  is active.
     *  @param moves List of candidate root moves to sort
     *  @param s Simulation representing the current position
     *  @param maximizing True when ordering for the maximizing player */
    private void presortByShallowEval(ArrayList<Square> moves, Simulation s, final boolean maximizing){
        final HashMap<Square, Double> scores = new HashMap<Square, Double>();
        for(int i = 0; i < moves.size(); i++){
            Square c = moves.get(i);
            Simulation n = new Simulation(s, c, maximizing ? 1 : 0);
            scores.put(c, Double.valueOf(n.calculateValue()));
            n.restore();
        }
        Collections.sort(moves, new Comparator<Square>() {
            public int compare(Square a, Square b) {
                double va = scores.get(a).doubleValue();
                double vb = scores.get(b).doubleValue();
                return maximizing ? Double.compare(vb, va) : Double.compare(va, vb);
            }
        });
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
        log("[AI] Starting move calculation (depth=" + maxDepth +
            ", color=" + (color == 1 ? "VERTICAL" : "HORIZONTAL") +
            ", free cells=" + base.getFreeCells().size() + ")");

        if(Analysis.ENABLED) Analysis.begin(dimension, color, base.getFreeCells().size());

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
        OptConfig.lastRootScore = rs;  // Instrumentation: root value is ordering-invariant
        log("[AI] Alpha-beta search completed in " + searchTime + "ms (score=" + String.format("%.2f", rs) + ")");

        if(best == null){
            if(OptConfig.USE_SMART_FALLBACK){
                log("[AI] Position decided; playing most connective move");
                best = bestFallbackMove(base, color);
            }
            if(best == null){
                log("[AI] No best move found, using random selection");
                OptConfig.randomFallbacks++;  // Instrumentation: nondeterministic move
                best = generateRandomMove();
            }
        }

        vector[0] = best.getRow();
        vector[1] = best.getColumn();

        long totalTime = System.currentTimeMillis() - startTime;
        if(Analysis.ENABLED) Analysis.finish(vector[0], vector[1], totalTime);
        log("[AI] Move calculation complete: (" + vector[0] + "," + vector[1] +
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
                    log("[AI]   Leaf position evaluated in " + evalTime + "ms for (" +
                        target.getRow() + "," + target.getColumn() + ")");
                } else {
                    log("[AI]   Leaf position evaluated in " + evalTime + "ms");
                }
            }
            return value;
        }

        // Reuse buffer instead of creating new ArrayList
        ArrayList <Square> free = freeBuffers[level];
        free.clear();
        s.getFreeCellsInto(free);  // Populate buffer

        if(level == maxDepth) {
            log("[AI] Evaluating " + free.size() + " candidate moves at root level...");
        }

        if(OptConfig.USE_ROOT_PRESORT && level == maxDepth)
            presortByShallowEval(free, s, true);
        else if(OptConfig.USE_LOCAL_ORDERING)
            sortByLocalityAndKillers(free, s, killerMoves[level]);  // Order by killers then near-stone locality
        else
            sortByProximityAndKillers(free, s.getTargetCell(), killerMoves[level]);  // Order by killers then proximity

        int movesEvaluated = 0;
        int cutoffs = 0;
        Iterator <Square> iterator = free.iterator();
        while(iterator.hasNext()){
            Square c = iterator.next();
            Simulation n = new Simulation(s, c, 1);

            double value = alphaBetaMin(n, level - 1, alpha, beta);
            movesEvaluated++;

            if(Analysis.ENABLED && level == maxDepth)
                Analysis.record(c.getRow(), c.getColumn(), value);

            if(alpha < value){
                alpha = value;
                if(level == maxDepth) { // Ensures best move is generated at last level
                    bestMax = c;
                    log("[AI]   New best move: (" + c.getRow() + "," + c.getColumn() +
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
                    log("[AI] Beta cutoff - pruned " + (free.size() - movesEvaluated) + " remaining moves");
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
                    log("[AI]   Leaf position evaluated in " + evalTime + "ms for (" +
                        target.getRow() + "," + target.getColumn() + ")");
                } else {
                    log("[AI]   Leaf position evaluated in " + evalTime + "ms");
                }
            }
            return value;
        }

        // Reuse buffer instead of creating new ArrayList
        ArrayList <Square> free = freeBuffers[level];
        free.clear();
        s.getFreeCellsInto(free);  // Populate buffer

        if(level == maxDepth) {
            log("[AI] Evaluating " + free.size() + " candidate moves at root level...");
        }

        if(OptConfig.USE_ROOT_PRESORT && level == maxDepth)
            presortByShallowEval(free, s, false);
        else if(OptConfig.USE_LOCAL_ORDERING)
            sortByLocalityAndKillers(free, s, killerMoves[level]);  // Order by killers then near-stone locality
        else
            sortByProximityAndKillers(free, s.getTargetCell(), killerMoves[level]);  // Order by killers then proximity

        int movesEvaluated = 0;
        int cutoffs = 0;
        Iterator <Square> iterator = free.iterator();
        while(iterator.hasNext()){
            Square c = (Square)iterator.next();
            Simulation n = new Simulation(s, c, 0);

            double value = alphaBetaMax(n, level - 1, alpha, beta);
            movesEvaluated++;

            if(Analysis.ENABLED && level == maxDepth)
                Analysis.record(c.getRow(), c.getColumn(), value);

            if(value < beta){
                beta = value;
                if(level == maxDepth) {
                    bestMin = c;
                    log("[AI]   New best move: (" + c.getRow() + "," + c.getColumn() +
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
                    log("[AI] Alpha cutoff - pruned " + (free.size() - movesEvaluated) + " remaining moves");
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
        /* Daemon threads: the pool is never shut down by the UI, and
         * non-daemon workers keep the JVM alive after the work is done
         * (harmless in the GUI, which calls System.exit, but it hangs
         * headless callers such as tests and benchmarks). */
        executor = Executors.newFixedThreadPool(numThreads, new ThreadFactory(){
            public Thread newThread(Runnable r){
                Thread t = new Thread(r, "hexodus-search");
                t.setDaemon(true);
                return t;
            }
        });
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

        log("[AI] Starting move calculation (depth=" + maxDepth +
            ", color=" + (color == 1 ? "VERTICAL" : "HORIZONTAL") +
            ", free cells=" + numFreeCells + ")");
        log("[AI] Submitting " + numFreeCells + " parallel evaluation tasks to thread pool...");

        if(Analysis.ENABLED) Analysis.begin(dimension, color, numFreeCells);

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
        log("[AI] All tasks submitted in " + submitTime + "ms, processing as they complete...");

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

                if(Analysis.ENABLED && result.cell != null)
                    Analysis.record(result.cell.getRow(), result.cell.getColumn(), result.value);

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
                    log("[AI]   New best: (" + bestCell.getRow() + "," + bestCell.getColumn() +
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
                    log("[AI] Early termination: found winning move, cancelled " +
                        cancelled + " remaining evaluations");
                    break;
                }
            }
        } catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
        }

        long waitTime = System.currentTimeMillis() - waitStart;
        log("[AI] Completed " + completed + " evaluations in " + waitTime + "ms" +
            (cancelled > 0 ? " (saved " + cancelled + " evaluations)" : ""));

        if(bestCell == null){
            if(OptConfig.USE_SMART_FALLBACK){
                log("[AI] Position decided; playing most connective move");
                bestCell = bestFallbackMove(base[0], color);
            }
            if(bestCell == null) bestCell = free.get(0);  // Fallback
        }

        vector[0] = bestCell.getRow();
        vector[1] = bestCell.getColumn();

        long totalTime = System.currentTimeMillis() - startTime;
        if(Analysis.ENABLED) Analysis.finish(vector[0], vector[1], totalTime);
        log("[AI] Move calculation complete: (" + vector[0] + "," + vector[1] +
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
                    log("[AI]     Position evaluation took " + evalTime + "ms for (" +
                        target.getRow() + "," + target.getColumn() + ")");
                } else {
                    log("[AI]     Position evaluation took " + evalTime + "ms");
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
            log("[AI]   Evaluating " + free.size() + " moves at depth " + level);
        }

        if(OptConfig.USE_LOCAL_ORDERING)
            sortByLocalityAndKillers(free, s, killerMoves[level]);  // Order by killers then near-stone locality
        else
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
                    log("[AI]   Cutoff at depth " + level + " after " + movesEvaluated + "/" + free.size() +
                        " moves (" + elapsed + "ms)");
                }
                return alpha;
            }
        }

        if(level == maxDepth - 1) {
            long elapsed = System.currentTimeMillis() - startTime;
            log("[AI]   Completed depth " + level + ": " + movesEvaluated + " moves in " + elapsed + "ms");
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
                    log("[AI]     Position evaluation took " + evalTime + "ms for (" +
                        target.getRow() + "," + target.getColumn() + ")");
                } else {
                    log("[AI]     Position evaluation took " + evalTime + "ms");
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
            log("[AI]   Evaluating " + free.size() + " moves at depth " + level);
        }

        if(OptConfig.USE_LOCAL_ORDERING)
            sortByLocalityAndKillers(free, s, killerMoves[level]);  // Order by killers then near-stone locality
        else
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
                    log("[AI]   Cutoff at depth " + level + " after " + movesEvaluated + "/" + free.size() +
                        " moves (" + elapsed + "ms)");
                }
                return beta;
            }
        }

        if(level == maxDepth - 1) {
            long elapsed = System.currentTimeMillis() - startTime;
            log("[AI]   Completed depth " + level + ": " + movesEvaluated + " moves in " + elapsed + "ms");
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

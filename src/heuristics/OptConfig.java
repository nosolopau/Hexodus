package heuristics;

/**
 *  Runtime switches for the optimization experiments, so a single binary can
 *  A/B-test each variant against the baseline. All flags default to false,
 *  which preserves the original behavior exactly.
 *
 *  Set the flags before creating any Heuristic/Simulation objects and do not
 *  change them mid-game.
 *
 *  @author Pau
 *  @version 1.0
 */
public final class OptConfig {

    /** Represent Path contents as a 128-bit mask (two longs) instead of an
     *  ArrayList of Cells. Valid for boards up to 11x11 (121 squares + 4
     *  borders = 125 ids); larger boards fail loudly in Path.add. Display
     *  helpers that iterate path cells are not supported in this mode. */
    public static boolean USE_BITPATH = false;

    /** Reuse per-thread scratch buffers in calculateResistance (connection
     *  matrix copy, Route objects, conductance matrices) instead of
     *  allocating fresh objects on every evaluation. */
    public static boolean USE_REUSE = false;

    /** Order root moves by a shallow depth-0 evaluation before the full
     *  alpha-beta search, instead of the proximity ordering, so the most
     *  promising options are searched first. */
    public static boolean USE_ROOT_PRESORT = false;

    /** Order candidate moves at every tree level by distance to the
     *  nearest stone (killers still first, distance to the last move as
     *  tiebreak). Every legal move is still searched — ordering only —
     *  so the root minimax value is unchanged. */
    public static boolean USE_LOCAL_ORDERING = false;

    /** Skip H-search triples wholesale when neither of the two routes
     *  contains a new path: the AND rule needs at least one new ingredient,
     *  so the skipped pair scan could not have fired. Exact (visit order
     *  preserved), so results are bit-identical to the baseline. */
    public static boolean USE_DIRTY_SKIP = false;

    /* Dirty-skip instrumentation: triples whose routes were checked, and
     * how many of those were skipped as clean. */
    public static long triplesTotal = 0;
    public static long triplesSkipped = 0;

    /** Lean OR-rule support structures: cloneWithoutPath copies the path
     *  list directly (skipping the excluded path) instead of re-running
     *  the add() dedup scan per path plus an ArrayList.remove, and
     *  hasDirectPath uses a cached flag instead of scanning. Profiling
     *  showed the OR-rule recursion at 54% of runtime, 40% of it in
     *  cloneWithoutPath. Semantics-preserving (bit-identical, verified),
     *  24-38% faster in every configuration — ADOPTED, on by default;
     *  benchmarks set it to false to measure the historic behavior. */
    public static boolean USE_LEAN_OR = true;

    /** Maximum number of alternative paths stored per route (cell pair).
     *  Historic default is 20. The H-search inner loop combines paths
     *  pairwise, so cost is bounded by this squared; too low and the OR
     *  rule misses provable connections. */
    public static int maxPathsPerRoute = 20;

    /** Print the engine's search progress to the console. Useful when
     *  working on the search, noise during a game, so off by default. */
    public static boolean VERBOSE = false;

    /** Number of leaf evaluations performed (calculateValue calls),
     *  incremented for instrumentation. */
    public static long evalCount = 0;

    /** In decided positions (no move improves the search bounds because
     *  the opponent's win is proven), pick the move minimizing the
     *  player's own resistance instead of a random free cell, so the
     *  losing side keeps building its best connection and play stays
     *  plausible. Changes play only in provably decided positions.
     *  ADOPTED, on by default; benchmarks disable it to keep timing
     *  comparable with the historic engine. */
    public static boolean USE_SMART_FALLBACK = true;

    /** Times chooseMove fell back to a random move because no move improved
     *  the initial search bounds (position considered decided). These moves
     *  are nondeterministic and have always been part of the engine. */
    public static long randomFallbacks = 0;

    /** Root search score of the last chooseMove call (instrumentation;
     *  alpha-beta's root value is ordering-invariant, so benchmarks compare
     *  it across ordering variants to verify correctness). */
    public static double lastRootScore = Double.NaN;

    /* Phase timers (nanoseconds, cumulative) for the evaluation pipeline,
     * used by the benchmark to report where the time goes. */
    public static long nsSetup = 0;     // Connection copy + G generation per calculateResistance
    public static long nsHSearch = 0;   // The H-search generation loop (AND/OR rules)
    public static long nsMatrix = 0;    // Conductance matrix build (M and N)
    public static long nsSolve = 0;     // Gauss solve
    public static long nsSimBuild = 0;  // Simulation constructor (graph surgery per tree node)
    public static long nsRestore = 0;   // Simulation restore (undo of graph surgery)

    /** Resets the phase timers (called by benchmarks before a measured run) */
    public static void resetTimers(){
        nsSetup = nsHSearch = nsMatrix = nsSolve = nsSimBuild = nsRestore = 0;
        triplesTotal = triplesSkipped = 0;
    }

    private OptConfig() {
    }
}

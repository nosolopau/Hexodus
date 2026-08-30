package heuristics;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Benchmark scaffold to compare H-search optimization variants.
 *
 * Plays a deterministic self-play game (SingleThread heuristic, no swap)
 * for a configurable board size, level and number of moves, and reports
 * per-repetition times, leaf-evaluation counts and the move sequence so
 * variants can be checked for identical play.
 *
 * Usage:
 *   java heuristics.OptimizationBenchmark [variant] [dim] [level] [moves] [reps]
 *
 * Variants: baseline | all | comma-list of bitpath,leanor,capN
 * Defaults: baseline 5 2 10 3
 */
public class OptimizationBenchmark {

    public static void main(String[] args) {
        String variant = args.length > 0 ? args[0] : "baseline";
        int dim      = args.length > 1 ? Integer.parseInt(args[1]) : 5;
        int level    = args.length > 2 ? Integer.parseInt(args[2]) : 2;
        int maxMoves = args.length > 3 ? Integer.parseInt(args[3]) : 10;
        int reps     = args.length > 4 ? Integer.parseInt(args[4]) : 3;

        configure(variant, dim);

        System.out.println("OPTIMIZATION BENCHMARK");
        System.out.println("======================");
        System.out.println("variant=" + variant + "  board=" + dim + "x" + dim
            + "  level=" + level + "  moves=" + maxMoves + "  reps=" + reps);
        System.out.println("flags: bitpath=" + OptConfig.USE_BITPATH
            + " leanor=" + OptConfig.USE_LEAN_OR
            + " pathCap=" + OptConfig.maxPathsPerRoute);
        System.out.println();

        // Warmup run (JIT compilation), not measured
        PrintStream real = System.out;
        mute();
        try {
            playGame(dim, level, Math.min(4, maxMoves));
        } finally {
            System.setOut(real);
        }

        OptConfig.resetTimers();   // Phase breakdown covers only the measured reps

        long[] times = new long[reps];
        long[] evals = new long[reps];
        String moveSeq = null;
        String scoreSeq = null;

        for (int r = 0; r < reps; r++) {
            mute();
            GameResult res;
            try {
                res = playGame(dim, level, maxMoves);
            } finally {
                System.setOut(real);
            }
            times[r] = res.millis;
            evals[r] = res.evals;
            moveSeq = res.moves;
            scoreSeq = res.scores;
            System.out.printf("rep %d: %6d ms   %8d leaf evals%n", r + 1, res.millis, res.evals);
        }

        long best = Long.MAX_VALUE, sum = 0;
        for (long t : times) { best = Math.min(best, t); sum += t; }
        long[] sorted = times.clone();
        Arrays.sort(sorted);
        long median = sorted[sorted.length / 2];

        System.out.println();
        System.out.printf("RESULT %s: best=%dms median=%dms mean=%dms evals/game=%d%n",
            variant, best, median, sum / reps, evals[reps - 1]);
        System.out.println("moves:  " + moveSeq);
        System.out.println("scores: " + scoreSeq);
        printPhaseBreakdown(sum);
    }

    /** Prints where the measured time went, from the phase timers
     *  accumulated across all measured reps. */
    private static void printPhaseBreakdown(long totalMillis) {
        long[] ns = {
            OptConfig.nsSimBuild, OptConfig.nsSetup, OptConfig.nsHSearch,
            OptConfig.nsMatrix, OptConfig.nsSolve, OptConfig.nsRestore
        };
        String[] names = {
            "sim build (graph surgery)", "eval setup (copy conns + G)",
            "H-search loop (AND/OR)", "matrix build", "Gauss solve",
            "restore (undo surgery)"
        };
        long accounted = 0;
        for (long n : ns) accounted += n;
        long totalNs = totalMillis * 1_000_000L;

        System.out.println();
        System.out.println("PHASE BREAKDOWN (all reps):");
        for (int i = 0; i < ns.length; i++) {
            System.out.printf("  %-28s %8d ms  %5.1f%%%n",
                names[i], ns[i] / 1_000_000, 100.0 * ns[i] / totalNs);
        }
        System.out.printf("  %-28s %8d ms  %5.1f%%%n", "other (sort, book, misc)",
            (totalNs - accounted) / 1_000_000, 100.0 * (totalNs - accounted) / totalNs);

    }

    /** Variant is "baseline", "all", or a comma-separated combination of
     *  bitpath, reuse and presort (e.g. "bitpath,reuse"). */
    private static void configure(String variant, int dim) {
        OptConfig.USE_BITPATH = false;
        OptConfig.USE_LEAN_OR = false;
        OptConfig.USE_SMART_FALLBACK = false;  // Historic behavior, keeps timings comparable
        OptConfig.maxPathsPerRoute = 20;

        if (variant.equals("baseline")) return;

        for (String token : variant.equals("all")
                ? new String[]{"bitpath", "leanor"} : variant.split(",")) {
            if (token.equals("bitpath")) {
                if (dim > 11) throw new IllegalArgumentException("bitpath supports boards up to 11x11");
                OptConfig.USE_BITPATH = true;
            }
            else if (token.equals("leanor")) OptConfig.USE_LEAN_OR = true;
            else if (token.startsWith("cap")) OptConfig.maxPathsPerRoute = Integer.parseInt(token.substring(3));
            else throw new IllegalArgumentException("unknown variant: " + token);
        }
    }

    private static class GameResult {
        final long millis;
        final long evals;
        final String moves;
        final String scores;
        GameResult(long millis, long evals, String moves, String scores) {
            this.millis = millis;
            this.evals = evals;
            this.moves = moves;
            this.scores = scores;
        }
    }

    /** Times chooseMove on a fixed scripted sequence of positions. The
     *  engine's answers are recorded (for cross-variant correctness checks)
     *  but the scripted move is what actually gets played, so every variant
     *  evaluates exactly the same positions. Only the search time is
     *  measured; applying the scripted moves is excluded. */
    private static GameResult playGame(int dim, int level, int maxMoves) {
        SingleThread h = new SingleThread(dim, level, false);
        int[][] script = buildScript(dim, maxMoves);
        StringBuilder chosen = new StringBuilder();
        StringBuilder scores = new StringBuilder();

        long evals0 = OptConfig.evalCount;
        long searchTime = 0;

        for (int m = 0; m < maxMoves; m++) {
            int color = (m % 2 == 0) ? 1 : 0;   // vertical starts
            OptConfig.lastRootScore = Double.NaN;   // Book moves leave it NaN
            long t0 = System.currentTimeMillis();
            int[] mv = h.chooseMove(color, m);
            searchTime += System.currentTimeMillis() - t0;
            chosen.append('(').append(mv[0]).append(',').append(mv[1]).append(") ");
            scores.append(String.format("%.6g ", OptConfig.lastRootScore));
            h.newMove(script[m][0], script[m][1], color);
        }

        return new GameResult(searchTime, OptConfig.evalCount - evals0,
            chosen.toString().trim(), scores.toString().trim());
    }

    /** Builds a deterministic move script for any board size: cells ordered
     *  by distance to the board center (then row, then column), which keeps
     *  play clustered around the center like a real game. */
    private static int[][] buildScript(int dim, int n) {
        ArrayList<int[]> cells = new ArrayList<int[]>();
        for (int r = 0; r < dim; r++)
            for (int c = 0; c < dim; c++)
                cells.add(new int[]{r, c});
        final double center = (dim - 1) / 2.0;
        java.util.Collections.sort(cells, new java.util.Comparator<int[]>() {
            public int compare(int[] a, int[] b) {
                double da = (a[0] - center) * (a[0] - center) + (a[1] - center) * (a[1] - center);
                double db = (b[0] - center) * (b[0] - center) + (b[1] - center) * (b[1] - center);
                if (da != db) return Double.compare(da, db);
                if (a[0] != b[0]) return a[0] - b[0];
                return a[1] - b[1];
            }
        });
        int[][] script = new int[n][];
        for (int i = 0; i < n; i++) script[i] = cells.get(i);
        return script;
    }

    private static void mute() {
        System.setOut(new PrintStream(new OutputStream() {
            public void write(int b) { }
            public void write(byte[] b, int off, int len) { }
        }));
    }
}

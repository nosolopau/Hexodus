package heuristics;

/**
 * Correctness check for the optimization variants: computes calculateValue
 * on a scripted sequence of positions with the baseline representation and
 * with each optimization flag, and verifies the values are identical.
 *
 * Usage: java heuristics.OptimizationEquivalence [dim] [moves]
 */
public class OptimizationEquivalence {

    public static void main(String[] args) {
        int dim = args.length > 0 ? Integer.parseInt(args[0]) : 5;
        int moves = args.length > 1 ? Integer.parseInt(args[1]) : 12;

        // Each config: {bitpath, reuse, dirtyskip, leanor}
        String[] names = {"baseline", "bitpath", "leanor", "both"};
        boolean[][] flags = {
            {false, false},
            {true,  false},
            {false, true },
            {true,  true },
        };

        double[][] values = new double[names.length][];
        for (int c = 0; c < names.length; c++)
            values[c] = valuesFor(dim, moves, flags[c][0], flags[c][1]);

        boolean ok = true;
        for (int i = 0; i < moves; i++) {
            boolean rowOk = true;
            StringBuilder row = new StringBuilder(String.format("pos %2d:", i + 1));
            for (int c = 0; c < names.length; c++) {
                row.append(String.format(" %s=%-22s", names[c], values[c][i]));
                if (values[c][i] != values[0][i]) rowOk = false;
            }
            System.out.println(row + (rowOk ? " OK" : " MISMATCH"));
            if (!rowOk) ok = false;
        }
        System.out.println(ok ? "\nALL VALUES IDENTICAL" : "\nMISMATCHES FOUND");
        if (!ok) System.exit(1);
    }

    /** Replays the scripted game and records calculateValue after each move. */
    private static double[] valuesFor(int dim, int moves, boolean bitpath, boolean leanOr) {
        OptConfig.USE_BITPATH = bitpath;
        OptConfig.USE_LEAN_OR = leanOr;

        double[] values = new double[moves];
        Simulation sim = new Simulation(dim);
        int[][] script = script(dim, moves);
        for (int m = 0; m < moves; m++) {
            int color = (m % 2 == 0) ? 1 : 0;
            sim = new Simulation(sim, script[m][0], script[m][1], color);
            values[m] = sim.calculateValue();
        }
        return values;
    }

    /** Same deterministic center-out script as the benchmark. */
    private static int[][] script(int dim, int n) {
        java.util.ArrayList<int[]> cells = new java.util.ArrayList<int[]>();
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
}

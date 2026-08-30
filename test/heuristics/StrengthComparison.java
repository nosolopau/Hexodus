package heuristics;

import game.*;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Strength comparison across the four engine combinations
 * (Object-Oriented / Bitmap H-Search, each with and without the lean OR
 * rule) at each difficulty level.
 *
 * Plays complete self-play games through the real game stack (Match, with
 * win detection) and records the full move sequence, per-move root score,
 * winner, and which moves came from the random fallback (the engine has
 * always played a random move once it considers the position decided —
 * those moves are nondeterministic by design).
 *
 * If the combinations are equally strong, their games must be identical
 * up to the first random-fallback move.
 *
 * Usage: java heuristics.StrengthComparison [dim] [maxLevel]
 */
public class StrengthComparison {

    public static void main(String[] args) throws Exception {
        int dim = args.length > 0 ? Integer.parseInt(args[0]) : 5;
        int maxLevel = args.length > 1 ? Integer.parseInt(args[1]) : 3;

        String[] names = {"OO H-Search", "OO + leanOR", "Bitmap H-Search", "Bitmap + leanOR"};
        boolean[][] flags = { // {bitpath, leanor}
            {false, false}, {false, true}, {true, false}, {true, true}
        };

        PrintStream real = System.out;

        for (int level = 1; level <= maxLevel; level++) {
            System.out.println("==================== LEVEL " + level + " (" + dim + "x" + dim + ") ====================");
            for (int c = 0; c < names.length; c++) {
                OptConfig.USE_BITPATH = flags[c][0];
                OptConfig.USE_LEAN_OR = flags[c][1];

                System.setOut(new PrintStream(new OutputStream() {
                    public void write(int b) { }
                    public void write(byte[] b, int off, int len) { }
                }));
                GameRecord rec;
                try {
                    rec = playGame(dim, level);
                } finally {
                    System.setOut(real);
                }

                System.out.printf("%-16s winner=%-10s moves=%2d  time=%6dms  firstFallback=%s%n",
                    names[c], rec.winner, rec.moveCount, rec.millis,
                    rec.firstFallback == -1 ? "none" : ("move " + rec.firstFallback));
                System.out.println("    moves:  " + rec.moves);
                System.out.println("    scores: " + rec.scores);
            }
            System.out.println();
        }
        // Restore defaults
        OptConfig.USE_BITPATH = false;
        OptConfig.USE_LEAN_OR = true;
    }

    private static class GameRecord {
        String moves = "", scores = "", winner = "none";
        int moveCount = 0, firstFallback = -1;
        long millis;
    }

    /** Plays one full self-play game through Match until a winner appears. */
    private static GameRecord playGame(int dim, int level) throws Exception {
        Match match = new Match(dim, false);
        match.setLevel(level);
        Player vertical = new Player(1, 1);
        Player horizontal = new Player(1, 0);

        GameRecord rec = new GameRecord();
        StringBuilder moves = new StringBuilder();
        StringBuilder scores = new StringBuilder();
        Player winner = null;

        long t0 = System.currentTimeMillis();
        for (int m = 1; m <= dim * dim && winner == null; m++) {
            Player cur = (m % 2 == 1) ? vertical : horizontal;
            long fb0 = OptConfig.randomFallbacks;
            OptConfig.lastRootScore = Double.NaN;
            int[] mv = match.generateMove(cur);
            boolean fellBack = OptConfig.randomFallbacks > fb0;
            if (fellBack && rec.firstFallback == -1) rec.firstFallback = m;

            moves.append('(').append(mv[0]).append(',').append(mv[1]).append(')')
                 .append(fellBack ? "* " : " ");
            scores.append(String.format("%.4g ", OptConfig.lastRootScore));

            winner = match.newMove(mv[0], mv[1], cur);
            rec.moveCount = m;
        }
        rec.millis = System.currentTimeMillis() - t0;
        rec.moves = moves.toString().trim() + "   (* = random fallback)";
        rec.scores = scores.toString().trim();
        rec.winner = (winner == null) ? "none" : ("player" + winner.getPosition());
        return rec;
    }
}

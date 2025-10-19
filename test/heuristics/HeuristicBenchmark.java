package heuristics;

import game.*;
import java.util.*;

/**
 * Performance benchmark for the Hexodus AI heuristic.
 * Measures timing and performance characteristics across different
 * board sizes and difficulty levels.
 *
 * Run separately from unit tests as this takes longer to execute.
 */
public class HeuristicBenchmark {

    private static class GameStats {
        int boardSize;
        int level;
        long totalGameTime;  // nanoseconds
        List<Long> moveTimes;  // nanoseconds per move
        int totalMoves;
        int moveNumber;

        public GameStats(int boardSize, int level) {
            this.boardSize = boardSize;
            this.level = level;
            this.moveTimes = new ArrayList<>();
            this.totalMoves = 0;
            this.moveNumber = 0;
        }

        public void addMoveTime(long nanos) {
            moveTimes.add(nanos);
            totalMoves++;
        }

        public double getAvgMoveTimeMs() {
            if (moveTimes.isEmpty()) return 0;
            long sum = 0;
            for (long t : moveTimes) sum += t;
            return (sum / moveTimes.size()) / 1_000_000.0;
        }

        public double getMinMoveTimeMs() {
            if (moveTimes.isEmpty()) return 0;
            long min = Long.MAX_VALUE;
            for (long t : moveTimes) if (t < min) min = t;
            return min / 1_000_000.0;
        }

        public double getMaxMoveTimeMs() {
            if (moveTimes.isEmpty()) return 0;
            long max = Long.MIN_VALUE;
            for (long t : moveTimes) if (t > max) max = t;
            return max / 1_000_000.0;
        }

        public double getTotalGameTimeSeconds() {
            return totalGameTime / 1_000_000_000.0;
        }

        public double getMedianMoveTimeMs() {
            if (moveTimes.isEmpty()) return 0;
            List<Long> sorted = new ArrayList<>(moveTimes);
            Collections.sort(sorted);
            int middle = sorted.size() / 2;
            if (sorted.size() % 2 == 0) {
                return (sorted.get(middle - 1) + sorted.get(middle)) / 2_000_000.0;
            } else {
                return sorted.get(middle) / 1_000_000.0;
            }
        }

        public double getFirstMoveAvgMs() {
            if (moveTimes.size() < 5) return 0;
            long sum = 0;
            for (int i = 0; i < Math.min(5, moveTimes.size()); i++) {
                sum += moveTimes.get(i);
            }
            return (sum / Math.min(5, moveTimes.size())) / 1_000_000.0;
        }

        public double getLastMoveAvgMs() {
            if (moveTimes.size() < 5) return 0;
            long sum = 0;
            int start = Math.max(0, moveTimes.size() - 5);
            for (int i = start; i < moveTimes.size(); i++) {
                sum += moveTimes.get(i);
            }
            return (sum / (moveTimes.size() - start)) / 1_000_000.0;
        }
    }

    /**
     * Runs a single AI vs AI game and collects statistics
     */
    private static GameStats runSingleGame(int boardSize, int level, boolean verbose) {
        GameStats stats = new GameStats(boardSize, level);

        try {
            Match match = new Match(boardSize, false);  // No swap for benchmark
            match.setLevel(level);

            Player p1 = new Player(1, 1);  // Vertical AI
            Player p2 = new Player(1, 0);  // Horizontal AI

            Player currentPlayer = p1;
            Player winner = null;
            int moveCount = 0;

            long gameStart = System.nanoTime();

            while (winner == null && moveCount < boardSize * boardSize) {
                long moveStart = System.nanoTime();

                int[] move = match.generateMove(currentPlayer);

                long moveEnd = System.nanoTime();
                long moveDuration = moveEnd - moveStart;

                stats.addMoveTime(moveDuration);

                if (verbose) {
                    System.out.printf("  Move %2d: [%d,%d] in %.1f ms\n",
                                     moveCount + 1, move[0], move[1],
                                     moveDuration / 1_000_000.0);
                }

                winner = match.newMove(move[0], move[1], currentPlayer);

                currentPlayer = (currentPlayer == p1) ? p2 : p1;
                moveCount++;
            }

            long gameEnd = System.nanoTime();
            stats.totalGameTime = gameEnd - gameStart;

            if (verbose) {
                System.out.printf("  Game finished in %d moves, winner: %s\n",
                                 moveCount,
                                 winner != null ? winner.getName() : "Draw");
            }

        } catch (Exception e) {
            System.err.println("Error running game: " + e.getMessage());
            e.printStackTrace();
        }

        return stats;
    }

    /**
     * Runs multiple games and averages the results
     */
    private static List<GameStats> runMultipleGames(int boardSize, int level, int numGames, boolean verbose) {
        List<GameStats> allStats = new ArrayList<>();

        System.out.printf("\nRunning %d games: %dx%d board, Level %d\n",
                         numGames, boardSize, boardSize, level);
        System.out.println("=".repeat(60));

        for (int i = 0; i < numGames; i++) {
            if (verbose) {
                System.out.printf("\nGame %d/%d:\n", i + 1, numGames);
            } else {
                System.out.printf("Game %d/%d... ", i + 1, numGames);
            }

            GameStats stats = runSingleGame(boardSize, level, verbose);
            allStats.add(stats);

            if (!verbose) {
                System.out.printf("%.1fs (%d moves)\n",
                                 stats.getTotalGameTimeSeconds(),
                                 stats.totalMoves);
            }
        }

        return allStats;
    }

    /**
     * Calculates average statistics from multiple games
     */
    private static void printSummary(List<GameStats> allStats) {
        if (allStats.isEmpty()) return;

        GameStats first = allStats.get(0);

        double avgGameTime = 0;
        double avgMoves = 0;
        double avgMoveTime = 0;
        double avgMedian = 0;
        double avgFirstMoves = 0;
        double avgLastMoves = 0;
        double minMoveTime = Double.MAX_VALUE;
        double maxMoveTime = 0;

        for (GameStats stats : allStats) {
            avgGameTime += stats.getTotalGameTimeSeconds();
            avgMoves += stats.totalMoves;
            avgMoveTime += stats.getAvgMoveTimeMs();
            avgMedian += stats.getMedianMoveTimeMs();
            avgFirstMoves += stats.getFirstMoveAvgMs();
            avgLastMoves += stats.getLastMoveAvgMs();
            minMoveTime = Math.min(minMoveTime, stats.getMinMoveTimeMs());
            maxMoveTime = Math.max(maxMoveTime, stats.getMaxMoveTimeMs());
        }

        int n = allStats.size();
        avgGameTime /= n;
        avgMoves /= n;
        avgMoveTime /= n;
        avgMedian /= n;
        avgFirstMoves /= n;
        avgLastMoves /= n;

        System.out.println("\n" + "=".repeat(60));
        System.out.println("SUMMARY STATISTICS");
        System.out.println("=".repeat(60));
        System.out.printf("Board Size:           %dx%d\n", first.boardSize, first.boardSize);
        System.out.printf("Difficulty Level:     %d\n", first.level);
        System.out.printf("Games Played:         %d\n", n);
        System.out.println("-".repeat(60));
        System.out.printf("Avg Game Duration:    %.2f seconds\n", avgGameTime);
        System.out.printf("Avg Moves per Game:   %.1f\n", avgMoves);
        System.out.printf("Avg Time per Move:    %.1f ms\n", avgMoveTime);
        System.out.printf("Median Move Time:     %.1f ms\n", avgMedian);
        System.out.printf("Min Move Time:        %.1f ms\n", minMoveTime);
        System.out.printf("Max Move Time:        %.1f ms\n", maxMoveTime);
        System.out.printf("Avg First 5 Moves:    %.1f ms\n", avgFirstMoves);
        System.out.printf("Avg Last 5 Moves:     %.1f ms\n", avgLastMoves);
        System.out.println("=".repeat(60));
    }

    /**
     * Main benchmark runner
     */
    public static void main(String[] args) {
        System.out.println("HEXODUS AI PERFORMANCE BENCHMARK");
        System.out.println("Generated: " + new Date());
        System.out.println("Processors: " + Runtime.getRuntime().availableProcessors());

        // Configuration
        int gamesPerConfig = 3;  // Number of games to average
        boolean verbose = false;  // Set to true for detailed move-by-move output

        // Test configurations: [boardSize, level]
        int[][] configs = {
            {3, 1},  // Small board, level 1
            {5, 1},  // Medium board, level 1
            {6, 1},  // Standard board, level 1
            {3, 2},  // Small board, level 2
            {5, 2},  // Medium board, level 2
        };

        Map<String, List<GameStats>> allResults = new LinkedHashMap<>();

        for (int[] config : configs) {
            int boardSize = config[0];
            int level = config[1];
            String key = boardSize + "x" + boardSize + "_L" + level;

            List<GameStats> results = runMultipleGames(boardSize, level, gamesPerConfig, verbose);
            allResults.put(key, results);
            printSummary(results);
        }

        // Print comparison table
        System.out.println("\n\n" + "=".repeat(80));
        System.out.println("COMPARISON TABLE");
        System.out.println("=".repeat(80));
        System.out.printf("%-12s | %-8s | %-12s | %-12s | %-10s\n",
                         "Config", "Avg Game", "Avg Move", "Moves/Game", "Throughput");
        System.out.println("-".repeat(80));

        for (Map.Entry<String, List<GameStats>> entry : allResults.entrySet()) {
            List<GameStats> stats = entry.getValue();
            double avgGameTime = 0;
            double avgMoveTime = 0;
            double avgMoves = 0;

            for (GameStats s : stats) {
                avgGameTime += s.getTotalGameTimeSeconds();
                avgMoveTime += s.getAvgMoveTimeMs();
                avgMoves += s.totalMoves;
            }

            avgGameTime /= stats.size();
            avgMoveTime /= stats.size();
            avgMoves /= stats.size();
            double throughput = avgMoves / avgGameTime;

            System.out.printf("%-12s | %7.2fs | %10.1f ms | %10.1f | %8.2f m/s\n",
                             entry.getKey(), avgGameTime, avgMoveTime, avgMoves, throughput);
        }

        System.out.println("=".repeat(80));
        System.out.println("\nBenchmark completed successfully!");
    }
}

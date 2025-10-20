package heuristics;

import game.*;

/**
 * Benchmark for 7x7 board: Computer vs Computer, Level 2 (Expert mode)
 */
public class Benchmark7x7 {
    public static void main(String[] args) {
        System.out.println("7x7 BOARD BENCHMARK");
        System.out.println("================================");
        System.out.println("Configuration: 7x7 board, Level 2 (Expert Mode)");
        System.out.println("Computer vs Computer (first 10 moves)\n");

        Match match = new Match(7, false);
        try {
            match.setLevel(2);
        } catch (IncorrectLevel e) {
            e.printStackTrace();
        }

        Player vertical = new Player(1, 1);   // Computer
        Player horizontal = new Player(1, 0); // Computer

        int moveNumber = 0;
        long totalTime = 0;
        long gameStart = System.currentTimeMillis();
        Player winner = null;

        // Only run first 10 moves (early game is the bottleneck)
        while (winner == null && moveNumber < 10) {
            moveNumber++;
            Player currentPlayer = (moveNumber % 2 == 1) ? vertical : horizontal;

            long moveStart = System.currentTimeMillis();
            int[] move = match.generateMove(currentPlayer);
            long moveTime = System.currentTimeMillis() - moveStart;
            totalTime += moveTime;

            System.out.printf("Move %2d: (%d,%d) - %5dms",
                moveNumber, move[0], move[1], moveTime);

            // Show which moves are particularly slow
            if (moveTime > 1000) {
                System.out.print(" [VERY SLOW]");
            } else if (moveTime > 500) {
                System.out.print(" [SLOW]");
            }
            System.out.println();

            try {
                winner = match.newMove(move[0], move[1], currentPlayer);
            } catch (Exception e) {
                System.err.println("Error making move: " + e.getMessage());
                break;
            }
        }

        long gameTime = System.currentTimeMillis() - gameStart;

        System.out.println("\n================================");
        System.out.println("BENCHMARK COMPLETE");
        System.out.println("================================");
        System.out.printf("Total moves:     %d\n", moveNumber);
        System.out.printf("Total time:      %.2f seconds\n", gameTime / 1000.0);
        System.out.printf("Avg per move:    %d ms\n", totalTime / moveNumber);
    }
}

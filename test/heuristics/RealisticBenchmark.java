package heuristics;

import game.*;

/**
 * Realistic benchmark: Computer vs Computer, 5x5 board, Level 2 (Expert mode)
 * This measures typical gameplay performance
 */
public class RealisticBenchmark {
    public static void main(String[] args) {
        System.out.println("REALISTIC PERFORMANCE BENCHMARK");
        System.out.println("================================");
        System.out.println("Configuration: 5x5 board, Level 2 (Expert Mode)");
        System.out.println("Computer vs Computer (full game)\n");

        Match match = new Match(5, false);
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

        while (winner == null && moveNumber < 25) {  // 5x5 has max 25 moves
            moveNumber++;
            Player currentPlayer = (moveNumber % 2 == 1) ? vertical : horizontal;

            long moveStart = System.currentTimeMillis();
            int[] move = match.generateMove(currentPlayer);
            long moveTime = System.currentTimeMillis() - moveStart;
            totalTime += moveTime;

            System.out.printf("Move %2d: (%d,%d) - %5dms",
                moveNumber, move[0], move[1], moveTime);

            // Show which moves are particularly slow
            if (moveTime > 500) {
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
        System.out.println("GAME COMPLETE");
        System.out.println("================================");
        System.out.printf("Total moves:     %d\n", moveNumber);
        System.out.printf("Total time:      %.2f seconds\n", gameTime / 1000.0);
        System.out.printf("Avg per move:    %d ms\n", totalTime / moveNumber);
        if (winner != null) {
            System.out.printf("Winner:          Player %d\n", winner.getPosition());
        } else {
            System.out.println("Winner:          Draw/Incomplete");
        }
    }
}

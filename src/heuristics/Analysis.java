package heuristics;

/**
 *  Records what the engine considered during its last move search, so the
 *  interface can show the reasoning behind a move.
 *
 *  Only the root level of the alpha-beta tree is recorded: for every
 *  candidate move the engine actually evaluated, the resulting score. Moves
 *  cut off by alpha-beta pruning are left unrecorded, which is itself
 *  informative — it shows what the search decided not to look at.
 *
 *  Recording is skipped entirely unless {@link #ENABLED} is set, so the
 *  engine pays nothing when the feature is off.
 *
 *  @author Pau
 *  @version 1.0
 */
public final class Analysis {

    /** True when the interface wants the engine to report its reasoning */
    public static volatile boolean ENABLED = false;

    private static double[][] scores = new double[0][0];
    private static int bestRow = -1, bestColumn = -1;
    private static int leaderRow = -1, leaderColumn = -1;   // Best so far, mid-search
    private static double leaderScore = Double.NaN;
    private static int mover = -1;          // Colour that was choosing a move
    private static int evaluated = 0;       // Candidates actually scored
    private static int candidates = 0;      // Legal moves available
    private static long millis = 0;
    private static boolean valid = false;   // Search finished
    private static boolean started = false; // Search under way or finished

    /** Notified after each candidate is scored, so the interface can show
     *  the search as it happens. Runs on whichever thread the search is
     *  using — both engines record from the thread that called chooseMove,
     *  which is the event thread when a move is played from the board. */
    private static volatile Runnable listener;

    private Analysis() {
    }

    /** Sets (or clears, with null) the progress listener */
    public static void setListener(Runnable r) {
        listener = r;
    }

    private static void notifyListener() {
        Runnable r = listener;
        if (r != null) r.run();
    }

    /** Starts a new recording for one move search.
     *  @param dimension Board dimension
     *  @param color Colour of the player to move
     *  @param legalMoves Number of legal moves available */
    public static synchronized void begin(int dimension, int color, int legalMoves) {
        if (scores.length != dimension) scores = new double[dimension][dimension];
        for (int i = 0; i < dimension; i++)
            for (int j = 0; j < dimension; j++)
                scores[i][j] = Double.NaN;      // NaN = never examined
        mover = color;
        candidates = legalMoves;
        evaluated = 0;
        bestRow = bestColumn = -1;
        leaderRow = leaderColumn = -1;
        leaderScore = Double.NaN;
        millis = 0;
        valid = false;
        started = true;
        notifyListener();
    }

    /** Records the score the search obtained for one candidate move, and
     *  tracks the best seen so far so the display can follow along. */
    public static void record(int row, int column, double score) {
        synchronized (Analysis.class) {
            if (row < 0 || column < 0 || row >= scores.length || column >= scores.length) return;
            if (Double.isNaN(scores[row][column])) evaluated++;
            scores[row][column] = score;

            boolean better = Double.isNaN(leaderScore)
                || (mover == 1 ? score > leaderScore : score < leaderScore);
            if (better) {
                leaderScore = score;
                leaderRow = row;
                leaderColumn = column;
            }
        }
        notifyListener();   // outside the lock: the listener repaints
    }

    /** Closes the recording with the move finally played. */
    public static void finish(int row, int column, long elapsedMillis) {
        synchronized (Analysis.class) {
            bestRow = row;
            bestColumn = column;
            millis = elapsedMillis;
            valid = true;
        }
        notifyListener();
    }

    /** Discards the recording (e.g. after a human move, so the overlay does
     *  not linger over a position it no longer describes). */
    public static synchronized void clear() {
        valid = false;
        started = false;
    }

    /** @return True if there is anything to display — including a search
     *  still in progress, so the overlay can follow the engine live */
    public static synchronized boolean isAvailable() {
        return ENABLED && started && evaluated > 0;
    }

    /** @return True while a search is running (no result yet) */
    public static synchronized boolean isSearching() {
        return ENABLED && started && !valid;
    }

    /** @return Copy of the score grid; NaN where the move was not examined */
    public static synchronized double[][] getScores() {
        double[][] copy = new double[scores.length][];
        for (int i = 0; i < scores.length; i++) copy[i] = scores[i].clone();
        return copy;
    }

    /** Row of the move to highlight: the one played once the search is
     *  done, or the best found so far while it is still running */
    public static synchronized int getBestRow()     { return valid ? bestRow : leaderRow; }
    public static synchronized int getBestColumn()  { return valid ? bestColumn : leaderColumn; }
    public static synchronized int getMover()       { return mover; }
    public static synchronized int getEvaluated()   { return evaluated; }
    public static synchronized int getCandidates()  { return candidates; }
    public static synchronized long getMillis()     { return millis; }
}

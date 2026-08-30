package heuristics;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *  Per-thread reusable buffers for calculateResistance, used when
 *  OptConfig.USE_REUSE is active. Avoids re-allocating the cloned connection
 *  matrix, its Route objects and the conductance matrices on every
 *  evaluation.
 *
 *  A Scratch instance is only valid within one calculateResistance call at a
 *  time (the buffers are recycled on the next call), which holds because
 *  calculateResistance never nests within a thread.
 *
 *  @author Pau
 *  @version 1.0
 */
final class Scratch {
    private static final ThreadLocal<Scratch> LOCAL = new ThreadLocal<Scratch>();

    private final int ids;              // Number of cell ids: dimension^2 + 4 borders
    private final Route[][] map;        // Reusable connection matrix
    private final ArrayList<Route> pool = new ArrayList<Route>(); // Recycled Route objects
    private int poolUsed;               // Routes handed out in the current copy

    // Conductance matrices cached by node count (numNodes shrinks as the game fills)
    private final double[][][] mBySize;
    private final double[][][] nBySize;
    private final double[][] bBySize;

    private Scratch(int dimension) {
        ids = dimension * dimension + 4;
        map = new Route[ids][ids];
        int maxNodes = dimension * dimension + 2; // All squares + the 2 borders of one player
        mBySize = new double[maxNodes + 1][][];
        nBySize = new double[maxNodes + 1][][];
        bBySize = new double[maxNodes + 1][];
    }

    /** Returns the scratch buffers of the current thread, creating or
     *  resizing them if needed for the given board dimension */
    static Scratch get(int dimension) {
        Scratch s = LOCAL.get();
        if (s == null || s.ids != dimension * dimension + 4) {
            s = new Scratch(dimension);
            LOCAL.set(s);
        }
        return s;
    }

    /** Copies the source connections into the reusable matrix, recycling
     *  Route objects from the pool. Equivalent to Connections.clone() but
     *  without allocating a new Route[][] or new Route/ArrayList objects.
     *  @param src Connections to copy
     *  @return A Connections view over the reusable matrix */
    Connections copyConnections(Connections src) {
        poolUsed = 0;
        Route[][] srcMap = src.getMap();
        for (int i = 0; i < ids; i++) {
            Route[] srcRow = srcMap[i];
            Route[] dstRow = map[i];
            for (int j = 0; j < ids; j++) {
                Route r = srcRow[j];
                dstRow[j] = (r == null) ? null : borrow(r);
            }
        }
        return new Connections(ids, map);
    }

    private Route borrow(Route src) {
        Route r;
        if (poolUsed < pool.size()) {
            r = pool.get(poolUsed);
        } else {
            r = new Route();
            pool.add(r);
        }
        poolUsed++;
        r.resetFrom(src);
        return r;
    }

    /** Reusable temporary conductance matrix. Safe without clearing: the
     *  build loop overwrites every off-diagonal element and never reads the
     *  diagonal. */
    double[][] getM(int numNodes) {
        if (mBySize[numNodes] == null) mBySize[numNodes] = new double[numNodes][numNodes];
        return mBySize[numNodes];
    }

    /** Reusable final conductance matrix. Safe without clearing: every
     *  element is overwritten while suppressing the ground node. */
    double[][] getN(int numNodes) {
        if (nBySize[numNodes] == null) nBySize[numNodes] = new double[numNodes - 1][numNodes - 1];
        return nBySize[numNodes];
    }

    /** Reusable independent-terms vector, cleared before use because only
     *  the source node entry is written. */
    double[] getB(int numNodes) {
        double[] b = bBySize[numNodes];
        if (b == null) {
            b = new double[numNodes - 1];
            bBySize[numNodes] = b;
        } else {
            Arrays.fill(b, 0.0);
        }
        return b;
    }
}

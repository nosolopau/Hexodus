package heuristics;

import java.util.*;

/**
 *  The connective structure of a position, reduced to what a player thinks
 *  in: touching stones merged into groups, and the virtual connections that
 *  already bind separate groups together or tie a group to its own edge.
 *
 *  H-Search discovers hundreds of connections, almost all of them between
 *  empty cells and only meaningful inside the search. This keeps the few
 *  that describe the position: links between the player's own groups, and
 *  from a group to the edge it is trying to reach.
 *
 *  @author Pau
 *  @version 1.0
 */
public class Skeleton {

    /** One virtual connection worth showing */
    public static class Link {
        /** Endpoints, as board cells; either may be null for a board edge */
        public final Square from, to;
        /** True when the link runs to the player's own edge rather than to
         *  another group; then only {@code from} is set */
        public final boolean toEdge;
        /** True for the far edge (south / east) when {@code toEdge} */
        public final boolean farEdge;
        /** Empty cells the connection depends on */
        public final List<Square> carrier;

        Link(Square from, Square to, boolean toEdge, boolean farEdge, List<Square> carrier) {
            this.from = from;
            this.to = to;
            this.toEdge = toEdge;
            this.farEdge = farEdge;
            this.carrier = carrier;
        }
    }

    private final List<Link> links = new ArrayList<Link>();

    public List<Link> getLinks() {
        return links;
    }

    /** Builds the skeleton for one player from a position that has already
     *  been evaluated.
     *  @param sim Simulation whose last evaluation discovered the connections
     *  @param color Player to describe
     *  @return The links worth drawing; empty if nothing was computed */
    public static Skeleton of(Simulation sim, int color) {
        Skeleton out = new Skeleton();
        Connections vc = sim.discovered(color);
        Board board = sim.getBoard();
        if (vc == null) return out;

        int dim = board.getDimension();
        List<List<Square>> groups = groups(board, color, dim);

        // Which edge cells belong to this player, for group-to-edge links
        Cell near = null, far = null;
        for (Cell c : board.generateG(color))
            if (c instanceof Border) {
                char name = ((Border) c).getName();
                if (name == 'N' || name == 'E') near = c; else far = c;
            }

        for (int i = 0; i < groups.size(); i++) {
            for (int j = i + 1; j < groups.size(); j++) {
                Square[] pair = closestConnected(vc, groups.get(i), groups.get(j));
                if (pair != null)
                    out.links.add(new Link(pair[0], pair[1], false, false,
                        carrier(vc, board, pair[0], pair[1], dim)));
            }
            Square anchor = connectedTo(vc, groups.get(i), near);
            if (anchor != null)
                out.links.add(new Link(anchor, null, true, false,
                    carrier(vc, board, anchor, near, dim)));
            anchor = connectedTo(vc, groups.get(i), far);
            if (anchor != null)
                out.links.add(new Link(anchor, null, true, true,
                    carrier(vc, board, anchor, far, dim)));
        }
        return out;
    }

    /** Touching stones of one colour, merged into groups */
    private static List<List<Square>> groups(Board board, int color, int dim) {
        List<List<Square>> groups = new ArrayList<List<Square>>();
        boolean[][] seen = new boolean[dim][dim];

        for (int r = 0; r < dim; r++) {
            for (int c = 0; c < dim; c++) {
                if (seen[r][c] || board.get(r, c).getColor() != color) continue;
                List<Square> group = new ArrayList<Square>();
                Deque<int[]> queue = new ArrayDeque<int[]>();
                queue.add(new int[]{r, c});
                seen[r][c] = true;
                while (!queue.isEmpty()) {
                    int[] p = queue.poll();
                    group.add(board.get(p[0], p[1]));
                    int[][] around = {{p[0]-1,p[1]-1},{p[0]-1,p[1]},{p[0],p[1]-1},
                                      {p[0],p[1]+1},{p[0]+1,p[1]},{p[0]+1,p[1]+1}};
                    for (int[] q : around) {
                        if (q[0] < 0 || q[1] < 0 || q[0] >= dim || q[1] >= dim) continue;
                        if (seen[q[0]][q[1]] || board.get(q[0], q[1]).getColor() != color) continue;
                        seen[q[0]][q[1]] = true;
                        queue.add(q);
                    }
                }
                groups.add(group);
            }
        }
        return groups;
    }

    /** The closest pair of stones across two groups that H-Search links */
    private static Square[] closestConnected(Connections vc, List<Square> a, List<Square> b) {
        Square[] best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (Square x : a)
            for (Square y : b) {
                if (!vc.hasConnectionEx(x, y)) continue;
                int d = Math.abs(x.getRow() - y.getRow()) + Math.abs(x.getColumn() - y.getColumn());
                if (d < bestDistance) { bestDistance = d; best = new Square[]{x, y}; }
            }
        return best;
    }

    /** The stone of a group that H-Search links to the given edge */
    private static Square connectedTo(Connections vc, List<Square> group, Cell edge) {
        if (edge == null) return null;
        for (Square s : group)
            if (vc.hasConnectionEx(s, edge)) return s;
        return null;
    }

    /** The empty cells the shortest form of a connection relies on */
    private static List<Square> carrier(Connections vc, Board board, Cell a, Cell b, int dim) {
        List<Square> cells = new ArrayList<Square>();
        Route route = vc.getRoute(a, b);
        if (route == null) return cells;
        Path shortest = route.getMinimumPath();
        if (shortest == null) return cells;

        for (Integer id : shortest.cellIds()) {
            int index = id.intValue();
            if (index < 0 || index >= dim * dim) continue;      // an edge, not a cell
            Square s = board.get(index / dim, index % dim);
            if (s.isEmpty()) cells.add(s);
        }
        return cells;
    }
}

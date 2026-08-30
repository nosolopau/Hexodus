package heuristics;
import java.util.*;

class NonexistentSquare extends Exception{};

/**
 * Represents the simulation of a move and allows undoing the changes of the
 * move. It is used to calculate the numerical value according to the corresponding heuristic
 * for the simulated move.
 *
 * @author Pau
 * @version 10.2
 */

public class Simulation {
    private int K;                      // Max size for path (5 to 6)
    private HashMap eliminatedNeighbors, eliminatedNeighbors2;
    private ArrayList <Cell> affectedNeighbors ;
    private ArrayList renew;
    private ArrayList <Cell> G[];       // List of G squares
    private Connections C[];            // List of virtual connections for each player
    private Connections SC[];           // List of virtual semi-connections for each player
    private Connections connections;
    private boolean newsConnections;    // Stores whether C or SC were created in the previous iteration
    private Square target;
    private double[][] potentials = new double[2][];  // Node voltages per player
    private Board board;
    
    /** Creates the objects common to all simulations. */
    private Simulation(){
        K = 5;
        
        /* Creates two instances (one for black and one for white) of the objects that
         * are duplicated for the two players */
        G = new ArrayList [2];     // List with the 'usable' squares of the graph
        C = new Connections [2];    // List with the virtual connections discovered
        SC = new Connections [2];   // List with the virtual semi-connections discovered

    }
    
    /** Creates a base simulation with an empty board for the dimension that
     *  is received as argument.
     *  @param dimension Game board dimension */
    public Simulation(int dimension) {
        this();
        
        for(int i = 0; i <= 1; i++){
            C[i] = new Connections(dimension);
            SC[i] = new Connections(dimension);
        }
        board = new Board(dimension);
        connections = board.getConnections();
    }
    
    /** Creates a new instance of Simulation with a target cell
     *  that is passed as argument
     *  @param base Parent simulation
     *  @param cel Target Square
     *  @param color Color of the player responsible for the simulation */
    public Simulation(Simulation base, Square cel, int color) {
        this(base, cel.getRow(), cel.getColumn(), color);
    }
    
    /** Creates a new instance of Simulation with a target cell
     *  identified by its row and column
     *  @param base Parent simulation
     *  @param row Row of the target square
     *  @param column Column of the target square
     *  @param color Color of the player responsible for the simulation */
    public Simulation(Simulation base, int row, int column, int color) {
        this();
        long tStart = System.nanoTime();    // Phase timer (instrumentation)

        board = base.getBoard();
        connections = board.getConnections();
        int dimension = board.getDimension();
        
        for(int i = 0; i <= 1; i++){
            G[i] = new ArrayList <Cell>();
            /* C[i] is never read during the search (calculateResistance works on
             * a copy of the board connections), only by the display helpers, so
             * the reuse variant skips this allocation. */
            if(!OptConfig.USE_REUSE)
                C[i] = new Connections(dimension);
            SC[i] = new Connections(dimension);
        }

        target = board.get(row, column);
        target.occupy(color);

        ArrayList add = new ArrayList();    // List of squares to be added as neighbors
        Cell neighbor = null;                       // Used to iterate over the neighbours of the target cell
        
        affectedNeighbors = new ArrayList<Cell>();
        eliminatedNeighbors = new HashMap();
        eliminatedNeighbors2 = new HashMap();
        
        // Iterate over the neighbors of the cell that was just occupied        
        Iterator <Cell> neighborIt = target.getNeighborList().iterator();
        while(neighborIt.hasNext()){
            neighbor = neighborIt.next();
            /* If a neighbor is the same color as the recin inserted it is necessary to
             * remove the old cell (with its possible connections) and transfer
             * its neighbors to the new one. */
            ArrayList <Cell> pendingRemoval = new ArrayList<Cell>();
            
            if((neighbor.getColor() == color) && !(neighbor instanceof Border)){
                ArrayList <Cell>movedNeighbors = new ArrayList<Cell>();
                ArrayList <Cell>sharedNeighbors = new ArrayList<Cell>();
                
                affectedNeighbors.add(neighbor);
                
                ArrayList <Cell> remove = new ArrayList<Cell>();
                
                Iterator neighborIt2 = neighbor.getNeighborList().iterator();
                while(neighborIt2.hasNext()){
                    Cell neighbor2 = (Cell)neighborIt2.next();

                    if(neighbor2 != target){
                        if(neighbor2.isNeighbor(target)){
                            sharedNeighbors.add(neighbor2);
                        }
                        else{
                            movedNeighbors.add(neighbor2);     
                            add.add(neighbor2);  // Cannot be added directly (see below)
                        }
                        connections.removeConnection(neighbor2, neighbor);  // These operations can be added to Board as is and called from here
                        remove.add(neighbor2);
                    }
                }
                
                Iterator removalIt = remove.iterator();
                while(removalIt.hasNext()){
                    Cell removedNeighbor = (Cell) removalIt.next();
                    neighbor.removeNeighbor(removedNeighbor);
                    removedNeighbor.removeNeighbor(neighbor);
                }
                        
                addRemoved(eliminatedNeighbors, neighbor, movedNeighbors);
                addRemoved(eliminatedNeighbors2, neighbor, sharedNeighbors);
                pendingRemoval.add(neighbor);
            }
            Iterator removalIt = pendingRemoval.iterator();
            while(removalIt.hasNext()){
                Cell removedNeighbor = (Cell) removalIt.next();
                neighbor.removeNeighbor(removedNeighbor);
                removedNeighbor.removeNeighbor(neighbor);
            }
        }
        
        /* The neighbors from the intermediate list are added. This is done in two
         * stages to avoid a concurrency problem. */
        Iterator addIt = add.iterator();
        while(addIt.hasNext()){
            Cell newNeighbor = (Cell)addIt.next();
            if(target != newNeighbor){    // A cell cannot be neighbor of itself
                
                target.addNeighbor(newNeighbor);
                newNeighbor.addNeighbor(target);
                
                connections.insertDirectPath(newNeighbor, target); // before above
            }
        }
        OptConfig.nsSimBuild += System.nanoTime() - tStart;
    }
    
    /** Returns the target square that the simulation studies
     *  @return Target square of the simulation */
    public Square getTargetCell(){
        return target;
    }
    
    /** Calculates the simulation value
     *  @return A double with the value of dividing the white resistance by
     *  the black resistance */
    public double calculateValue() {
        double r0 = 0, r1 = 0;
        OptConfig.evalCount++;

        try {
            r0 = calculateResistance(0);
            restoreExpiredPaths();
            if(r0 == 0) return 0;

            r1 = calculateResistance(1);
            restoreExpiredPaths();
            if(r1 == 0) return Double.POSITIVE_INFINITY;
        } catch (NonexistentSquare ex) {
            ex.printStackTrace();
        }
        
        return r0/r1;
    }
    
    /** Restores the data from la simulacin current basndose en el history of
     *  recorded changes */
    public void restore(){
        restoreImpl();
    }

    /** Computes only the given player's resistance for this position.
     *  Used by the decided-position fallback: when the full evaluation is
     *  Infinity for every move, the player's own resistance still ranks
     *  moves by how much they strengthen the player's own connection
     *  (lower is better).
     *  @param color Player whose resistance is computed
     *  @return The player's equivalent resistance */
    public double calculateOwnResistance(int color){
        double r = Double.POSITIVE_INFINITY;
        try {
            r = calculateResistance(color);
            restoreExpiredPaths();
        } catch (NonexistentSquare ex) {
            ex.printStackTrace();
        }
        return r;
    }

    private void restoreImpl(){
        long tStart = System.nanoTime();    // Phase timer (instrumentation)
        target.occupy(-1);
        boolean reconnected = false;
        ArrayList remove = new ArrayList();
        
        Iterator i = affectedNeighbors.iterator();
        while(i.hasNext()){
            reconnected = true;
            Cell c = (Cell)i.next();
            
            connections.insertDirectPath(target, c);
            target.addNeighbor(c);
            c.addNeighbor(target);
            
            if(eliminatedNeighbors.containsKey(c)){
                ArrayList a = (ArrayList) eliminatedNeighbors.get(c);
                Iterator i2 = a.iterator();
                while(i2.hasNext()){
                    Cell c2 = (Cell)i2.next();
                    c.addNeighbor(c2);
                    c2.addNeighbor(c);
                    
                    connections.removeConnection(target, c2);
                    remove.add(c2);
                    
                    connections.insertDirectPath(c, c2);
                }
            }
            
            if(eliminatedNeighbors2.containsKey(c)){
                ArrayList a = (ArrayList) eliminatedNeighbors2.get(c);
                Iterator i2 = a.iterator();
                while(i2.hasNext()){
                    Cell c2 = (Cell)i2.next();
                    c.addNeighbor(c2);
                    c2.addNeighbor(c);
                    
                    connections.insertDirectPath(c, c2);
                }
            }
            
            Iterator removalIt = remove.iterator();
            while(removalIt.hasNext()){
                Cell removedNeighbor = (Cell) removalIt.next();
                target.removeNeighbor(removedNeighbor);
                removedNeighbor.removeNeighbor(target);
            }
        }
        OptConfig.nsRestore += System.nanoTime() - tStart;
    }
    
    /** Returns the free squares of the board associated with the simulation
     *  @return ArrayList of free squares */
    public ArrayList<Square> getFreeCells(){
        return board.getFreeCells();
    }

    /** Populates the provided list with free cells instead of creating a new ArrayList.
     *  Optimization to avoid object allocation in alpha-beta search.
     *  @param list ArrayList to populate with free cells */
    public void getFreeCellsInto(ArrayList<Square> list){
        board.getFreeCellsInto(list);
    }
    
    /** Adds a removed cell to the list to undo changes
     *  @param m The HashMap to which the cell in question must be added
     *  @param c Cell to be added
     *  @param neighbors List of neighbors of the cell to be added */
    private void addRemoved(HashMap m, Cell c, ArrayList neighbors){
        m.put(c, neighbors);
    }
    
    /** Calculates the valid combinations between the elements of the set G,
     *  called g, g1 and g2. Keeps g fixed and varies g1 and g2 over it: */
    private double calculateResistance(int color) throws NonexistentSquare{
        long tPhase = System.nanoTime();    // Phase timer (instrumentation)
        int maxDepth = 100;

        /* Creates a list to contain the paths that should expire in the
         * first search iteration */
        ArrayList expireOriginal = board.getExpireList();

        // A series of structures to implement path expiration:
        ArrayList expiring = new ArrayList(); // Paths that will expire in an iteration
        renew = new ArrayList(); // Paths that have aged and must be restored upon completion
        ArrayList nextExpiring = new ArrayList();  // Squares that will expire next iteration

        expiring = (ArrayList) expireOriginal.clone();  // Copy to expiring the paths that will expire in the second iteration

        G[color] = board.generateG(color);
        int numNodes = G[color].size();
        Cell[] nodes = (Cell []) G[color].toArray(new Cell [numNodes]);
        
        Connections SubSC = SC[color];
        Connections SubC;

        /* The search works on a private copy of the board connections. The
         * reuse variant copies into per-thread scratch buffers (recycling the
         * Route matrix and Route objects) instead of cloning. */
        if(OptConfig.USE_REUSE)
            SubC = Scratch.get(board.getDimension()).copyConnections(connections);
        else
            SubC = connections.clone();

        // Prevents indices from having to be redefined in each iteration
        int g1;
        int g2;
        int g;
        
        // Temporary references to cells
        Cell cg = null;
        Cell cg1 = null;
        Cell cg2 = null;
        
        // Variables for connection search
        newsConnections = true;
        int iterations = 0;

        long tNow = System.nanoTime();
        OptConfig.nsSetup += tNow - tPhase;
        tPhase = tNow;

        while(newsConnections && (iterations < maxDepth)){
            newsConnections = false;
            iterations++;
            if(OptConfig.USE_DIRTY_SKIP) SubC.refreshNewFlags();
            for(g = 0; g < numNodes; g++){
                cg = nodes[g];
                for(g1 = 0; g1 < numNodes; g1++){
                    if(g1 != g){
                        cg1 = nodes[g1];
                        if((!(cg.getColor() == color)) || (cg1.isEmpty())){
                            for(g2 = g1 + 1; g2 < numNodes; g2++){
                                if(g2 != g){
                                    cg2 = nodes[g2];
                                    if((!(cg.getColor() == color)) || (cg2.isEmpty())){
                                        Iterator ic1 = null, ic2 = null;

                                        /* Gets an iterator for the set of paths between two squares. The paths
                                         * are only represented in one direction, so the combination
                                         * inverse of squares is also checked.
                                         */
                                        Route r1 = SubC.getRoute(cg, cg1);
                                        if(r1 == null) r1 = SubC.getRoute(cg1, cg);
                                        
                                        Route r2 = SubC.getRoute(cg, cg2);
                                        if(r2 == null) r2 = SubC.getRoute(cg2, cg);
                                        
                                        if((r1 != null) && (r2 != null) && routePairWorthVisiting(r1, r2)){
                                            ic1 = r1.getIterator();

                                            /* The following block ensures that all possible combinations
                                             * of paths from g - g1 and g - g2 are traversed. The nested while ensures that
                                             * all cases are studied. */
                                            while(ic1.hasNext()){
                                                Path c1 = (Path) ic1.next();
                                                ic2 = r2.getIterator();  // Reset iterator for each c1
                                                while(ic2.hasNext()){
                                                    Path c2 = (Path) ic2.next();
                                                    if(c1.hasEmptyIntersection(c2) && !c2.contains(cg1) && !c1.contains(cg2)){
                                                        if(c1.isNew() || c2.isNew()){
                                                            nextExpiring.add(c1);
                                                            nextExpiring.add(c2);
                                                            
                                                            // Apply the AND rule by studying the color of the target square:
                                                            if(cg.getColor() == color){
                                                                if(SubC.getRoute(cg1, cg2) == null){ // If there is no route
                                                                    SubC.newConnection(cg1, cg2);
                                                                    newsConnections = true;
                                                                }
                                                                Route r = SubC.getRoute(cg1, cg2);
                                                                Path c = c1.union(c2);
                                                                r.add(c);

                                                                if((cg1 instanceof Border) && (cg2 instanceof Border)){
                                                                    if((((((Border)cg1).getName() == 'N') && (((Border)cg2).getName() == 'S')) ||
                                                                            ((((Border)cg1).getName() == 'S') && (((Border)cg2).getName() == 'N'))) ||
                                                                            (((((Border)cg1).getName() == 'E') && (((Border)cg2).getName() == 'W')) ||
                                                                            ((((Border)cg1).getName() == 'W') && (((Border)cg2).getName() == 'E')))){
                                                                        OptConfig.nsHSearch += System.nanoTime() - tPhase;
                                                                        C[color] = SubC;   // a won position still has connections worth showing
                                                                        return 0;
                                                                    }
                                                                }
                                                            }
                                                            else{
                                                                // A SCV should not be inserted if a VC already exists
                                                                if(!SubC.hasConnectionEx(cg1, cg2)){
                                                                    Path sc = c1.union(c2, cg);
                                                                    
                                                                    if(SubSC.getRoute(cg1, cg2) == null){
                                                                        SubSC.newConnection(cg1, cg2);
                                                                        newsConnections = true;
                                                                    }
                                                                    Route r = SubSC.getRoute(cg1, cg2);

                                                                    if(r.add(sc)){
                                                                        Route rsc = r.cloneWithoutPath(sc);
                                                                        if(applyOrRule(SubC, cg1, cg2, rsc, sc, sc)){
                                                                            OptConfig.nsHSearch += System.nanoTime() - tPhase;
                                                                            C[color] = SubC;   // as above
                                                                            return 0;
                                                                        }
                                                                    }
                                                                }                                                               
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }                                    
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            /* Marks as old the paths used in the previous iteration
             * and prepares those used in the current one to expire in the next */
            Iterator expiringIt = expiring.iterator();
            while(expiringIt.hasNext()){
                Path mod = (Path)expiringIt.next();
                mod.changeNew(false);
                renew.add(mod);
            }
            if(OptConfig.USE_REUSE){
                // Swap the lists instead of cloning; net effect is identical
                ArrayList tmp = expiring;
                expiring = nextExpiring;
                nextExpiring = tmp;
                nextExpiring.clear();
            }
            else{
                expiring.clear();
                expiring = (ArrayList) nextExpiring.clone();
                nextExpiring.clear();
            }
        }

        tNow = System.nanoTime();
        OptConfig.nsHSearch += tNow - tPhase;
        tPhase = tNow;

        /* Keep the connections this search discovered so the interface can
         * show them. The search itself works on the local copy. */
        C[color] = SubC;

        HashSet<Cell> Visited = new HashSet<Cell>();      // Creates a set for visited nodes (O(1) contains)

        char sourceBorder;
        char ground;

        if(color == 1){
            sourceBorder = 'N';
            ground = 'S';
        }
        else{
            sourceBorder = 'E';
            ground = 'W';
        }

        double M[][];   // A temporary conductance matrix
        double N[][];   // Final conductance matrix
        double B[];     // The column matrix of independent terms of the equation
        int sourceIndex = -1;

        if(OptConfig.USE_REUSE){
            Scratch scratch = Scratch.get(board.getDimension());
            M = scratch.getM(numNodes);
            N = scratch.getN(numNodes);
            B = scratch.getB(numNodes);
        }
        else{
            M = new double [numNodes][numNodes];
            N = new double [numNodes-1][numNodes-1];
            B = new double [numNodes-1];
        }

        int Current = 1; // Intensity transmitted by the current source
        int n = 0;          // Index in G of current element
        int removeIndex = -1;    // Index of the node considered ground    
                    
        /* Traverses the elements of G adding the conductance to the matrix according to
         * whether connection exists. Notes the index of the ground-connected node to eliminate it
         * and writes the intensity in the node connected to the source. */
        for(int i=0; i<numNodes; i++){
            Cell c1 = nodes[i];

            if(c1 instanceof Border){
                if(((Border)c1).getName() == ground)
                    removeIndex = n;
                else if(((Border)c1).getName() == sourceBorder){
                    B[n] = Current; // If it is the node connected to the source, mark a 1 in the intensity matrix
                    sourceIndex = n;
                }
            }

            /* Marks the current node as visited to not return. This can
             * be done because the matrix is symmetric: */
            Visited.add(c1);
            
            Iterator it2 = G[color].iterator(); // Gets an iterator of the nodes
            int m = 0;  // Index in G of current element
            while(it2.hasNext()){
                Cell c2 = (Cell) it2.next();
                if(!Visited.contains(c2)){
                    /* If the elements are connected, inserts into the matrix
                     * the conductance between them. If they are not, inserts 0 */
                    if(SubC.hasConnectionEx(c1, c2)){
                        M[n][m] = M[m][n] = -1 / (double) (c1.getResistance(color) + c2.getResistance(color));
                    }
                    else M[n][m] = M[m][n] = 0;
                }
                m++;
            }
            n++;
        }
        
        /* Traverses matrix M and copies its elements to N, suppressing
         * the row and column of the ground node and writing the appropriate data
         * in the diagonal of the matrix. */
        double ac; // Accumulator of the sum of conductances of each row
        int a = 0;
        int i = 0;
        while(i < numNodes){
            ac = 0;
            int j = 0;
            int b = 0;
            if(i != removeIndex){
                while(j < numNodes){
                    if(j != i){
                        if(j != removeIndex){
                            N[a][b] = M[i][j];
                            b++;
                        }
                        ac += (M[i][j] * -1);
                    }
                    else b++;
                    j++;
                }
                N[a][a] = ac;
                a++;
            }
            i++;
        }
        tNow = System.nanoTime();
        OptConfig.nsMatrix += tNow - tPhase;
        tPhase = tNow;

        Matrix m = new Matrix(N);
        double c[] = new double[numNodes-1];
        c = m.solve(B, true);

        /* Keep the node voltages: the solver already computes one per cell,
         * and only the source value is needed for the score. Ground sits at
         * 0; every other node maps to a reduced index that skips it. */
        double[] field = new double[board.getDimension() * board.getDimension() + 4];
        java.util.Arrays.fill(field, Double.NaN);
        for(int k = 0; k < numNodes; k++){
            if(k == removeIndex) field[nodes[k].getId()] = 0.0;
            else field[nodes[k].getId()] = c[k < removeIndex ? k : k - 1];
        }
        potentials[color] = field;

        OptConfig.nsSolve += System.nanoTime() - tPhase;
        return c[sourceIndex];
    }
    
    /** Dirty-skip check for one H-search triple: when neither route
     *  contains a new path, the pair scan below cannot fire (the AND rule
     *  requires at least one new ingredient), so it can be skipped
     *  wholesale. Exact, not heuristic: the skipped scan has no observable
     *  effect, and visit order is preserved, so results are bit-identical.
     *  @param r1 Route between the pivot and the first auxiliary cell
     *  @param r2 Route between the pivot and the second auxiliary cell
     *  @return True if the pair scan must run */
    private static boolean routePairWorthVisiting(Route r1, Route r2){
        if(!OptConfig.USE_DIRTY_SKIP) return true;
        OptConfig.triplesTotal++;
        if(r1.hasAnyNew() || r2.hasAnyNew()) return true;
        OptConfig.triplesSkipped++;
        return false;
    }

    /** Recursive function that applies the OR rule on a route created by applying
     *  the AND rule. The length of the routes passed to it is limited by K
     *  to prevent the calls from exploding. */
    private boolean applyOrRule(Connections c, Cell g1, Cell g2, Route sc, Path u, Path i){
        if(sc.getLength() > K) return false;
        
        Iterator <Path>isc = sc.getIterator();
        Path sc1 = null;
        Path u1 = null;
        Path i1 = null;
        Route route = null;

        while(isc.hasNext()){
            sc1 = isc.next();
            u1 = u.union(sc1);
            if(i.hasEmptyIntersection(sc1)){ 
                if(c.getRoute(g1, g2) == null){
                    c.newConnection(g1, g2);
                    newsConnections = true;

                }
                route = c.getRoute(g1,g2);
                route.add(u1);
                
                if((g1 instanceof Border) && (g2 instanceof Border)){
                    if((((((Border)g1).getName() == 'N') && (((Border)g2).getName() == 'S')) ||
                            ((((Border)g1).getName() == 'S') && (((Border)g2).getName() == 'N'))) ||
                            (((((Border)g1).getName() == 'E') && (((Border)g2).getName() == 'W')) ||
                            ((((Border)g1).getName() == 'W') && (((Border)g2).getName() == 'E'))))
                        return true;
                }
            }
            else{
                i1 = i.intersection(sc1);
                Route rsc = sc.cloneWithoutPath(sc1);
                if(applyOrRule(c, g1, g2, rsc, u1, i1) == true) return true;
            }
        }
        return false;
    }
   
    /** Traverses the board and shows the pairs of squares and borders between which there is
     *  an established connection.
     *  @param  displayMode Allows configuring the list: 1 = only CV, 2 = only SCV, 3 = only connections.
     *  @return Returns the average connections per endpoint (float) */
   public float displayConnectionsEx(int color, int displayMode){
        Connections ob = null;
        Iterator it1 = G[color].iterator();
        float total = 0, tmp = 0;
        int count = 0;
        Route r = null;

        switch(displayMode){
            case 1:
                ob = C[color];
                break;
            case 2:
                ob = SC[color];
                break;
        }
                
        while(it1.hasNext()){
            Iterator it2 = G[color].iterator();
            Cell c1 = (Cell) it1.next();
            while(it2.hasNext()){
                Cell c2 = (Cell) it2.next();
                if(c2 != c1){ 
                    if(ob.hasConnection(c1, c2)){
                        r = ob.getRoute(c1, c2);
                        tmp = r.getLength();
                        total = total + tmp;
                        count++;
                        if(r != null) System.out.println("(" +  c1 + ", " + r + " , " + c2 + ") - " + (int)tmp);
                    }
                }
            }
        }
        return ((float) (total / count));
    }
    
    /** Traverses the board and shows the pairs of squares and borders between which there is
     *  an established connection.
     *  @param displayMode Allows configuring the list: 1 = only CV, 2 = only SCV, 3 = only connections. */
    public void displayMinimalConnections(int color, int displayMode){
        Connections ob = null;
        Iterator it1 = G[color].iterator();
        int count = 0;
        Route r = null;

        switch(displayMode){
            case 1:
                ob = C[color];
                break;
            case 2:
                ob = SC[color];
                break;
        }  
                            
        while(it1.hasNext()){
            Iterator it2 = G[color].iterator();
            Cell c1 = (Cell) it1.next();
            while(it2.hasNext()){
                Cell c2 = (Cell) it2.next();
                if(c2 != c1){
                    if(ob.hasConnection(c1, c2)){
                        r = ob.getRoute(c1, c2);
                        count++;
                        if(r != null) System.out.println("(" +  c1 + ", " + r.getMinimumPath() + " , " + c2 + ")");
                    }
                }
            }
        }
    }
    
    /** Returns the node voltages from the last evaluation, indexed by cell
     *  id; NaN where the cell took no part in that player's circuit.
     *  @param color Player colour
     *  @return Voltage per cell id, or null if not computed */
    public double[] getPotentials(int color){
        return potentials[color];
    }

    /** Returns the virtual connections discovered for a player by the
     *  last evaluation, for display purposes.
     *  @param color Player colour
     *  @return The discovered connections, or null if none were computed */
    public Connections discovered(int color){
        return C[color];
    }

    /** Returns the board associated with the simulation
     *  @return The simulation board */
    public Board getBoard(){
        return board;
    }
    
    /** Takes the paths marked as old and renews them
     *  to prepare them for the next iteration */
    private void restoreExpiredPaths(){
        Iterator renewIt = renew.iterator();
        while(renewIt.hasNext()){
            ((Path)renewIt.next()).changeNew(true);
        }
    }
}

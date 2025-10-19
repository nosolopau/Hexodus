/*
 * Hexodus >> Simulation.java
 *
 * Created on March 29, 2007 at 15:50
 *
 * Copyright (C) 2006 - 2008 Pablo Torrecilla. GNU General Public License
 */

package heuristics;
import java.util.*;

class NonexistentSquare extends Exception{};

/**
 * Representa la simulacin de una jugada y permite deshacer los cambios de la
 * misma. Se utiliza para calcular el valor numrico de acuerdo con la heurstica correspondiente
 * for the simulated move.
 *
 * @author Pau
 * @version 10.2
 */

public class Simulation {
    private int K;   // Tamao mximo del camino en las llamadas recursivas OR (entre 5 y 6)
    private HashMap eliminatedNeighbors, eliminatedNeighbors2;
    private ArrayList <Cell> affectedNeighbors ;
    private ArrayList renew;
    private ArrayList <Cell> G[];         // List of G squares
    private Connections C[];        // List of virtual connections for each player
    private Connections SC[];       // List of virtual semi-connections for each player
    private Connections connections;
    private boolean NuevasConnections;   // Almacena si se crearon C o SC en la iteracin anterior
    private Square target;
    private Board board;
    
    /** Creates the objects common to all simulations. This constructor is
     *  para uso interno, y por tanto es de mbito privado */
    private Simulation(){
        K = 5;
        
        /* Creates two instances (one for black and one for white) of the objects that
         * are duplicated for the two players */
        G = new ArrayList [2];     // List with the 'usable' squares of the graph
        C = new Connections [2];    // List with the virtual connections discovered
        SC = new Connections [2];   // List with the virtual semi-connections discovered

    }
    
    /** Crea una simulacin base con el tablero vaco para la dimensin que
     *  is received as argument.
     *  @param dimension Dimensin del tablero de juego */
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
     *  @param base Simulacin padre
     *  @param cel Target Square
     *  @param color Color del jugador responsable de la simulacin */
    public Simulation(Simulation base, Square cel, int color) {
        this(base, cel.getRow(), cel.getColumn(), color);
    }
    
    /** Creates a new instance of Simulation with a target cell
     *  identified by its row and column
     *  @param base Simulacin padre
     *  @param row Row of the target square
     *  @param column Column of the target square
     *  @param color Color del jugador responsable de la simulacin */
    public Simulation(Simulation base, int row, int column, int color) {
        this();
        
        board = base.getBoard();
        connections = board.getConnections();
        int dimension = board.getDimension();
        
        for(int i = 0; i <= 1; i++){
            G[i] = new ArrayList <Cell>();
            C[i] = new Connections(dimension);
            SC[i] = new Connections(dimension);
        }

        target = board.get(row, column);
        target.occupy(color);

        ArrayList add = new ArrayList();    // List of squares to be added as neighbors
        Cell vec = null;                       // Used to iterate over the neighbors of obj
        
        affectedNeighbors = new ArrayList<Cell>();
        eliminatedNeighbors = new HashMap();
        eliminatedNeighbors2 = new HashMap();
        
        // Iterate over the neighbors of the cell that was just occupied        
        Iterator <Cell> itv = target.getNeighborList().iterator();
        while(itv.hasNext()){
            vec = itv.next();
            /* Si una vecina es del mismo color que la recin insertada hay que
             * remove the old cell (with its possible connections) and transfer
             * its neighbors to the new one. */
            ArrayList <Cell> remove2 = new ArrayList<Cell>();
            
            if((vec.getColor() == color) && !(vec instanceof Border)){
                ArrayList <Cell>Elim = new ArrayList<Cell>();
                ArrayList <Cell>Elim2 = new ArrayList<Cell>();
                
                affectedNeighbors.add(vec);
                
                ArrayList <Cell> remove = new ArrayList<Cell>();
                
                Iterator itv2 = vec.getNeighborList().iterator();
                while(itv2.hasNext()){
                    Cell vec2 = (Cell)itv2.next();

                    if(vec2 != target){
                        if(vec2.isNeighbor(target)){
                            Elim2.add(vec2);
                        }
                        else{
                            Elim.add(vec2);     
                            add.add(vec2);  // No puede agregarse directamente (ver abajo)
                        }
                        connections.removeConnection(vec2, vec);  // Estas operaciones pueden aadirse a Board tal cual y llamarlas desde aqu
                        remove.add(vec2);
                    }
                }
                
                Iterator bor = remove.iterator();
                while(bor.hasNext()){
                    Cell prov = (Cell) bor.next();
                    vec.removeNeighbor(prov);
                    prov.removeNeighbor(vec);
                }
                        
                addRemoved(eliminatedNeighbors, vec, Elim);
                addRemoved(eliminatedNeighbors2, vec, Elim2);
                remove2.add(vec);
            }
            Iterator bor = remove2.iterator();
            while(bor.hasNext()){
                Cell prov = (Cell) bor.next();
                vec.removeNeighbor(prov);
                prov.removeNeighbor(vec);
            }
        }
        
        /* The neighbors from the intermediate list are added. This is done in two
         * stages to avoid a concurrency problem. */
        Iterator ita = add.iterator();
        while(ita.hasNext()){
            Cell newNeighbor = (Cell)ita.next();
            if(target != newNeighbor){    // Una celda no puede ser vecina de s misma
                
                target.addNeighbor(newNeighbor);
                newNeighbor.addNeighbor(target);
                
                connections.insertDirectPath(newNeighbor, target); // before above
            }
        }
    }
    
    /** Devuelve la casilla objetivo que estudia la simulacin
     *  @return Target Square de la simulacin */
    public Square getTargetCell(){
        return target;
    }
    
    /** Calcula el valor de la simulacin
     *  @return A double with the value of dividing the white resistance by
     *  the black resistance */
    public double calculateValue() {
        double r0 = 0, r1 = 0;
        
        try {
            r0 = calculateResistance(0);
            RestauraPaths();
            if(r0 == 0) return 0;

            r1 = calculateResistance(1);
            RestauraPaths();
            if(r1 == 0) return Double.POSITIVE_INFINITY;
        } catch (NonexistentSquare ex) {
            ex.printStackTrace();
        }
        
        return r0/r1;
    }
    
    /** Restaura los datos de la simulacin actual basndose en el historial de
     *  recorded changes */
    public void restore(){
        target.occupy(-1);
        boolean conectar = false;
        ArrayList remove = new ArrayList();
        
        Iterator i = affectedNeighbors.iterator();
        while(i.hasNext()){
            conectar = true;
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
            
            Iterator bor = remove.iterator();
            while(bor.hasNext()){
                Cell prov = (Cell) bor.next();
                target.removeNeighbor(prov);
                prov.removeNeighbor(target);
            }
        }
    }
    
    /** Devuelve las casillas libres del tablero asociado a la simulacin
     *  @return ArrayList of free squares */
    public ArrayList<Square> getFreeCells(){
        return board.getCellsLibres();
    }
    
    /** Adds a removed cell to the list to undo changes
     *  @param m El HashMap al que hay que agregar la celda en cuestin
     *  @param c Cell to be added
     *  @param vecinas List of neighbors of the cell to be added */
    private void addRemoved(HashMap m, Cell c, ArrayList vecinas){
        m.put(c, vecinas);
    }
    
    /** Calcula las combinaciones vlidas entre los elementos del conjunto G, 
     *  denominados g, g1 y g2. Mantiene fijo g y vara sobre l g1 y g2: */
    private double calculateResistance(int color) throws NonexistentSquare{
        int profundidad = 100;
        
        /* Creates a list to contain the paths that should expire in the
         * primera iteracin de la bsqueda */
        ArrayList expireOriginal = board.getExpireList();
        
        // A series of structures to implement path expiration:
        ArrayList Caducar = new ArrayList(); // Paths que caducarn en una iteracin
        renew = new ArrayList(); // Paths that have aged and must be restored upon completion
        ArrayList SiguientesCaducar = new ArrayList();  // Squares que caducarn la prxima iteracin
        
        Caducar = (ArrayList) expireOriginal.clone();  // Copiar en Caducar los caminos que caducarn en la segunda iteracin

        G[color] = board.generateG(color);
        int NumeroNodos = G[color].size();
        Cell[] ArrayG = (Cell []) G[color].toArray(new Cell [NumeroNodos]);
        
        Connections SubC = C[color];
        Connections SubSC = SC[color];
        
        SubC = connections.clone();

        // Evita que los ndices tengan que volver a definirse en cada iteracin
        int g1;
        int g2;
        int g;
        
        // Temporary references to cells
        Cell cg = null;
        Cell cg1 = null;
        Cell cg2 = null;
        
        // Variables para la bsqueda de conexiones
        NuevasConnections = true;
        int iteraciones = 0;

        while(NuevasConnections && (iteraciones < profundidad)){
            NuevasConnections = false;
            iteraciones++;
            for(g = 0; g < NumeroNodos; g++){
                cg = ArrayG[g];
                for(g1 = 0; g1 < NumeroNodos; g1++){
                    if(g1 != g){
                        cg1 = ArrayG[g1];
                        if((!(cg.getColor() == color)) || (cg1.isEmpty())){
                            for(g2 = g1 + 1; g2 < NumeroNodos; g2++){
                                if(g2 != g){
                                    cg2 = ArrayG[g2];
                                    if((!(cg.getColor() == color)) || (cg2.isEmpty())){
                                        Iterator ic1 = null, ic2 = null;

                                        /* Gets an iterator for the set of paths between two squares. The paths
                                         * slo estn representados en un sentido, por lo que se comprueba tambin la combinacin
                                         * inverse of squares is also checked.
                                         */
                                        Route r1 = SubC.getRoute(cg, cg1);
                                        if(r1 == null) r1 = SubC.getRoute(cg1, cg);
                                        
                                        Route r2 = SubC.getRoute(cg, cg2);
                                        if(r2 == null) r2 = SubC.getRoute(cg2, cg);
                                        
                                        if((r1 != null) && (r2 != null)){
                                            ic1 = r1.getIterator();
                                            ic2 = r2.getIterator();

                                            /* The following block ensures that all possible combinations
                                             * of paths from g - g1 and g - g2 are traversed. The nested while ensures that
                                             * all cases are studied. */                                     
                                            while(ic1.hasNext()){
                                                Path c1 = (Path) ic1.next();
                                                while(ic2.hasNext()){
                                                    Path c2 = (Path) ic2.next();
                                                    if(c1.hasEmptyIntersection(c2) && !c2.contains(cg1) && !c1.contains(cg2)){
                                                        if(c1.isNew() || c2.isNew()){
                                                            SiguientesCaducar.add(c1);
                                                            SiguientesCaducar.add(c2);
                                                            
                                                            // Apply the AND rule by studying the color of the target square:
                                                            if(cg.getColor() == color){
                                                                if(SubC.getRoute(cg1, cg2) == null){ // If there is no route
                                                                    SubC.newConnection(cg1, cg2);
                                                                    NuevasConnections = true;
                                                                }
                                                                Route r = SubC.getRoute(cg1, cg2);
                                                                Path c = c1.union(c2);
                                                                r.add(c);

                                                                if((cg1 instanceof Border) && (cg2 instanceof Border)){
                                                                    if((((((Border)cg1).getName() == 'N') && (((Border)cg2).getName() == 'S')) ||
                                                                            ((((Border)cg1).getName() == 'S') && (((Border)cg2).getName() == 'N'))) ||
                                                                            (((((Border)cg1).getName() == 'E') && (((Border)cg2).getName() == 'O')) ||
                                                                            ((((Border)cg1).getName() == 'O') && (((Border)cg2).getName() == 'E'))))
                                                                        return 0;
                                                                }
                                                            }
                                                            else{
                                                                // A SCV should not be inserted if a VC already exists
                                                                if(!SubC.hasConnectionEx(cg1, cg2)){
                                                                    Path sc = c1.union(c2, cg);
                                                                    
                                                                    if(SubSC.getRoute(cg1, cg2) == null){
                                                                        SubSC.newConnection(cg1, cg2);
                                                                        NuevasConnections = true;
                                                                    }
                                                                    Route r = SubSC.getRoute(cg1, cg2);

                                                                    if(r.add(sc)){
                                                                        Route rsc = r.cloneWithoutPath(sc);
                                                                        if(AplicarReglaOR(SubC, cg1, cg2, rsc, sc, sc)) return 0;
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
            
            /* Marca como antiguos los caminos utilizados en la anterior iteracin
             * and prepares those used in the current one to expire in the next */
            Iterator cad = Caducar.iterator();
            while(cad.hasNext()){
                Path mod = (Path)cad.next();
                mod.changeNew(false);
                renew.add(mod);
            }
            Caducar.clear();
            Caducar = (ArrayList) SiguientesCaducar.clone();
            SiguientesCaducar.clear();
        }
        
        ArrayList Visitados = new ArrayList();      // Creates a list for visited nodes

        char superior;
        char tierra;
        
        if(color == 1){
            superior = 'N';
            tierra = 'S';
        }
        else{
            superior = 'E';
            tierra = 'O'; 
        }

        double M[][];   // A temporary conductance matrix
        double N[][];   // Final conductance matrix
        double B[];     // La matriz column de trminos independientes de la ecuacin
        int conectado = -1;

        M = new double [NumeroNodos][NumeroNodos];
        N = new double [NumeroNodos-1][NumeroNodos-1];
        B = new double [NumeroNodos-1];
        
        int Intensidad = 1; // Intensity transmitted by the current source
        int n = 0;          // Index in G of current element
        int quitar = -1;    // Index of the node considered ground    
                    
        /* Recorre los elementos de G agregando la conductancia a la matriz segn
         * exista o no conexin. Anota el ndice del nodo conectado a tierra para eliminarlo
         * and writes the intensity in the node connected to the source. */
        for(int i=0; i<NumeroNodos; i++){
            Cell c1 = ArrayG[i];

            if(c1 instanceof Border){
                if(((Border)c1).getName() == tierra)
                    quitar = n;
                else if(((Border)c1).getName() == superior){
                    B[n] = Intensidad; // If it is the node connected to the source, mark a 1 in the intensity matrix   
                    conectado = n;
                }
            }

            /* Marks the current node as visited to not return. This can
             * hacer porque la matriz es simtrica: */
            Visitados.add(c1);
            
            Iterator it2 = G[color].iterator(); // Gets an iterator of the nodes
            int m = 0;  // êndice en G del elemento actual
            while(it2.hasNext()){
                Cell c2 = (Cell) it2.next();
                if(!Visitados.contains(c2)){
                    /* Si los elementos estn conectados, inserta en la matriz 
                     * la conductancia entre ellos. Si no lo estn, inserta 0 */
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
        while(i < NumeroNodos){
            ac = 0;
            int j = 0;
            int b = 0;
            if(i != quitar){
                while(j < NumeroNodos){
                    if(j != i){
                        if(j != quitar){
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
        Matrix m = new Matrix(N);
        double c[] = new double[NumeroNodos-1];
        c = m.solve(B, true);
        
        return c[conectado];
    }
    
    /** Funcin recursiva que aplica la regla OR sobre una ruta creada aplicando
     *  la regla AND. La longitud de las rutas que se le pasan est limitada por K
     *  to prevent the calls from exploding. */
    private boolean AplicarReglaOR(Connections c, Cell g1, Cell g2, Route sc, Path u, Path i){
        if(sc.getLength() > K) return false;
        
        Iterator <Path>isc = sc.getIterator();
        Path sc1 = null;
        Path u1 = null;
        Path i1 = null;
        Route ruta = null;

        while(isc.hasNext()){
            sc1 = isc.next();
            u1 = u.union(sc1);
            if(i.hasEmptyIntersection(sc1)){ 
                if(c.getRoute(g1, g2) == null){
                    c.newConnection(g1, g2);
                    NuevasConnections = true;

                }
                ruta = c.getRoute(g1,g2);
                ruta.add(u1);
                
                if((g1 instanceof Border) && (g2 instanceof Border)){
                    if((((((Border)g1).getName() == 'N') && (((Border)g2).getName() == 'S')) ||
                            ((((Border)g1).getName() == 'S') && (((Border)g2).getName() == 'N'))) ||
                            (((((Border)g1).getName() == 'E') && (((Border)g2).getName() == 'O')) ||
                            ((((Border)g1).getName() == 'O') && (((Border)g2).getName() == 'E'))))
                        return true;
                }
            }
            else{
                i1 = i.intersection(sc1);
                Route rsc = sc.cloneWithoutPath(sc1);
                if(AplicarReglaOR(c, g1, g2, rsc, u1, i1) == true) return true;
            }
        }
        return false;
    }
   
    /** Traverses the board and shows the pairs of squares and borders between which there is 
     *  una conexin establecida.
     *  @param  mostrar Permite configurar la lista: 1 = slo CV, 2 = slo SCV, 3 = slo connections.
     *  @return Returns the average connections per endpoint (float) */ 
   public float MostrarConnectionsEx(int color, int mostrar){
        Connections ob = null;
        Iterator it1 = G[color].iterator();
        float total = 0, tmp = 0;
        int numero = 0;
        Route r = null;
        
        switch(mostrar){
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
                        numero++;
                        if(r != null) System.out.println("(" +  c1 + ", " + r + " , " + c2 + ") - " + (int)tmp);
                    }
                }
            }
        }
        return ((float) (total / numero));
    }
    
    /** Traverses the board and shows the pairs of squares and borders between which there is 
     *  una conexin establecida.
     *  @param mostrar Permite configurar la lista: 1 = slo CV, 2 = slo SCV, 3 = slo connections. */ 
    public void MostrarConnectionsMinimas(int color, int mostrar){
        Connections ob = null;
        Iterator it1 = G[color].iterator();
        int numero = 0;
        Route r = null;
        
        switch(mostrar){
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
                        numero++;
                        if(r != null) System.out.println("(" +  c1 + ", " + r.getMinimumPath() + " , " + c2 + ")");
                    }
                }
            }
        }
    }
    
    /** Devuelve el tablero asociado a la simulacin
     *  @return El tablero de la simulacin */
    public Board getBoard(){
        return board;
    }
    
    /** Takes the paths marked as old and renews them
     *  para prepararlos para la siguiente iteracin */
    private void RestauraPaths(){
        Iterator ren = renew.iterator();
        while(ren.hasNext()){
            ((Path)ren.next()).changeNew(true);
        }
    }
}

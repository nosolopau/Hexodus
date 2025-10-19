/*
 * Hexodus >> Simulation.java
 *
 * Creado el 29 de marzo de 2007 a las 15:50
 *
 * Copyright (C) 2006 - 2008 Pablo Torrecilla. GNU General Public License
 */

package heuristics;
import java.util.*;

class NonexistentSquare extends Exception{};

/**
 * Representa la simulacin de una jugada y permite deshacer los cambios de la
 * misma. Se utiliza para calcular el valor numrico de acuerdo con la heurstica correspondiente
 * a la jugada simulada.
 *
 * @author Pau
 * @version 10.2
 */

public class Simulation {
    private int K;   // Tamao mximo del camino en las llamadas recursivas OR (entre 5 y 6)
    private HashMap eliminatedNeighbors, eliminatedNeighbors2;
    private ArrayList <Cell> affectedNeighbors ;
    private ArrayList renew;
    private ArrayList <Cell> G[];         // Lista de casillas G
    private Connections C[];        // Lista de conexiones virtuales para cada jugador
    private Connections SC[];       // Lista de semiconexiones virtuales para cada jugador
    private Connections connections;
    private boolean NuevasConnections;   // Almacena si se crearon C o SC en la iteracin anterior
    private Square target;
    private Board board;
    
    /** Crea los objetos comunes a todas las simulaciones. Este constructor es
     *  para uso interno, y por tanto es de mbito privado */
    private Simulation(){
        K = 5;
        
        /* Crea dos instancias (una para negras y otra para blancas) de los objetos que
         * se encuentran duplicados para los dos jugadores */
        G = new ArrayList [2];     // Lista con las casillas 'utilizables' del grafo
        C = new Connections [2];    // Lista con las conexiones virtuales descubiertas
        SC = new Connections [2];   // Lista con las semiconexiones virtuales descubiertas

    }
    
    /** Crea una simulacin base con el tablero vaco para la dimensin que
     *  se reciba como argumento.
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
    
    /** Crea una nueva instancia de Simulation con una celda objetivo
     *  que se le pase como argumento
     *  @param base Simulacin padre
     *  @param cel Square objetivo
     *  @param color Color del jugador responsable de la simulacin */
    public Simulation(Simulation base, Square cel, int color) {
        this(base, cel.getRow(), cel.getColumn(), color);
    }
    
    /** Crea una nueva instancia de Simulation con una celda objetivo
     *  identificada por su row y column
     *  @param base Simulacin padre
     *  @param row Fila de la casilla objetivo
     *  @param column Columna de la casilla objetivo
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

        ArrayList add = new ArrayList();    // Lista de casillas que hay que agregar como vecinas
        Cell vec = null;                       // Se usa para recorrer las vecinas de obj
        
        affectedNeighbors = new ArrayList<Cell>();
        eliminatedNeighbors = new HashMap();
        eliminatedNeighbors2 = new HashMap();
        
        // Iterar sobre las vecinas de la celda que acaba de ocuparse        
        Iterator <Cell> itv = target.getNeighborList().iterator();
        while(itv.hasNext()){
            vec = itv.next();
            /* Si una vecina es del mismo color que la recin insertada hay que
             * eliminar la celda antigua (con sus posibles conexiones) y transferir
             * sus vecinas a la nueva. */
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
        
        /* Se agregan las vecinas de la lista intermedia. Esto se hace en dos
         * tiempos para evitar un problema de concurrencia. */
        Iterator ita = add.iterator();
        while(ita.hasNext()){
            Cell newNeighbor = (Cell)ita.next();
            if(target != newNeighbor){    // Una celda no puede ser vecina de s misma
                
                target.addNeighbor(newNeighbor);
                newNeighbor.addNeighbor(target);
                
                connections.insertDirectPath(newNeighbor, target); // antes arriba
            }
        }
    }
    
    /** Devuelve la casilla objetivo que estudia la simulacin
     *  @return Square objetivo de la simulacin */
    public Square getTargetCell(){
        return target;
    }
    
    /** Calcula el valor de la simulacin
     *  @return Un double con el valor de dividir la resistencia de las blancas entre
     *  la de las negras */
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
     *  cambios registrados */
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
     *  @return ArrayList de casillas libres */
    public ArrayList<Square> getFreeCells(){
        return board.getCellsLibres();
    }
    
    /** Agrega una celda eliminada a la lista para deshacer los cambios
     *  @param m El HashMap al que hay que agregar la celda en cuestin
     *  @param c Cell que se agregar
     *  @param vecinas Lista de vecinas de la celda que se agregar */
    private void addRemoved(HashMap m, Cell c, ArrayList vecinas){
        m.put(c, vecinas);
    }
    
    /** Calcula las combinaciones vlidas entre los elementos del conjunto G, 
     *  denominados g, g1 y g2. Mantiene fijo g y vara sobre l g1 y g2: */
    private double calculateResistance(int color) throws NonexistentSquare{
        int profundidad = 100;
        
        /* Crea una lista para contener los caminos que deben caducar en la
         * primera iteracin de la bsqueda */
        ArrayList expireOriginal = board.getExpireList();
        
        // Una serie de estructuras para implantar la caducidad de caminos:
        ArrayList Caducar = new ArrayList(); // Paths que caducarn en una iteracin
        renew = new ArrayList(); // Paths que han envejecido y que deben ser restaurados al terminar
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
        
        // Referencias temporales a celdas
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

                                        /* Obtiene un iterador para el conjunto de caminos entre dos casillas. Los caminos
                                         * slo estn representados en un sentido, por lo que se comprueba tambin la combinacin
                                         * inversa de casillas.
                                         */
                                        Route r1 = SubC.getRoute(cg, cg1);
                                        if(r1 == null) r1 = SubC.getRoute(cg1, cg);
                                        
                                        Route r2 = SubC.getRoute(cg, cg2);
                                        if(r2 == null) r2 = SubC.getRoute(cg2, cg);
                                        
                                        if((r1 != null) && (r2 != null)){
                                            ic1 = r1.getIterator();
                                            ic2 = r2.getIterator();

                                            /* El siguiente bloque asegura que se recorren todas las posibles combinaciones
                                             * de caminos de g - g1 y g - g2. El while anidado asegura que se estudian
                                             * todos los casos. */                                     
                                            while(ic1.hasNext()){
                                                Path c1 = (Path) ic1.next();
                                                while(ic2.hasNext()){
                                                    Path c2 = (Path) ic2.next();
                                                    if(c1.hasEmptyIntersection(c2) && !c2.contains(cg1) && !c1.contains(cg2)){
                                                        if(c1.isNew() || c2.isNew()){
                                                            SiguientesCaducar.add(c1);
                                                            SiguientesCaducar.add(c2);
                                                            
                                                            // Aplicar la regla AND estudiando el color de la casilla objetivo:
                                                            if(cg.getColor() == color){
                                                                if(SubC.getRoute(cg1, cg2) == null){ // Si no hay ruta
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
                                                                // No debe insertarse una SCV si ya existe una CV
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
             * y prepara los utilizados en la actual para caducar en la siguiente */
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
        
        ArrayList Visitados = new ArrayList();      // Crea una lista para los nodos visitados

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

        double M[][];   // Una matriz de conductancias temporal
        double N[][];   // Matrix de conductancias definitiva
        double B[];     // La matriz column de trminos independientes de la ecuacin
        int conectado = -1;

        M = new double [NumeroNodos][NumeroNodos];
        N = new double [NumeroNodos-1][NumeroNodos-1];
        B = new double [NumeroNodos-1];
        
        int Intensidad = 1; // Intensidad transmitida por la fuente de corriente
        int n = 0;          // êndice en G de elemento actual
        int quitar = -1;    // êndice del nodo que se considera tierra    
                    
        /* Recorre los elementos de G agregando la conductancia a la matriz segn
         * exista o no conexin. Anota el ndice del nodo conectado a tierra para eliminarlo
         * y escribe la intensidad en el nodo conectado a la fuente. */
        for(int i=0; i<NumeroNodos; i++){
            Cell c1 = ArrayG[i];

            if(c1 instanceof Border){
                if(((Border)c1).getName() == tierra)
                    quitar = n;
                else if(((Border)c1).getName() == superior){
                    B[n] = Intensidad; // Si es el nodo conectado a la fuente, marcar un 1 en la matriz de intensidades   
                    conectado = n;
                }
            }

            /* Marca el nodo actual como visitado para no volver. Esto se puede
             * hacer porque la matriz es simtrica: */
            Visitados.add(c1);
            
            Iterator it2 = G[color].iterator(); // Obtiene un iterador de los nodos
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
        
        /* Recorre la matriz M y copia sus elementos en N, suprimiendo
         * la row y la column del nodo tierra y escribiendo los datos adecuados
         * en la diagonal de la matriz. */
        double ac; // Acumulador de la suma de las conductancias de cada row
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
     *  para impedir que se disparen las llamadas. */
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
   
    /** Recorre el tablero y muestra las parejas de casillas y bordes entre los que hay 
     *  una conexin establecida.
     *  @param  mostrar Permite configurar la lista: 1 = slo CV, 2 = slo SCV, 3 = slo connections.
     *  @return Devuelve la media de conexiones por extremo (float) */ 
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
    
    /** Recorre el tablero y muestra las parejas de casillas y bordes entre los que hay 
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
    
    /** Toma los caminos marcados como viejos y los renueva
     *  para prepararlos para la siguiente iteracin */
    private void RestauraPaths(){
        Iterator ren = renew.iterator();
        while(ren.hasNext()){
            ((Path)ren.next()).changeNew(true);
        }
    }
}

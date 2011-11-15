/*
 * Hexodus >> Simulacion.java
 *
 * Creado el 29 de marzo de 2007 a las 15:50
 *
 * Copyright (C) 2006 - 2008 Pablo Torrecilla. GNU General Public License
 */

package heuristica;
import java.util.*;

class CasillaInexistente extends Exception{};

/**
 * Representa la simulaci�n de una jugada y permite deshacer los cambios de la
 * misma. Se utiliza para calcular el valor num�rico de acuerdo con la heur�stica correspondiente
 * a la jugada simulada.
 *
 * @author Pau
 * @version 10.2
 */

public class Simulacion {
    private int K;   // Tama�o m�ximo del camino en las llamadas recursivas OR (entre 5 y 6)
    private HashMap VecinasEliminadas, VecinasEliminadas2;
    private ArrayList <Celda> VecinasAfectadas ;
    private ArrayList Renovar;
    private ArrayList <Celda> G[];         // Lista de casillas G
    private Conexiones C[];        // Lista de conexiones virtuales para cada jugador
    private Conexiones SC[];       // Lista de semiconexiones virtuales para cada jugador
    private Conexiones T;
    private boolean NuevasConexiones;   // Almacena si se crearon C o SC en la iteraci�n anterior
    private Casilla Objetivo;
    private Tablero Tab;
    
    /** Crea los objetos comunes a todas las simulaciones. Este constructor es
     *  para uso interno, y por tanto es de �mbito privado */
    private Simulacion(){
        K = 5;
        
        /* Crea dos instancias (una para negras y otra para blancas) de los objetos que
         * se encuentran duplicados para los dos jugadores */
        G = new ArrayList [2];     // Lista con las casillas 'utilizables' del grafo
        C = new Conexiones [2];    // Lista con las conexiones virtuales descubiertas
        SC = new Conexiones [2];   // Lista con las semiconexiones virtuales descubiertas

    }
    
    /** Crea una simulaci�n base con el tablero vac�o para la dimensi�n que
     *  se reciba como argumento.
     *  @param dimension Dimensi�n del tablero de juego */
    public Simulacion(int dimension) {
        this();
        
        for(int i = 0; i <= 1; i++){
            C[i] = new Conexiones(dimension);
            SC[i] = new Conexiones(dimension);
        }
        Tab = new Tablero(dimension);
        T = Tab.ObtenerT();
    }
    
    /** Crea una nueva instancia de Simulacion con una celda objetivo
     *  que se le pase como argumento
     *  @param base Simulaci�n padre
     *  @param cel Casilla objetivo
     *  @param color Color del jugador responsable de la simulaci�n */
    public Simulacion(Simulacion base, Casilla cel, int color) {
        this(base, cel.ObtenerFila(), cel.ObtenerColumna(), color);
    }
    
    /** Crea una nueva instancia de Simulacion con una celda objetivo
     *  identificada por su fila y columna
     *  @param base Simulaci�n padre
     *  @param fila Fila de la casilla objetivo
     *  @param columna Columna de la casilla objetivo
     *  @param color Color del jugador responsable de la simulaci�n */
    public Simulacion(Simulacion base, int fila, int columna, int color) {
        this();
        
        Tab = base.ObtenerTablero();
        T = Tab.ObtenerT();
        int dimension = Tab.ObtenerDimension();
        
        for(int i = 0; i <= 1; i++){
            G[i] = new ArrayList <Celda>();
            C[i] = new Conexiones(dimension);
            SC[i] = new Conexiones(dimension);
        }

        Objetivo = Tab.Obtener(fila, columna);
        Objetivo.Ocupar(color);

        ArrayList Agregar = new ArrayList();    // Lista de casillas que hay que agregar como vecinas
        Celda vec = null;                       // Se usa para recorrer las vecinas de obj
        
        VecinasAfectadas = new ArrayList<Celda>();
        VecinasEliminadas = new HashMap();
        VecinasEliminadas2 = new HashMap();
        
        // Iterar sobre las vecinas de la celda que acaba de ocuparse        
        Iterator <Celda> itv = Objetivo.ObtenerListaVecinas().iterator();
        while(itv.hasNext()){
            vec = itv.next();
            /* Si una vecina es del mismo color que la reci�n insertada hay que
             * eliminar la celda antigua (con sus posibles conexiones) y transferir
             * sus vecinas a la nueva. */
            ArrayList <Celda> Eliminar2 = new ArrayList<Celda>();
            
            if((vec.ObtenerColor() == color) && !(vec instanceof Borde)){
                ArrayList <Celda>Elim = new ArrayList<Celda>();
                ArrayList <Celda>Elim2 = new ArrayList<Celda>();
                
                VecinasAfectadas.add(vec);
                
                ArrayList <Celda> Eliminar = new ArrayList<Celda>();
                
                Iterator itv2 = vec.ObtenerListaVecinas().iterator();
                while(itv2.hasNext()){
                    Celda vec2 = (Celda)itv2.next();

                    if(vec2 != Objetivo){
                        if(vec2.EsVecina(Objetivo)){
                            Elim2.add(vec2);
                        }
                        else{
                            Elim.add(vec2);     
                            Agregar.add(vec2);  // No puede agregarse directamente (ver abajo)
                        }
                        T.EliminarConexion(vec2, vec);  // Estas operaciones pueden a�adirse a Tablero tal cual y llamarlas desde aqu�
                        Eliminar.add(vec2);
                    }
                }
                
                Iterator bor = Eliminar.iterator();
                while(bor.hasNext()){
                    Celda prov = (Celda) bor.next();
                    vec.EliminarVecina(prov);
                    prov.EliminarVecina(vec);
                }
                        
                AgregarEliminada(VecinasEliminadas, vec, Elim);
                AgregarEliminada(VecinasEliminadas2, vec, Elim2);
                Eliminar2.add(vec);
            }
            Iterator bor = Eliminar2.iterator();
            while(bor.hasNext()){
                Celda prov = (Celda) bor.next();
                vec.EliminarVecina(prov);
                prov.EliminarVecina(vec);
            }
        }
        
        /* Se agregan las vecinas de la lista intermedia. Esto se hace en dos
         * tiempos para evitar un problema de concurrencia. */
        Iterator ita = Agregar.iterator();
        while(ita.hasNext()){
            Celda nuevavec = (Celda)ita.next();
            if(Objetivo != nuevavec){    // Una celda no puede ser vecina de s� misma
                
                Objetivo.AgregarVecina(nuevavec);
                nuevavec.AgregarVecina(Objetivo);
                
                T.InsertarCaminoDirecto(nuevavec, Objetivo); // antes arriba
            }
        }
    }
    
    /** Devuelve la casilla objetivo que estudia la simulaci�n
     *  @return Casilla objetivo de la simulaci�n */
    public Casilla ObtenerCeldaObjetivo(){
        return Objetivo;
    }
    
    /** Calcula el valor de la simulaci�n
     *  @return Un double con el valor de dividir la resistencia de las blancas entre
     *  la de las negras */
    public double CalcularValor() {
        double r0 = 0, r1 = 0;
        
        try {
            r0 = CalcularResistencia(0);
            RestauraCaminos();
            if(r0 == 0) return 0;

            r1 = CalcularResistencia(1);
            RestauraCaminos();
            if(r1 == 0) return Double.POSITIVE_INFINITY;
        } catch (CasillaInexistente ex) {
            ex.printStackTrace();
        }
        
        return r0/r1;
    }
    
    /** Restaura los datos de la simulaci�n actual bas�ndose en el historial de
     *  cambios registrados */
    public void Restaurar(){
        Objetivo.Ocupar(-1);
        boolean conectar = false;
        ArrayList Eliminar = new ArrayList();
        
        Iterator i = VecinasAfectadas.iterator();
        while(i.hasNext()){
            conectar = true;
            Celda c = (Celda)i.next();
            
            T.InsertarCaminoDirecto(Objetivo, c);
            Objetivo.AgregarVecina(c);
            c.AgregarVecina(Objetivo);
            
            if(VecinasEliminadas.containsKey(c)){
                ArrayList a = (ArrayList) VecinasEliminadas.get(c);
                Iterator i2 = a.iterator();
                while(i2.hasNext()){
                    Celda c2 = (Celda)i2.next();
                    c.AgregarVecina(c2);
                    c2.AgregarVecina(c);
                    
                    T.EliminarConexion(Objetivo, c2);
                    Eliminar.add(c2);
                    
                    T.InsertarCaminoDirecto(c, c2);
                }
            }
            
            if(VecinasEliminadas2.containsKey(c)){
                ArrayList a = (ArrayList) VecinasEliminadas2.get(c);
                Iterator i2 = a.iterator();
                while(i2.hasNext()){
                    Celda c2 = (Celda)i2.next();
                    c.AgregarVecina(c2);
                    c2.AgregarVecina(c);
                    
                    T.InsertarCaminoDirecto(c, c2);
                }
            }
            
            Iterator bor = Eliminar.iterator();
            while(bor.hasNext()){
                Celda prov = (Celda) bor.next();
                Objetivo.EliminarVecina(prov);
                prov.EliminarVecina(Objetivo);
            }
        }
    }
    
    /** Devuelve las casillas libres del tablero asociado a la simulaci�n
     *  @return ArrayList de casillas libres */
    public ArrayList<Casilla> ObtenerCeldasLibres(){
        return Tab.ObtenerCeldasLibres();
    }
    
    /** Agrega una celda eliminada a la lista para deshacer los cambios
     *  @param m El HashMap al que hay que agregar la celda en cuesti�n
     *  @param c Celda que se agregar�
     *  @param vecinas Lista de vecinas de la celda que se agregar� */
    private void AgregarEliminada(HashMap m, Celda c, ArrayList vecinas){
        m.put(c, vecinas);
    }
    
    /** Calcula las combinaciones v�lidas entre los elementos del conjunto G, 
     *  denominados g, g1 y g2. Mantiene fijo g y var�a sobre �l g1 y g2: */
    private double CalcularResistencia(int color) throws CasillaInexistente{
        int profundidad = 100;
        
        /* Crea una lista para contener los caminos que deben caducar en la
         * primera iteraci�n de la b�squeda */
        ArrayList CaducarOriginal = Tab.ObtenerListaCaducar();
        
        // Una serie de estructuras para implantar la caducidad de caminos:
        ArrayList Caducar = new ArrayList(); // Caminos que caducar�n en una iteraci�n
        Renovar = new ArrayList(); // Caminos que han envejecido y que deben ser restaurados al terminar
        ArrayList SiguientesCaducar = new ArrayList();  // Casillas que caducar�n la pr�xima iteraci�n
        
        Caducar = (ArrayList) CaducarOriginal.clone();  // Copiar en Caducar los caminos que caducar�n en la segunda iteraci�n

        G[color] = Tab.GenerarG(color);
        int NumeroNodos = G[color].size();
        Celda[] ArrayG = (Celda []) G[color].toArray(new Celda [NumeroNodos]);
        
        Conexiones SubC = C[color];
        Conexiones SubSC = SC[color];
        
        SubC = T.clone();

        // Evita que los �ndices tengan que volver a definirse en cada iteraci�n
        int g1;
        int g2;
        int g;
        
        // Referencias temporales a celdas
        Celda cg = null;
        Celda cg1 = null;
        Celda cg2 = null;
        
        // Variables para la b�squeda de conexiones
        NuevasConexiones = true;
        int iteraciones = 0;

        while(NuevasConexiones && (iteraciones < profundidad)){
            NuevasConexiones = false;
            iteraciones++;
            for(g = 0; g < NumeroNodos; g++){
                cg = ArrayG[g];
                for(g1 = 0; g1 < NumeroNodos; g1++){
                    if(g1 != g){
                        cg1 = ArrayG[g1];
                        if((!(cg.ObtenerColor() == color)) || (cg1.esVacia())){
                            for(g2 = g1 + 1; g2 < NumeroNodos; g2++){
                                if(g2 != g){
                                    cg2 = ArrayG[g2];
                                    if((!(cg.ObtenerColor() == color)) || (cg2.esVacia())){
                                        Iterator ic1 = null, ic2 = null;

                                        /* Obtiene un iterador para el conjunto de caminos entre dos casillas. Los caminos
                                         * s�lo est�n representados en un sentido, por lo que se comprueba tambi�n la combinaci�n
                                         * inversa de casillas.
                                         */
                                        Ruta r1 = SubC.ObtenerRuta(cg, cg1);
                                        if(r1 == null) r1 = SubC.ObtenerRuta(cg1, cg);
                                        
                                        Ruta r2 = SubC.ObtenerRuta(cg, cg2);
                                        if(r2 == null) r2 = SubC.ObtenerRuta(cg2, cg);
                                        
                                        if((r1 != null) && (r2 != null)){
                                            ic1 = r1.ObtenerIterador();
                                            ic2 = r2.ObtenerIterador();

                                            /* El siguiente bloque asegura que se recorren todas las posibles combinaciones
                                             * de caminos de g - g1 y g - g2. El while anidado asegura que se estudian
                                             * todos los casos. */                                     
                                            while(ic1.hasNext()){
                                                Camino c1 = (Camino) ic1.next();
                                                while(ic2.hasNext()){
                                                    Camino c2 = (Camino) ic2.next();
                                                    if(c1.InterseccionVacia(c2) && !c2.Contiene(cg1) && !c1.Contiene(cg2)){
                                                        if(c1.EsNuevo() || c2.EsNuevo()){
                                                            SiguientesCaducar.add(c1);
                                                            SiguientesCaducar.add(c2);
                                                            
                                                            // Aplicar la regla AND estudiando el color de la casilla objetivo:
                                                            if(cg.ObtenerColor() == color){
                                                                if(SubC.ObtenerRuta(cg1, cg2) == null){ // Si no hay ruta
                                                                    SubC.NuevaConexion(cg1, cg2);
                                                                    NuevasConexiones = true;
                                                                }
                                                                Ruta r = SubC.ObtenerRuta(cg1, cg2);
                                                                Camino c = c1.Union(c2);
                                                                r.Agregar(c);

                                                                if((cg1 instanceof Borde) && (cg2 instanceof Borde)){
                                                                    if((((((Borde)cg1).ObtenerNombre() == 'N') && (((Borde)cg2).ObtenerNombre() == 'S')) ||
                                                                            ((((Borde)cg1).ObtenerNombre() == 'S') && (((Borde)cg2).ObtenerNombre() == 'N'))) ||
                                                                            (((((Borde)cg1).ObtenerNombre() == 'E') && (((Borde)cg2).ObtenerNombre() == 'O')) ||
                                                                            ((((Borde)cg1).ObtenerNombre() == 'O') && (((Borde)cg2).ObtenerNombre() == 'E'))))
                                                                        return 0;
                                                                }
                                                            }
                                                            else{
                                                                // No debe insertarse una SCV si ya existe una CV
                                                                if(!SubC.HayConexionEx(cg1, cg2)){
                                                                    Camino sc = c1.Union(c2, cg);
                                                                    
                                                                    if(SubSC.ObtenerRuta(cg1, cg2) == null){
                                                                        SubSC.NuevaConexion(cg1, cg2);
                                                                        NuevasConexiones = true;
                                                                    }
                                                                    Ruta r = SubSC.ObtenerRuta(cg1, cg2);

                                                                    if(r.Agregar(sc)){
                                                                        Ruta rsc = r.ClonarSinCamino(sc);
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
            
            /* Marca como antiguos los caminos utilizados en la anterior iteraci�n
             * y prepara los utilizados en la actual para caducar en la siguiente */
            Iterator cad = Caducar.iterator();
            while(cad.hasNext()){
                Camino mod = (Camino)cad.next();
                mod.CambiarNuevo(false);
                Renovar.add(mod);
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
        double N[][];   // Matriz de conductancias definitiva
        double B[];     // La matriz columna de t�rminos independientes de la ecuaci�n
        int conectado = -1;

        M = new double [NumeroNodos][NumeroNodos];
        N = new double [NumeroNodos-1][NumeroNodos-1];
        B = new double [NumeroNodos-1];
        
        int Intensidad = 1; // Intensidad transmitida por la fuente de corriente
        int n = 0;          // �ndice en G de elemento actual
        int quitar = -1;    // �ndice del nodo que se considera tierra    
                    
        /* Recorre los elementos de G agregando la conductancia a la matriz seg�n
         * exista o no conexi�n. Anota el �ndice del nodo conectado a tierra para eliminarlo
         * y escribe la intensidad en el nodo conectado a la fuente. */
        for(int i=0; i<NumeroNodos; i++){
            Celda c1 = ArrayG[i];

            if(c1 instanceof Borde){
                if(((Borde)c1).ObtenerNombre() == tierra)
                    quitar = n;
                else if(((Borde)c1).ObtenerNombre() == superior){
                    B[n] = Intensidad; // Si es el nodo conectado a la fuente, marcar un 1 en la matriz de intensidades   
                    conectado = n;
                }
            }

            /* Marca el nodo actual como visitado para no volver. Esto se puede
             * hacer porque la matriz es sim�trica: */
            Visitados.add(c1);
            
            Iterator it2 = G[color].iterator(); // Obtiene un iterador de los nodos
            int m = 0;  // �ndice en G del elemento actual
            while(it2.hasNext()){
                Celda c2 = (Celda) it2.next();
                if(!Visitados.contains(c2)){
                    /* Si los elementos est�n conectados, inserta en la matriz 
                     * la conductancia entre ellos. Si no lo est�n, inserta 0 */
                    if(SubC.HayConexionEx(c1, c2)){
                        M[n][m] = M[m][n] = -1 / (double) (c1.ObtenerResistencia(color) + c2.ObtenerResistencia(color));
                    }
                    else M[n][m] = M[m][n] = 0;
                }
                m++;
            }
            n++;
        }
        
        /* Recorre la matriz M y copia sus elementos en N, suprimiendo
         * la fila y la columna del nodo tierra y escribiendo los datos adecuados
         * en la diagonal de la matriz. */
        double ac; // Acumulador de la suma de las conductancias de cada fila
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
        Matriz m = new Matriz(N);
        double c[] = new double[NumeroNodos-1];
        c = m.Solucion(B, true);
        
        return c[conectado];
    }
    
    /** Funci�n recursiva que aplica la regla OR sobre una ruta creada aplicando
     *  la regla AND. La longitud de las rutas que se le pasan est� limitada por K
     *  para impedir que se disparen las llamadas. */
    private boolean AplicarReglaOR(Conexiones c, Celda g1, Celda g2, Ruta sc, Camino u, Camino i){
        if(sc.ObtenerLongitud() > K) return false;
        
        Iterator <Camino>isc = sc.ObtenerIterador();
        Camino sc1 = null;
        Camino u1 = null;
        Camino i1 = null;
        Ruta ruta = null;

        while(isc.hasNext()){
            sc1 = isc.next();
            u1 = u.Union(sc1);
            if(i.InterseccionVacia(sc1)){ 
                if(c.ObtenerRuta(g1, g2) == null){
                    c.NuevaConexion(g1, g2);
                    NuevasConexiones = true;

                }
                ruta = c.ObtenerRuta(g1,g2);
                ruta.Agregar(u1);
                
                if((g1 instanceof Borde) && (g2 instanceof Borde)){
                    if((((((Borde)g1).ObtenerNombre() == 'N') && (((Borde)g2).ObtenerNombre() == 'S')) ||
                            ((((Borde)g1).ObtenerNombre() == 'S') && (((Borde)g2).ObtenerNombre() == 'N'))) ||
                            (((((Borde)g1).ObtenerNombre() == 'E') && (((Borde)g2).ObtenerNombre() == 'O')) ||
                            ((((Borde)g1).ObtenerNombre() == 'O') && (((Borde)g2).ObtenerNombre() == 'E'))))
                        return true;
                }
            }
            else{
                i1 = i.Interseccion(sc1);
                Ruta rsc = sc.ClonarSinCamino(sc1);
                if(AplicarReglaOR(c, g1, g2, rsc, u1, i1) == true) return true;
            }
        }
        return false;
    }
   
    /** Recorre el tablero y muestra las parejas de casillas y bordes entre los que hay 
     *  una conexi�n establecida.
     *  @param  mostrar Permite configurar la lista: 1 = s�lo CV, 2 = s�lo SCV, 3 = s�lo T.
     *  @return Devuelve la media de conexiones por extremo (float) */ 
   public float MostrarConexionesEx(int color, int mostrar){
        Conexiones ob = null;
        Iterator it1 = G[color].iterator();
        float total = 0, tmp = 0;
        int numero = 0;
        Ruta r = null;
        
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
            Celda c1 = (Celda) it1.next();
            while(it2.hasNext()){
                Celda c2 = (Celda) it2.next();
                if(c2 != c1){ 
                    if(ob.HayConexion(c1, c2)){
                        r = ob.ObtenerRuta(c1, c2);
                        tmp = r.ObtenerLongitud();
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
     *  una conexi�n establecida.
     *  @param mostrar Permite configurar la lista: 1 = s�lo CV, 2 = s�lo SCV, 3 = s�lo T. */ 
    public void MostrarConexionesMinimas(int color, int mostrar){
        Conexiones ob = null;
        Iterator it1 = G[color].iterator();
        int numero = 0;
        Ruta r = null;
        
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
            Celda c1 = (Celda) it1.next();
            while(it2.hasNext()){
                Celda c2 = (Celda) it2.next();
                if(c2 != c1){  
                    if(ob.HayConexion(c1, c2)){
                        r = ob.ObtenerRuta(c1, c2);
                        numero++;
                        if(r != null) System.out.println("(" +  c1 + ", " + r.ObtenerCaminoMinimo() + " , " + c2 + ")");
                    }
                }
            }
        }
    }
    
    /** Devuelve el tablero asociado a la simulaci�n
     *  @return El tablero de la simulaci�n */
    public Tablero ObtenerTablero(){
        return Tab;
    }
    
    /** Toma los caminos marcados como viejos y los renueva
     *  para prepararlos para la siguiente iteraci�n */
    private void RestauraCaminos(){
        Iterator ren = Renovar.iterator();
        while(ren.hasNext()){
            ((Camino)ren.next()).CambiarNuevo(true);
        }
    }
}

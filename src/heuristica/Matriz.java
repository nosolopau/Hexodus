/*
 * Hexodus >> Matriz.java
 *
 * Creado el 25 de febrero de 2007 a las 17:33
 *
 * Copyright (C) 2006 - 2008 Pablo Torrecilla. GNU General Public License
 */

package heuristica;

import java.util.*;
import java.text.*;

/**
 *  Representa una matriz matemática y habilita la resolución de ecuaciones por
 *  el método del pivote de Gauss.
 *
 *  @author Pau
 *  @version 2.0
 */
public class Matriz{  
    private double [][] matriz = null; 
    private int NumeroFilas = 0;  
    private int NumeroColumnas = 0; 
	
    /** Crea una matriz cuadrada de dimensión n, inicializando los valores a 0
     *  @param n dimensión de la matriz */
    public Matriz(int n){
	NumeroFilas = n; 
	NumeroColumnas = n;
	matriz = new double[n][n];
	for(int i = 0; i < n; i++){
	    for(int j = 0; j < n; j++){
		matriz[i][j] = 0;
	    }
	}
    }
  	
    /** Crea una matriz cuadrada de dimensión n * m, inicializando los valores a 0
     *  @param n número de filas de la matriz
     *  @param m número de columnas de la matriz*/
    public Matriz(int n, int m){
	NumeroFilas = n; 
    	NumeroColumnas = m;
    	matriz = new double[n][m];
    	for(int i = 0; i < n; i++){
	    for(int j = 0; j < m; j++){
    	       	matriz[i][j] = 0;
    	    }
	}
    }

    /** Crea una matriz cuadrada a partir de un vector bidimensional dado.
     *  @param a vertor bidimensional */
    public Matriz(double[][] a){
	NumeroFilas = a.length;
	NumeroColumnas = a[0].length;
	matriz = a;
    }

    /** Asigna un valor v al elemento i, j 
     *  @param i fila
     *  @param j columna
     *  @param v valor */
    public void Asigna(int i, int j, double v){
    	matriz[i][j] = v;
    }

    /** Genera una matriz identidad de dimensión determinada 
     *  @param n dimensión de la matriz
     *  @return matriz identidad de dimensión n*/
    public static Matriz Identidad(int n){
	Matriz mat = new Matriz(n);
    	for(int i = 0; i < n; i++){
	    for(int j = 0; j < n; j++){
      		mat.Asigna(i, j,(i == j ? 1 : 0));
      	    }
    	}
    	return mat;
    }
  	
    /** Suma una matriz dada a la actual y devuelve la matriz resultante
     *  @param matriz la matriz que se va a sumar
     *  @return la matriz suma de las dos matrices */ 
    public Matriz Suma(Matriz matriz){
       	int n = this.ObtenerNumeroFilas();
       	int m = this.ObtenerColumna();
       	Matriz suma = new Matriz(n, m);
       	for(int i = 0; i < n; i++){
	    for(int j = 0; j < m; j++) {
      		suma.Asigna(i, j, this.ObtenerValor(i, j) + matriz.ObtenerValor(i, j));
	    }
	}
	return suma;	
    }
	
    /** Realiza el producto escalar
     *  @param escalar el escalar por el que se multiplica la matriz
     *  @return la matriz moltiplicata per lo scalare
    */ 
    public Matriz Multiplicacion(double escalar){
       	int n = this.ObtenerNumeroFilas();
       	int m = this.ObtenerColumna();
       	Matriz prod = new Matriz(n, m);
       	for(int i = 0; i < n; i++){
	    for(int j = 0; j < m; j++){
		prod.Asigna(i, j, this.ObtenerValor(i, j) * escalar);
	    }
	}
	return prod;	
    }
  	
    /** Realiza el producto entre la matriz actual y la dada
     *  @param matriz matriz por la que se va a multiplicar
     *  @return matriz producto de las dos */ 
    public Matriz Producto(Matriz matriz){
	int n = this.ObtenerNumeroFilas();
       	int m = matriz.ObtenerColumna();
       	int z = matriz.ObtenerNumeroFilas();
       	Matriz producto = new Matriz(n, m);
       	for(int i = 0; i < n; i++){
	    for(int j = 0; j < m; j++){
		double p = 0;
		for(int k = 0; k < z; k++){
		    p = p + ((this.ObtenerValor(i, k)) * (matriz.ObtenerValor(k, j)));
		}	
		producto.Asigna(i, j, p);
	    }
	}
	return producto;	
    }
	
    /** Intercambia la fila i con la k
     *  @param i la primera fila
     *  @param k la segunda fila */
    public void Intercambia(int i, int k){
	int m = this.ObtenerColumna();
       	double[] riga1 = new double[m];
       	for(int j = 0; j < m; j++){
	    riga1[j] = matriz[i][j];
	    matriz[i][j] = matriz[k][j];
	    matriz[k][j] = riga1[j];
	}
    }
	
    /** Aplica el algoritmo de Gauss sin efectuar intercambios de filas
     *  @return matriz triangular obtenida por Gauss (sin efectuar intercambios de filas) */	
    public Matriz Gauss() {
	Matriz ma = this.Copia();
       	int n = ma.ObtenerNumeroFilas();
       	int m = ma.ObtenerColumna();
       	boolean ind = true;
       	int k = 0;
       	while(ind && (k < n - 1)){
	    double pivot = ma.ObtenerValor(k, k);
	    ind = (pivot != 0);
	    if(ind){
		for(int i = k + 1; i < n; i++){
		    double mp = (ma.ObtenerValor(i, k)) / pivot;
		    for(int z = k; z < m; z++){
			ma.Asigna(i, z, ma.ObtenerValor(i, z) - (mp * ma.ObtenerValor(k, z)));
		    }
		}
	    }
	    k = k + 1;	
	}
	return ma;
    }
	
    /** Aplica el algoritmo de Gauss con la técnica del máximo pivote
     *  @return matriz triangular obtenida por Gauss */	
    public Matriz GaussPivote() {
       	Matriz ma = this.Copia();
       	int n = ma.ObtenerNumeroFilas();
       	int m = ma.ObtenerColumna();
       	boolean ind = true;
       	int k = 0;
       	while(ind && (k < n - 1)){
	    double pivot = ma.ObtenerValor(k, k);
       	    for(int i = k + 1; i < n; i++){
		if(Math.abs(ma.ObtenerValor(i, k)) > Math.abs(pivot)){
		    pivot = ma.ObtenerValor(i, k);
		    ma.Intercambia(i, k);
		}
	    }
	    ind = (pivot != 0);
	    if(ind){
		for(int i = k + 1; i < n; i++){
		    double mp = (ma.ObtenerValor(i, k)) / pivot;
		    for(int z = k; z < m; z++){
			ma.Asigna(i, z, ma.ObtenerValor(i, z) - (mp * ma.ObtenerValor(k, z)));
		    }
		}
	    }
	    k = k + 1;	
	}
	return ma;
    }
      	
    /** Calcula las soluciones de la matriz aplicada al vector b utilizando el método de Gauss 
     *  @param pivot si es verdadero se utilizará la técnica del pivote
     *  @param b el vector de valores de la ecuación
     *  @return el vertor solución de la ecuación */
    public double[] Solucion(double[] b, boolean pivot){
	int n = this.ObtenerNumeroFilas();
       	int m = this.ObtenerColumna();
        
	Matriz ma = new Matriz(n, m + 1);
        
	for(int i = 0; i < n; i++){
	    for(int j = 0; j < m; j++){
		ma.Asigna(i, j, this.ObtenerValor(i, j));
	    }
	    ma.Asigna(i, m, b[i]);
	}
       	return ma.SolucionCompleta(pivot);
    }

    /** Calcula las soluciones de la matriz aumentada utilizando el método de Gauss 
     *  @param pivot si es verdadero se utilizará la técnica del pivote
     *  @return el vertor solución de la ecuación */
    public double[] SolucionCompleta(boolean pivot){
       	int n = this.ObtenerNumeroFilas();
       	int m = this.ObtenerColumna();
       	double[] s = new double[n];
       	Matriz ma = (pivot ? this.GaussPivote() : this.Gauss());
       	s[n - 1] = ma.ObtenerValor(n - 1, m - 1) / ma.ObtenerValor(n - 1, m - 2);
       	for(int i = n - 2; i >= 0; i--){
	    for(int j = i + 1; j < n; j++){
		ma.Asigna(i, m - 1, ma.ObtenerValor(i, m - 1) - (ma.ObtenerValor(i, j) * s[j]));
	    }
	    s[i] = ma.ObtenerValor(i, m - 1) / ma.ObtenerValor(i, i);	
	} 
        return s;
    }

    /** Calcula la inversa de la matriz (A * A(^-1) = I)
     *  @return la matriz A^(-1) */
    public Matriz Inversa(){
       	int n = this.ObtenerNumeroFilas();
       	Matriz inv = new Matriz(n);
       	Matriz ma = new Matriz(n, 2 * n);
       	for(int i = 0; i < n; i++){
	    for(int j = 0; j < n; j++){
		ma.Asigna(i, j, this.ObtenerValor(i, j));
	    }
	}
	for(int i = 0; i < n; i++){
	    for(int j = n; j < (2 * n); j++){
		ma.Asigna(i, j, (n == (j - i) ? 1 : 0));
	    }
	}
	Matriz m1 = ma.Gauss();
	for(int k = n; k < 2 * n; k++){
	    Matriz mr = new Matriz(n, n + 1);
	    for(int i = 0; i < n; i++){
		for(int j = 0; j < n; j++)
		{
		    mr.Asigna(i, j, m1.ObtenerValor(i, j));
		}
	        mr.Asigna(i, n, m1.ObtenerValor(i, k));
	    }
	    double[] s = mr.SolucionCompleta(false);
	    for(int i = 0; i < n; i++){ 
		inv.Asigna(i, k - n, s[i]);
	    }
	}
	return inv;
    }
	
    /** Calcula el determinante de la matriz
     *  @return el determinante de la matriz */
    public double Determinante(){
       	int n = this.ObtenerNumeroFilas();
       	Matriz mat1 = this.Gauss();
       	double det = 1;
       	for(int i = 0; i < n; i++)
	    det = det * mat1.ObtenerValor(i, i);
	return det;
    }	

    /** Devuelve una copia de la matriz en forma de array bidimensional
     *  @return vector bidimensional con la copia */
    public double[][] ObtenerCopiaMatriz(){
	int n = this.ObtenerNumeroFilas();
	int m = this.ObtenerColumna();
	double[][] cm = new double[n][m];
       	for(int i = 0; i < n; i++)
	    for(int j = 0; j < m; j++)
		cm[i][j] = this.ObtenerValor(i, j);
	return cm;
    }

    /** Utiliza la generación a través de un vector bidimensional para generar
     *  una copia de la matriz actual
     *  @return copia de la matriz */			
    public Matriz Copia(){
	Matriz ma = new Matriz(this.ObtenerCopiaMatriz());
	return ma;
    }	
	
    /** Devuelve el valor del elemento (i, j)
     *  @param i fila del elemento
     *  @param j columna del elemento
     *  @return valor del elemento */				
    public double ObtenerValor(int i, int j){
       	return this.matriz[i][j];
    }
				
    /** Devuelve el número de filas de la matriz
     *  @return numero de filas */	
    public int ObtenerNumeroFilas(){ 
       	return this.NumeroFilas;
    }
  	
    /** Devuelve el número de columnas de la matriz
     *  @return numero de columnas */	
    public int ObtenerColumna(){ 
	return this.NumeroColumnas;
    }
    
    /** (SUPR) Norma 2 para resolver por Gauss-Seidel */
    private double ResolverNorma(double vector[], int dim) { 
        int i;
        double norma; 
        norma = 0;
        for(i = 0; i < dim; i++)
            norma += (vector[i]*vector[i]);
        norma = Math.sqrt(norma);
        return(norma);
    }
    
    /** (SUPR) Resolver por Gauss-Seidel */
    private double Resolver(double A[][], double b[], int dim){
        double norma_2, sum1, sum2;
        
        int maxit = 100;     // Número máximo de iteraciones
        double tol = 0.001;    // Tolerancia
        
        double [] x0  = new double [dim];
        double [] x1  = new double [dim];
        double [] dif = new double [dim];
        
        int i, j, k;
        
        /* Iteracion inicial */ 
	for(i = 0; i < dim; i++)
            x0[i] = 0.;
	k = 1;
        
        do{ 
            // Calculamos la iteracion de Gauss-Seidel
            sum1 = 0; 
            for(j = 1; j < dim; j++)
                    sum1 += A[0][j] * x0[j]; 
            x1[0] = (b[0] - sum1) / A[0][0];
            
            for(i = 1; i < dim-1; i++) { 
                sum1=0; 
                sum2=0; 
                for(j = 0; j < i; j++)          sum1 += A[i][j] * x1[j]; 
                for(j = i + 1; j < dim; j++)    sum2 += A[i][j] * x0[j]; 
                x1[i] = (b[i]-sum1-sum2) / A[i][i]; 
            } 
            sum2 = 0; 
            for(j = 0; j < dim - 1; j++)
                sum2 += A[dim-1][j] * x1[j];   
            x1[dim-1] = (b[dim-1]-sum2) / A[dim-1][dim-1]; 

            for(i=0; i<dim; i++) { 
                    dif[i] = x1[i] - x0[i]; 
                    x0[i] = x1[i]; 
            } 
            norma_2 = ResolverNorma(dif, dim); 
            norma_2 = norma_2 / ResolverNorma(x1, dim); 
            k++;
	} while(norma_2 >= tol && k <= maxit);
        
        if(norma_2 >= tol ) { 
            System.out.printf("\n\nNo se alcanzo la precision pedida en %d",maxit); 
            System.out.printf(" iteraciones\n"); 
	}
	System.out.printf("Solucion aproximada\n\n");
        for(i = 0; i < dim; i++)
            System.out.println("x[" + i + "]=" + x1[i]);
	System.out.printf("\nNumero de iteraciones: %d\n",k); 
        
	/*En teoría sirve sólo para organizar los resultados de 4 en 4 ¬¬
            for(i = 0; i < dim; i += 4) { 
		System.out.printf("x[%d]=%f  x[%d]=%f  x[%d]=%f  x[%d]=%f\n",i,x1[i], 
		i+1,x1[i+1],i+2,x1[i+2],i+3,x1[i+3]); 
	}*/
        return x1[0];
     }
}
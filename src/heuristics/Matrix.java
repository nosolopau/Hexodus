package heuristics;

import java.util.*;
import java.text.*;

/**
 *  Represents a mathematical matrix and enables equation solving using
 *  the Gauss pivot method.
 *
 *  @author Pau
 *  @version 2.0
 */
public class Matrix{
    private double [][] matrix = null;
    private int rowCount = 0;
    private int columnCount = 0;

    /** Creates a square matrix of dimension n, initializing values to 0
     *  @param n matrix dimension */
    public Matrix(int n){
        rowCount = n;
        columnCount = n;
        matrix = new double[n][n];
        for(int i = 0; i < n; i++){
            for(int j = 0; j < n; j++){
                matrix[i][j] = 0;
            }
        }
    }

    /** Creates a rectangular matrix of dimension n * m, initializing values to 0
     *  @param n matrix row count
     *  @param m matrix column count*/
    public Matrix(int n, int m){
        rowCount = n;
        columnCount = m;
        matrix = new double[n][m];
        for(int i = 0; i < n; i++){
            for(int j = 0; j < m; j++){
                matrix[i][j] = 0;
            }
        }
    }

    /** Creates a square matrix from a given two-dimensional array.
     *  @param a two-dimensional array */
    public Matrix(double[][] a){
        rowCount = a.length;
        columnCount = a[0].length;
        matrix = a;
    }

    /** Assigns value v to element i, j
     *  @param i row
     *  @param j column
     *  @param v value */
    public void assign(int i, int j, double v){
        matrix[i][j] = v;
    }

    /** Generates an identity matrix of given dimension
     *  @param n matrix dimension
     *  @return identity matrix of dimension n*/
    public static Matrix identity(int n){
        Matrix mat = new Matrix(n);
        for(int i = 0; i < n; i++){
            for(int j = 0; j < n; j++){
                mat.assign(i, j,(i == j ? 1 : 0));
            }
        }
        return mat;
    }

    /** Adds a given matrix to the current one and returns the resulting matrix
     *  @param matrix the matrix to add
     *  @return the sum matrix of the two matrices */
    public Matrix sum(Matrix matrix){
        int n = this.getRowCount();
        int m = this.getColumnCount();
        Matrix sum = new Matrix(n, m);
        for(int i = 0; i < n; i++){
            for(int j = 0; j < m; j++) {
                sum.assign(i, j, this.getValue(i, j) + matrix.getValue(i, j));
            }
        }
        return sum;
    }

    /** Performs scalar multiplication
     *  @param scalar the scalar by which to multiply the matrix
     *  @return the matrix multiplied by the scalar
    */
    public Matrix multiplication(double scalar){
        int n = this.getRowCount();
        int m = this.getColumnCount();
        Matrix prod = new Matrix(n, m);
        for(int i = 0; i < n; i++){
            for(int j = 0; j < m; j++){
                prod.assign(i, j, this.getValue(i, j) * scalar);
            }
        }
        return prod;
    }

    /** Performs the product between the current matrix and the given one
     *  @param matrix matrix to multiply by
     *  @return product matrix of the two */
    public Matrix product(Matrix matrix){
        int n = this.getRowCount();
        int m = matrix.getColumnCount();
        int z = matrix.getRowCount();
        Matrix product = new Matrix(n, m);
        for(int i = 0; i < n; i++){
            for(int j = 0; j < m; j++){
                double p = 0;
                for(int k = 0; k < z; k++){
                    p = p + ((this.getValue(i, k)) * (matrix.getValue(k, j)));
                }
                product.assign(i, j, p);
            }
        }
        return product;
    }

    /** Swaps row i with row k
     *  @param i the first row
     *  @param k the second row */
    public void swapRows(int i, int k){
        int m = this.getColumnCount();
        double[] row1 = new double[m];
        for(int j = 0; j < m; j++){
            row1[j] = matrix[i][j];
            matrix[i][j] = matrix[k][j];
            matrix[k][j] = row1[j];
        }
    }

    /** Applies Gauss algorithm without performing row swaps
     *  @return triangular matrix obtained by Gauss (without performing row swaps) */
    public Matrix gauss() {
        Matrix ma = this.copy();
        int n = ma.getRowCount();
        int m = ma.getColumnCount();
        boolean ind = true;
        int k = 0;
        while(ind && (k < n - 1)){
            double pivot = ma.getValue(k, k);
            ind = (pivot != 0);
            if(ind){
                for(int i = k + 1; i < n; i++){
                    double mp = (ma.getValue(i, k)) / pivot;
                    for(int z = k; z < m; z++){
                        ma.assign(i, z, ma.getValue(i, z) - (mp * ma.getValue(k, z)));
                    }
                }
            }
            k = k + 1;
        }
        return ma;
    }

    /** Applies Gauss algorithm with maximum pivot technique
     *  @return triangular matrix obtained by Gauss */
    public Matrix gaussPivot() {
        Matrix ma = this.copy();
        int n = ma.getRowCount();
        int m = ma.getColumnCount();
        boolean ind = true;
        int k = 0;
        while(ind && (k < n - 1)){
            double pivot = ma.getValue(k, k);
            for(int i = k + 1; i < n; i++){
                if(Math.abs(ma.getValue(i, k)) > Math.abs(pivot)){
                    pivot = ma.getValue(i, k);
                    ma.swapRows(i, k);
                }
            }
            ind = (pivot != 0);
            if(ind){
                for(int i = k + 1; i < n; i++){
                    double mp = (ma.getValue(i, k)) / pivot;
                    for(int z = k; z < m; z++){
                        ma.assign(i, z, ma.getValue(i, z) - (mp * ma.getValue(k, z)));
                    }
                }
            }
            k = k + 1;
        }
        return ma;
    }

    /** Calculates the solutions of the matrix applied to vector b using Gauss method
     *  @param pivot if true, the pivot technique will be used
     *  @param b the value vector of the equation
     *  @return the solution vector of the equation */
    public double[] solve(double[] b, boolean pivot){
        int n = this.getRowCount();
        int m = this.getColumnCount();

        Matrix ma = new Matrix(n, m + 1);

        for(int i = 0; i < n; i++){
            for(int j = 0; j < m; j++){
                ma.assign(i, j, this.getValue(i, j));
            }
            ma.assign(i, m, b[i]);
        }
        return ma.solveFull(pivot);
    }

    /** Calculates the solutions of the augmented matrix using Gauss method
     *  @param pivot if true, the pivot technique will be used
     *  @return the solution vector of the equation */
    public double[] solveFull(boolean pivot){
        int n = this.getRowCount();
        int m = this.getColumnCount();
        double[] s = new double[n];
        Matrix ma = (pivot ? this.gaussPivot() : this.gauss());
        s[n - 1] = ma.getValue(n - 1, m - 1) / ma.getValue(n - 1, m - 2);
        for(int i = n - 2; i >= 0; i--){
            for(int j = i + 1; j < n; j++){
                ma.assign(i, m - 1, ma.getValue(i, m - 1) - (ma.getValue(i, j) * s[j]));
            }
            s[i] = ma.getValue(i, m - 1) / ma.getValue(i, i);
        }
        return s;
    }

    /** Calculates the inverse of the matrix (A * A(^-1) = I)
     *  @return the matrix A^(-1) */
    public Matrix inverse(){
        int n = this.getRowCount();
        Matrix inv = new Matrix(n);
        Matrix ma = new Matrix(n, 2 * n);
        for(int i = 0; i < n; i++){
            for(int j = 0; j < n; j++){
                ma.assign(i, j, this.getValue(i, j));
            }
        }
        for(int i = 0; i < n; i++){
            for(int j = n; j < (2 * n); j++){
                ma.assign(i, j, (n == (j - i) ? 1 : 0));
            }
        }
        Matrix m1 = ma.gauss();
        for(int k = n; k < 2 * n; k++){
            Matrix mr = new Matrix(n, n + 1);
            for(int i = 0; i < n; i++){
                for(int j = 0; j < n; j++)
                {
                    mr.assign(i, j, m1.getValue(i, j));
                }
                mr.assign(i, n, m1.getValue(i, k));
            }
            double[] s = mr.solveFull(false);
            for(int i = 0; i < n; i++){
                inv.assign(i, k - n, s[i]);
            }
        }
        return inv;
    }

    /** Calculates the determinant of the matrix
     *  @return the matrix determinant */
    public double determinant(){
        int n = this.getRowCount();
        Matrix mat1 = this.gauss();
        double det = 1;
        for(int i = 0; i < n; i++)
            det = det * mat1.getValue(i, i);
        return det;
    }

    /** Returns a copy of the matrix as a two-dimensional array
     *  @return two-dimensional vector with the copy */
    public double[][] getMatrixCopy(){
        int n = this.getRowCount();
        int m = this.getColumnCount();
        double[][] cm = new double[n][m];
        for(int i = 0; i < n; i++)
            for(int j = 0; j < m; j++)
                cm[i][j] = this.getValue(i, j);
        return cm;
    }

    /** Uses generation through a two-dimensional vector to generate
     *  a copy of the current matrix
     *  @return matrix copy */
    public Matrix copy(){
        Matrix ma = new Matrix(this.getMatrixCopy());
        return ma;
    }

    /** Returns the value of element (i, j)
     *  @param i element row
     *  @param j element column
     *  @return element value */
    public double getValue(int i, int j){
        return this.matrix[i][j];
    }

    /** Returns the number of matrix rows
     *  @return number of rows */
    public int getRowCount(){
        return this.rowCount;
    }

    /** Returns the number of matrix columns
     *  @return number of columns */
    public int getColumnCount(){
        return this.columnCount;
    }

    /** (DEPRECATED) Norm 2 for solving by Gauss-Seidel */
    private double solveNorm(double vector[], int dim) {
        int i;
        double norm;
        norm = 0;
        for(i = 0; i < dim; i++)
            norm += (vector[i]*vector[i]);
        norm = Math.sqrt(norm);
        return(norm);
    }

    /** (DEPRECATED) Solve by Gauss-Seidel */
    private double solveGaussSeidel(double A[][], double b[], int dim){
        double norm_2, sum1, sum2;

        int maxit = 100;        // Maximum number of iterations
        double tol = 0.001;     // Tolerance

        double [] x0  = new double [dim];
        double [] x1  = new double [dim];
        double [] dif = new double [dim];

        int i, j, k;

        /* Initial iteration */
        for(i = 0; i < dim; i++)
            x0[i] = 0.;
        k = 1;

        do{
            // Calculate Gauss-Seidel iteration
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
            norm_2 = solveNorm(dif, dim);
            norm_2 = norm_2 / solveNorm(x1, dim);
            k++;
        } while(norm_2 >= tol && k <= maxit);

        if(norm_2 >= tol ) {
            System.out.printf("\n\nRequested precision not reached in %d",maxit);
            System.out.printf(" iterations\n");
        }
        System.out.printf("Approximate solution\n\n");
        for(i = 0; i < dim; i++)
            System.out.println("x[" + i + "]=" + x1[i]);
        System.out.printf("\nNumber of iterations: %d\n",k);

        /*In theory it only serves to organize results 4 by 4 !!
            for(i = 0; i < dim; i += 4) {
                System.out.printf("x[%d]=%f  x[%d]=%f  x[%d]=%f  x[%d]=%f\n",i,x1[i],
                i+1,x1[i+1],i+2,x1[i+2],i+3,x1[i+3]);
        }*/
        return x1[0];
     }
}

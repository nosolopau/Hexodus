package heuristics;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Test suite for the Matrix class.
 * Tests matrix operations including Gauss elimination, solving equations,
 * determinant, inverse, and basic arithmetic operations.
 */
public class MatrixTest {
    private static final double EPSILON = 0.0001;

    @Test
    public void testMatrixCreation_Square() {
        // Test creating a square matrix
        Matrix matrix = new Matrix(3);

        assertEquals(3, matrix.getRowCount());
        assertEquals(3, matrix.getColumnCount());
    }

    @Test
    public void testMatrixCreation_Rectangular() {
        // Test creating a rectangular matrix
        Matrix matrix = new Matrix(3, 4);

        assertEquals(3, matrix.getRowCount());
        assertEquals(4, matrix.getColumnCount());
    }

    @Test
    public void testMatrixCreation_FromArray() {
        // Test creating matrix from 2D array
        double[][] array = {{1, 2}, {3, 4}};
        Matrix matrix = new Matrix(array);

        assertEquals(2, matrix.getRowCount());
        assertEquals(2, matrix.getColumnCount());
        assertEquals(1.0, matrix.getValue(0, 0), EPSILON);
        assertEquals(4.0, matrix.getValue(1, 1), EPSILON);
    }

    @Test
    public void testMatrixCreation_InitializedToZero() {
        // Test that matrix is initialized to zero
        Matrix matrix = new Matrix(2);

        assertEquals(0.0, matrix.getValue(0, 0), EPSILON);
        assertEquals(0.0, matrix.getValue(0, 1), EPSILON);
        assertEquals(0.0, matrix.getValue(1, 0), EPSILON);
        assertEquals(0.0, matrix.getValue(1, 1), EPSILON);
    }

    @Test
    public void testAssign_Value() {
        // Test assigning values to matrix
        Matrix matrix = new Matrix(2);
        matrix.assign(0, 0, 5.0);
        matrix.assign(1, 1, 10.0);

        assertEquals(5.0, matrix.getValue(0, 0), EPSILON);
        assertEquals(10.0, matrix.getValue(1, 1), EPSILON);
    }

    @Test
    public void testIdentity_Creation() {
        // Test creating identity matrix
        Matrix identity = Matrix.identity(3);

        assertEquals(1.0, identity.getValue(0, 0), EPSILON);
        assertEquals(1.0, identity.getValue(1, 1), EPSILON);
        assertEquals(1.0, identity.getValue(2, 2), EPSILON);
        assertEquals(0.0, identity.getValue(0, 1), EPSILON);
        assertEquals(0.0, identity.getValue(1, 0), EPSILON);
    }

    @Test
    public void testSum_TwoMatrices() {
        // Test matrix addition
        Matrix m1 = new Matrix(2);
        m1.assign(0, 0, 1.0);
        m1.assign(0, 1, 2.0);
        m1.assign(1, 0, 3.0);
        m1.assign(1, 1, 4.0);

        Matrix m2 = new Matrix(2);
        m2.assign(0, 0, 5.0);
        m2.assign(0, 1, 6.0);
        m2.assign(1, 0, 7.0);
        m2.assign(1, 1, 8.0);

        Matrix sum = m1.sum(m2);

        assertEquals(6.0, sum.getValue(0, 0), EPSILON);
        assertEquals(8.0, sum.getValue(0, 1), EPSILON);
        assertEquals(10.0, sum.getValue(1, 0), EPSILON);
        assertEquals(12.0, sum.getValue(1, 1), EPSILON);
    }

    @Test
    public void testMultiplication_Scalar() {
        // Test scalar multiplication
        Matrix matrix = new Matrix(2);
        matrix.assign(0, 0, 2.0);
        matrix.assign(0, 1, 4.0);
        matrix.assign(1, 0, 6.0);
        matrix.assign(1, 1, 8.0);

        Matrix result = matrix.multiplication(3.0);

        assertEquals(6.0, result.getValue(0, 0), EPSILON);
        assertEquals(12.0, result.getValue(0, 1), EPSILON);
        assertEquals(18.0, result.getValue(1, 0), EPSILON);
        assertEquals(24.0, result.getValue(1, 1), EPSILON);
    }

    @Test
    public void testProduct_TwoMatrices() {
        // Test matrix multiplication
        Matrix m1 = new Matrix(2);
        m1.assign(0, 0, 1.0);
        m1.assign(0, 1, 2.0);
        m1.assign(1, 0, 3.0);
        m1.assign(1, 1, 4.0);

        Matrix m2 = new Matrix(2);
        m2.assign(0, 0, 2.0);
        m2.assign(0, 1, 0.0);
        m2.assign(1, 0, 1.0);
        m2.assign(1, 1, 2.0);

        Matrix product = m1.product(m2);

        assertEquals(4.0, product.getValue(0, 0), EPSILON);
        assertEquals(4.0, product.getValue(0, 1), EPSILON);
        assertEquals(10.0, product.getValue(1, 0), EPSILON);
        assertEquals(8.0, product.getValue(1, 1), EPSILON);
    }

    @Test
    public void testSwapRows() {
        // Test swapping rows
        Matrix matrix = new Matrix(3);
        matrix.assign(0, 0, 1.0);
        matrix.assign(0, 1, 2.0);
        matrix.assign(0, 2, 3.0);
        matrix.assign(1, 0, 4.0);
        matrix.assign(1, 1, 5.0);
        matrix.assign(1, 2, 6.0);

        matrix.swapRows(0, 1);

        assertEquals(4.0, matrix.getValue(0, 0), EPSILON);
        assertEquals(5.0, matrix.getValue(0, 1), EPSILON);
        assertEquals(6.0, matrix.getValue(0, 2), EPSILON);
        assertEquals(1.0, matrix.getValue(1, 0), EPSILON);
        assertEquals(2.0, matrix.getValue(1, 1), EPSILON);
        assertEquals(3.0, matrix.getValue(1, 2), EPSILON);
    }

    @Test
    public void testGauss_UpperTriangular() {
        // Test Gauss elimination
        Matrix matrix = new Matrix(3);
        matrix.assign(0, 0, 2.0);
        matrix.assign(0, 1, 1.0);
        matrix.assign(0, 2, -1.0);
        matrix.assign(1, 0, -3.0);
        matrix.assign(1, 1, -1.0);
        matrix.assign(1, 2, 2.0);
        matrix.assign(2, 0, -2.0);
        matrix.assign(2, 1, 1.0);
        matrix.assign(2, 2, 2.0);

        Matrix gauss = matrix.gauss();

        // Should be upper triangular
        assertEquals(0.0, gauss.getValue(1, 0), EPSILON);
        assertEquals(0.0, gauss.getValue(2, 0), EPSILON);
    }

    @Test
    public void testGaussPivot_UpperTriangular() {
        // Test Gauss elimination with pivot
        Matrix matrix = new Matrix(3);
        matrix.assign(0, 0, 2.0);
        matrix.assign(0, 1, 1.0);
        matrix.assign(0, 2, -1.0);
        matrix.assign(1, 0, -3.0);
        matrix.assign(1, 1, -1.0);
        matrix.assign(1, 2, 2.0);
        matrix.assign(2, 0, -2.0);
        matrix.assign(2, 1, 1.0);
        matrix.assign(2, 2, 2.0);

        Matrix gauss = matrix.gaussPivot();

        // Should be upper triangular
        assertEquals(0.0, gauss.getValue(1, 0), EPSILON);
        assertEquals(0.0, gauss.getValue(2, 0), EPSILON);
    }

    @Test
    public void testSolve_LinearSystem() {
        // Test solving a linear system: 2x + y = 5, x + y = 3
        Matrix matrix = new Matrix(2);
        matrix.assign(0, 0, 2.0);
        matrix.assign(0, 1, 1.0);
        matrix.assign(1, 0, 1.0);
        matrix.assign(1, 1, 1.0);

        double[] b = {5.0, 3.0};
        double[] solution = matrix.solve(b, false);

        // Solution should be x=2, y=1
        assertEquals(2.0, solution[0], EPSILON);
        assertEquals(1.0, solution[1], EPSILON);
    }

    @Test
    public void testSolve_WithPivot() {
        // Test solving with pivot technique
        Matrix matrix = new Matrix(2);
        matrix.assign(0, 0, 2.0);
        matrix.assign(0, 1, 1.0);
        matrix.assign(1, 0, 1.0);
        matrix.assign(1, 1, 1.0);

        double[] b = {5.0, 3.0};
        double[] solution = matrix.solve(b, true);

        // Solution should be x=2, y=1
        assertEquals(2.0, solution[0], EPSILON);
        assertEquals(1.0, solution[1], EPSILON);
    }

    @Test
    public void testDeterminant_2x2() {
        // Test determinant calculation for 2x2 matrix
        Matrix matrix = new Matrix(2);
        matrix.assign(0, 0, 3.0);
        matrix.assign(0, 1, 8.0);
        matrix.assign(1, 0, 4.0);
        matrix.assign(1, 1, 6.0);

        double det = matrix.determinant();

        // det = 3*6 - 8*4 = 18 - 32 = -14
        assertEquals(-14.0, det, EPSILON);
    }

    @Test
    public void testDeterminant_3x3() {
        // Test determinant calculation for 3x3 matrix
        Matrix matrix = new Matrix(3);
        matrix.assign(0, 0, 2.0);
        matrix.assign(0, 1, -1.0);
        matrix.assign(0, 2, 0.0);
        matrix.assign(1, 0, -1.0);
        matrix.assign(1, 1, 2.0);
        matrix.assign(1, 2, -1.0);
        matrix.assign(2, 0, 0.0);
        matrix.assign(2, 1, -1.0);
        matrix.assign(2, 2, 2.0);

        double det = matrix.determinant();

        // This is a tridiagonal matrix with det = 4
        assertEquals(4.0, det, EPSILON);
    }

    @Test
    public void testDeterminant_Identity() {
        // Test determinant of identity matrix (should be 1)
        Matrix identity = Matrix.identity(3);
        double det = identity.determinant();

        assertEquals(1.0, det, EPSILON);
    }

    @Test
    public void testInverse_2x2() {
        // Test matrix inversion for 2x2 matrix
        Matrix matrix = new Matrix(2);
        matrix.assign(0, 0, 4.0);
        matrix.assign(0, 1, 7.0);
        matrix.assign(1, 0, 2.0);
        matrix.assign(1, 1, 6.0);

        Matrix inverse = matrix.inverse();

        // Verify A * A^(-1) = I
        Matrix product = matrix.product(inverse);

        assertEquals(1.0, product.getValue(0, 0), EPSILON);
        assertEquals(0.0, product.getValue(0, 1), EPSILON);
        assertEquals(0.0, product.getValue(1, 0), EPSILON);
        assertEquals(1.0, product.getValue(1, 1), EPSILON);
    }

    @Test
    public void testInverse_Identity() {
        // Test inverse of identity matrix (should be identity)
        Matrix identity = Matrix.identity(3);
        Matrix inverse = identity.inverse();

        assertEquals(1.0, inverse.getValue(0, 0), EPSILON);
        assertEquals(1.0, inverse.getValue(1, 1), EPSILON);
        assertEquals(1.0, inverse.getValue(2, 2), EPSILON);
        assertEquals(0.0, inverse.getValue(0, 1), EPSILON);
    }

    @Test
    public void testCopy_Independence() {
        // Test that copy is independent from original
        Matrix original = new Matrix(2);
        original.assign(0, 0, 5.0);
        original.assign(1, 1, 10.0);

        Matrix copy = original.copy();

        // Modify original
        original.assign(0, 0, 99.0);

        // Copy should be unchanged
        assertEquals(5.0, copy.getValue(0, 0), EPSILON);
        assertEquals(10.0, copy.getValue(1, 1), EPSILON);
    }

    @Test
    public void testGetMatrixCopy_Array() {
        // Test getting matrix as 2D array
        Matrix matrix = new Matrix(2);
        matrix.assign(0, 0, 1.0);
        matrix.assign(0, 1, 2.0);
        matrix.assign(1, 0, 3.0);
        matrix.assign(1, 1, 4.0);

        double[][] array = matrix.getMatrixCopy();

        assertEquals(2, array.length);
        assertEquals(2, array[0].length);
        assertEquals(1.0, array[0][0], EPSILON);
        assertEquals(4.0, array[1][1], EPSILON);
    }

    @Test
    public void testSolve_3x3System() {
        // Test solving larger system
        Matrix matrix = new Matrix(3);
        matrix.assign(0, 0, 1.0);
        matrix.assign(0, 1, 2.0);
        matrix.assign(0, 2, 3.0);
        matrix.assign(1, 0, 2.0);
        matrix.assign(1, 1, 3.0);
        matrix.assign(1, 2, 4.0);
        matrix.assign(2, 0, 3.0);
        matrix.assign(2, 1, 4.0);
        matrix.assign(2, 2, 6.0);

        double[] b = {14.0, 20.0, 29.0};
        double[] solution = matrix.solve(b, true);

        // Verify solution by substituting back
        double result0 = matrix.getValue(0, 0) * solution[0] +
                        matrix.getValue(0, 1) * solution[1] +
                        matrix.getValue(0, 2) * solution[2];
        assertEquals(b[0], result0, EPSILON);
    }
}

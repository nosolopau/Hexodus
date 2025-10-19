# Iterative Solver (Gauss-Seidel) Experiment

## Summary

This document describes an optimization attempt that **did not work as expected**.

## The Idea

The foundational paper "The Game of Hex: The Hierarchical Approach" by Vadim Anshelevich mentions using "a method of iterations" for solving the resistance calculation in the Hex heuristic.

**Hypothesis**: Replacing Gaussian elimination (O(n³)) with an iterative solver like Gauss-Seidel would:
- Reduce computation time for sparse matrices
- Scale better with board size
- Improve overall AI performance

## Implementation

Created a clean Gauss-Seidel iterative solver:
- Added `Matrix.solveIterative()` method (src/heuristics/Matrix.java:237-295)
- Modified `Simulation.calculateResistance()` to use iterative solver
- Configuration: 100 max iterations, 0.0001 convergence tolerance

## Results

**The optimization made performance SIGNIFICANTLY WORSE:**

| Configuration | Gaussian Elim | Iterative | Difference |
|---------------|---------------|-----------|------------|
| 3×3, Level 1  | 11.4 ms | 15.3 ms | **+34% slower** |
| 5×5, Level 1  | 44.4 ms | 130.6 ms | **+194% slower** |
| 6×6, Level 1  | 72.9 ms | 159.3 ms | **+119% slower** |
| 3×3, Level 2  | 1.6 ms | 2.6 ms | **+63% slower** |
| 5×5, Level 2  | 81.2 ms | 130.7 ms | **+61% slower** |
| 3×3, Level 3  | 1.8 ms | 3.7 ms | **+106% slower** |

The largest degradation was on 5×5 boards (nearly **3x slower**).

## Why It Failed

### 1. **Matrix sizes are too small for iterative methods to win**

Hex board resistance matrices:
- 3×3 board: ~9×9 matrix
- 5×5 board: ~25×25 matrix
- 6×6 board: ~36×36 matrix

Iterative methods typically outperform direct methods for matrices of 100×100 or larger.

### 2. **Iteration overhead dominates for small matrices**

Each Gauss-Seidel iteration requires:
- Full matrix traversal
- Convergence check calculations
- Multiple iterations to reach tolerance (typically 10-30 iterations)

For small matrices, this overhead exceeds the O(n³) cost of Gaussian elimination.

### 3. **Gaussian elimination with partial pivoting is highly optimized**

The existing `Matrix.solve()` with pivoting:
- Uses efficient in-place operations
- Performs backward substitution in one pass
- Benefits from CPU cache locality on small matrices
- JVM optimizes the tight loops very well

### 4. **The matrices are not sparse enough at this scale**

While each cell connects to ~6 neighbors (making the matrix conceptually sparse), on a 5×5 board with 25 nodes:
- Sparsity ratio: ~6/25 = 24%
- This isn't sparse enough to benefit iterative solvers on small matrices

## Lessons Learned

✅ **Paper recommendations don't always apply to all scales** - The paper likely tested on larger boards (9×9 or larger)

✅ **Matrix size matters for algorithm selection** - Small matrices favor direct methods, large matrices favor iterative methods

✅ **Always benchmark before assuming optimization works** - Theoretical complexity doesn't tell the whole story

✅ **Overhead can dominate for small problem sizes** - The crossover point where iterative beats direct is problem-dependent

## Why The Paper Recommends Iterative Methods

The paper likely used:
1. **Larger boards** (9×9, 11×11, 13×13) where matrices are 81×81, 121×121, 169×169
2. **Different convergence criteria** optimized for their use case
3. **Different implementation** possibly with specialized sparse matrix storage

## Files Preserved for Documentation

- `benchmark_arraylist_reuse.txt` - Baseline with Gaussian elimination
- `benchmark_iterative_solver.txt` - Failed iterative solver attempt
- `ITERATIVE_SOLVER_EXPERIMENT.md` - This document

## Outcome

**Code was reverted** after benchmarking confirmed significant performance regression.

The codebase continues using standard Gaussian elimination with partial pivoting (`Matrix.solve(B, true)`).

The `Matrix.solveIterative()` method remains in the code (lines 237-295) but is unused, marked for potential future use on larger boards.

## Real Optimization Opportunities

Future efforts should focus on:
1. **Pre-allocate matrices** in `calculateResistance()` instead of creating new ones
2. **Object pooling for Path objects** in resistance calculation
3. **Cache resistance calculations** for identical board states
4. **Optimize Path union/intersection** operations

## Date

October 19, 2025

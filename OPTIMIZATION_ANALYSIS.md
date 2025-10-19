# Hexodus Performance Analysis & Optimization Opportunities

## Current Architecture

### Heuristic Engine
- **Factory Pattern**: Automatically selects SingleThread or MultiThread based on CPU count
- **Multi-threading**: Creates one thread per possible move (up to dimension² threads)
- **Algorithm**: Alpha-beta pruning with configurable depth (level 1 or 2)
- **Core Calculation**: Resistance-based evaluation using virtual connections

### Performance Characteristics

**MultiThread Implementation (lines 188-387 in Heuristic.java)**
- Creates `dimension × dimension` simulation objects upfront (line 196-199)
- Each move spawns a new thread (line 234)
- For a 6×6 board with ~20 free cells: creates 20 threads simultaneously
- Threads run alpha-beta search independently and synchronize at join()

**Resistance Calculation (Simulation.java:255-494)**
- Most expensive operation
- Builds conductance matrix M[n×n] where n = free cells + borders
- Reduces to matrix N[(n-1)×(n-1)] by removing ground node
- Solves linear system using Gauss elimination O(n³)
- **Called twice per evaluation** (once for each player)

## Identified Performance Issues

### 1. **Excessive Thread Creation** ⚠️ CRITICAL
```java
// Current: Creates dimension² threads, uses only ~free_cells threads
threads = new GameThread [dimension*dimension];  // Line 223
```
**Problem**: 
- 6×6 board allocates 36 thread slots but might use only 15-20
- Memory waste and thread pool overhead
- Thread creation/destruction for each move

**Impact**: High for repeated move generation

### 2. **Duplicate Simulation Objects** ⚠️ HIGH
```java
// Creates dimension² simulations even though only free_cells are used
base = new Simulation [dimension*dimension];  // Line 196
for(int i = 0; i < dimension*dimension; i++){
    base[i] = new Simulation(dimension);  // Line 198
}
```
**Problem**:
- Wasteful memory allocation
- Each Simulation creates full board structures

**Impact**: Memory overhead ~O(dimension⁴)

### 3. **Matrix Recalculation** ⚠️ MEDIUM
**Problem**:
- Conductance matrix rebuilt from scratch for each evaluation
- Many cells don't change between evaluations
- No caching of partial results

**Impact**: O(n²) matrix building repeated for every node in search tree

### 4. **Virtual Connection Iteration** ⚠️ MEDIUM
```java
// Triple nested loop in calculateResistance (lines 295-391)
for(g = 0; g < NumeroNodos; g++){
    for(g1 = 0; g1 < NumeroNodos; g1++){
        for(g2 = g1 + 1; g2 < NumeroNodos; g2++){
```
**Problem**: O(n³) per iteration, runs up to 100 iterations

**Impact**: Can dominate runtime on larger boards

## Proposed Optimizations

### Priority 1: Fix Thread Pool ⭐⭐⭐
**Effort**: Low | **Impact**: High

```java
// Before
threads = new GameThread [dimension*dimension];

// After
ArrayList<GameThread> threads = new ArrayList<>();
for(Square c1 : free) {
    threads.add(new GameThread(base[i], c1, color));
    i++;
}
```

**Benefits**:
- Exact allocation for needed threads
- Reduced memory footprint
- Clearer code

### Priority 2: Use Thread Pool Executor ⭐⭐⭐
**Effort**: Medium | **Impact**: High

Replace manual thread management with `ExecutorService`:
```java
ExecutorService executor = Executors.newFixedThreadPool(
    Math.min(free.size(), Runtime.getRuntime().availableProcessors())
);
```

**Benefits**:
- Thread reuse across moves
- Automatic load balancing
- Better CPU utilization
- Less GC pressure

### Priority 3: Incremental Matrix Updates ⭐⭐
**Effort**: High | **Impact**: Medium-High

Instead of rebuilding the entire conductance matrix:
- Cache matrix from previous evaluation
- Update only changed rows/columns
- Use sparse matrix representation for large boards

**Benefits**:
- Reduces O(n²) → O(k) where k = changed cells
- Significant speedup for deep search trees

### Priority 4: Early Termination in Virtual Connections ⭐
**Effort**: Low | **Impact**: Medium

Add heuristics to break out of the 100-iteration loop early:
```java
if(iteraciones > 10 && !newsConnections_last_5_iterations) break;
```

**Benefits**:
- Avoid unnecessary iterations when convergence detected
- Reduces worst-case performance

### Priority 5: Opening Book Expansion ⭐
**Effort**: Low | **Impact**: Low (but nice)

Current opening book only has 11 positions. Expand to cover more openings.

**Benefits**:
- Instant responses for first 2-3 moves
- Better strategic play
- Reduced computation in early game

## Measurement Plan

Before implementing optimizations:

1. **Add Timing Instrumentation**
   ```java
   long start = System.nanoTime();
   // ... operation ...
   long elapsed = (System.nanoTime() - start) / 1_000_000;
   System.out.println("Operation took: " + elapsed + "ms");
   ```

2. **Profile Key Operations**
   - Time per `calculateResistance()` call
   - Time per move evaluation
   - Thread creation overhead
   - Matrix operations

3. **Benchmark Suite**
   - Create test cases for boards at different fill levels
   - Measure with 1 CPU vs multi-CPU
   - Compare SingleThread vs MultiThread performance

## Expected Results

Implementing Priorities 1-2:
- **30-50% reduction** in move generation time
- **60% reduction** in memory usage
- **Better scaling** with CPU count

Implementing Priority 3 (incremental updates):
- **Additional 40-60% speedup** for deep searches
- More benefit on larger boards (7×7+)

## Risks & Considerations

1. **Correctness**: Matrix caching must invalidate properly
2. **Complexity**: Incremental updates add code complexity
3. **Testing**: Need comprehensive regression tests (✅ already have 74 tests)
4. **Platform**: MultiThread already handles single-CPU gracefully

## Next Steps

1. Add performance benchmarks
2. Implement Priority 1 (quick win)
3. Measure improvement
4. Implement Priority 2 if Priority 1 shows promise
5. Consider Priority 3 only if needed for larger boards

---
Generated: 2025-10-18
Status: Ready for Review

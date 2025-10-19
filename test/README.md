# Hexodus Test Suite

This directory contains unit tests for the Hexodus project using JUnit 4.

## Test Coverage

### Game Package Tests (14 tests)
**BoardTest.java**
- Board creation with valid dimensions (3x3, 5x5, 7x7)
- Invalid dimension handling
- Move validation and square occupation
- Exception handling (NonexistentSquare, OccupiedSquare)
- Winning condition detection for both players
- Edge cases: out-of-bounds moves, already occupied squares

### Heuristics Package Tests (50 tests)
**MatrixTest.java** (14 tests)
- Matrix creation (square, rectangular, from arrays)
- Identity matrix generation
- Matrix operations (sum, scalar multiplication, matrix multiplication)
- Determinant calculation (2x2, 3x3)
- Linear system solving with Gauss elimination
- Matrix copy and deep copy verification
- Gauss pivot algorithm

**PathTest.java** (36 tests)
- Path creation and cell management
- Contains/add operations
- Union operations (disjoint and overlapping paths)
- Intersection operations  
- Empty intersection detection
- Direct path handling
- Path equality and newness tracking

## Running the Tests

### Compile Tests
```bash
javac -encoding UTF-8 \
  -cp "lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar:build/classes" \
  -d build/test \
  test/game/*.java test/heuristics/*.java
```

### Run All Tests
```bash
java -cp "lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar:build/classes:build/test" \
  org.junit.runner.JUnitCore \
  game.BoardTest \
  heuristics.MatrixTest \
  heuristics.PathTest
```

### Run Specific Test Class
```bash
java -cp "lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar:build/classes:build/test" \
  org.junit.runner.JUnitCore game.BoardTest
```

## Test Statistics
- **Total Tests**: 64
- **Test Files**: 3
- **Coverage**: Non-trivial methods with significant logic in game and heuristics packages

## Dependencies
- JUnit 4.13.2
- Hamcrest Core 1.3

Dependencies are located in the `lib/` directory.

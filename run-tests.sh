#!/bin/bash

echo "Compiling tests..."
javac -encoding UTF-8 \
  -cp "lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar:build/classes" \
  -d build/test \
  test/game/*.java test/heuristics/*.java

if [ $? -eq 0 ]; then
    echo "✓ Compilation successful!"
    echo ""
    echo "Running tests..."
    java -cp "lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar:build/classes:build/test" \
      org.junit.runner.JUnitCore \
      game.BoardTest \
      heuristics.MatrixTest \
      heuristics.PathTest \
      heuristics.HeuristicTest
else
    echo "✗ Compilation failed!"
    exit 1
fi

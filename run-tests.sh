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
    # Every *Test class is run. Discovering them keeps the suite from
    # quietly skipping a class someone forgot to list here — which is how
    # six failing tests once went unnoticed. Benchmarks and harnesses do
    # not end in "Test" and are therefore left out.
    TEST_CLASSES=$(cd test && find . -name '*Test.java' \
        | sed 's|^\./||; s|\.java$||; s|/|.|g' | sort)

    echo "Test classes:"
    echo "$TEST_CLASSES" | sed 's/^/  /'
    echo ""

    java -cp "lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar:build/classes:build/test" \
      org.junit.runner.JUnitCore $TEST_CLASSES
else
    echo "✗ Compilation failed!"
    exit 1
fi

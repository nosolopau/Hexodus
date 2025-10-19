#!/bin/bash

# Build and Run Script for Hexodus
# Compiles the project, generates JAR, and runs it

set -e  # Exit on any error

echo "======================================"
echo "  Hexodus Build and Run Script"
echo "======================================"
echo ""

# Step 1: Clean and create build directories
echo "[1/5] Cleaning build directories..."
rm -rf build/classes
mkdir -p build/classes
mkdir -p dist
echo "✓ Build directories ready"
echo ""

# Step 2: Compile Java source files
echo "[2/5] Compiling Java source files..."
javac -encoding UTF-8 -d build/classes $(find src -name "*.java")
if [ $? -eq 0 ]; then
    echo "✓ Compilation successful"
else
    echo "✗ Compilation failed"
    exit 1
fi
echo ""

# Step 3: Copy resources (images)
echo "[3/5] Copying resources..."
if [ -d "src/images" ]; then
    mkdir -p build/classes/images
    cp src/images/*.png build/classes/images/
    echo "✓ Images copied to build/classes/images/"
fi
echo ""

# Step 4: Create JAR file
echo "[4/5] Creating JAR file..."
cd build/classes
jar cfm ../../dist/Hexodus.jar ../../manifest.mf $(find . -name "*.class" -o -name "*.png")
cd ../..
echo "✓ JAR file created: dist/Hexodus.jar"
echo ""

# Step 5: Run the JAR file
echo "[5/5] Running Hexodus..."
echo "======================================"
echo ""
java -jar dist/Hexodus.jar

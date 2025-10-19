#!/bin/bash

# Build Script for Hexodus
# Compiles the project and generates JAR (without running)

set -e  # Exit on any error

echo "======================================"
echo "  Hexodus Build Script"
echo "======================================"
echo ""

# Step 1: Clean and create build directories
echo "[1/4] Cleaning build directories..."
rm -rf build/classes
mkdir -p build/classes
mkdir -p dist
echo "✓ Build directories ready"
echo ""

# Step 2: Compile Java source files
echo "[2/4] Compiling Java source files..."
javac -encoding UTF-8 -d build/classes $(find src -name "*.java")
if [ $? -eq 0 ]; then
    echo "✓ Compilation successful"
else
    echo "✗ Compilation failed"
    exit 1
fi
echo ""

# Step 3: Copy resources (images)
echo "[3/4] Copying resources..."
if [ -d "src/images" ]; then
    mkdir -p build/classes/images
    cp src/images/*.png build/classes/images/
    echo "✓ Images copied to build/classes/images/"
fi
echo ""

# Step 4: Create JAR file
echo "[4/4] Creating JAR file..."
cd build/classes
jar cfm ../../dist/Hexodus.jar ../../manifest.mf $(find . -name "*.class" -o -name "*.png")
cd ../..
echo "✓ JAR file created: dist/Hexodus.jar"
echo ""

echo "======================================"
echo "  Build Complete!"
echo "======================================"
echo ""
echo "To run the application:"
echo "  java -jar dist/Hexodus.jar"
echo ""
echo "Or use:"
echo "  ./build-and-run.sh"
echo ""

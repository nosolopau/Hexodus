# Hexodus

A Java implementation of the Hexodus board game with AI using heuristic algorithms.

## Documentation

A comprehensive technical report is available in the `docs/` directory in [Spanish](docs/Technical_Report_ES.md) (original) and [English](docs/Technical_Report_EN.md) (translated version).

## Prerequisites

- Java Development Kit (JDK) 8 or higher
- Apache Ant (optional, for using the build.xml)

## Project Structure

```
Hexodus/
├── src/              # Source code
│   ├── game/         # Game logic
│   ├── heuristics/   # AI heuristic algorithms
│   ├── ui/           # User interface
│   └── images/       # Image resources
├── build/            # Compiled classes (generated)
├── dist/             # Distribution JAR files (generated)
├── build.xml         # Ant build configuration
└── manifest.mf       # JAR manifest file
```

## How to Compile the Project

### Method 1: Using javac (Manual Compilation)

1. Clean previous builds (optional):
   ```bash
   rm -rf build/classes
   mkdir -p build/classes
   ```

2. Compile all Java source files:
   ```bash
   javac -encoding UTF-8 -d build/classes -sourcepath src $(find src -name "*.java")
   ```

   **Note:** The `-encoding UTF-8` flag is recommended for consistency.

### Method 2: Using Apache Ant

If you have Ant installed, you can use the provided build configuration:

1. Install Apache Ant (if not already installed):
   - macOS: `brew install ant`
   - Linux: `sudo apt-get install ant` or `sudo yum install ant`
   - Windows: Download from https://ant.apache.org/

2. Run the Ant build:
   ```bash
   ant compile
   ```

## How to Generate a JAR File

### Method 1: Using jar command (Manual)

1. First, compile the project (see above)

2. Copy resources to the build directory:
   ```bash
   cp -r src/images build/classes/
   ```

3. Create the distribution directory:
   ```bash
   mkdir -p dist
   ```

4. Generate the JAR file:
   ```bash
   jar cvfm dist/Hexodus.jar manifest.mf -C build/classes .
   ```

   The JAR file will be created at `dist/Hexodus.jar`

   **Note:** The manifest.mf file must contain the Main-Class attribute pointing to `ui.Main`

### Method 2: Using Apache Ant

If you have Ant installed:

```bash
ant jar
```

This will compile the project and create the JAR file in one step.

## How to Run the Application

After generating the JAR file, run the application with:

```bash
java -jar dist/Hexodus.jar
```

Or if you want to run from the compiled classes without creating a JAR:

```bash
java -cp build/classes ui.Main
```

## Quick Build Script

For convenience, you can create a build script. Save this as `build.sh`:

```bash
#!/bin/bash
# Clean and create directories
rm -rf build/classes dist
mkdir -p build/classes dist

# Compile
echo "Compiling..."
javac -encoding UTF-8 -d build/classes -sourcepath src $(find src -name "*.java")

if [ $? -eq 0 ]; then
    echo "Compilation successful!"

    # Copy resources
    echo "Copying resources..."
    cp -r src/images build/classes/

    # Create JAR
    echo "Creating JAR file..."
    jar cvfm dist/Hexodus.jar manifest.mf -C build/classes .

    if [ $? -eq 0 ]; then
        echo "Build complete! JAR file created at dist/Hexodus.jar"
    else
        echo "JAR creation failed!"
        exit 1
    fi
    echo "Run with: java -jar dist/Hexodus.jar"
else
    echo "Compilation failed!"
    exit 1
fi
```

Make it executable:
```bash
chmod +x build.sh
```

Run it:
```bash
./build.sh
```

## Troubleshooting

### Encoding Errors
The source files are encoded in UTF-8. If you encounter encoding errors, ensure you use the `-encoding UTF-8` flag.

### Missing Resources
If images don't appear when running the application, make sure the `images` directory was copied to the build/classes directory before creating the JAR.

### Java Version
The project is configured for Java 1.5 compatibility but should work with any modern Java version (8+).

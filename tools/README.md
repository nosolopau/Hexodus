# Asset generators

The board artwork and stone icons in `src/images/` are generated, not
hand-drawn. These tools reproduce them exactly.

## Boards

`BoardStyles.java` draws a board for any dimension in a given style. The
shipped artwork uses `modernframe`:

```bash
javac -d /tmp tools/BoardStyles.java
java -cp /tmp BoardStyles 7 modernframe src/images/7.png
```

Styles: `classic` (the original 2007 look), `bands`, `soft`, `modern`,
`modernsharp`, `modernink`, `modernframe`.

Geometry is fixed across every size — pointy-top hexagons of radius
61/sqrt(3), horizontal pitch 61, vertical pitch 52.5, rows shifted 30.5
left, 20px margin for the border frame. `GameWindow.newGame` derives the
window layout from the same numbers, so a new size lines up automatically:
add it to `Main.DIMENSIONS` and generate the matching `<dim>.png`.

`BoardGen.java` is the earlier generator that reproduces the original
artwork's geometry; kept for reference.

## Stones

`StoneGen.java` writes `red.png`, `blue.png` and `sug.png` — transparent,
40x40, coloured as deeper members of the board palette:

```bash
javac -d /tmp tools/StoneGen.java && java -cp /tmp StoneGen src/images
```

Transparency matters: the original icons had an opaque white background,
which shows as a white box on the non-white board.

## Screenshot

`ScreenshotTool.java` regenerates the README screenshot. It builds a real
`GameWindow`, plays a short opening through the actual cell buttons (so
the engine runs and the analysis overlay is genuine), then paints the
window into a PNG — no screen-capture permission needed, and the image
can never drift from the current interface:

```bash
javac -cp build/classes -d /tmp tools/ScreenshotTool.java
java -cp build/classes:/tmp ui.ScreenshotTool 7 docs/images/screenshot.png
```

It declares `package ui` because `GameWindow` is package-private.

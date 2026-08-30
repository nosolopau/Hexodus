package ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Renders a screenshot of the real interface for the README.
 *
 * The window paints itself into an image, so this needs no screen-capture
 * permission and always matches the current code: it builds an actual
 * GameWindow, plays a scripted opening through the real cell buttons (so
 * the engine really runs and the analysis overlay is genuine), then paints
 * the root pane to a PNG.
 *
 * Lives in package ui because GameWindow is package-private.
 *
 * Usage: java ui.ScreenshotTool <dimension> <out.png>
 */
public class ScreenshotTool {

    public static void main(String[] args) throws Exception {
        final int dim = args.length > 0 ? Integer.parseInt(args[0]) : 7;
        final String out = args.length > 1 ? args[1] : "docs/images/screenshot.png";

        Theme.apply();
        heuristics.OptConfig.USE_BITPATH = true;
        heuristics.Analysis.ENABLED = true;      // show the engine's reasoning

        final GameWindow[] holder = new GameWindow[1];
        SwingUtilities.invokeAndWait(new Runnable(){
            public void run(){
                // human (vertical) vs computer (horizontal), no swap, Normal
                holder[0] = new GameWindow(dim, 0, 1, false, 1);
            }
        });
        GameWindow window = holder[0];
        JButton[][] cells = buttons(window);

        // A short opening; each click runs the engine for the reply
        int[][] moves = {{3,3}, {3,4}, {2,4}, {4,2}};
        for (int[] m : moves) {
            click(cells[m[1]][m[0]]);
            Thread.sleep(400);
        }

        Thread.sleep(600);   // let the last repaint settle
        write(window, out);
        System.out.println("wrote " + out);
        System.exit(0);
    }

    /** The board buttons are private; the screenshot needs to drive them. */
    private static JButton[][] buttons(GameWindow window) throws Exception {
        Field f = GameWindow.class.getDeclaredField("b");
        f.setAccessible(true);
        return (JButton[][]) f.get(window);
    }

    private static void click(final JButton b) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable(){
            public void run(){ b.doClick(); }
        });
    }

    /** Paints the root pane (menu bar, status bar and board) into a PNG. */
    private static void write(final GameWindow window, final String out) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable(){
            public void run(){
                try {
                    JRootPane root = window.getRootPane();
                    int w = root.getWidth(), h = root.getHeight();
                    BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g = img.createGraphics();
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                    root.paint(g);
                    g.dispose();

                    File file = new File(out);
                    if (file.getParentFile() != null) file.getParentFile().mkdirs();
                    ImageIO.write(img, "png", file);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}

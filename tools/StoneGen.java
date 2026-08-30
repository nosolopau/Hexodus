import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.imageio.ImageIO;
import java.io.File;

/**
 * Generates the stone icons with real transparency, for use on the
 * framed modern board (whose cells are not white, so the old icons'
 * baked-in white background would show as a square behind every stone).
 *
 * Colours are deeper members of the board's own palette: the same hues as
 * the red/blue border frame (#C0705F / #5B7FA6), darkened for contrast
 * against the light tiles so the position stays readable at a glance.
 *
 * Usage: java StoneGen <outDir>
 */
public class StoneGen {

    static final int SIZE = 40;        // icon box; fits the 50x45 cell button
    static final double DIA = 37.0;    // stone diameter

    static final Color RED  = new Color(0x8C, 0x3B, 0x2E);
    static final Color BLUE = new Color(0x33, 0x54, 0x7A);
    static final Color SUG  = new Color(0x9A, 0x93, 0x88);

    public static void main(String[] args) throws Exception {
        String dir = args[0];
        write(dir + "/red.png",  stone(RED,  false));
        write(dir + "/blue.png", stone(BLUE, false));
        write(dir + "/sug.png",  stone(SUG,  true));
        System.out.println("wrote red.png, blue.png, sug.png (" + SIZE + "x" + SIZE + ", transparent)");
    }

    /** A flat stone. When ghost is true it is drawn as a translucent
     *  outline instead, for the suggested-move marker. */
    static BufferedImage stone(Color c, boolean ghost) {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        double off = (SIZE - DIA) / 2.0;
        Shape circle = new Ellipse2D.Double(off, off, DIA, DIA);

        if (ghost) {
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 40));
            g.fill(circle);
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 150));
            g.setStroke(new BasicStroke(2.0f));
            g.draw(new Ellipse2D.Double(off + 1, off + 1, DIA - 2, DIA - 2));
        } else {
            g.setColor(c);
            g.fill(circle);
        }

        g.dispose();
        return img;
    }

    static void write(String path, BufferedImage img) throws Exception {
        ImageIO.write(img, "png", new File(path));
    }
}

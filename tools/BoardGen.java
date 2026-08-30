import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.imageio.ImageIO;
import java.io.File;

/**
 * Generates a Hexodus board background image for an arbitrary dimension,
 * matching the geometry and palette of the original 5/6/7 artwork.
 *
 * Geometry (derived from the existing art and the UI layout constants):
 *   pointy-top hexagons, R = 61/sqrt(3) ~ 35.22
 *   horizontal pitch 61, vertical pitch 52.5, row shift -30.5
 *
 * Usage: java BoardGen <dim> <out.png>
 */
public class BoardGen {

    static final double PITCH_X = 61.0;
    static final double PITCH_Y = 52.5;
    static final double SHIFT   = 30.5;
    static final double R       = PITCH_X / Math.sqrt(3.0);  // 35.22
    static final double MARGIN  = 6.5;

    static final Color OUTLINE = new Color(0x32, 0x32, 0x32);
    static final Color RED     = new Color(0xCC, 0x00, 0x00);
    static final Color BLUE    = new Color(0x1A, 0x45, 0xA1);

    static final double INSET     = 0.8;   // gap between neighboring cells
    static final double CORNER    = 9.0;   // corner rounding radius
    static final double BORDER_OFF = 5.0;  // how far the colored edge sits outside

    static int n;
    static double originX, originY;

    public static void main(String[] args) throws Exception {
        n = Integer.parseInt(args[0]);
        String out = args[1];

        int w = (int)Math.round(91.5 * (n - 1) + PITCH_X + 2 * MARGIN);
        int h = (int)Math.round(PITCH_Y * (n - 1) + 2 * R + 2 * MARGIN);

        // Layout x ranges from -SHIFT*(n-1) to PITCH_X*(n-1); shift into the image
        originX = SHIFT * (n - 1) + SHIFT + MARGIN;
        originY = R + MARGIN;

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setComposite(AlphaComposite.Src);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.setComposite(AlphaComposite.SrcOver);

        // Cells
        g.setColor(OUTLINE);
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int j = 0; j < n; j++)
            for (int i = 0; i < n; i++)
                g.draw(hexagon(cx(i, j), cy(j), R - INSET));

        // Coloured borders
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(RED);
        g.draw(topBorder());
        g.draw(bottomBorder());
        g.setColor(BLUE);
        g.draw(leftBorder());
        g.draw(rightBorder());

        g.dispose();
        ImageIO.write(img, "png", new File(out));
        System.out.println("wrote " + out + " (" + w + "x" + h + ")  R=" + String.format("%.2f", R));
        System.out.println("suggested UI: hOff=" + Math.round(0.0) + " (computed by caller), vOff=68");
    }

    static double cx(int i, int j) { return originX + PITCH_X * i - SHIFT * j; }
    static double cy(int j)        { return originY + PITCH_Y * j; }

    /** Vertex k (0=top, clockwise) of a pointy-top hexagon */
    static Point2D v(double cx, double cy, double r, int k) {
        double ang = Math.toRadians(-90 + 60 * k);
        return new Point2D.Double(cx + r * Math.cos(ang), cy + r * Math.sin(ang));
    }

    /** Hexagon path with rounded corners */
    static Shape hexagon(double cx, double cy, double r) {
        Point2D[] p = new Point2D[6];
        for (int k = 0; k < 6; k++) p[k] = v(cx, cy, r, k);

        Path2D path = new Path2D.Double();
        for (int k = 0; k < 6; k++) {
            Point2D cur = p[k], prev = p[(k + 5) % 6], next = p[(k + 1) % 6];
            Point2D a = along(cur, prev, CORNER);
            Point2D b = along(cur, next, CORNER);
            if (k == 0) path.moveTo(a.getX(), a.getY());
            else path.lineTo(a.getX(), a.getY());
            path.quadTo(cur.getX(), cur.getY(), b.getX(), b.getY());
        }
        path.closePath();
        return path;
    }

    /** Point at distance d from 'from' toward 'to' */
    static Point2D along(Point2D from, Point2D to, double d) {
        double dx = to.getX() - from.getX(), dy = to.getY() - from.getY();
        double len = Math.hypot(dx, dy);
        return new Point2D.Double(from.getX() + dx / len * d, from.getY() + dy / len * d);
    }

    /** Offset polyline with rounded corners, matching the rounding of the
     *  hexagon outlines the border runs alongside. */
    static Path2D polyline(java.util.List<Point2D> pts, double ox, double oy) {
        java.util.List<Point2D> q = new java.util.ArrayList<>();
        for (Point2D p : pts) q.add(new Point2D.Double(p.getX() + ox, p.getY() + oy));

        Path2D path = new Path2D.Double();
        path.moveTo(q.get(0).getX(), q.get(0).getY());
        for (int k = 1; k < q.size() - 1; k++) {
            Point2D cur = q.get(k), prev = q.get(k - 1), next = q.get(k + 1);
            double back = Math.min(CORNER, dist(cur, prev) / 2);
            double fwd  = Math.min(CORNER, dist(cur, next) / 2);
            Point2D a = along(cur, prev, back);
            Point2D b = along(cur, next, fwd);
            path.lineTo(a.getX(), a.getY());
            path.quadTo(cur.getX(), cur.getY(), b.getX(), b.getY());
        }
        Point2D last = q.get(q.size() - 1);
        path.lineTo(last.getX(), last.getY());
        return path;
    }

    static double dist(Point2D a, Point2D b) {
        return Math.hypot(a.getX() - b.getX(), a.getY() - b.getY());
    }

    /** Zigzag over the top edges of row 0 */
    static Shape topBorder() {
        java.util.List<Point2D> pts = new java.util.ArrayList<>();
        for (int i = 0; i < n; i++) {
            double x = cx(i, 0), y = cy(0);
            pts.add(v(x, y, R, 5));   // upper-left
            pts.add(v(x, y, R, 0));   // top
        }
        pts.add(v(cx(n - 1, 0), cy(0), R, 1));  // final upper-right
        return polyline(pts, 0, -BORDER_OFF);
    }

    /** Zigzag under the bottom edges of the last row */
    static Shape bottomBorder() {
        java.util.List<Point2D> pts = new java.util.ArrayList<>();
        int j = n - 1;
        for (int i = 0; i < n; i++) {
            double x = cx(i, j), y = cy(j);
            pts.add(v(x, y, R, 4));   // lower-left
            pts.add(v(x, y, R, 3));   // bottom
        }
        pts.add(v(cx(n - 1, j), cy(j), R, 2));  // final lower-right
        return polyline(pts, 0, BORDER_OFF);
    }

    /** Staircase down the left side of column 0 */
    static Shape leftBorder() {
        java.util.List<Point2D> pts = new java.util.ArrayList<>();
        for (int j = 0; j < n; j++) {
            double x = cx(0, j), y = cy(j);
            pts.add(v(x, y, R, 5));   // upper-left
            pts.add(v(x, y, R, 4));   // lower-left
        }
        return polyline(pts, -BORDER_OFF, 0);
    }

    /** Staircase down the right side of the last column */
    static Shape rightBorder() {
        java.util.List<Point2D> pts = new java.util.ArrayList<>();
        int i = n - 1;
        for (int j = 0; j < n; j++) {
            double x = cx(i, j), y = cy(j);
            pts.add(v(x, y, R, 1));   // upper-right
            pts.add(v(x, y, R, 2));   // lower-right
        }
        return polyline(pts, BORDER_OFF, 0);
    }
}

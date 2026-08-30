import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Alternative visual designs for the Hexodus board, on the same cell
 * geometry as the original art (pointy-top hexagons, pitch 61 x 52.5),
 * so any of them can drop into the existing UI layout unchanged.
 *
 * Usage: java BoardStyles <dim> <style> <out.png>
 *   styles: classic | bands | soft | dark
 */
public class BoardStyles {

    static final double PITCH_X = 61.0, PITCH_Y = 52.5, SHIFT = 30.5;
    static final double R = PITCH_X / Math.sqrt(3.0);

    static int n;
    static double originX, originY, margin;
    static String style;

    public static void main(String[] args) throws Exception {
        n = Integer.parseInt(args[0]);
        style = args[1];
        String out = args[2];

        margin = style.equals("classic") ? 6.5 : 20.0;
        int w = (int)Math.round(91.5 * (n - 1) + PITCH_X + 2 * margin);
        int h = (int)Math.round(PITCH_Y * (n - 1) + 2 * R + 2 * margin);
        originX = SHIFT * (n - 1) + SHIFT + margin;
        originY = R + margin;

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        switch (style) {
            case "bands":   drawBands(g, w, h);   break;
            case "soft":    drawSoft(g, w, h);    break;
            case "dark":    drawDark(g, w, h);    break;
            case "modernframe": drawModernFrame(g, w, h, 3.0); break;
            case "modern":      drawModern(g, w, h, 3.0, false); break;
            case "modernsharp": drawModern(g, w, h, 0.0, false); break;
            case "modernink":   drawModern(g, w, h, 3.0, true);  break;
            default:        drawClassic(g, w, h); break;
        }

        g.dispose();
        ImageIO.write(img, "png", new File(out));
        System.out.println("wrote " + out + " (" + w + "x" + h + ") style=" + style);
    }

    /* ---------------- styles ---------------- */

    /** Original look: outlined cells, thin coloured edge lines. */
    static void drawClassic(Graphics2D g, int w, int h) {
        fill(g, w, h, Color.WHITE);
        g.setColor(new Color(0x323232));
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        cells(g, 0.8, 9.0, null, new Color(0x323232), 1.6f);
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        edges(g, 5.0, new Color(0xCC0000), new Color(0x1A45A1), 2.2f, 9.0);
    }

    /** Tournament look: solid coloured bands own each side, cells sit on top. */
    static void drawBands(Graphics2D g, int w, int h) {
        fill(g, w, h, Color.WHITE);
        edges(g, 11.0, new Color(0xC62828), new Color(0x1A45A1), 19f, 12.0);
        cells(g, 1.0, 8.0, new Color(0xFCFCFC), new Color(0x5A6270), 1.5f);
    }

    /** Soft modern: filled tiles, no hard grid, muted rounded edge bars. */
    static void drawSoft(Graphics2D g, int w, int h) {
        fill(g, w, h, new Color(0xF7F5F1));
        edges(g, 10.0, new Color(0xC0705F), new Color(0x5B7FA6), 13f, 14.0);
        cells(g, 2.6, 13.0, new Color(0xE7E2DA), null, 0f);
    }

    /** Modern flat, but with real hexagons: crisp corners, filled tiles,
     *  edges following the hex zigzag rather than rounded into ropes.
     *  @param corner corner radius (0 = perfectly sharp hexagons)
     *  @param ink    add a thin outline for extra cell definition */
    static void drawModern(Graphics2D g, int w, int h, double corner, boolean ink) {
        fill(g, w, h, new Color(0xF7F5F1));
        edges(g, 9.0, new Color(0xC0705F), new Color(0x5B7FA6), 11f, corner);
        cells(g, 2.2, corner, new Color(0xE7E2DA), ink ? new Color(0xCFC7BB) : null, 1.2f);
    }

    /** Modern flat with a true mitered border frame: the four coloured
     *  sides are cut out of one continuous ring, so red and blue meet on
     *  an exact diagonal at each corner instead of butting into each
     *  other with overlapping stroke caps. */
    static void drawModernFrame(Graphics2D g, int w, int h, double corner) {
        fill(g, w, h, new Color(0xF7F5F1));
        frame(g, 4.0, 11.0, corner, new Color(0xC0705F), new Color(0x5B7FA6));
        cells(g, 2.2, corner, new Color(0xE7E2DA), null, 0f);
    }

    /** Draws the border as a ring around the board silhouette, split into
     *  four sides by miter lines running from the board centre through
     *  each corner vertex.
     *  @param gap   distance between the cells and the ring
     *  @param band  ring thickness
     *  @param corner cell corner radius used for the silhouette */
    static void frame(Graphics2D g, double gap, double band, double corner, Color red, Color blue) {
        Area sil = new Area();
        for (int j = 0; j < n; j++)
            for (int i = 0; i < n; i++)
                sil.add(new Area(hexagon(cx(i, j), cy(j), R, corner)));

        Area ring = outset(sil, gap + band);
        ring.subtract(outset(sil, gap));

        // The four silhouette corners, in order around the board
        Point2D[] corners = {
            v(cx(0, 0), cy(0), R, 5),                  // top-left
            v(cx(n - 1, 0), cy(0), R, 1),              // top-right
            v(cx(n - 1, n - 1), cy(n - 1), R, 2),      // bottom-right
            v(cx(0, n - 1), cy(n - 1), R, 4),          // bottom-left
        };
        Rectangle2D b = sil.getBounds2D();
        Point2D mid = new Point2D.Double(b.getCenterX(), b.getCenterY());

        // Sides between consecutive corners: top, right, bottom, left
        Color[] sideColor = {red, blue, red, blue};
        for (int k = 0; k < 4; k++) {
            Area piece = new Area(ring);
            piece.intersect(new Area(wedge(mid, corners[k], corners[(k + 1) % 4])));
            g.setColor(sideColor[k]);
            g.fill(piece);
        }
    }

    /** Grows an area outward by d, keeping corners rounded. */
    static Area outset(Area a, double d) {
        Area r = new Area(a);
        r.add(new Area(new BasicStroke((float)(2 * d), BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND).createStrokedShape(a)));
        return r;
    }

    /** Triangle from the centre through two corners, extended well past
     *  the ring so the intersection covers that whole side. */
    static Shape wedge(Point2D mid, Point2D a, Point2D b) {
        Path2D p = new Path2D.Double();
        p.moveTo(mid.getX(), mid.getY());
        Point2D fa = extend(mid, a, 6.0), fb = extend(mid, b, 6.0);
        p.lineTo(fa.getX(), fa.getY());
        p.lineTo(fb.getX(), fb.getY());
        p.closePath();
        return p;
    }

    static Point2D extend(Point2D from, Point2D through, double factor) {
        return new Point2D.Double(from.getX() + (through.getX() - from.getX()) * factor,
                                  from.getY() + (through.getY() - from.getY()) * factor);
    }

    /** Dark: deep background, subtle cells, saturated edges. */
    static void drawDark(Graphics2D g, int w, int h) {
        fill(g, w, h, new Color(0x1E2228));
        edges(g, 11.0, new Color(0xD1495B), new Color(0x3E7CB1), 17f, 12.0);
        cells(g, 1.6, 10.0, new Color(0x2B313A), new Color(0x424B57), 1.4f);
    }

    /* ---------------- primitives ---------------- */

    static void fill(Graphics2D g, int w, int h, Color c) {
        g.setComposite(AlphaComposite.Src);
        g.setColor(c);
        g.fillRect(0, 0, w, h);
        g.setComposite(AlphaComposite.SrcOver);
    }

    static void cells(Graphics2D g, double inset, double corner, Color fill, Color stroke, float sw) {
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
                Shape hex = hexagon(cx(i, j), cy(j), R - inset, corner);
                if (fill != null) { g.setColor(fill); g.fill(hex); }
                if (stroke != null) {
                    g.setColor(stroke);
                    g.setStroke(new BasicStroke(sw, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.draw(hex);
                }
            }
        }
    }

    static void edges(Graphics2D g, double off, Color red, Color blue, float sw, double corner) {
        g.setStroke(new BasicStroke(sw, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(red);
        g.draw(border(true, false, off, corner));
        g.draw(border(true, true, off, corner));
        g.setColor(blue);
        g.draw(border(false, false, off, corner));
        g.draw(border(false, true, off, corner));
        cornerJoins(g, off, red, blue);
    }

    /** Closes the four corners where a red edge meets a blue one. Each
     *  colour runs to the diagonal corner point, giving the split-corner
     *  look of a physical Hex board. */
    static void cornerJoins(Graphics2D g, double off, Color red, Color blue) {
        // {shared vertex, red offset sign y, blue offset sign x}
        Point2D[] p = {
            v(cx(0, 0), cy(0), R, 5),                 // top-left
            v(cx(n - 1, 0), cy(0), R, 1),             // top-right
            v(cx(0, n - 1), cy(n - 1), R, 4),         // bottom-left
            v(cx(n - 1, n - 1), cy(n - 1), R, 2),     // bottom-right
        };
        int[] sy = {-1, -1, 1, 1};
        int[] sx = {-1, 1, -1, 1};

        for (int k = 0; k < 4; k++) {
            double x = p[k].getX(), y = p[k].getY();
            double cxr = x + sx[k] * off, cyr = y + sy[k] * off;   // diagonal corner
            g.setColor(red);
            g.draw(new Line2D.Double(x, y + sy[k] * off, cxr, cyr));
            g.setColor(blue);
            g.draw(new Line2D.Double(x + sx[k] * off, y, cxr, cyr));
        }
    }

    /** horizontal=true -> top/bottom (red); far=true -> the bottom/right side */
    static Shape border(boolean horizontal, boolean far, double off, double corner) {
        List<Point2D> pts = new ArrayList<>();
        if (horizontal) {
            int j = far ? n - 1 : 0;
            int a = far ? 4 : 5, b = far ? 3 : 0, c = far ? 2 : 1;
            for (int i = 0; i < n; i++) {
                pts.add(v(cx(i, j), cy(j), R, a));
                pts.add(v(cx(i, j), cy(j), R, b));
            }
            pts.add(v(cx(n - 1, j), cy(j), R, c));
            return rounded(pts, 0, far ? off : -off, corner);
        } else {
            int i = far ? n - 1 : 0;
            int a = far ? 1 : 5, b = far ? 2 : 4;
            for (int j = 0; j < n; j++) {
                pts.add(v(cx(i, j), cy(j), R, a));
                pts.add(v(cx(i, j), cy(j), R, b));
            }
            return rounded(pts, far ? off : -off, 0, corner);
        }
    }

    static double cx(int i, int j) { return originX + PITCH_X * i - SHIFT * j; }
    static double cy(int j)        { return originY + PITCH_Y * j; }

    static Point2D v(double cx, double cy, double r, int k) {
        double ang = Math.toRadians(-90 + 60 * k);
        return new Point2D.Double(cx + r * Math.cos(ang), cy + r * Math.sin(ang));
    }

    static Shape hexagon(double cx, double cy, double r, double corner) {
        Point2D[] p = new Point2D[6];
        for (int k = 0; k < 6; k++) p[k] = v(cx, cy, r, k);
        Path2D path = new Path2D.Double();
        for (int k = 0; k < 6; k++) {
            Point2D cur = p[k], prev = p[(k + 5) % 6], next = p[(k + 1) % 6];
            Point2D a = along(cur, prev, corner), b = along(cur, next, corner);
            if (k == 0) path.moveTo(a.getX(), a.getY()); else path.lineTo(a.getX(), a.getY());
            path.quadTo(cur.getX(), cur.getY(), b.getX(), b.getY());
        }
        path.closePath();
        return path;
    }

    static Point2D along(Point2D from, Point2D to, double d) {
        double dx = to.getX() - from.getX(), dy = to.getY() - from.getY();
        double len = Math.hypot(dx, dy);
        return new Point2D.Double(from.getX() + dx / len * d, from.getY() + dy / len * d);
    }

    static Path2D rounded(List<Point2D> pts, double ox, double oy, double corner) {
        List<Point2D> q = new ArrayList<>();
        for (Point2D p : pts) q.add(new Point2D.Double(p.getX() + ox, p.getY() + oy));
        Path2D path = new Path2D.Double();
        path.moveTo(q.get(0).getX(), q.get(0).getY());
        for (int k = 1; k < q.size() - 1; k++) {
            Point2D cur = q.get(k), prev = q.get(k - 1), next = q.get(k + 1);
            double back = Math.min(corner, dist(cur, prev) / 2), fwd = Math.min(corner, dist(cur, next) / 2);
            Point2D a = along(cur, prev, back), b = along(cur, next, fwd);
            path.lineTo(a.getX(), a.getY());
            path.quadTo(cur.getX(), cur.getY(), b.getX(), b.getY());
        }
        Point2D last = q.get(q.size() - 1);
        path.lineTo(last.getX(), last.getY());
        return path;
    }

    static double dist(Point2D a, Point2D b) { return Math.hypot(a.getX() - b.getX(), a.getY() - b.getY()); }
}

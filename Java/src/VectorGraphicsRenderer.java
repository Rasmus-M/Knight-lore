import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: Rasmus
 * Date: 29-12-2014
 * Time: 22:54
 */
public class VectorGraphicsRenderer {

    private VDPCanvas vdpCanvas;

    public VectorGraphicsRenderer(VDPCanvas vdpCanvas) {
        this.vdpCanvas = vdpCanvas;
    }

    public void plot(int x, int y, int color) {
        vdpCanvas.plot(x, y, color);
    }

    public void fatPlot(int x, int y, int color) {
        vdpCanvas.fatPlot(x, y, color);
    }

    public void drawLine(int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int sx = x1 < x2 ? 1 : -1;
        int dy = Math.abs(y2 - y1);
        int sy = y1 < y2 ? 1 : -1;
        int err = (dx > dy ? dx : -dy) / 2;
        int e2;
        boolean done = false;
        while(!done) {
            vdpCanvas.plot(x1, y1, color);
            if (x1 == x2 && y1 == y2) {
                done = true;
            }
            else {
                e2 = err;
                if (e2 > -dx) {
                    err -= dy;
                    x1 += sx;
                }
                if (e2 < dy) {
                    err += dx;
                    y1 += sy;
                }
            }
        }
    }

    public void drawHorizontalLine(int y, int left, int right, int color) {
        for (int x = left; x <= right; x++) {
            vdpCanvas.plot(x, y, color);
        }
    }

    public void drawVerticalLine(int x, int top, int bottom, int color) {
        for (int y = top; y <= bottom; y++) {
            vdpCanvas.plot(x, y, color);
        }
    }

    public void drawFatVerticalLine(int x, int top, int bottom, int color) {
        for (int y = top; y <= bottom; y++) {
            vdpCanvas.fatPlot(x, y, color);
        }
    }

    public void drawVerticalMLine(int x, int top, int bottom, int color) {
        for (int y = top; y <= bottom; y++) {
            vdpCanvas.mplot(x, y, color);
        }
    }

    public void drawRectangle(int x, int y, int w, int h, int color) {
        drawHorizontalLine(y, x, x + w - 1, color);
        drawHorizontalLine(y + h - 1, x, x + w - 1, color);
        drawVerticalLine(x, y, y + h - 1, color);
        drawVerticalLine(x + w - 1, y, y + h - 1, color);
    }


    public void drawPolygon(Point[] vertices, int color) {
        Edge[] edges = new Edge[vertices.length + 1];
        int n = 0;
        for (int i = 0; i < vertices.length; i++) {
            Point p1 = vertices[i];
            int i2 = i + 1;
            if (i2 == vertices.length) {
                i2 = 0;
            }
            Point p2 = vertices[i2];
            // Skip horizontal edges
            if (p1.y != p2.y) {
                // Swap vertices if p1 is at the bottom
                if (p1.y > p2.y) {
                    Point p3 = p1;
                    p1 = p2;
                    p2 = p3;
                }
                edges[n++] = new Edge(p1.x, p1.y, p2.x, p2.y);
            }
        }
        edges[n] = null;
        if (n > 1) {
            // Bubble sort edges according to y
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (edges[i].y1 > edges[j].y1) {
                        Edge tmp = edges[i];
                        edges[i] = edges[j];
                        edges[j] = tmp;
                    }
                }
            }
            int e = 0;
            Edge edge1 = edges[e++];
            Edge edge2 = edges[e++];
            int y = edges[0].y1;
            int yMax = edges[n - 1].y2;
            while (true) {
                // Draw scanline
                int x1 = edge1.x;
                int x2 = edge2.x;
                if (x1 > x2) {
                    int tmp = x1;
                    x1 = x2;
                    x2 = tmp;
                }
                for (int x = x1; x <= x2; x++) {
                    plot(x, y, color);
                }
                // Update edge1
                if (y >= edge1.y2 && y != yMax) {
                    edge1 = edges[e++];
                    if (edge1 == null) {
                        break;
                    }
                }
                else {
                    edge1.err -= edge1.dx;
                    while (edge1.err <= 0) {
                        edge1.x += edge1.sx;
                        edge1.err += edge1.dy;
                    }

                }
                // Update edge2
                if (y >= edge2.y2 && y != yMax) {
                    edge2 = edges[e++];
                    if (edge2 == null) {
                        break;
                    }
                }
                else {
                    edge2.err -= edge2.dx;
                    while (edge2.err <= 0) {
                        edge2.x += edge2.sx;
                        edge2.err += edge2.dy;
                    }

                }
                // Next scanline
                y++;
            }
        }
    }

    private class Edge {

        int x;
        int y1;
        int y2;
        int dx;
        int dy;
        int sx;
        int err;

        private Edge(int x1, int y1, int x2, int y2) {
            this.x = x1;
            this.y1 = y1;
            this.y2 = y2;
            this.dx = Math.abs(x2 - x1);
            this.sx = x1 < x2 ? 1 : -1;
            this.dy = Math.abs(y2 - y1);
            this.err = this.dy / 2;
        }
    }
}

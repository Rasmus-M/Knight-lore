import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

/**
 * Created with IntelliJ IDEA.
 * User: Rasmus
 * Date: 29-12-2014
 * Time: 21:02
 */
public class VDPCanvas extends Canvas {

    public static final int WIDTH = 256;
    public static final int HEIGHT = 192;

    public static final int MWIDTH = 64;
    public static final int MHEIGHT = 48;

    public static final int CWIDTH = 32;
    public static final int CHEIGHT = 24;

    public static int TRANSPARENT = 0;
    public static int BLACK = 1;
    public static int MEDIUM_GREEN = 2;
    public static int LIGHT_GREEN = 3;
    public static int DARK_BLUE = 4;
    public static int MEDIUM_BLUE = 5;
    public static int DARK_RED = 6;
    public static int CYAN = 7;
    public static int MEDIUM_RED = 8;
    public static int LIGHT_RED = 9;
    public static int DARK_YELLOW = 10;
    public static int MEDIUM_YELLOW = 11;
    public static int DARK_GREEN = 12;
    public static int MAGENTA = 13;
    public static int GREY = 14;
    public static int WHITE = 15;


    public static final Color[] PALETTE = {
        new Color(0,0,0),
        new Color(0,0,0),
        new Color(33,200,66),
        new Color(94,220,120),
        new Color(84,85,237),
        new Color(125,118,252),
        new Color(212,82,77),
        new Color(66,235,245),
        new Color(252,85,84),
        new Color(255,121,120),
        new Color(212,193,84),
        new Color(230,206,128),
        new Color(33,176,59),
        new Color(201,91,186),
        new Color(204,204,204),
        new Color(255,255,255)
    };

    private BufferStrategy strategy;
    private Graphics2D g;
    private int scale;
    private int backColor;
    private int mscale;
    private int[][] buffer;

    public VDPCanvas(int scale, int backColor) {
        this.scale = scale;
        this.backColor = backColor;
        setBounds(0, 0, WIDTH * scale, HEIGHT * scale);
        mscale = 4 * scale;
    }

    public void init() {
        createBufferStrategy(2);
        strategy = getBufferStrategy();
        buffer = new int[HEIGHT][WIDTH];
    }

    public void startFrame() {
        g = (Graphics2D) strategy.getDrawGraphics();
    }

    public void endFrame() {
        g.dispose();
        strategy.show();
    }

    public void clear() {
        g.setColor(PALETTE[backColor]);
        g.fillRect(0, 0, WIDTH * scale, HEIGHT * scale);
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                buffer[y][x] = backColor;
            }
        }
    }

    public void drawGrid() {
        g.setColor(Color.GRAY);
        for (int y = 0; y < HEIGHT; y += 8) {
            for (int x = 0; x < WIDTH; x += 8) {
                g.drawRect(x * scale, y * scale, 8 * scale, 8 * scale);
            }
        }
    }

    public void setBackColor(int backColor) {
        this.backColor = backColor;
    }

    public void plot(int x, int y, int color) {
        g.setColor(color != TRANSPARENT ? PALETTE[color] : PALETTE[backColor]);
        g.fillRect(x * scale, y * scale, scale, scale);
        if (x >= 0 && y >= 0 && x < WIDTH && y < HEIGHT) {
            buffer[y][x] = color;
        }
    }

    public void fatPlot(int x, int y, int color) {
        g.setColor(color != TRANSPARENT ? PALETTE[color] : PALETTE[backColor]);
        int x1 = x << 1;
        g.fillRect(x1 * scale, y * scale, scale << 1, scale);
        if (x1 >= 0 && y >= 0 && x1 < WIDTH && y < HEIGHT) {
            buffer[y][x1] = color;
            buffer[y][x1 + 1] = color;
        }
    }

    public void mplot(int x, int y, int color) {
        g.setColor(color != TRANSPARENT ? PALETTE[color] : PALETTE[backColor]);
        g.fillRect(x * mscale, y * mscale, mscale, mscale);
        if (x >= 0 && y >= 0 && x < WIDTH - 1 && y < HEIGHT - 1) {
            buffer[y][x] = color;
            buffer[y][x + 1] = color;
            buffer[y + 1][x] = color;
            buffer[y + 1][x + 1] = color;
        }
    }

    public void sprite(int x, int y, String patternStr, int color) {
        int[] pattern = new int[16];
        for (int i = 0; i < 16; i++) {
            pattern[i] = Integer.parseInt(patternStr.substring(i << 2, (i << 2) + 4), 16);
        }
        sprite(x, y, pattern, color);
    }

    public void sprite(int x, int y, int[] pattern, int color) {
        int pat = 0, mask = 0;
        for (int col = 0; col < 2; col++) {
            for (int row = 0; row < 16; row++) {
                if ((row & 1) == 0) {
                    pat = pattern[(col << 3) + (row >> 1)];
                    mask = 0x8000;
                }
                for (int p = 0; p < 8; p++) {
                    if ((pat & mask) != 0) {
                        plot(x + p + (col << 3), y + row, color);
                    }
                    mask >>= 1;
                }
            }
        }
    }

    public void character(int col, int row, int[] pattern, int color) {
        pixelCharacter(col << 3, row << 3, pattern, color, false);
    }

    public void character(int col, int row, int[] pattern, int[] color) {
        pixelCharacter(col << 3, row << 3, pattern, color, false);
    }

    public void pixelCharacter(int x, int y, int[] pattern, int color, boolean splash) {
        int[] colors = new int[8];
        for (int n = 0; n < 8; n++) {
            colors[n] = color;
        }
        pixelCharacter(x, y, pattern, colors, splash);
    }

    // Draw character at pixel position
    public void pixelCharacter(int x, int y, int[] pattern, int[] color, boolean splash) {
        int pat = 0, mask = 0;
        for (int row = 0; row < 8; row++) {
            int foreColor = (color[row] & 0xF0) >> 4;
            int backColor = color[row] & 0x0F;
            pat = pattern[row];
            mask = 0x80;
            for (int p = 0; p < 8; p++) {
                plot(x + p, y + row, (pat & mask) != 0 ? foreColor : backColor);
                mask >>= 1;
            }
            if (splash) {
                int y1 = y + row;
                for (int x1 = (x & 0xF8); x1 < ((x & 0xF8) + ((x & 7) == 0 ? 8 : 16)); x1++) {
                    if (x1 >= 0 && y1 >= 0 && x1 < WIDTH && y1 < HEIGHT && buffer[y1][x1] != 0 && buffer[y1][x1] != foreColor) {
                        plot(x1, y1, foreColor);
                    }
                }
            }
        }
    }

    public byte[] getBitmapPatternTable() {
        byte[] table = new byte[(WIDTH * HEIGHT) / 8];
        int i = 0;
        for (int row = 0; row < HEIGHT / 8; row++) {
            int y0 = row * 8;
            for (int col = 0; col < WIDTH / 8; col++) {
                int x0 = col * 8;
                for (int y1 = 0; y1 < 8; y1++) {
                    int y = y0 + y1;
                    byte b = 0;
                    for (int x1 = 0; x1 < 8; x1++) {
                        if (x1 != 0) {
                            b <<= 1;
                        }
                        if (buffer[y][x0 + x1] != backColor) {
                            b |= 1;
                        }
                    }
                    table[i++] = b;
                }
            }
        }
        return table;
    }

    public BufferedImage getImage() {
        byte[] r = new byte[16];
        byte[] g = new byte[16];
        byte[] b = new byte[16];
        for (int i = 0; i < 16; i++) {
            r[i] = (byte) PALETTE[i].getRed();
            g[i] = (byte) PALETTE[i].getGreen();
            b[i] = (byte) PALETTE[i].getBlue();
        }
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_BYTE_INDEXED, new IndexColorModel(4, 16, r, g, b));
        WritableRaster raster = image.getRaster();
        int[] pixel = new int[1];
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                pixel[0] = buffer[y][x];
                raster.setPixel(x, y, pixel);
            }
        }
        return image;
    }
}

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Rasmus
 * Date: 03-06-2017
 * Time: 13:50
 */

public class KnightLore extends VDPFrame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new KnightLore(args));
    }

    private final static int BLOCK_TYPE_DATA_ADDR = 0x6c0b;
    private final static int BACKGROUND_TYPE_DATA_ADDR = 0x6d12;
    private final static int SPRITE_DATA_ADDR = 0x728a;

    private final int[] palette = {1,4,6,13,12,7,10,14,1,5,8,13,2,7,11,15};

    private byte[] roomSizes;
    private byte[] locations;
    private byte[] blockTypes;
    private byte[] blockTypeData;
    private byte[] backgroundTypes;
    private byte[] backgroundTypeData;
    private byte[] objects;
    private byte[] sprites;
    private byte[] spriteData;

    private int roomNo = 0;

    private KnightLore(String[] args) {
        super(args);
        try {
            loadBinaries();
            // Shuffle objects
            for (int i = 0; i < 32; i++) {
                objects[i * 9] = (byte) (0x60 + (int) Math.floor(Math.random() * 8));
            }
        } catch (IOException e) {
            e.printStackTrace();
            setRunning(false);
        }
    }

    protected void drawFrame(VectorGraphicsRenderer renderer, int frameNo) {
        drawLocation(roomNo & 0xFF);
        setPaused(true);
    }

    private void drawLocation(int locNo) {
        System.out.println("***********************************");
        System.out.println("Location " + locNo);
        int locationAddr = 0;
        int locationNo = getByte(locations, locationAddr);
        int locationSize = getByte(locations, locationAddr + 1);
        while (locationNo != locNo && locationAddr < locations.length - 2) {
            locationAddr += locationSize + 1;
            if (locationAddr < locations.length - 2) {
                locationNo = getByte(locations, locationAddr);
                locationSize = getByte(locations, locationAddr + 1);
                // System.out.println("Location: " + locationNo);
            }
        }
        if (locationNo == locNo) {
            System.out.println("Found, size=" + locationSize);
            locationAddr += 2;
            int locationBytesLeft = locationSize - 1;
            int roomSizeAndColor = getByte(locations, locationAddr++);
            locationBytesLeft--;
            int roomSize = (roomSizeAndColor & 0xF8) >> 3;
            int roomX = getByte(roomSizes, roomSize * 3);
            int roomY = getByte(roomSizes, roomSize * 3 + 1);
            int roomZ = getByte(roomSizes, roomSize * 3 + 2);
            System.out.println("Size: " + roomSize + " (" + roomX + ", " + roomY + ", " + roomZ + ")");
            int attr = roomSizeAndColor & 0x07;
            System.out.println("Color attr: " + attr);
            // Background types
            System.out.println("--- Background types ---");
            int backgroundType = getByte(locations, locationAddr++);
            locationBytesLeft--;
            while (backgroundType != 0xFF && locationBytesLeft > 0) {
                System.out.println("Background type: " + backgroundType);
                if (backgroundType >= 0) {
                    drawBackgroundType(backgroundType, palette[attr]);
                }
                backgroundType = getByte(locations, locationAddr++);
                locationBytesLeft--;
            }
            // Block types
            System.out.println("--- Block types ---");
            while (locationBytesLeft > 0) {
                int blockCount = getByte(locations, locationAddr++);
                locationBytesLeft--;
                int blockType = (blockCount & 0xF8) >> 3;
                System.out.println("Block type: " + blockType);
                int count = (blockCount & 0x07) + 1;
                System.out.println("Count: " + count);
                for (int i = 0; i < count; i++) {
                    int pos = getByte(locations, locationAddr++);
                    locationBytesLeft--;
                    int x = (pos & 0x07) * 16;
                    int y = ((pos & 0x38) >> 3) * 16;
                    int z = ((pos & 0xC0) >> 6) * 12;
                    drawBlockType(blockType, x, y, z, roomZ, palette[attr]);
                }
            }
            // Objects
            System.out.println("--- Objects ---");
            for (int i = 0; i < 32; i++) {
                int objectAddr = i * 9;
                if (getByte(objects, objectAddr + 4) == locationNo) {
                    int spriteNo = getByte(objects, objectAddr);
                    System.out.println("Object " + i + ", sprite No: " + spriteNo);
                    int x = getByte(objects, objectAddr + 1 );
                    int y = getByte(objects, objectAddr + 2 );
                    int z = getByte(objects, objectAddr + 3 );
                    System.out.println("Position = (" + x + ", " + y +  ", " + z + ")");
                    Point2D point = projectIso(new Point3D(x, y, z));
                    System.out.println("Projected = (" + point.getX() + ", " + point.getY() + ")");
                    drawSprite(spriteNo, point.getX(), point.getY(), false, false, 15);
                }
            }
        } else {
            System.out.println("Not found.");
        }
        System.out.println();
    }

    private void drawBackgroundType(int backgroundType, int color) {
        int dataAddr = getWord(backgroundTypes, backgroundType * 2);
        System.out.println("Addr: " + Integer.toHexString(dataAddr));
        dataAddr -= BACKGROUND_TYPE_DATA_ADDR;
        int spriteNo = getByte(backgroundTypeData, dataAddr++);
        while (spriteNo != 0) {
            System.out.println("Sprite no: " + spriteNo);
            int x = getByte(backgroundTypeData, dataAddr++);
            int y = getByte(backgroundTypeData, dataAddr++);
            int z = getByte(backgroundTypeData, dataAddr++);
            System.out.println("Position = (" + x + ", " + y +  ", " + z + ")");
            int widthX = getByte(backgroundTypeData, dataAddr++);
            int depthY = getByte(backgroundTypeData, dataAddr++);
            int heightZ = getByte(backgroundTypeData, dataAddr++);
            System.out.println("Size = (" + widthX + ", " + depthY +  ", " + heightZ + ")");
            int attr = getByte(backgroundTypeData, dataAddr++);
            boolean flipV = (attr & 0x80) != 0;
            boolean flipH = (attr & 0x40) != 0;
            System.out.println("FlipV: " + flipV + " FlipH: " + flipH);
            Point2D point = projectIso(new Point3D(x, y, z));
            System.out.println("Projected = (" + point.getX() + ", " + point.getY() + ")");
            drawSprite(spriteNo, point.getX(), point.getY(), flipV, flipH, color);
            spriteNo = getByte(backgroundTypeData, dataAddr++);
        }
    }

    private void drawBlockType(int blockType, int x, int y, int z, int roomZ, int color) {
        int dataAddr = getWord(blockTypes, blockType * 2);
        System.out.println("Addr: " + Integer.toHexString(dataAddr));
        dataAddr -= BLOCK_TYPE_DATA_ADDR;
        int spriteNo = getByte(blockTypeData, dataAddr++);
        while (spriteNo != 0) {
            System.out.println("Sprite no: " + spriteNo);
            int widthX = getByte(blockTypeData, dataAddr++);
            int depthY = getByte(blockTypeData, dataAddr++);
            int heightZ = getByte(blockTypeData, dataAddr++);
            System.out.println("Size = (" + widthX + ", " + depthY + ", " + heightZ + ")");
            int attr = getByte(blockTypeData, dataAddr++);
            boolean flipV = (attr & 0x80) != 0;
            boolean flipH = (attr & 0x40) != 0;
            System.out.println("FlipV: " + flipV + " FlipH: " + flipH);
            int position = getByte(blockTypeData, dataAddr++);
            int x1 = (position & 0x01) * 8;
            int y1 = ((position & 0x02) >> 1) * 8;
            int z1 = ((position & 0xFC) >> 2) * 4;
            System.out.println("Position = (" + x1 + ", " + y1 +  ", " + z1 + ")");
            Point2D point = projectIso(new Point3D(x + x1 + 72, y + y1 + 72, z + z1 + roomZ));
            System.out.println("Projected = (" + point.getX() + ", " + point.getY() + ")");
            drawSprite(spriteNo, point.getX(), point.getY(), flipV, flipH, color);
            spriteNo = getByte(blockTypeData, dataAddr++);
        }
    }

    private void drawSprite(int spriteNo, int x0, int y0, boolean flipV, boolean flipH, int color) {
        Point2D adjustment = calculatePixelAdjustment(spriteNo, flipV, flipH);
        x0 += adjustment.getX();
        y0 += adjustment.getY();
        int dataAddr = getWord(sprites, spriteNo * 2) - SPRITE_DATA_ADDR;
        int width = getByte(spriteData, dataAddr++);
        int height = getByte(spriteData, dataAddr++);
        VDPCanvas canvas = this.vdpCanvas;
        // System.out.println(width + " x " + height);
        for (int y = 0; y < height; y++) {
            int y1 = flipV ? y0 + height - y - 1 : y0 + y;
            for (int x = 0; x < width; x++) {
                int x1 = x0 + (x << 3); //  - (width << 3);
                if (flipH) {
                    int bit = 0x01;
                    int msk = getByte(spriteData, dataAddr + (width - 1 - x) * 2);
                    int img = getByte(spriteData, dataAddr + (width - 1 - x) * 2 + 1);
                    for (int b = 0; b < 8; b++) {
                        if ((msk & bit) != 0) {
                            canvas.plot(x1, 191 - y1, 1);
                        }
                        if ((img & bit) != 0) {
                            canvas.plot(x1, 191 - y1, color);
                        }
                        x1++;
                        bit <<= 1;
                    }
                } else {
                    int bit = 0x80;
                    int msk = getByte(spriteData, dataAddr + x * 2);
                    int img = getByte(spriteData, dataAddr + x * 2 + 1);
                    for (int b = 0; b < 8; b++) {
                        if ((msk & bit) != 0) {
                            canvas.plot(x1, 191 - y1, 1);
                        }
                        if ((img & bit) != 0) {
                            canvas.plot(x1, 191 - y1, color);
                        }
                        x1++;
                        bit >>= 1;
                    }
                }
            }
            dataAddr += width * 2;
        }
    }

    private Point2D calculatePixelAdjustment(int spriteNo, boolean flipV, boolean flipH) {
        Point2D adjustment = new Point2D(0 ,0);
        switch (spriteNo) {
            case 2:
            case 4:
                if (!flipH) {
                    if (spriteNo == 2) {
                        // Stone arch
                        adjustment.setX(-1); // -3
                        adjustment.setY(-10); // -7
                    } else {
                        // Tree arch
                        adjustment.setX(7); // -3
                        adjustment.setY(-10); // 1
                    }
                } else {
                    // Stone arch + tree arch
                    adjustment.setX(-12); // -2
                    adjustment.setY(-8); // -17
                }
                break;
            case 3:
            case 5:
                // Stone arch + tree arch
                if (!flipH) {
                    adjustment.setX(-3);
                    adjustment.setY(-9);
                } else {
                    adjustment.setX(-2);
                    adjustment.setY(-7);
                }
                break;
        }
        return adjustment;
    }

    void drawHelpLines(VectorGraphicsRenderer renderer) {
        for (int x = 0; x < 256; x += 8) {
            Point2D point = projectIso(new Point3D(x, 0, 128));
            renderer.plot(point.getX(), point.getY(), 8);
        }
        for (int y = 0; y < 256; y += 8) {
            Point2D point = projectIso(new Point3D(0, y, 128));
            renderer.plot(point.getX(), point.getY(), 10);
        }
        for (int z = 128; z < 256; z += 8) {
            Point2D point = projectIso(new Point3D(0, 0, z));
            renderer.plot(point.getX(), point.getY(), 12);
        }

        for (int x = 0; x < 256; x += 8) {
            Point2D point = projectIso(new Point3D(x, 196, 128));
            renderer.plot(point.getX(), point.getY(), 9);
        }
        for (int y = 0; y < 256; y += 8) {
            Point2D point = projectIso(new Point3D(196, y, 128));
            renderer.plot(point.getX(), point.getY(), 14);
        }
    }

    /*

    z
    |      x
    |    /
    |  /
    |/
     \
      \
       \
        y
     y
     |
     |
     |
     |
     ------------ x

     */

    Point2D projectIso(Point3D p) {
        return new Point2D(
            p.getX() + p.getY() - 128 - 8,
            ((p.getY() - p.getX() + 128) >> 1) + p.getZ() - 104 - 32
        );
    }

    private int getByte(byte[] bytes, int offset) {
        return bytes[offset] & 0xFF;
    }

    private int getWord(byte[] bytes, int offset) {
        return (bytes[offset] & 0xFF) + ((bytes[offset + 1] & 0xFF) << 8);
    }

    private void loadBinaries() throws IOException {
        roomSizes = loadFile("room-sizes.bin");
        locations = loadFile("locations.bin");
        blockTypes = loadFile("block-types.bin");
        blockTypeData = loadFile("block-type-data.bin");
        backgroundTypes = loadFile("background-types.bin");
        backgroundTypeData = loadFile("background-type-data.bin");
        objects = loadFile("objects.bin");
        sprites = loadFile("sprites.bin");
        spriteData = loadFile("sprite-data.bin");
    }

    private byte[] loadFile(String fileName) throws IOException {
        byte[] buffer = new byte[0x4000];
        FileInputStream fis = new FileInputStream(fileName);
        int len = fis.read(buffer);
        fis.close();
        byte[] file = new byte[len];
        System.arraycopy(buffer, 0, file, 0, len);
        return file;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        roomNo++;
        setPaused(false);
    }
}

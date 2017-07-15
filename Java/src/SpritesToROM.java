import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Rasmus on 11-07-2017.
 */
public class SpritesToROM implements Runnable {

    private final static int SPRITE_DATA_ADDR = 0x728a;
    private final static int START_BANK = 0;
    private final static int BANK_OFFSET = 4;
    private final static int BANK_SIZE = 0x2000;
    private final static int HEADER_SIZE = 0x120;
    private final static int ROM_SIZE = 0x40000 - 0x8000;

    public static void main(String[] args) {
        try {
            new SpritesToROM().run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] sprites;
    private byte[] spriteData;
    private int indexAddr;
    private int dataAddr;
    private byte[] rom;

    public SpritesToROM() throws IOException {
        sprites = loadFile("sprites.bin");
        spriteData = loadFile("sprite-data.bin");
    }

    public void run() {
        System.out.println(mirrorByte((byte) 1) & 0xff);
        rom = new byte[ROM_SIZE];
        indexAddr = START_BANK * BANK_SIZE + HEADER_SIZE;
        dataAddr = (START_BANK + 1) * BANK_SIZE + HEADER_SIZE;
        for (int spriteNo = 0; spriteNo < 188; spriteNo++) {
            writeSprite(getSprite(spriteNo, false, false));
            writeSprite(getSprite(spriteNo, false, true));
            writeSprite(getSprite(spriteNo, true, false));
            writeSprite(getSprite(spriteNo, true, true));
        }
        try {
            saveFile("sprite-rom.bin", rom);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeSprite(byte[] spriteData) {
        System.out.println("Index addr=" + indexAddr);
        System.out.println("Data addr=" + dataAddr);
        int size = spriteData.length;
        int bankBytesLeft = BANK_SIZE - (dataAddr % BANK_SIZE);
        if (bankBytesLeft < size) {
            dataAddr += bankBytesLeft + HEADER_SIZE;
            System.out.println("Datataa=" + Integer.toHexString(dataAddr));
        }
        int bank = dataAddr / BANK_SIZE;
        int offset = dataAddr % BANK_SIZE;
        indexAddr += writeWord(rom, indexAddr, (bank + BANK_OFFSET) * 2 + 0x6000);
        indexAddr += writeWord(rom, indexAddr, offset + 0x6000);
        dataAddr += writeBytes(rom, dataAddr, spriteData);
    }

    private byte[] getSprite(int spriteNo, boolean flipV, boolean flipH) {
        System.out.println("Sprite No: " + spriteNo);
        int srcAddr = getWord(sprites, spriteNo * 2) - SPRITE_DATA_ADDR;
        int width = getByte(spriteData, srcAddr++);
        if ((width & 0x80) != 0) {
            flipV = !flipV;
            width = width & 0x07;
        }
        int height = getByte(spriteData, srcAddr++);
        System.out.println("Width: " + width + ", Height: " + height);
        byte[] sprite = new byte[2 + 2 * width * height];
        int dstAddr = 0;
        dstAddr += writeByte(sprite, dstAddr, width);
        dstAddr += writeByte(sprite, dstAddr, height);
        for (int y = 0; y < height; y++) {
            int lineAddr = srcAddr + 2 * (flipV ? (height - y - 1) : y) * width;
            if (!flipH) {
                for (int x = 0; x < width; x++) {
                    sprite[dstAddr++] = spriteData[lineAddr + 2 * x];
                    sprite[dstAddr++] = spriteData[lineAddr + 2 * x + 1];
                }
            }
            else {
                for (int x = width - 1; x >= 0; x--) {
                    sprite[dstAddr++] = mirrorByte(spriteData[lineAddr + 2 * x]);
                    sprite[dstAddr++] = mirrorByte(spriteData[lineAddr + 2 * x + 1]);
                }
            }
        }
        if (spriteNo == 2) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    System.out.print(hexByte(sprite[2 + y * width * 2 + x * 2]));
                }
                System.out.println();
            }
        }
        return sprite;
    }

    private String hexByte(byte b) {
        String s = Integer.toHexString(b & 0xff);
        if (s.length() == 1) {
            s = "0" + s;
        }
        return s;
    }

    private byte mirrorByte(byte b) {
        int m = 0;
        for (int bit = 0; bit < 8; bit++) {
            m <<= 1;
            if ((b & 1) != 0) {
                m |= 1;
            }
            b >>= 1;
        }
        return (byte) (m & 0xFF);
    }

    private int getByte(byte[] bytes, int offset) {
        return bytes[offset] & 0xFF;
    }

    private int writeByte(byte[] bytes, int offset, int b) {
        bytes[offset] = (byte) (b & 0xFF);
        return 1;
    }

    private int getWord(byte[] bytes, int offset) {
        return (bytes[offset] & 0xFF) + ((bytes[offset + 1] & 0xFF) << 8);
    }

    private int writeWord(byte[] bytes, int offset, int w) {
        bytes[offset] = (byte) ((w & 0xFF00) >> 8);
        bytes[offset + 1] = (byte) (w & 0x00FF);
        return 2;
    }

    private int writeBytes(byte[] dst, int offset, byte[] src) {
        System.arraycopy(src, 0, dst, offset, src.length);
        return src.length;
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

    private void saveFile(String fileName, byte[] bytes) throws IOException {
        FileOutputStream fos = new FileOutputStream(fileName);
        fos.write(bytes);
        fos.close();
    }
}

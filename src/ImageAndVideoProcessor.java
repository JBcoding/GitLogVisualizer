import org.ietf.jgss.Oid;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelGrabber;
import java.io.*;
import java.nio.file.Files;
import java.sql.Array;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Created by madsbjoern on 01/06/16.
 */
public final class ImageAndVideoProcessor {

    private static String hashtags = null;

    public static void addImage(BufferedImage image) throws IOException, InterruptedException {
        int h = image.getHeight();
        int w = image.getWidth();

        int missingBytes = 4096 - (w * h * 3 + 3 + 4 + 2 + Integer.toString(w).length() + Integer.toString(h).length()) % 4096;

        int index = 0;
        byte[] bytes = new byte[h * w * 3 + 2 + missingBytes + 1 + Integer.toString(w).length() + Integer.toString(h).length() + 1 + 1 + 4];

        index = appendToArray(bytes, index, "P6".getBytes());

        if (hashtags == null) {
            hashtags = "";
            for (int i = 0; i < missingBytes; i++) {
                hashtags += '#';
            }
        }
        index = appendToArray(bytes, index, hashtags.getBytes());
        index = appendToArray(bytes, index, "\n".getBytes());

        index = appendToArray(bytes, index, (image.getWidth() + " " + image.getHeight() + "\n").getBytes());
        index = appendToArray(bytes, index, "255\n".getBytes());

        long timeS = System.nanoTime();
        int[] pixels = (int[])image.getRaster().getDataElements(0, 0, image.getWidth(), image.getHeight(), null);

        for (int i = 0; i < h * w; i ++) {
            int  clr   = pixels[i];
            byte red   = (byte)((clr & 0x00ff0000) >> 16);
            byte green = (byte)((clr & 0x0000ff00) >> 8);
            byte blue  = (byte)(clr & 0x000000ff);
            int pos = index + i * 3;
            bytes[pos] = red;
            bytes[pos + 1] = green;
            bytes[pos + 2] = blue;
        }
        System.out.write(bytes);
    }

    public static int appendToArray(byte[] array, int index, byte[] newData) {
        System.arraycopy(newData, 0, array, index, newData.length);
        return index + newData.length;
    }
}

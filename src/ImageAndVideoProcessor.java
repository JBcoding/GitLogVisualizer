import org.ietf.jgss.Oid;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
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

    public static void addImage(BufferedImage image) throws IOException {
        int h = image.getHeight();
        int w = image.getWidth();

        System.out.write("P6".getBytes());

        if (hashtags == null) {
            hashtags = "";
            int missingBytes = 4096 - (w * h * 3 + 3 + 4 + 2 + Integer.toString(w).length() + Integer.toString(h).length()) % 4096;
            for (int i = 0; i < missingBytes; i++) {
                hashtags += '#';
            }
        }
        System.out.write(hashtags.getBytes());
        System.out.write("\n".getBytes());

        System.out.write((image.getWidth() + " " + image.getHeight() + "\n").getBytes());
        System.out.write("255\n".getBytes());

        byte[] pixels = new byte[w * 3];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int  clr   = image.getRGB(x, y);
                byte red   = (byte)((clr & 0x00ff0000) >> 16);
                byte green = (byte)((clr & 0x0000ff00) >> 8);
                byte blue  = (byte)(clr & 0x000000ff);
                pixels[x * 3] = red;
                pixels[x * 3 + 1] = green;
                pixels[x * 3 + 2] = blue;
            }
            System.out.write(pixels, 0, w * 3);
        }
    }
}

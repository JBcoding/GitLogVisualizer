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

        long timeS = System.nanoTime();
        int[] pixels = (int[])image.getRaster().getDataElements(0, 0, image.getWidth(), image.getHeight(), null);

        byte[] bytes = new byte[h * w * 3];
        for (int i = 0; i < h * w; i ++) {
            int  clr   = pixels[i];
            byte red   = (byte)((clr & 0x00ff0000) >> 16);
            byte green = (byte)((clr & 0x0000ff00) >> 8);
            byte blue  = (byte)(clr & 0x000000ff);
            bytes[i * 3] = red;
            bytes[i * 3 + 1] = green;
            bytes[i * 3 + 2] = blue;
        }
        //System.out.println(System.nanoTime() - timeS);
        System.out.write(bytes, 0, h * w * 3);
    }
}

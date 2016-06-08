import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.security.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by mathias on 08/06/16
 */
public final class AuthorImageRetriever {
    private static List<String> foundAuthors = new ArrayList<>();
    private static List<BufferedImage> authorImages = new ArrayList<>();
    public static BufferedImage getImage(Commit commit) {

        Pattern pattern = Pattern.compile("<(.*?)>");
        Matcher matcher = pattern.matcher(commit.getAuthor());
        final String baseUrl = "https://gravatar.com/avatar/";

        String url;
        if (matcher.find()) { //If commit contains email(s)
            String email = matcher.group(1);

            if (email.contains(",")) { //If multiple emails
                email = email.split(",")[0];
            }

            url = baseUrl + md5Hex(email) + "?d=mm";

        } else { //get default gravatar logo
            url = baseUrl + "default?d=mm&f=y";
        }


        int index = foundAuthors.indexOf(url);
        if (index != -1) return authorImages.get(index); //Return cached if already downloaded once

        BufferedImage userAvatar;
        try {
            userAvatar = cropImage(ImageIO.read(new URL(url)));
            foundAuthors.add(url);
            authorImages.add(userAvatar);
        } catch (IOException e) {
            userAvatar = new BufferedImage(80, 80, 0);
        }

        return userAvatar;
    }

    private static BufferedImage cropImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Point center = new Point(width / 2, height / 2);
        int radius = center.x; //Half of width, will have width as diameter

        final int transparentPixel = 0x00000000;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double distanceFromCenter = Math.sqrt(Math.pow(center.x - x, 2) + Math.pow(center.y - y, 2));
                if (distanceFromCenter <= radius - 1) {
                    newImage.setRGB(x, y, image.getRGB(x, y)); //Remove whatever transparency image may have
                } else if (distanceFromCenter <= radius) {
                    newImage.setRGB(x, y, image.getRGB(x, y) + 0x50000000);
                } else {
                    newImage.setRGB(x, y, transparentPixel);
                }
            }
        }

        return newImage;
    }

    private static String hex(byte[] array) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < array.length; ++i) {
            sb.append(Integer.toHexString((array[i]
                    & 0xFF) | 0x100).substring(1,3));
        }
        return sb.toString();
    }
    private static String md5Hex (String message) {
        try {
            MessageDigest md =
                    MessageDigest.getInstance("MD5");
            return hex (md.digest(message.getBytes("CP1252")));
        } catch (NoSuchAlgorithmException e) {
        } catch (UnsupportedEncodingException e) {
        }
        return null;
    }
}

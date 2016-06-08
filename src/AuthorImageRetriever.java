import javax.imageio.ImageIO;
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
        final String baseUrl = "http://gravatar.com/avatar/";

        String url;
        if (matcher.find()) { //If commit contains email(s)
            String email = matcher.group(1);
            url = baseUrl + md5Hex(email);

            if (url.contains(",")) { //If multiple emails
                url = url.split(",")[0];
            }

        } else { //get default gravatar logo
            url = baseUrl + "default";
        }


        int index = foundAuthors.indexOf(url);
        if (index != -1) return authorImages.get(index); //Return cached if already downloaded once

        BufferedImage userAvatar;
        try {
            userAvatar = ImageIO.read(new URL(url));
            foundAuthors.add(url);
            authorImages.add(userAvatar);
        } catch (IOException e) {
            userAvatar = new BufferedImage(80, 80, 0);
        }

        return userAvatar;
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

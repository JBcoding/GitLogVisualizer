import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mathias on 08/06/16
 */
public class LabelBox {
    private List<String> content = new ArrayList<>();
    private String title;
    private BufferedImage image;
    private int width = 0;
    private int height = 0;
    private int x, y;

    public LabelBox(String title, int x, int y) {
        this.title = title;
        this.x = x;
        this.y = y;
    }

    public void setContent(Graphics2D graphic, String ... labels) {
        boolean equal = false;
        if (labels.length == content.size()) {
            equal = true;
            for (int i = 0; i < labels.length; i++) {
                if (!labels[i].equals(content.get(i))) {
                    equal = false;
                    break;
                }
            }
        }
        if (!equal) {
            content.clear();
            content.addAll(Arrays.asList(labels));
            updateImage(graphic);
        }

        graphic.drawImage(image, x, y - height, null);
    }

    private void updateImage(Graphics2D graphic) {
        int titleWidth = graphic.getFontMetrics().stringWidth(title);
        int largestWidth = titleWidth + 20;
        int lineHeight = graphic.getFontMetrics().getHeight();
        int boxHeight =  lineHeight * (content.size() + 1) + (content.size() - 1) * 5 + 10;
        for (String s : content) {
            largestWidth = Math.max(largestWidth, graphic.getFontMetrics().stringWidth(s));
        }
        width = Math.max(largestWidth + 10, width);
        height = Math.max(boxHeight + 10, height);

        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = image.createGraphics();

        g.setColor(new Color(141, 148, 143));
        g.fillRect(0, 0, width, height);
        g.setColor(new Color(194, 194, 194));
        g.fillRect(3, 3, width - 6, height - 6);

        g.setColor(Color.BLACK);

        int offsetY = 10;
        int startX = width / 2 - titleWidth / 2;
        g.drawString(title, startX, offsetY + lineHeight);
        offsetY += 5 + lineHeight;

        for (int i = 0; i < content.size(); i++) {
            g.drawString(content.get(i), 5, offsetY + lineHeight);
            offsetY += 5 + lineHeight;
        }
    }
}

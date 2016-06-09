import java.awt.*;
import java.util.Map;

/**
 * Created by madsbjoern on 09/06/16.
 */
public class FadingString {
    private String text;
    private float timeBeforeFading;
    private float time;
    private float timeToFadeOut;
    private Vector2D location;
    private boolean scaleBeforeDraw;
    private Node node;

    public FadingString(String text, float timeBeforeFading, float timeToFadeOut, boolean scaleBeforeDraw, Vector2D location, Node node) {
        this.text = new String(text);
        this.timeBeforeFading = timeBeforeFading;
        this.timeToFadeOut = timeToFadeOut;
        this.scaleBeforeDraw = scaleBeforeDraw;
        this.location = location;
        this.node = node;
        time = 0;
    }

    public void update(float deltaTime) {
        time += deltaTime / 2;
    }

    public void draw(Graphics2D g, float scale, Vector2D offset, double angle, Vector2D rectCentrum) {
        Vector2D tempVec = location;
        if (scaleBeforeDraw) {
            if (node == null) {
                return;
            }
            tempVec = node.getPhysicsNode().getLocation().transform(rectCentrum, angle);
            tempVec = tempVec.scale(scale).translate(offset);
        }
        int x = (int) tempVec.x;
        int y = (int) tempVec.y;
        if (time < timeBeforeFading) {
            g.drawString(text, x, y);
        } else {
            float alpha = 1 - ((time - timeBeforeFading) / timeToFadeOut);
            alpha = Math.max(0, alpha);
            alpha = Math.min(1, alpha);
            Color oldColor = g.getColor();
            g.setColor(new Color(oldColor.getRed(), oldColor.getGreen(), oldColor.getBlue(), (int)(alpha * 255)));
            g.drawString(text, x, y);
            g.setColor(oldColor);
        }
    }

    public boolean cleanUpCheck() {
        if (time >= timeToFadeOut + timeBeforeFading) {
            location = null;
            text = null;
            if (node != null) {
                node.clearFadingString();
                node = null;
            }
            return true;
        }
        return false;
    }
}

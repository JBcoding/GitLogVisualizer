import java.awt.*;

/**
 * Created by madsbjoern on 08/06/16.
 */
public class Beam {
    private Vector2D location;
    private Node node;
    private float alpha;

    public Beam(Vector2D location, Node node) {
        this.location = location;
        this.node = node;
        alpha = 128;
    }

    public void draw(Graphics2D g, float scale, Vector2D offset, double angle, Vector2D rectCentrum) {
        if (node.getAlpha() <= 0) {
            alpha = 0;
            return;
        }
        Color oldColor = g.getColor();
        Color thisColor = node.getHashColor();
        g.setColor(new Color(thisColor.getRed(), thisColor.getGreen(), thisColor.getBlue(), (int)alpha));
        Vector2D tempCenter = node.getPhysicsNode().getLocation().transform(rectCentrum, angle);
        tempCenter = tempCenter.scale(scale).translate(offset);
        Vector2D locationDiff = new Vector2D(tempCenter.x - location.x, tempCenter.y - location.y);
        float distance = tempCenter.distance(location);
        Vector2D direction = new Vector2D(locationDiff.x / distance, locationDiff.y / distance);
        float radiusAfterScale = node.getPhysicsNode().getRadius() * scale;
        Vector2D topSpot = new Vector2D(-direction.y * radiusAfterScale + tempCenter.x, direction.x * radiusAfterScale + tempCenter.y);
        Vector2D bottomSpot = new Vector2D(direction.y * radiusAfterScale + tempCenter.x, -direction.x * radiusAfterScale + tempCenter.y);
        g.fillPolygon(new int[]{(int) location.x, (int) topSpot.x, (int) bottomSpot.x}, new int[]{(int) location.y, (int) topSpot.y, (int) bottomSpot.y}, 3);
        g.setColor(oldColor);
    }

    public void update(float deltaTime) {
        alpha -= deltaTime * 25;
        alpha = Math.max(alpha, 0);
    }

    public boolean cleanUpCheck() {
        if (alpha == 0) {
            node = null;
            location = null;
            return true;
        }
        return false;
    }
}

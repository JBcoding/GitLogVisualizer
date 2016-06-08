import com.sun.javafx.geom.Vec2f;

import java.awt.*;

/**
 * Created by madsbjoern on 08/06/16.
 */
public class Beam {
    private Vec2f location;
    private Node node;
    private float alpha;

    public Beam(Vec2f location, Node node) {
        this.location = location;
        this.node = node;
        alpha = 128;
    }

    public void draw(Graphics2D g, float scale, Vec2f offset, double angle, Vec2f rectCentrum) {
        if (node.getAlpha() <= 0) {
            alpha = 0;
            return;
        }
        Color oldColor = g.getColor();
        Color thisColor = node.getHashColor();
        g.setColor(new Color(thisColor.getRed(), thisColor.getGreen(), thisColor.getBlue(), (int)alpha));
        Vec2f tempCenter = Visualizer.rotateVecter(node.getPhysicsNode().getLocation(), angle, rectCentrum);
        tempCenter.x = tempCenter.x * scale + offset.x;
        tempCenter.y = tempCenter.y * scale + offset.y;
        Vec2f locationDiff = new Vec2f(tempCenter.x - location.x, tempCenter.y - location.y);
        float distance = tempCenter.distance(location);
        Vec2f direction = new Vec2f(locationDiff.x / distance, locationDiff.y / distance);
        float radiusAfterScale = node.getPhysicsNode().getRadius() * scale;
        Vec2f topSpot = new Vec2f(-direction.y * radiusAfterScale + tempCenter.x, direction.x * radiusAfterScale + tempCenter.y);
        Vec2f bottomSpot = new Vec2f(direction.y * radiusAfterScale + tempCenter.x, -direction.x * radiusAfterScale + tempCenter.y);
        g.fillPolygon(new int[]{(int) location.x, (int) topSpot.x, (int) bottomSpot.x}, new int[]{(int) location.y, (int) topSpot.y, (int) bottomSpot.y}, 3);
        g.setColor(oldColor);
    }

    public void update(float deltaTime) {
        alpha -= deltaTime * 25;
        alpha = Math.max(alpha, 0);
    }

    public boolean cleanUpCheck() {
        return (alpha == 0);
    }
}

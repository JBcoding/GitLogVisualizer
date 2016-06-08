import java.awt.*;

/**
 * Created by madsbjoern on 30/05/16.
 */
public class Spring {
    private PhysicsSpring physics;
    private boolean visible;
    private Node[] nodes;

    public Spring(Node node1, Node node2, float springConstant, float length, boolean visible) {
        this.visible = visible;
        this.physics = new PhysicsSpring(node1.getPhysicsNode(), node2.getPhysicsNode(), springConstant, length);
        node1.addSpring(this);
        node2.addSpring(this);
        nodes = new Node[] {node1, node2};
    }

    public void draw(Graphics2D g, float scale, Vector2D offset, double angle, Vector2D rectCentrum) {
        if (!visible) {
            return;
        }
        Vector2D tempVec1 = physics.getLocation(0).transform(rectCentrum, angle);
        Vector2D tempVec2 = physics.getLocation(1).transform(rectCentrum, angle);
        tempVec1 = tempVec1.scale(scale).translate(offset);
        tempVec2 = tempVec2.scale(scale).translate(offset);
        int x1 = (int)(tempVec1.x);
        int y1 = (int)(tempVec1.y);
        int x2 = (int)(tempVec2.x);
        int y2 = (int)(tempVec2.y);
        Color oldColor = g.getColor();
        g.setColor(new Color(oldColor.getRed(), oldColor.getGreen(), oldColor.getBlue(), (int)(Math.min(nodes[0].getAlpha(), nodes[1].getAlpha())*255)));
        g.drawLine(x1, y1, x2, y2);
        g.setColor(oldColor);
    }

    public void update() {
        physics.update();
    }
}

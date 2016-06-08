import com.sun.javafx.geom.Vec2f;

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

    public void draw(Graphics2D g, float scale, Vec2f offset, double angle, Vec2f rectCentrum) {
        if (!visible) {
            return;
        }
        Vec2f tempVec1 = Visualizer.rotateVecter(physics.getLocation(0), angle, rectCentrum);
        Vec2f tempVec2 = Visualizer.rotateVecter(physics.getLocation(1), angle, rectCentrum);
        int x1 = (int)(tempVec1.x * scale + offset.x);
        int y1 = (int)(tempVec1.y * scale + offset.y);
        int x2 = (int)(tempVec2.x * scale + offset.x);
        int y2 = (int)(tempVec2.y * scale + offset.y);
        Color oldColor = g.getColor();
        g.setColor(new Color(oldColor.getRed(), oldColor.getGreen(), oldColor.getBlue(), (int)(Math.min(nodes[0].getAlpha(), nodes[1].getAlpha()))*255));
        g.drawLine(x1, y1, x2, y2);
        g.setColor(oldColor);
    }

    public void update() {
        physics.update();
    }
}

import com.sun.javafx.geom.Vec2f;
import javafx.geometry.Bounds;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by madsbjoern on 30/05/16.
 */
public class Node {
    private PhysicsNode physics;
    public String name;
    public boolean folder;
    private List<Node> children;
    private List<Spring> springs;
    private Node parent;
    private float alpha;
    private List<Spring> allSprings;

    public Node(Vec2f location, float mass, List<Spring> allSprings) {
        physics = new PhysicsNode(location, mass);
        folder = false;
        name = "";
        children = new ArrayList<>();
        springs = new ArrayList<>();
        parent = null;
        alpha = 1.f;
        this.allSprings = allSprings;
    }

    public void draw(Graphics2D g, float scale, Vec2f offset) {
        int x = (int)(physics.getLocation().x * scale + offset.x);
        int y = (int)(physics.getLocation().y * scale + offset.y);
        Color oldColor = g.getColor();
        g.setColor(new Color(oldColor.getRed(), oldColor.getGreen(), oldColor.getBlue(), alpha));
        g.drawOval(x - 2, y - 2, 4, 4);
        g.setColor(new Color(1, 0, 0, alpha));
        g.drawString(name, x, y);
        g.setColor(oldColor);
        for (Node child : children) {
            child.draw(g, scale, offset);
        }
    }

    public PhysicsNode getPhysicsNode() {
        return physics;
    }

    public void doNodeRepulsion(List<PhysicsNode> nodes) {
        physics.doNodeRepulsion(nodes);
        for (Node n : children) {
            n.doNodeRepulsion(nodes);
        }
    }

    public void update(float time) {
        physics.update(time);
        for (Node child : children) {
            child.update(time);
        }
        if (alpha != 1) {
            float fadeOutTime = 2.5f; // in seconds
            alpha -= time / fadeOutTime;
        }
        for (int i = 0; i < children.size(); i ++) {
            int size = children.size();
            children.get(i).cleanUpCheck();
            if (size != children.size()) {
                i --;
            }
        }
    }

    private void cleanUpCheck() {
        if (alpha <= 0) {
            cleanUp();
        }
    }

    public void startFading() {
        alpha = .99f;
    }

    public float getAlpha() {
        return alpha;
    }

    public List<PhysicsNode> getPhysicsNodes() {
        List<PhysicsNode> physicsNodes = new ArrayList<>();
        physicsNodes.add(physics);

        for (Node n : children) {
            physicsNodes.addAll(n.getPhysicsNodes());
        }

        return physicsNodes;
    }

    public void addChild(Node child) {
        /*for (Node n : children) {
            Spring s = new Spring(child, n, 10, 100, false);
            springs.add(s);
        }*/
        children.add(child);
        child.setParent(this);
        Spring mainSpring = new Spring(this, child, 50, 50, true);
        allSprings.add(mainSpring);
        alpha = 1;
    }

    public void addSpring(Spring spring) {
        springs.add(spring);
    }

    public Node findChildWithName(String name) {
        for (Node child : children) {
            if (child.name.equals(name)) {
                return child;
            }
        }
        return null;
    }

    public void cleanUp() {
        for (Node n : children) {
            n.cleanUp();
        }
        for (Spring spring : springs) {
            allSprings.remove(spring);
        }
        parent.removeChild(this);
        springs.clear();
        children.clear();
    }

    public void removeChild(Node n) {
        children.remove(n);
        if (children.size() == 0) {
            startFading();
        }
    }

    public int[] getBoundingBox() {
        int x = (int)physics.getLocation().x;
        int y = (int)physics.getLocation().y;
        int[] rect = new int[] {x, y, x, y};
        getBoundingBox(rect);
        return rect;
    }

    private void getBoundingBox(int[] rect) {
        int x = (int)physics.getLocation().x;
        int y = (int)physics.getLocation().y;
        if (rect[0] > x) {rect[0] = x;}
        if (rect[1] > y) {rect[1] = y;}
        if (rect[2] < x) {rect[2] = x;}
        if (rect[3] < y) {rect[3] = y;}
        for (Node child : children) {
            child.getBoundingBox(rect);
        }
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }
}

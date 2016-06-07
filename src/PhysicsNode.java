import com.sun.javafx.geom.Vec2f;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by madsbjoern on 30/05/16.
 */
public class PhysicsNode {
    private Vec2f location;
    private Vec2f velocity;
    private Vec2f forces;
    private float mass;

    public PhysicsNode(Vec2f location, float mass) {
        this.location = location;
        this.mass = mass;
        velocity = new Vec2f(0.0f, 0.0f);
        forces = new Vec2f(0.0f, 0.0f);
    }

    public void ApplyForce(Vec2f force) {
        forces.x += force.x;
        forces.y += force.y;
    }

    public void doNodeRepulsion(List<PhysicsNode> nodes) {
        for (PhysicsNode n : nodes) {
            if (n == this) {
                continue;
            }
            Vec2f deltaPos = new Vec2f(n.getLocation().x - this.getLocation().x, n.getLocation().y - this.getLocation().y);
            float distanceBetweenNodes = this.getLocation().distance(n.getLocation());
            distanceBetweenNodes = Math.max(20, distanceBetweenNodes);
            Vec2f direction = new Vec2f(deltaPos.x / distanceBetweenNodes, deltaPos.y / distanceBetweenNodes);

            // F = k * m4 * m1 / d^2
            float k = 10000.f;
            Vec2f F = new Vec2f(direction.x * k * mass * n.mass / (float)Math.pow(distanceBetweenNodes, 2), direction.y * k * mass * n.mass / (float)Math.pow(distanceBetweenNodes, 2));
            n.ApplyForce(F);
        }
    }

    public void update(float time) {
        velocity.x += forces.x / mass * time;
        velocity.y += forces.y / mass * time;

        // resistance, so that nodes will settle
        velocity.x *= .90;
        velocity.y *= .90;

        forces.set(0, 0);

        location.x += velocity.x * time;
        location.y += velocity.y * time;
    }

    public Vec2f getLocation() {
        return location;
    }

    public float getMass() {
        return mass;
    }
}

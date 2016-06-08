import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by madsbjoern on 30/05/16.
 */
public class PhysicsNode {
    private Vector2D location;
    private Vector2D velocity;
    private Vector2D forces;
    private float mass;
    private float radius;
    private Node node;

    public PhysicsNode(Vector2D location, float mass, float radius, Node node) {
        this.location = location;
        this.mass = mass;
        this.radius = radius;
        this.node = node;
        velocity = new Vector2D(0.0f, 0.0f);
        forces = new Vector2D(0.0f, 0.0f);
    }

    public void ApplyForce(Vector2D force) {
        forces.x += force.x;
        forces.y += force.y;
    }

    public void doNodeRepulsion(List<PhysicsNode> nodes) {
        for (PhysicsNode n : nodes) {
            if (n == this) {
                continue;
            }
            Vector2D deltaPos = new Vector2D(n.getLocation().x - this.getLocation().x, n.getLocation().y - this.getLocation().y);
            float distanceBetweenNodes = this.getLocation().distance(n.getLocation());
            distanceBetweenNodes = Math.max(20, distanceBetweenNodes);
            Vector2D direction = new Vector2D(deltaPos.x / distanceBetweenNodes, deltaPos.y / distanceBetweenNodes);

            // F = k * m1 * m2 / d^2
            float k = 10000.f;
            Vector2D F = new Vector2D(direction.x * k * mass * n.mass / (float)Math.pow(distanceBetweenNodes, 2), direction.y * k * mass * n.mass / (float)Math.pow(distanceBetweenNodes, 2));
            n.ApplyForce(F);
        }
    }

    public void collisionDetection(List<PhysicsNode> nodes) {
        for (PhysicsNode n : nodes) {
            if (n == this) {
                continue;
            }
            // collision detection
            Vector2D deltaPos = new Vector2D(n.getLocation().x - this.getLocation().x, n.getLocation().y - this.getLocation().y);
            float distanceBetweenNodes = this.getLocation().distance(n.getLocation());
            Vector2D direction = new Vector2D(deltaPos.x / distanceBetweenNodes, deltaPos.y / distanceBetweenNodes);
            Vector2D reverseDirection = new Vector2D(-direction.x, -direction.y);
            if (n.radius + this.radius > distanceBetweenNodes) {
                float distanceToMove = n.radius + this.radius - distanceBetweenNodes;
                float totalMass = n.mass + this.mass;
                n.moveByVector(direction, (1 - n.mass / totalMass) * distanceToMove);
                this.moveByVector(reverseDirection, (1 - this.mass / totalMass) * distanceToMove);
            }
        }
    }

    private void moveByVector(Vector2D deltaLocation, float distance) {
        location = location.translate(deltaLocation.scale(distance));
    }

    public void update(float time) {
        velocity = velocity.translate(forces.scale(1 / mass).scale(time));

        // resistance, so that nodes will settle
        velocity = velocity.scale(.9f);

        forces = new Vector2D(0, 0);

        location = location.translate(velocity.scale(time));
    }

    public Vector2D getLocation() {
        return location;
    }

    public float getMass() {
        return mass;
    }

    public float getRadius() {
        return radius;
    }
}

import com.sun.javafx.geom.Vec2f;

/**
 * Created by madsbjoern on 30/05/16.
 */
public class PhysicsSpring {
    private PhysicsNode[] nodes;
    private float springConstant;
    private float length;

    public PhysicsSpring(PhysicsNode node1, PhysicsNode node2, float springConstant, float length) {
        this.springConstant = springConstant;
        this.length = length;
        this.nodes = new PhysicsNode[2];
        this.nodes[0] = node1;
        this.nodes[1] = node2;
    }

    public void update() {
        Vec2f deltaPos = new Vec2f(nodes[0].getLocation().x - nodes[1].getLocation().x, nodes[0].getLocation().y - nodes[1].getLocation().y);
        float distanceBetweenNodes = nodes[0].getLocation().distance(nodes[1].getLocation());
        Vec2f direction = new Vec2f(deltaPos.x / distanceBetweenNodes, deltaPos.y / distanceBetweenNodes);

        // F = -k z
        Vec2f z = new Vec2f(deltaPos.x - direction.x * length, deltaPos.y - direction.y * length);
        Vec2f F = new Vec2f(- springConstant * z.x, -springConstant * z.y);
        Vec2f negativeF = new Vec2f(-F.x, -F.y);
        nodes[0].ApplyForce(F);
        nodes[1].ApplyForce(negativeF);
    }

    public Vec2f getLocation(int nodeID) {
        return nodes[nodeID].getLocation();
    }
}

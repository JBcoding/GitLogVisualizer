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
        Vector2D deltaPos = new Vector2D(nodes[0].getLocation().x - nodes[1].getLocation().x, nodes[0].getLocation().y - nodes[1].getLocation().y);
        float distanceBetweenNodes = nodes[0].getLocation().distance(nodes[1].getLocation());
        Vector2D direction = new Vector2D(deltaPos.x / distanceBetweenNodes, deltaPos.y / distanceBetweenNodes);

        // F = -k z
        Vector2D z = new Vector2D(deltaPos.x - direction.x * length, deltaPos.y - direction.y * length);
        Vector2D F = new Vector2D(- springConstant * z.x, -springConstant * z.y);
        Vector2D negativeF = new Vector2D(-F.x, -F.y);
        nodes[0].ApplyForce(F);
        nodes[1].ApplyForce(negativeF);
    }

    public Vector2D getLocation(int nodeID) {
        return nodes[nodeID].getLocation();
    }
}

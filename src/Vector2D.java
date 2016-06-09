/**
 * Created by mathias on 09/06/16
 */
public class Vector2D {
    public float x, y;

    public Vector2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D(Vector2D vec) {
        this(vec.x, vec.y);
    }

    public Vector2D scale(float factor) {
        return new Vector2D(x * factor, y * factor);
    }

    public Vector2D translate(Vector2D vec) {
        return new Vector2D(x + vec.x, y + vec.y);
    }

    public Vector2D transform(double angle) {
        return transform(new Vector2D(0,0), angle);
    }

    public Vector2D transform(Vector2D point, double angle) {
        Vector2D vec = new Vector2D(x - point.x, y - point.y); //New vector at origo
        double thisAngle = Math.atan2(vec.y, vec.x) + angle; //Angle of new vector
        double vectorLength = vec.length();

        Vector2D newVec = new Vector2D(
            (float) ((Math.cos(thisAngle) * vectorLength) + point.x),
            (float) ((Math.sin(thisAngle) * vectorLength) + point.y)
        );

        return newVec;
    }

    public float length() {
        return (float)Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }

    public float distance(Vector2D vec) {
        return (float)Math.sqrt(Math.pow(x - vec.x, 2) + Math.pow(y - vec.y, 2));
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}

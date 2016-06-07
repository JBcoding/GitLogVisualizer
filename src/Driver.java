import java.io.FileNotFoundException;

/**
 * Created by madsbjoern on 30/05/16.
 */
public class Driver {
    public static void main(String[] args) throws FileNotFoundException {
        if (args.length == 2) {
            return;
        }
        new Visualizer();
    }
}

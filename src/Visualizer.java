import com.sun.javafx.geom.Matrix3f;
import com.sun.javafx.geom.Vec2f;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.List;

import static com.sun.tools.doclint.Entity.image;

/**
 * Created by madsbjoern on 30/05/16.
 */

// git log --name-status > log.txt

public class Visualizer {
    private List<Commit> commits;
    private ImageAndVideoProcessor imageAndVideoProcessor;

    //java -cp out/production/GitLogVisualizer/ Driver | avconv -y -r 30 -f image2pipe -vcodec ppm -i - movie.mp4

    public Visualizer() throws FileNotFoundException {
        // check file
        File log = new File("log.txt");
        if (!log.exists()) {
            throw new IllegalArgumentException("File not found");
        }

        // load file
        Scanner input = new Scanner(log);
        String logContent = "";
        while (input.hasNextLine()) {
            logContent += input.nextLine() + "\n";
        }

        // get commits
        String[] commitsStrings = logContent.split("commit [a-f0-9]{40}\\n");
        commits = new ArrayList<>();
        for (int i = 1; i < commitsStrings.length; i ++) {
            try {
                commits.add(new Commit(commitsStrings[i]));
            } catch (IllegalArgumentException e) {
                if (!e.getMessage().equals("Just a 'Merge'")) {
                    e.printStackTrace();
                }
            }
        }
        Collections.reverse(commits);

        createVideo();
    }

    public void createVideo() {
        int numberOfImages = commits.size() * 100;
        numberOfImages = 2000;

        int w = 1280;
        int h = 720;

        List<Spring> springs = new ArrayList<>();
        Node masterNode = new Node(new Vec2f(w / 2, h / 2), 100, springs);
        masterNode.folder = false;

        for (int i = 0; i < numberOfImages; i ++) {
            if (i % 100 == 0) {
                List<Change> changes = commits.get(i / 100).getChanges();
                for (Change c : changes) {
                    String name = c.getFileName();
                    String[] nameParts = name.split("/");
                    Node currentNode = masterNode;
                    for (int j = 0; j < nameParts.length - 1; j ++) {
                        Node child = currentNode.findChildWithName(nameParts[j]);
                        if (child == null) {
                            double angle = Math.random() * Math.PI * 2;
                            child = new Node(new Vec2f((float)Math.cos(angle) * 40 + currentNode.getPhysicsNode().getLocation().x, (float)Math.sin(angle) * 40 + currentNode.getPhysicsNode().getLocation().y), currentNode.getPhysicsNode().getMass() / 2, springs);
                            child.name = nameParts[j];
                            child.folder = true;
                            currentNode.addChild(child);
                        }
                        currentNode = child;
                    }
                    if (c.getChangeType() == 'A') {
                        double angle = Math.random() * Math.PI * 2;
                        Node newChild = new Node(new Vec2f((float)Math.cos(angle) * 40 + currentNode.getPhysicsNode().getLocation().x, (float)Math.sin(angle) * 40 + currentNode.getPhysicsNode().getLocation().y), currentNode.getPhysicsNode().getMass() / 2, springs);
                        newChild.name = nameParts[nameParts.length - 1];
                        newChild.folder = false;
                        currentNode.addChild(newChild);
                    } else if (c.getChangeType() == 'D') {
                        currentNode.findChildWithName(nameParts[nameParts.length - 1]).startFading();
                    }
                }
            }

            BufferedImage off_Image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = off_Image.createGraphics();

            g2.setColor(Color.WHITE);
            if (i == 0) {
                g2.setColor(Color.GREEN);
            }
            g2.fillRect(0, 0, w, h);
            g2.setColor(Color.BLACK);

            // calculate scale and offset
            int[] rect = masterNode.getBoundingBox();
            float scale = (float)Math.min((double)w / (rect[2] - rect[0]), (double)h / (rect[3] - rect[1]) * 620/720.f);
            Vec2f offset = new Vec2f(-(rect[2] + rect[0]) / 2 * scale + w/2, -(rect[3] + rect[1]) / 2 * scale + h / 2);
            for (Spring s : springs) {
                s.draw(g2, scale, offset);
            }
            masterNode.draw(g2, scale, offset);


            try {
                imageAndVideoProcessor.addImage(off_Image);
            } catch (IOException e) {
                e.printStackTrace();
            }

            springs.forEach(Spring::update);
            masterNode.doNodeRepulsion(masterNode.getPhysicsNodes());
            masterNode.update(1 / 15.f);
        }
    }
}

import com.sun.javafx.geom.Matrix3f;
import com.sun.javafx.geom.Vec2f;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.List;


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

    private static Node masterNode;
    public void createVideo() {
        int numberOfImages = commits.size() * 100;
        numberOfImages = 2000;

        int w = 1280;
        int h = 720;

        List<Spring> springs = new ArrayList<>();
        masterNode = new Node(new Vec2f(w / 2, h / 2), 100, springs);
        masterNode.folder = false;

        float currentScale = 1.5f;
        float scaleVelocity = 0.0f;
        float scaleGoal = 1.5f;
        float scaleMaxAcceleration = .125f;

        Vec2f currentOffset = new Vec2f(0, 0);

        double currentAngle = 0.f;
        double angleGoal = 0.f;

        for (int i = -99; i < numberOfImages; i ++) {
            float deltaTime = 1 / 15.f;
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
            g2.fillRect(0, 0, w, h);
            g2.setColor(Color.BLACK);

            // calculate scale, offset and rotation
            // scale
            int[] rect = masterNode.getBoundingBox(currentAngle, new Vec2f(w / 2, h / 2));
            Vec2f rectCentrum = new Vec2f(w / 2, h / 2);
            float newScale = 1;
            try {
                newScale = (float)Math.min((double)w / (rect[2] - rect[0]), (double)h / (rect[3] - rect[1]) * 620/720.f);
            } catch (ArithmeticException e) {}
            newScale = (float)Math.min(1.5, newScale);

            if (Math.abs(Math.log(newScale)/Math.log(2) - Math.log(scaleGoal)/Math.log(2)) >= .1) {
                scaleGoal = newScale;
            }
            float scaleDiff = scaleGoal - currentScale;
            if (scaleDiff != 0) {
                scaleVelocity += scaleDiff / Math.abs(scaleDiff) * deltaTime * scaleMaxAcceleration;
                scaleVelocity *= .90f;
                currentScale += scaleVelocity * deltaTime;
                if (Math.abs(scaleVelocity) / Math.abs(scaleDiff) > 1 / 4.f) {
                    scaleVelocity = scaleDiff / 4;
                }
            }
            g2.drawString(String.valueOf(currentScale), 1000, 550);
            // offset
            Vec2f newOffset = new Vec2f(-(rect[2] + rect[0]) / 2 * currentScale + w/2, -(rect[3] + rect[1]) / 2 * currentScale + h / 2);
            float distance = currentOffset.distance(newOffset);
            if (distance <= 5 || (currentOffset.x == 0 && currentOffset.y == 0)) {
                currentOffset = newOffset;
            } else {
                float offsetSpeed = 5; // pixels per frame
                Vec2f deltaPos = new Vec2f(newOffset.x - currentOffset.x, newOffset.y - currentOffset.y);
                currentOffset.x += deltaPos.x / distance * offsetSpeed;
                currentOffset.y += deltaPos.y / distance * offsetSpeed;
            }
            g2.drawString(String.valueOf(currentOffset), 1000, 600);
            //g2.drawString(String.valueOf(lastCentrum), 1000, 700);
            // rotation
            if (i % 100 == 40 && i > 0) {
                double bestAngle = currentAngle;
                float bestAngleScale = 0;
                List<Vec2f> nodePositions = masterNode.getListOfPositions();
                for (double angle = currentAngle - Math.PI / 2; angle < currentAngle + Math.PI / 2; angle += Math.PI / 36) {
                    int[] newRect = new int[4];
                    for (int j = 0; j < nodePositions.size(); j++) {
                        Vec2f point = new Vec2f(nodePositions.get(j).x, nodePositions.get(j).y);
                        point.x = (int) (point.x * currentScale + currentOffset.x);
                        point.y = (int) (point.y * currentScale + currentOffset.y);
                        point = rotateVecter(point, angle, rectCentrum);
                        if (j == 0) {
                            newRect[0] = newRect[2] = (int) point.x;
                            newRect[1] = newRect[3] = (int) point.y;
                        } else {
                            if (newRect[0] > point.x) {newRect[0] = (int) point.x;}
                            if (newRect[1] > point.y) {newRect[1] = (int) point.y;}
                            if (newRect[2] < point.x) {newRect[2] = (int) point.x;}
                            if (newRect[3] < point.y) {newRect[3] = (int) point.y;}
                        }
                    }
                    float angleScale = (float)Math.min((double)w / (newRect[2] - newRect[0]), (double)h / (newRect[3] - newRect[1]) * 620/720.f);
                    if (angleScale > bestAngleScale) {
                        bestAngleScale = angleScale;
                        bestAngle = angle;
                    }
                }
                angleGoal = bestAngle;
            }
            double angleSpeed = Math.PI / 400; // radians per frame
            if (Math.abs(angleGoal - currentAngle) < angleSpeed) {
                currentAngle = angleGoal;
            } else {
                double angleDiff = angleGoal - currentAngle;
                currentAngle += angleDiff / Math.abs(angleDiff) * angleSpeed;
            }
            g2.drawString(String.valueOf(currentAngle), 1000, 650);

            // draw
            for (Spring s : springs) {
                s.draw(g2, currentScale, currentOffset, currentAngle, rectCentrum);
            }
            masterNode.draw(g2, currentScale, currentOffset, currentAngle, rectCentrum);

            try {
                imageAndVideoProcessor.addImage(off_Image);
            } catch (IOException e) {
                e.printStackTrace();
            }

            springs.forEach(Spring::update);
            masterNode.doNodeRepulsion(masterNode.getPhysicsNodes());
            masterNode.update(deltaTime);
        }
    }

    public static Vec2f rotateVecter(Vec2f vec, double angle, Vec2f offset) {
        vec.x -= offset.x;
        vec.y -= offset.y;
        double thisAngle = Math.atan2(vec.y, vec.x);
        thisAngle += angle;
        double vectorLength = vec.distance(0, 0);
        Vec2f newVec = new Vec2f(0, 0);
        newVec.x = (float) ((Math.cos(thisAngle) * vectorLength) + offset.x);
        newVec.y = (float) ((Math.sin(thisAngle) * vectorLength) + offset.y);
        vec.x += offset.x;
        vec.y += offset.y;
        return newVec;
    }
}
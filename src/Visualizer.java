import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;



/**
 * Created by madsbjoern on 30/05/16.
 */

// git log --name-status > log.txt

public class Visualizer {
    private List<Commit> commits;
    private ImageAndVideoProcessor imageAndVideoProcessor;

    // java -cp out/production/GitLogVisualizer/ Driver | ffmpeg -y -r 30 -f image2pipe -vcodec ppm -i - -vcodec libx264 -preset ultrafast -pix_fmt yuv420p -crf 1 -threads 4 -bf 0 movie.mp4

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
        //numberOfImages = 1000;

        int w = 1280;
        int h = 720;

        List<Spring> springs = new ArrayList<>();
        masterNode = new Node(new Vector2D(w / 2, h / 2), 100, springs);
        masterNode.folder = false;

        float currentScale = 1.5f;
        float scaleVelocity = 0.0f;
        float scaleGoal = 1.5f;
        float scaleMaxAcceleration = .125f;

        Vector2D currentOffset = new Vector2D(0, 0);

        double currentAngle = 0.f;
        double angleGoal = 0.f;

        double[] angleAtTime = new double[100];

        List<BufferedImage> lastAuthors = new LinkedList<>();
        String lastAuthorName = "";

        LabelBox lb = new LabelBox("P2-AAU", 0, 720);

        List<Beam> beams = new ArrayList<>();
        List<FadingString> fadingStrings = new ArrayList<>();

        for (int i = -99; i < numberOfImages; i ++) {
            float deltaTime = 1 / 15.f;
            if (i % 100 == 0) {
                Commit commit = commits.get(i / 100);
                lastAuthors.add(0, AuthorImageRetriever.getImage(commit));
                if (lastAuthors.size() == 7) {
                    lastAuthors.remove(6);
                }
                lastAuthorName = commit.getAuthor().split("<")[0];
                List<Change> changes = commit.getChanges();
                for (Change c : changes) {
                    double angle = Math.random() * Math.PI * 2;
                    String name = c.getFileName();
                    String[] nameParts = name.split("/");
                    Node currentNode = masterNode;
                    for (int j = 0; j < nameParts.length - 1; j ++) {
                        Node child = currentNode.findChildWithName(nameParts[j]);
                        if (child == null) {
                            if (currentNode.getParent() != null) {
                                Vector2D childLocation = currentNode.getPhysicsNode().getLocation(),
                                         parentLocation = currentNode.getParent().getPhysicsNode().getLocation();
                                double angleToParent = Math.atan2(childLocation.y - parentLocation.y,
                                                                  childLocation.x - parentLocation.x);
                                angle = angleToParent + Math.random() * Math.PI / 4 - Math.PI / 8;
                            }
                            child = new Node(new Vector2D((float)Math.cos(angle) * 200 + currentNode.getPhysicsNode().getLocation().x, (float)Math.sin(angle) * 200 + currentNode.getPhysicsNode().getLocation().y), currentNode.getPhysicsNode().getMass(), springs);
                            child.name = nameParts[j];
                            child.folder = true;
                            currentNode.addChild(child);
                        }
                        if (child.getFadingString() == null) {
                            FadingString fs = new FadingString(child.getName(), 2, 1, true, child.getPhysicsNode().getLocation(), child);
                            fadingStrings.add(fs);
                            child.setFadingString(fs);
                        }
                        currentNode = child;
                    }
                    if (c.getChangeType() == 'A') {
                        int layer = calculateLayer(currentNode.getNumberOfChildrenWitoutFolders()) + 2;
                        Node newChild = new Node(new Vector2D((float)Math.cos(angle) * 40 * layer + currentNode.getPhysicsNode().getLocation().x, (float)Math.sin(angle) * 40 * layer + currentNode.getPhysicsNode().getLocation().y), currentNode.getPhysicsNode().getMass(), springs);
                        newChild.name = nameParts[nameParts.length - 1];
                        newChild.folder = false;
                        currentNode.addChild(newChild);
                        FadingString fs = new FadingString(newChild.getName(), 2, 1, true, newChild.getPhysicsNode().getLocation(), newChild);
                        fadingStrings.add(fs);
                        newChild.setFadingString(fs);
                    } else if (c.getChangeType() == 'D') {
                        Node theChild = currentNode.findChildWithName(nameParts[nameParts.length - 1]);
                        if (theChild == null) {
                            continue;
                        }
                        theChild.startFading();
                        FadingString fs = new FadingString(theChild.getName(), 2, 1, true, theChild.getPhysicsNode().getLocation(), theChild);
                        fadingStrings.add(fs);
                        theChild.setFadingString(fs);
                    } else if (c.getChangeType() == 'M') {
                        Node theChild = currentNode.findChildWithName(nameParts[nameParts.length - 1]);
                        if (theChild == null) {
                            continue;
                        }
                        FadingString fs = new FadingString(theChild.getName(), 2, 1, true, theChild.getPhysicsNode().getLocation(), theChild);
                        fadingStrings.add(fs);
                        theChild.setFadingString(fs);
                    }
                    beams.add(new Beam(new Vector2D(50, 50), currentNode.findChildWithName(nameParts[nameParts.length - 1])));
                }
            }

            BufferedImage off_Image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = off_Image.createGraphics();

            g2.setColor(new Color(42, 44, 43));
            g2.fillRect(0, 0, w, h);
            g2.setColor(Color.WHITE);


            // calculate scale, offset and rotation
            // scale
            int[] rect = masterNode.getBoundingBox(currentAngle, new Vector2D(w / 2, h / 2));
            Vector2D rectCentrum = new Vector2D(w / 2, h / 2);
            float newScale = 1;
            try {
                newScale = (float)Math.min((double)w / (rect[2] - rect[0]) * 1080/1280.f, (double)h / (rect[3] - rect[1]) * 620/720.f);
            } catch (ArithmeticException e) {}
            newScale = (float)Math.min(1.5, newScale);

            if (Math.abs(Math.log(newScale)/Math.log(2) - Math.log(scaleGoal)/Math.log(2)) >= .1) {
                scaleGoal = newScale;
            }
            float scaleDiff = scaleGoal - currentScale;
            if (Math.abs(Math.log(scaleGoal) / Math.log(2) - Math.log(currentScale) / Math.log(2)) >= .005) {
                scaleVelocity += scaleDiff / Math.abs(scaleDiff) * deltaTime * scaleMaxAcceleration;
                scaleVelocity *= .90f;
                currentScale += scaleVelocity * deltaTime;
                if (Math.abs(scaleVelocity) / Math.abs(scaleDiff) > 1 / 4.f) {
                    scaleVelocity = scaleDiff / 4;
                }
            } else {
                currentScale = scaleGoal;
            }
            // offset
            Vector2D newOffset = new Vector2D(-(rect[2] + rect[0]) / 2 * currentScale + w/2 + 50, -(rect[3] + rect[1]) / 2 * currentScale + h / 2);
            float distance = currentOffset.distance(newOffset);
            if (distance <= 5 || (currentOffset.x == 0 && currentOffset.y == 0)) {
                currentOffset = newOffset;
            } else {
                float offsetSpeed = 5; // pixels per frame
                Vector2D deltaPos = new Vector2D(newOffset.x - currentOffset.x, newOffset.y - currentOffset.y);
                currentOffset = currentOffset.translate(deltaPos.scale(1 / distance).scale(offsetSpeed));
            }
            // rotation
            if (i % 100 == 40 && i > 0) {
                double bestAngle = currentAngle;
                float bestAngleScale = 0;
                List<Vector2D> nodePositions = masterNode.getListOfPositions();
                for (double angle = currentAngle - Math.PI / 2; angle < currentAngle + Math.PI / 2; angle += Math.PI / 36) {
                    int[] newRect = new int[4];
                    for (int j = 0; j < nodePositions.size(); j++) {
                        Vector2D point = new Vector2D(nodePositions.get(j).x, nodePositions.get(j).y);
                        point = point.scale(currentScale).translate(currentOffset);
                        point = point.transform(rectCentrum, angle);
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
                    float angleScale = (float)Math.min((double)w / (newRect[2] - newRect[0]) * 1080/1280.f, (double)h / (newRect[3] - newRect[1]) * 620/720.f);
                    if (angleScale > bestAngleScale) {
                        bestAngleScale = angleScale;
                        bestAngle = angle;
                    }
                }
                angleGoal = bestAngle;
                double fullAngleSpeed = (Math.PI / 160);
                int sign = 1;
                if (angleGoal - currentAngle < 0) {
                    sign = -1;
                }
                int steps = (int) (Math.abs(angleGoal - currentAngle) / fullAngleSpeed) + 10;
                if (steps >= 20) {
                    if (steps < 50) {
                        steps -= 10;
                        steps *= 2;
                        steps += 10;
                        fullAngleSpeed /= 2;
                    }
                    // speed up
                    int j = 0;
                    for (j = 0; j < 10; j ++) {
                        angleAtTime[j] = sign * fullAngleSpeed / 10 * (j + 1) * j / 2 + currentAngle;
                    }
                    // const speed
                    for (; j < steps - 10; j ++) {
                        angleAtTime[j] = angleAtTime[j - 1] + sign * fullAngleSpeed;
                    }
                    // slow down
                    for (; j < steps; j ++) {
                        angleAtTime[j] = angleAtTime[j - 1] + sign * fullAngleSpeed / 10 * (steps - j);
                    }
                    for (; j < 100; j ++) {
                        angleAtTime[j] = angleGoal;
                    }
                } else if (steps > 10) {
                    // speed up
                    angleAtTime[0] = currentAngle + (angleGoal - currentAngle) / 100;
                    for (int j = 1; j < 100; j ++) {
                        angleAtTime[j] = angleAtTime[j - 1] + (angleGoal - currentAngle) / 100;
                    }
                } else {
                    for (int j = 0; j < 100; j ++) {
                        angleAtTime[j] = angleGoal;
                    }
                }
            }
            currentAngle = angleAtTime[((i - 40) % 100 + 100) % 100];

            // draw
            for (Spring s : springs) {
                s.draw(g2, currentScale, currentOffset, currentAngle, rectCentrum);
            }
            for (Beam b : beams) {
                b.draw(g2, currentScale, currentOffset, currentAngle, rectCentrum);
            }
            masterNode.draw(g2, currentScale, currentOffset, currentAngle, rectCentrum);
            for (FadingString fs : fadingStrings) {
                fs.draw(g2, currentScale, currentOffset, currentAngle, rectCentrum);
            }
            if (i >= 0) {
                if (lastAuthors.size() == 6) {
                    BufferedImage img = lastAuthors.get(5);
                    g2.drawImage(img, 20, 20 + 100 * 4, 80, 80, null);
                }
                for (int j = 0; j < Math.min(lastAuthors.size(), 5); j ++) {
                    BufferedImage img = lastAuthors.get(j);
                    if (img != null) {
                        if (i % 100 < 30) {
                            g2.drawImage(img, 20, (int)Math.max(20, (20 + 100 * j - 3.33 * (30 - (i % 100)))), 80, 80, null);
                        } else {
                            g2.drawImage(img, 20, 20 + 100 * j, 80, 80, null);
                        }
                    }
                }
                if (i % 100 == 0) {
                    fadingStrings.add(new FadingString(lastAuthorName, 2, 1, false, new Vector2D(120, 60 + g2.getFontMetrics().getHeight() / 5 * 2), null));
                }
            }
            lb.setContent(g2, "Message:", commits.get(i / 100).getText(), "Date: " + commits.get(i / 100).getDate(), "Scale: " + currentScale);
            try {
                imageAndVideoProcessor.addImage(off_Image);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            springs.forEach(Spring::update);
            masterNode.doNodeRepulsion(masterNode.getPhysicsNodes());
            masterNode.collisionDetection(masterNode.getPhysicsNodes());
            masterNode.update(deltaTime);
            for (Beam b : beams) {
                b.update(deltaTime);
            }
            for (int j = 0; j < beams.size(); j ++) {
                if (beams.get(j).cleanUpCheck()) {
                    beams.remove(j);
                    j --;
                }
            }
            for (FadingString fs : fadingStrings) {
                fs.update(deltaTime);
            }
            for (int j = 0; j < fadingStrings.size(); j ++) {
                if (fadingStrings.get(j).cleanUpCheck()) {
                    fadingStrings.remove(j);
                    j --;
                }
            }
        }
    }

    public static int calculateLayer(int numberOfChildren) {
        int numberInCurrentLayer = 6;
        int layer = 1;
        while (numberOfChildren > numberInCurrentLayer) {
            layer ++;
            numberOfChildren -= numberInCurrentLayer;
            numberInCurrentLayer += 6;
        }
        return layer;
    }
}

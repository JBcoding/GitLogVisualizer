/**
 * Created by madsbjoern on 30/05/16.
 */
public class Change {
    private char changeType;
    private String fileName;
    public Change(String content) {
        changeType = content.charAt(0);
        fileName = content.substring(2); // sub 2 for removing "M	"
    }

    public String getFileName() {
        return fileName;
    }

    public char getChangeType() {
        return changeType;
    }
}

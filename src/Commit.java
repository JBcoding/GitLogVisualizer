import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by madsbjoern on 30/05/16.
 */
public class Commit {
    private String author;
    private Date date;
    private String text;
    private List<Change> changes;

    public Commit(String content) {
        if (content.substring(0, 5).equals("Merge")) {
            // auto generated commit
            throw new IllegalArgumentException("Just a 'Merge'");
        }
        String[] contentParts = content.split("\n\n");
        String commitInfo = contentParts[0];
        String commitText = contentParts[1];
        String commitChanges = contentParts[2];

        // info
        String[] infoParts = commitInfo.split("\n");
        author = infoParts[0].substring(8); // sub 8 for removing "Author: "
        date = getDateFromString(infoParts[1].substring(8)); // sub 8 for removing "Date:   "

        // text
        text = "";
        String newLine = "";
        for (String part : commitText.split("\n")) {
            text += newLine + part.substring(4); // sub 4 for removing "    "
            newLine = "\n"; // Fence post problem
        }

        // changes
        changes = new ArrayList<>();
        for (String changeText : commitChanges.split("\n")) {
            changes.add(new Change(changeText));
        }
    }

    private Date getDateFromString(String dateString) {
        DateFormat format = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);
        Date date;
        try {
            date = format.parse(dateString);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Date not the right format");
        }
        return date;
    }

    public List<Change> getChanges() {
        return changes;
    }
}

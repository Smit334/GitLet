package gitlet;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

public class Commit implements Serializable {

    /** */
    private String message;

    /** */
    private Date timestamp;

    /** */
    private String parent;

    /** */
    private String parent2 = null;

    /** */
    private File commit;

    /** */
    private String shaID;

    /** */
    private TreeMap<String, String> trackedFiles;


    public Commit(String m, String p, File commitFolder) {
        this.message = m;
        this.parent = p;
        if (this.parent == null) {
            this.timestamp = new Date(0);
            this.trackedFiles = new TreeMap<>();
        } else {
            timestamp = new Date();
            this.trackedFiles = Utils.readObject(
                    Utils.join(commitFolder, parent),
                    Commit.class).getTrackedFiles();
        }
        StringBuilder treeMapKeys = new StringBuilder();
        for (String i: trackedFiles.keySet()) {
            treeMapKeys.append(i);
        }
        this.shaID = Utils.sha1(treeMapKeys.toString(),
                message, getTimestamp());
        commit = Utils.join(commitFolder, this.shaID);
        Utils.writeObject(commit, this);
    }

    public String getMessage() {
        return this.message;
    }

    public String getTimestamp() {
        SimpleDateFormat dtf = new
                SimpleDateFormat("EEE LLL d HH:mm:ss yyyy ZZZ");
        return dtf.format(timestamp);
    }
    public String getParent() {
        return this.parent;
    }

    public String getParent2() {
        return this.parent2;
    }

    public void setParent2(String parent2ID) {
        this.parent2 = parent2ID;
    }

    public TreeMap<String, String> getTrackedFiles() {
        return this.trackedFiles;
    }

    public String getShaID() {
        return this.shaID;
    }

    public void updateFile() {
        Utils.writeObject(commit, this);
    }

    public String getLogMessage() {
        return "===\n"
                + "commit " + getShaID() + "\n"
                + "Date: " + getTimestamp() + "\n"
                + getMessage() + "\n";
    }
}

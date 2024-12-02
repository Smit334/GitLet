package gitlet;

import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {

    /** */
    private String blobCode;

    /** */
    private String fileContents;

    public Blob(File filename) {
        this.fileContents = Utils.readContentsAsString(filename);
        this.blobCode = Utils.sha1(fileContents, filename.getName());
    }

    public String getBlobCode() {
        return this.blobCode;
    }

    public String getFileContents() {
        return this.fileContents;
    }
}

package gitlet;

import java.io.File;

public class Remote {

    public static void addRemote(String remoteName, String directoryName) {
        File remoteDirectoryFile = Utils.join(
                Repository.getGitlet(), remoteName);
        Utils.writeContents(remoteDirectoryFile, directoryName);
    }

    public static void rmRemote(String remoteName) {
        File fileToBeDeleted = Utils.join(Repository.getGitlet(), remoteName);
        if (!fileToBeDeleted.exists()) {
            System.out.println("A remote with that name does not exist.");
            System.exit(0);
        }
        fileToBeDeleted.delete();
    }



}

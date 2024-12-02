package gitlet;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Objects;
import java.util.TreeMap;
import java.util.HashSet;


@SuppressWarnings({"unchecked"})
public class Repository {

    /** */
    private static final File WORKING_DIR = new File(
            System.getProperty("user.dir"));

    /** */
    private static final File GITLET = Utils.join(
            WORKING_DIR, ".gitlet");

    /** */
    private static final File BRANCH_FOLDER = Utils.join(
            GITLET, "Branches");

    /** */
    private static final File MASTER_BRANCH = Utils.join(
            BRANCH_FOLDER, "master");

    /** */
    private static final File COMMIT_FOLDER = Utils.join(
            GITLET, "Commits");

    /** */
    private static final File BLOBS_FILE = Utils.join(
            GITLET, "Blobs");

    /** */
    private static final File STAGE_FILE = Utils.join(
            GITLET, "stage");

    /** */
    private static final File HEAD_FILE = Utils.join(
            GITLET, "Head");

    /** */
    private static final File REMOVE_FILE = Utils.join(
            GITLET, "removed");

    /** */
    private static ArrayList<String> removeList = new ArrayList<>();

    /** */
    private static TreeMap<String, String> blobTreeMap = new TreeMap<>();

    /** */
    private static TreeMap<String, String> stageTreeMap = new TreeMap<>();

    public static TreeMap<String, String> getBlobTreeMap() {
        return blobTreeMap;
    }

    public static TreeMap<String, String> getStageTreeMap() {
        return stageTreeMap;
    }

    public static ArrayList<String> getRemoveList() {
        return removeList;
    }

    public static File getGitlet() {
        return GITLET;
    }

    public static File getCommitFolder() {
        return COMMIT_FOLDER;
    }

    private static File getHead() {
        return Utils.join(BRANCH_FOLDER, Utils.readContentsAsString(HEAD_FILE));
    }

    private static Commit getCommit(File branch) {
        return Utils.readObject(Utils.join(
                COMMIT_FOLDER, Utils.readContentsAsString(
                        branch)), Commit.class);
    }

    public static void isInitialised() {
        if (!GITLET.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    public static void init() {
        if (!GITLET.exists()) {
            GITLET.mkdir();
            COMMIT_FOLDER.mkdir();
            BRANCH_FOLDER.mkdir();
            Utils.writeObject(REMOVE_FILE, removeList);
            Utils.writeObject(BLOBS_FILE, blobTreeMap);
            Utils.writeObject(STAGE_FILE, stageTreeMap);
            Commit initialCommit = new Commit(
                    "initial commit", null, COMMIT_FOLDER);
            Utils.writeContents(MASTER_BRANCH, initialCommit.getShaID());
            Utils.writeContents(HEAD_FILE, MASTER_BRANCH.getName());
        } else {
            System.out.println("Gitlet "
                    + "version-control system already "
                    + "exists in the current directory.");
            System.exit(0);
        }
    }

    public static void add(String filename) {
        File currentFile = Utils.join(WORKING_DIR, filename);
        if (!currentFile.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        } else {
            Blob blob = new Blob(currentFile);
            blobTreeMap = Utils.readObject(BLOBS_FILE, TreeMap.class);
            stageTreeMap = Utils.readObject(STAGE_FILE, TreeMap.class);
            removeList = Utils.readObject(REMOVE_FILE, ArrayList.class);
            Commit latestCommit = getCommit(getHead());
            TreeMap<String, String> trackedFiles =
                    latestCommit.getTrackedFiles();
            if (!trackedFiles.containsValue(blob.getBlobCode())
                    && !stageTreeMap.containsKey(filename)) {
                blobTreeMap.put(blob.getBlobCode(), blob.getFileContents());
                stageTreeMap.put(filename, blob.getBlobCode());
            }
            if (removeList.contains(filename)) {
                stageTreeMap.remove(filename);
                removeList.remove(filename);
            }
            Utils.writeObject(REMOVE_FILE, removeList);
            Utils.writeObject(BLOBS_FILE, blobTreeMap);
            Utils.writeObject(STAGE_FILE, stageTreeMap);
        }
    }

    public static void commit(String message) {
        File branch = getHead();
        Commit commit = new Commit(message,
                Utils.readContentsAsString(branch), COMMIT_FOLDER);
        stageTreeMap = Utils.readObject(STAGE_FILE, TreeMap.class);
        removeList = Utils.readObject(REMOVE_FILE, ArrayList.class);
        if (stageTreeMap.isEmpty() && removeList.isEmpty()) {
            File tobeDeleted = Utils.join(COMMIT_FOLDER, commit.getShaID());
            tobeDeleted.delete();
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        for (String i: stageTreeMap.keySet()) {
            commit.getTrackedFiles().put(i, stageTreeMap.get(i));
        }
        for (String i: removeList) {
            commit.getTrackedFiles().remove(i);
        }
        commit.updateFile();
        Utils.writeContents(branch, commit.getShaID());
        stageTreeMap.clear();
        Utils.writeObject(STAGE_FILE, stageTreeMap);
        removeList.clear();
        Utils.writeObject(REMOVE_FILE, removeList);
    }

    public static void log() {
        Commit commit = getCommit(getHead());
        while (commit.getParent() != null) {
            Utils.message("%s", commit.getLogMessage());
            commit = Utils.readObject(
                    Utils.join(COMMIT_FOLDER,
                            commit.getParent()), Commit.class);
        }
        Utils.message("%s", commit.getLogMessage());
    }

    public static void globalLog() {
        for (String commitFileName: Objects.requireNonNull(
                Utils.plainFilenamesIn(COMMIT_FOLDER))) {
            Commit commit = Utils.readObject(
                    Utils.join(COMMIT_FOLDER, commitFileName), Commit.class);
            Utils.message("%s", commit.getLogMessage());
        }
    }

    public static void checkoutFile(String filename) {
        File currentBranch = getHead();
        checkoutCommit(filename, Utils.readContentsAsString(currentBranch));
    }

    public static void checkoutCommit(String filename, String commitName) {

        File commitFile = Utils.join(COMMIT_FOLDER, commitName);
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit commit = Utils.readObject(commitFile, Commit.class);
        if (!commit.getTrackedFiles().containsKey(filename)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        blobTreeMap = Utils.readObject(BLOBS_FILE, TreeMap.class);
        String blobContents = blobTreeMap.get(
                commit.getTrackedFiles().get(filename));
        File fileInWorkingDir = Utils.join(WORKING_DIR, filename);
        Utils.writeContents(fileInWorkingDir, blobContents);
    }

    public static void checkoutBranch(String branchName) {
        File branch = Utils.join(BRANCH_FOLDER, branchName);
        if (!branch.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        } else if (branchName.equals(getHead().getName())) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        } else {
            helperForResetAndCheckoutBranch(Utils.readContentsAsString(branch));
            Utils.writeContents(HEAD_FILE, branchName);
        }
    }

    public static void reset(String commitName) {
        File commitFile = Utils.join(COMMIT_FOLDER, commitName);
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit commit = Utils.readObject(commitFile, Commit.class);
        helperForResetAndCheckoutBranch(commitName);
        Utils.writeContents(getHead(), commit.getShaID());
    }

    private static void resetHard(String commitName) {
        File commitFile = Utils.join(COMMIT_FOLDER, commitName);
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit commit = Utils.readObject(commitFile, Commit.class);
        stageTreeMap = Utils.readObject(STAGE_FILE, TreeMap.class);
        removeList = Utils.readObject(REMOVE_FILE, ArrayList.class);
        ArrayList<File> filesToBeDeleted = new ArrayList<>();
        for (String fileInWorkingDir: Objects.requireNonNull(
                Utils.plainFilenamesIn(WORKING_DIR))) {
            if (!commit.getTrackedFiles().containsKey(fileInWorkingDir)) {
                File fileToDelete = Utils.join(WORKING_DIR, fileInWorkingDir);
                filesToBeDeleted.add(fileToDelete);
            }
        }
        for (String filename: commit.getTrackedFiles().keySet()) {
            checkoutCommit(filename, commitName);
        }
        for (File fileToDelete: filesToBeDeleted) {
            fileToDelete.delete();
        }
        stageTreeMap.clear();
        Utils.writeObject(STAGE_FILE, stageTreeMap);
        removeList.clear();
        Utils.writeObject(REMOVE_FILE, removeList);
        Utils.writeContents(getHead(), commit.getShaID());
    }

    private static void helperForResetAndCheckoutBranch(String commitName) {
        File commitFile = Utils.join(COMMIT_FOLDER, commitName);
        Commit commit = Utils.readObject(commitFile, Commit.class);
        Commit currentCommit = getCommit(getHead());
        stageTreeMap = Utils.readObject(STAGE_FILE, TreeMap.class);
        removeList = Utils.readObject(REMOVE_FILE, ArrayList.class);
        ArrayList<File> filesToBeDeleted = new ArrayList<>();
        for (String fileInWorkingDir: Objects.requireNonNull(
                Utils.plainFilenamesIn(WORKING_DIR))) {
            if (!currentCommit.getTrackedFiles().containsKey(fileInWorkingDir)
                    && !stageTreeMap.containsKey(fileInWorkingDir)
                    && !removeList.contains(fileInWorkingDir)) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
                System.exit(0);
            } else if (!commit.getTrackedFiles().
                    containsKey(fileInWorkingDir)) {
                File fileToDelete = Utils.join(WORKING_DIR, fileInWorkingDir);
                filesToBeDeleted.add(fileToDelete);
            }
        }
        for (String filename: commit.getTrackedFiles().keySet()) {
            checkoutCommit(filename, commitName);
        }
        for (File fileToDelete: filesToBeDeleted) {
            fileToDelete.delete();
        }
        stageTreeMap.clear();
        Utils.writeObject(STAGE_FILE, stageTreeMap);
        removeList.clear();
        Utils.writeObject(REMOVE_FILE, removeList);
    }

    public static void branch(String branchname) {
        Commit commit = getCommit(getHead());
        File newBranch = Utils.join(BRANCH_FOLDER, branchname);
        if (newBranch.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        Utils.writeContents(newBranch, commit.getShaID());
    }

    public static void rm(String filename) {
        Commit currentCommit = getCommit(getHead());
        File currentFile = Utils.join(WORKING_DIR, filename);
        stageTreeMap = Utils.readObject(STAGE_FILE, TreeMap.class);
        removeList = Utils.readObject(REMOVE_FILE, ArrayList.class);
        if (currentCommit.getTrackedFiles().containsKey(filename)) {
            removeList.add(filename);
            Utils.writeObject(REMOVE_FILE, removeList);
            currentFile.delete();
        } else if (!stageTreeMap.containsKey(filename)) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        if (stageTreeMap.containsKey(filename)) {
            stageTreeMap.remove(filename);
            Utils.writeObject(STAGE_FILE, stageTreeMap);
        }
    }

    public static void find(String commitMessage) {
        boolean foundCommit = false;
        for (String commitFileName: Objects.requireNonNull(
                Utils.plainFilenamesIn(COMMIT_FOLDER))) {
            Commit commit = Utils.readObject(Utils.join(
                    COMMIT_FOLDER, commitFileName), Commit.class);
            if (commit.getMessage().equals(commitMessage)) {
                Utils.message("%s", commit.getShaID());
                foundCommit = true;
            }
        }
        if (!foundCommit) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    public static void status() {
        stageTreeMap = Utils.readObject(STAGE_FILE, TreeMap.class);
        removeList = Utils.readObject(REMOVE_FILE, ArrayList.class);
        Commit latestCommit = getCommit(getHead());
        TreeMap<String, String> trackedFiles = latestCommit.getTrackedFiles();
        System.out.println("=== Branches ===");
        for (String branch: Objects.requireNonNull(
                Utils.plainFilenamesIn(BRANCH_FOLDER))) {
            if (branch.equals(getHead().getName())) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println("\n=== Staged Files ===");
        for (String file: stageTreeMap.keySet()) {
            System.out.println(file);
        }
        System.out.println("\n=== Removed Files ===");
        for (String file: removeList) {
            System.out.println(file);
        }
        System.out.println("\n=== Modifications Not Staged For Commit ===");
        for (String filename: trackedFiles.keySet()) {
            File currentFile = Utils.join(WORKING_DIR, filename);
            if (currentFile.exists()) {
                Blob blob = new Blob(currentFile);
                if (trackedFiles.containsKey(filename)
                        && !Objects.equals(trackedFiles.get(filename),
                        blob.getBlobCode())
                        && !stageTreeMap.containsKey(filename)) {
                    System.out.println(filename + " (modified)");
                } else if (stageTreeMap.containsKey(filename)
                        && !Objects.equals(stageTreeMap.get(filename),
                        blob.getBlobCode())) {
                    System.out.println(filename + " (modified)");
                }
            } else {
                if ((trackedFiles.containsKey(filename)
                        && !removeList.contains(filename))
                        || stageTreeMap.containsKey(filename)) {
                    System.out.println(filename + " (deleted)");
                }
            }
        }
        System.out.println("\n=== Untracked Files ===");
        for (String filename: Objects.requireNonNull(
                Utils.plainFilenamesIn(WORKING_DIR))) {
            if (!trackedFiles.containsKey(filename)
                    && !stageTreeMap.containsKey(filename)
                    && !removeList.contains(filename)) {
                System.out.println(filename);
            }
        }
        System.out.println("\n");
    }

    public static void rmBranch(String branchName) {
        File branchToBeRemoved = Utils.join(BRANCH_FOLDER, branchName);
        if (branchToBeRemoved.equals(getHead())) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        } else if (!branchToBeRemoved.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        } else {
            branchToBeRemoved.delete();
        }
    }

    public static void merge(String branchName) {
        File branch = Utils.join(BRANCH_FOLDER, branchName);
        if (!branch.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        Commit currentCommit = getCommit(getHead());
        Commit branchCommit = getCommit(branch);
        mergeErrorHandling(currentCommit, branchCommit);
        HashSet<Commit> ancestors = new HashSet<>();
        stageTreeMap.clear();
        removeList.clear();
        populateAncestors(ancestors, branchCommit);
        HashSet<String> ancestorsNames = new HashSet<>();
        for (Commit ancestorCommit: ancestors) {
            ancestorsNames.add(ancestorCommit.getShaID());
        }
        String latestAncestorString = bfs(currentCommit, ancestorsNames);
        Commit latestCommonAncestor = Utils.readObject(
                Utils.join(COMMIT_FOLDER, latestAncestorString), Commit.class);
        if (Objects.equals(latestAncestorString, branchCommit.getShaID())) {
            System.out.println("Given "
                    + "branch is an ancestor of the current branch.");
            System.exit(0);
        }
        if (Objects.equals(latestAncestorString, currentCommit.getShaID())) {
            checkoutBranch(branchName);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        mergeConditions(currentCommit, branchCommit, latestCommonAncestor);
        String mergeCommitMessage = "Merged "
                + branchName + " into " + getHead().getName() + ".";
        commit(mergeCommitMessage);
        Commit mergeCommit = getCommit(getHead());
        mergeCommit.setParent2(branchCommit.getShaID());
        mergeCommit.updateFile();
        resetHard(mergeCommit.getShaID());
    }

    private static void mergeConditions(Commit current, Commit branch,
                                        Commit latestAncestor) {
        TreeMap<String, String> currentTracked = current.getTrackedFiles();
        TreeMap<String, String> branchTracked = branch.getTrackedFiles();
        TreeMap<String, String> splitTracked = latestAncestor.getTrackedFiles();
        HashSet<String> allFiles = getAllFiles(
                splitTracked, currentTracked, branchTracked);
        for (String filename: allFiles) {
            String splitCode = splitTracked.get(filename);
            String currentCode = currentTracked.get(filename);
            String branchCode = branchTracked.get(filename);
            if (currentTracked.containsKey(filename)
                    && branchTracked.containsKey(filename)) {
                if (Objects.equals(currentCode, branchCode)) {
                    stageTreeMap.put(filename, currentCode);
                    Utils.writeObject(STAGE_FILE, stageTreeMap);
                } else if (Objects.equals(splitCode, currentCode)
                        && !Objects.equals(splitCode, branchCode)) {
                    stageTreeMap.put(filename, branchCode);
                    Utils.writeObject(STAGE_FILE, stageTreeMap);
                } else if (Objects.equals(splitCode, branchCode)
                        && !Objects.equals(splitCode, currentCode)) {
                    stageTreeMap.put(filename, currentCode);
                    Utils.writeObject(STAGE_FILE, stageTreeMap);
                } else if (!Objects.equals(splitCode, branchCode)
                        && !Objects.equals(splitCode, currentCode)) {
                    mergeConflict(currentCode, branchCode, filename);
                } else if (!splitTracked.containsKey(filename)) {
                    mergeConflict(currentCode, branchCode, filename);
                }
            } else if (!splitTracked.containsKey(filename)
                    && !branchTracked.containsKey(filename)
                    && currentTracked.containsKey(filename)) {
                stageTreeMap.put(filename, currentCode);
                Utils.writeObject(STAGE_FILE, stageTreeMap);
            } else if (!splitTracked.containsKey(filename)
                    && !currentTracked.containsKey(filename)
                    && branchTracked.containsKey(filename)) {
                stageTreeMap.put(filename, branchCode);
                Utils.writeObject(STAGE_FILE, stageTreeMap);
            } else if (splitTracked.containsKey(filename)
                    && currentTracked.containsKey(filename)
                    && !branchTracked.containsKey(filename)) {
                if (!Objects.equals(splitCode, currentCode)) {
                    mergeConflict(currentCode, null, filename);
                } else {
                    removeList.add(filename);
                    Utils.writeObject(REMOVE_FILE, removeList);
                }
            } else if (splitTracked.containsKey(filename)
                    && branchTracked.containsKey(filename)
                    && !currentTracked.containsKey(filename)) {
                if (!Objects.equals(splitCode, branchCode)) {
                    mergeConflict(null, branchCode, filename);
                } else {
                    removeList.add(filename);
                    Utils.writeObject(REMOVE_FILE, removeList);
                }
            }
        }
    }

    private static void mergeConflict(String currentCode,
                                      String branchCode, String filename) {
        blobTreeMap = Utils.readObject(BLOBS_FILE, TreeMap.class);
        stageTreeMap = Utils.readObject(STAGE_FILE, TreeMap.class);
        String newContents;
        if (currentCode == null) {
            String branchContents = blobTreeMap.get(branchCode);
            newContents = "<<<<<<< HEAD\n"
                    + "=======\n"
                    + branchContents
                    + ">>>>>>>\n";
        } else if (branchCode == null) {
            String currentContents = blobTreeMap.get(currentCode);
            newContents = "<<<<<<< HEAD\n"
                    + currentContents
                    + "=======\n"
                    + ">>>>>>>\n";
        } else {
            String currentContents = blobTreeMap.get(currentCode);
            String branchContents = blobTreeMap.get(branchCode);
            newContents = "<<<<<<< HEAD\n"
                    + currentContents
                    + "=======\n"
                    + branchContents
                    + ">>>>>>>\n";
        }
        stageTreeMap.put(filename, Utils.sha1(newContents, filename));
        Utils.writeObject(STAGE_FILE, stageTreeMap);
        Utils.writeContents(Utils.join(WORKING_DIR, filename), newContents);
        blobTreeMap.put(Utils.sha1(newContents, filename), newContents);
        Utils.writeObject(BLOBS_FILE, blobTreeMap);
        System.out.println("Encountered a merge conflict.");
    }

    private static HashSet<String> getAllFiles(
            TreeMap<String, String> split, TreeMap<String, String> current,
            TreeMap<String, String> branch) {
        HashSet<String> allFiles = new HashSet<>();
        allFiles.addAll(current.keySet());
        allFiles.addAll(branch.keySet());
        allFiles.addAll(split.keySet());
        return allFiles;
    }

    private static String bfs(Commit currentCommit, HashSet<String> ancestors) {
        ArrayDeque<String> work = new ArrayDeque<>();
        work.push(currentCommit.getShaID());
        while (!work.isEmpty()) {
            String node = work.removeFirst();
            Commit nodeCommit = Utils.readObject(Utils.join(COMMIT_FOLDER,
                    node), Commit.class);
            if (ancestors.contains(node)) {
                return node;
            }
            if (nodeCommit.getParent() != null) {
                work.add(nodeCommit.getParent());
                if (nodeCommit.getParent2() != null) {
                    work.add(nodeCommit.getParent2());
                }
            }
        }
        return null;
    }

    private static void populateAncestors(HashSet<Commit> ancestors,
                                          Commit commit) {
        while (commit.getParent() != null) {
            ancestors.add(commit);
            if (commit.getParent2() != null) {
                Commit parent2 = Utils.readObject(
                        Utils.join(COMMIT_FOLDER,
                                commit.getParent2()), Commit.class);
                populateAncestors(ancestors, parent2);
            }
            commit = Utils.readObject(
                    Utils.join(COMMIT_FOLDER,
                            commit.getParent()), Commit.class);
        }
        ancestors.add(commit);
    }

    private static void mergeErrorHandling(Commit currentCommit,
                                           Commit branchCommit) {
        stageTreeMap = Utils.readObject(STAGE_FILE, TreeMap.class);
        removeList = Utils.readObject(REMOVE_FILE, ArrayList.class);
        if (Objects.equals(currentCommit.getShaID(), branchCommit.getShaID())) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        } else if (!stageTreeMap.isEmpty() || !removeList.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        } else {
            for (String fileInWorkingDir: Objects.requireNonNull(
                    Utils.plainFilenamesIn(WORKING_DIR))) {
                if (!currentCommit.getTrackedFiles().containsKey(
                        fileInWorkingDir)
                        && !stageTreeMap.containsKey(fileInWorkingDir)
                        && !removeList.contains(fileInWorkingDir)) {
                    System.out.println("There is an untracked file in the way;"
                            + " delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }
    }
}


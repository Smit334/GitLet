package gitlet;

import java.util.Objects;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Smit Malde
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        } else if (!Objects.equals(args[0], "init")) {
            Repository.isInitialised();
        }
        switch (args[0]) {
        case "init" -> {
            argsChecker(1, args); Repository.init(); }
        case "add" -> {
            argsChecker(2, args); Repository.add(args[1]); }
        case "commit" -> {
            if (args.length == 1 || Objects.equals(args[1], "")) {
                System.out.println("Please enter a commit message.");
                System.exit(0);
            }
            argsChecker(2, args); Repository.commit(args[1]); }
        case "checkout" -> checkoutChecker(args);
        case "log" -> {
            argsChecker(1, args); Repository.log(); }
        case "global-log" -> {
            argsChecker(1, args); Repository.globalLog(); }
        case "rm" -> {
            argsChecker(2, args); Repository.rm(args[1]); }
        case "find" -> {
            argsChecker(2, args); Repository.find(args[1]); }
        case "branch" -> {
            argsChecker(2, args); Repository.branch(args[1]); }
        case "reset" -> {
            argsChecker(2, args); Repository.reset(args[1]); }
        case "status" -> {
            argsChecker(1, args); Repository.status(); }
        case "rm-branch" -> {
            argsChecker(2, args); Repository.rmBranch(args[1]); }
        case "merge" -> {
            argsChecker(2, args); Repository.merge(args[1]); }
        case "add-remote" -> {
            argsChecker(3, args); Remote.addRemote(args[1], args[2]); }
        case "rm-remote" -> {
            argsChecker(2, args); Remote.rmRemote(args[1]); }
        default -> System.out.println("No command with that name exists.");
        }
        System.exit(0);
    }

    public static void argsChecker(int argsLength, String... args) {
        if (args.length != argsLength) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    public static void checkoutChecker(String... args) {
        switch (args.length) {
        case 3 -> {
            if (!Objects.equals(args[1], "--")) {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
            Repository.checkoutFile(args[2]);
        }
        case 4 -> {
            if (!Objects.equals(args[2], "--")) {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
            if (args[1].length() >= 6 && args[1].length() < ARGS_LENGTH) {
                for (String commit: Objects.requireNonNull(
                        Utils.plainFilenamesIn(Repository.getCommitFolder()))) {
                    if (commit.indexOf(args[1]) == 0) {
                        Repository.checkoutCommit(args[3], commit);
                    }
                }
            } else {
                Repository.checkoutCommit(args[3], args[1]);
            }
        }
        case 2 -> Repository.checkoutBranch(args[1]);
        default -> System.out.println("Incorrect operands.");
        }
        System.exit(0);
    }

    /** */
    private static final int ARGS_LENGTH = 40;

}

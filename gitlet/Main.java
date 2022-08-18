package gitlet;
import java.io.IOException;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Bruce Xu
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        Gitlet gitlet = new Gitlet();
        part1(gitlet, args);
    }

    /** First part of gitlet commands.
     * @param gitlet **The gitlet class object**
     * @param args **Input text**
     */
    public static void part1(Gitlet gitlet, String... args) throws IOException {
        switch (args[0]) {
        case "init":
            if (args.length != 1) {
                System.out.println("Incorrect operands.");
                break;
            }
            gitlet.init();
            break;
        case "commit":
            if (args[1].length() == 0) {
                System.out.println("Please enter a commit message.");
                break;
            }
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
                break;
            }
            gitlet.commit(args[1]);
            break;
        case "add":
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
                break;
            }
            gitlet.add(args[1]);
            break;
        case "rm":
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
                break;
            }
            gitlet.rm(args[1]);
            break;
        case "log":
            if (args.length != 1) {
                System.out.println("Incorrect operands.");
            }
            gitlet.log();
            break;
        case "global-log":
            if (args.length != 1) {
                System.out.println("Incorrect operands.");
            }
            gitlet.globallog();
            break;
        default:
            part2(gitlet, args);
        }
    }

    /** Second part of gitlet commands.
     * @param gitlet **The gitlet class object**
     * @param args **Input text**
     */
    public static void part2(Gitlet gitlet, String... args)
            throws IOException {
        switch (args[0]) {
        case "find":
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
                break;
            }
            gitlet.find(args[1]);
            break;
        case "status":
            if (args.length != 1) {
                System.out.println("Incorrect operands.");
                break;
            }
            gitlet.status();
            break;
        case "checkout":
            if (args.length == 3) {
                if (!args[1].equals("--")) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                gitlet.checkout(args[2]);
                break;
            } else if (args.length == 4) {
                if (!args[2].equals("--")) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                gitlet.checkout(args[1], args[3]);
                break;
            } else if (args.length == 2) {
                gitlet.checkout2(args[1]);
                break;
            } else {
                System.out.println("Incorrect operands.");
                break;
            }
        case "branch":
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
                break;
            }
            gitlet.branch(args[1]);
            break;
        case "rm-branch":
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
                break;
            }
            gitlet.rmbranch(args[1]);
            break;
        default:
            part3(gitlet, args);
        }
    }

    /** Third part of gitlet commands.
     * @param gitlet **The gitlet class object**
     * @param args **Input text**
     */
    public static void part3(Gitlet gitlet, String... args) throws IOException {
        switch (args[0]) {
        case "reset":
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
                break;
            }
            gitlet.reset(args[1]);
            break;
        case "merge":
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
                break;
            }
            gitlet.merge(args[1]);
            return;
        default:
            System.out.println("No command with that name exists.");
        }
    }

}

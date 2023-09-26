package gitlet;
import java.io.*;
import java.io.File;
import java.util.Arrays;

/** Driver class for Gitlet, a subset of the Git version-control system
 *
 * This class handles the commands entered by the user. Depending on the command
 * and operands, the appropriate corresponding repository method will be called
 * to handle the command.
 *
 *  @author Roberto Moron Jimenez
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        //What if args is empty?
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        if (!firstArg.equals("init") && !Repository.GITLET_DIR.exists()) {
            System.out.print("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        switch(firstArg) {
            case "init":
                Repository.initialize();
                break;
            case "add":
                Repository.add(args[1]);
                break;
            case "commit":
                if(args[1].equals("")){
                    System.out.print("Please enter a commit message.");
                    System.exit(0);
                }
                Repository.commit(args[1]);
                break;
            case "log":
                Repository.log();
                break;
            case "checkout":
                if(args.length == 2){
                    Repository.checkoutBranch(args[1]);
                }
                else if(args[1].equals("--") && args.length == 3){
                    Repository.checkout(args[2]);
                }
                else if(args[2].equals("--") && args.length == 4){
                    Repository.checkout(args[1], args[3]);
                }
                else{
                    System.out.print("Incorrect operands.");
                    System.exit(0);
                }
                break;
            case "global-log":
                Repository.globalLog();
                break;
            case "find":
                Repository.find(args[1]);
                break;
            case "rm":
                Repository.remove(args[1]);
                break;
            case "branch":
                Repository.branch(args[1]);
                break;
            case "status":
                Repository.status();
                break;
            case "rm-branch":
                Repository.removeBranch(args[1]);
                break;
            case "reset":
                Repository.reset(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
        }
    }

}
package gitlet;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.*;
import java.text.*;

//import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *
 * This class serves as the blueprint for a gitlet repository. It contains
 * necessary fields such as a staging area object for files to be staged, and
 * a commit tree to store all the commits made. It also contains references to
 * directories that save fields like the aforementioned so that the state of
 * the program persists. Additionally, this class contains methods that handle
 * the commands entered by the user.
 *
 *  @author Roberto Moron Jimenez
 */
public class Repository implements Serializable {

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = Utils.join(CWD, ".gitlet");


    private static HashMap<String, Commit> commitTree;  //key is the sha1 of commit and value is the commit
    private static StagingArea stage;
    private static File stagingAreaFolder = Utils.join(GITLET_DIR, "Staging Area");
    private static File blobsFolder = Utils.join(GITLET_DIR, "Blobs");
    private static File commitFolder = Utils.join(GITLET_DIR, "Commit");
    private static Commit initialCommit = new Commit();
    private static String master;  //The head master pointers should point to this initial commit
    private static File headFolder = Utils.join(GITLET_DIR, "Head");
    private static File branchesFolder = Utils.join(GITLET_DIR, "Branches");

    /** Initializes a repository and all the objects needed within it. */
    public static void initialize() {
        if (new File(".gitlet").exists()){
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        GITLET_DIR.mkdir();
        stagingAreaFolder.mkdir();
        commitFolder.mkdir();
        blobsFolder.mkdir();
        headFolder.mkdir();
        branchesFolder.mkdir();

        stage = new StagingArea();
        master = sha1(initialCommit);
        commitTree = new HashMap<String, Commit>();
        commitTree.put(sha1(initialCommit), initialCommit);

        save(stagingAreaFolder, "Staging Area", stage);
        save(commitFolder,"Commit Tree", commitTree);
        save(branchesFolder, "master", master);
        save(headFolder, "Head", "master");
    }

    /** Stores the serializable obj in the destination directory under the name fileName.
     * fileName will usually be the obj's HashID */
    public static void save(File desDirectory, String fileName, Serializable obj) {
        Utils.writeObject(Utils.join(desDirectory, fileName), obj);
    }

    /** Performs the add command for git. */
    public static void add(String fileName){
        File addedFile = Utils.join(Repository.CWD, fileName);
        if (!addedFile.exists()){
            System.out.println("File does not exist.");
            System.exit(0);
        }
        byte[] fileContents = Utils.readContents(addedFile);
        String addedFilesha1 = Utils.sha1(fileContents);

        StagingArea stage = Utils.readObject(Utils.join(stagingAreaFolder, "Staging Area"), StagingArea.class);
        HashMap<String, Commit> commitTree = Utils.readObject(Utils.join(commitFolder, "Commit Tree"), HashMap.class);
        String currentBranchName = Utils.readObject(Utils.join(headFolder, "Head"), String.class);
        String currentBranch = Utils.readObject(Utils.join(branchesFolder, currentBranchName), String.class);
        Commit currentCommit = commitTree.get(currentBranch);

        if(!currentCommit.getBlobs().containsKey(fileName) || !currentCommit.getBlobs().containsValue(addedFilesha1) && !stage.stageContainsSHA1(addedFilesha1, true)){
            stage.stageToAddition(fileName, addedFilesha1);
            Utils.writeContents(Utils.join(blobsFolder, addedFilesha1), fileContents);
        }
        else if(currentCommit.getBlobs().containsKey(fileName) && currentCommit.getBlobs().containsValue(addedFilesha1)  && stage.stageContainsSHA1(addedFilesha1, true)){
            stage.removeFromStage(fileName, true);
        }
        else if(currentCommit.getBlobs().containsValue(addedFilesha1)  && stage.stageContainsSHA1(addedFilesha1, false)){
            stage.removeFromStage(fileName, false);
        }
        save(stagingAreaFolder, "Staging Area", stage);
    }

    /** Commit function for git commit -m message */
    public static void commit(String message){
        StagingArea stage = Utils.readObject(Utils.join(stagingAreaFolder, "Staging Area"), StagingArea.class);
        HashMap<String, Commit> commitTree = Utils.readObject(Utils.join(commitFolder, "Commit Tree"), HashMap.class);
        String currentBranchName = Utils.readObject(Utils.join(headFolder, "Head"), String.class);
        String currentBranch = Utils.readObject(Utils.join(branchesFolder, currentBranchName), String.class); //currentBranch stores the sha1 of the current commit

        if(stage.getAdditionStage().isEmpty() && stage.getRemovalStage().isEmpty()){
            System.out.print("No changes added to the commit.");
            System.exit(0);
        }
        Commit parentCommit = commitTree.get(currentBranch);
        Commit newCommit = new Commit(message, currentBranch, new HashMap<String, String>());
        newCommit.addBlobs(parentCommit.getBlobs());
        newCommit.addBlobs(stage.getAdditionStage());
        newCommit.removeBlobs(stage.getRemovalStage());
        currentBranch = sha1(newCommit);
        commitTree.put(sha1(newCommit), newCommit);
        stage.erase();
        save(stagingAreaFolder, "Staging Area", stage);
        save(commitFolder, "Commit Tree", commitTree);
        save(branchesFolder, currentBranchName, currentBranch);
        save(headFolder, "Head", currentBranchName);
    }

    /** Returns the sha1 String of the commit object sent in */
    public static String sha1(Commit commit){
        return Utils.sha1(Utils.serialize(commit));
    }

    /** Returns the output of git log */
    public static void log(){
        HashMap<String, Commit> commitTree = Utils.readObject(Utils.join(commitFolder, "Commit Tree"), HashMap.class);
        String currentBranchName = Utils.readObject(Utils.join(headFolder, "Head"), String.class);
        String currentBranch = Utils.readObject(Utils.join(branchesFolder, currentBranchName), String.class);

        Commit currentCommit = commitTree.get(currentBranch);
        while(currentCommit.getParentSHA1() != null){
            printCommitLog(currentCommit);
            currentCommit = commitTree.get(currentCommit.getParentSHA1());
        }
        printCommitLog(currentCommit);
    }
    /** Returns the output of globalLog() */
    public static void globalLog(){
        HashMap<String, Commit> commitTree = Utils.readObject(Utils.join(commitFolder, "Commit Tree"), HashMap.class);
        commitTree.forEach((k,v) -> printCommitLog(v));
    }

    /** Prints and formats an individual commit as it should be in log */
    public static void printCommitLog(Commit commit){
        System.out.println("===");
        System.out.println("commit " + sha1(commit));
        System.out.println("Date: " + commit.getTimeStamp());
        System.out.println(commit.getMessage());
        System.out.println();
    }
    /** Checkout function for case 1, where the file with name filename is being checked out
     * from the current commit.  */
    public static void checkout(String filename){
        HashMap<String, Commit> commitTree = Utils.readObject(Utils.join(commitFolder, "Commit Tree"), HashMap.class);
        String currentBranchName = Utils.readObject(Utils.join(headFolder, "Head"), String.class);
        String currentBranch = Utils.readObject(Utils.join(branchesFolder, currentBranchName), String.class);
        Commit currentCommit = commitTree.get(currentBranch);

        if(!currentCommit.getBlobs().containsKey(filename)){
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        String checkoutSHA1 = currentCommit.getBlobs().get(filename);
        if(Utils.join(CWD, filename).exists()){
            Utils.restrictedDelete(filename);
        }
        String contents = Utils.readContentsAsString(Utils.join(blobsFolder, checkoutSHA1));
        Utils.writeContents(Utils.join(CWD,filename), contents);
    }

    /** Checkout function for case 2, where the file with name filename is being checked out from
     * the commit with id commitID. */
    public static void checkout(String commitID, String filename){
        HashMap<String, Commit> commitTree = Utils.readObject(Utils.join(commitFolder, "Commit Tree"), HashMap.class);
        if(commitID.length() < 40) {
            int commitIDlength = commitID.length();
            for (String id : commitTree.keySet()) {
                if(id.substring(0, commitIDlength).equals(commitID)) {
                    commitID = id;
                }
            }
        }
        if(!commitTree.containsKey(commitID)){
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        else if(!commitTree.get(commitID).getBlobs().containsKey(filename)){
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        String targetSHA1 = commitTree.get(commitID).getBlobs().get(filename);
        if(Utils.join(CWD, filename).exists()){
            Utils.restrictedDelete(filename);
        }
        String contents = Utils.readContentsAsString(Utils.join(blobsFolder,targetSHA1));
        Utils.writeContents(Utils.join(CWD,filename), contents);
    }

    /** Checkout function for case 3, where all the files in the commit at the head of the branch,
     * givenBranchName, are being checked out */
    public static void checkoutBranch(String givenBranchName){
        HashMap<String, Commit> commitTree = Utils.readObject(Utils.join(commitFolder, "Commit Tree"), HashMap.class);
        String currentBranchName = Utils.readObject(Utils.join(headFolder, "Head"), String.class);
        String currentBranch = Utils.readObject(Utils.join(branchesFolder, currentBranchName), String.class);
        Commit currentCommit = commitTree.get(currentBranch);
        StagingArea stage = Utils.readObject(Utils.join(stagingAreaFolder, "Staging Area"), StagingArea.class);

        if(!Utils.join(branchesFolder, givenBranchName).exists()){
            System.out.print("No such branch exists.");
            System.exit(0);
        }
        else if(givenBranchName.equals(currentBranchName)){
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }

        String givenBranch = Utils.readObject(Utils.join(branchesFolder, givenBranchName), String.class);
        Commit givenBranchCommit = commitTree.get(givenBranch);
        List<String> filesInCWD = Utils.plainFilenamesIn(CWD);

        for(int i = 0; i < filesInCWD.size(); i++){
            if(!currentCommit.getBlobs().containsKey(filesInCWD.get(i)) && givenBranchCommit.getBlobs().containsKey(filesInCWD.get(i))){
                System.out.print("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        for(int i = 0; i < filesInCWD.size(); i++){
            if(currentCommit.getBlobs().containsKey(filesInCWD.get(i)) && !givenBranchCommit.getBlobs().containsKey(filesInCWD.get(i))){
                Utils.restrictedDelete(filesInCWD.get(i));
            }
        }
        for(Map.Entry<String, String> element : givenBranchCommit.getBlobs().entrySet()){
            String sha1 = element.getValue();
            String filename = element.getKey();
            if(Utils.join(CWD, filename).exists()){
                Utils.restrictedDelete(filename);
            }
            String contents = Utils.readContentsAsString(Utils.join(blobsFolder,sha1));
            Utils.writeContents(Utils.join(CWD, filename), contents);
        }

        stage.erase();
        save(headFolder, "Head", givenBranchName);
        save(stagingAreaFolder, "Staging Area", stage);
    }

    /**Function for find command */
    public static void find(String commitMessage){
        boolean noCommitExists = true;
        HashMap<String, Commit> commitTree = Utils.readObject(Utils.join(commitFolder, "Commit Tree"), HashMap.class);
        for(Commit commit : commitTree.values()){
            if(commit.getMessage().equals(commitMessage)){
                System.out.println(sha1(commit)); //might fail because second new line
                noCommitExists = false;
            }
        }
        if(noCommitExists){
            System.out.print("Found no commit with that message.");
        }
    }

    /** Returns the output of git remove <filename> (essentially removes a file from a commit) */
    public static void remove(String filename){
        HashMap<String, Commit> commitTree = Utils.readObject(Utils.join(commitFolder, "Commit Tree"), HashMap.class);
        String currentBranchName = Utils.readObject(Utils.join(headFolder, "Head"), String.class);
        String currentBranch = Utils.readObject(Utils.join(branchesFolder, currentBranchName), String.class);
        Commit currentCommit = commitTree.get(currentBranch);
        StagingArea stage = Utils.readObject(Utils.join(stagingAreaFolder, "Staging Area"), StagingArea.class);

        if(!stage.getAdditionStage().containsKey(filename) && !currentCommit.getBlobs().containsKey(filename)){
            System.out.print("No reason to remove the file.");
            System.exit(0);
        }
        if(stage.getAdditionStage().containsKey(filename)){
            stage.removeFromStage(filename, true);
        }
        if(currentCommit.getBlobs().containsKey(filename)){
            String targetFileSHA1 = currentCommit.getBlobs().get(filename);
            stage.stageToRemoval(filename, targetFileSHA1);
            if(Utils.join(CWD, filename).exists()){
                Utils.restrictedDelete(filename);
            }
        }
        save(stagingAreaFolder, "Staging Area", stage);
    }


    /** Creates a new branch in the branchesFolder */
    public static void branch(String branchName){
        String currentBranchName = Utils.readObject(Utils.join(headFolder, "Head"), String.class);
        String currentBranch = Utils.readObject(Utils.join(branchesFolder, currentBranchName), String.class);

        if(Utils.join(branchesFolder, branchName).exists()){
            System.out.print("A branch with that name already exists.");
            System.exit(0);
        }
        save(branchesFolder, branchName, currentBranch);
    }

    /** Returns output of git status */
    public static void status(){
        String currentBranchName = Utils.readObject(Utils.join(headFolder, "Head"), String.class);
        StagingArea stage = Utils.readObject(Utils.join(stagingAreaFolder, "Staging Area"), StagingArea.class);
        List<String> branches = Utils.plainFilenamesIn(branchesFolder);
        Object[] additionStageFiles = stage.getAdditionStage().keySet().toArray();
        Arrays.sort(additionStageFiles);
        Object[] removalStageFiles = stage.getRemovalStage().keySet().toArray();
        Arrays.sort(removalStageFiles);

        //Printing
        System.out.println("=== Branches ===");
        for(String b : branches){
            if(currentBranchName.equals(b)){
                System.out.println("*" + currentBranchName);
            }
            else {
                System.out.println(b);
            }
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        for(Object s : additionStageFiles){
            System.out.println(s);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        for(Object s : removalStageFiles){
            System.out.println(s);
        }
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();

        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    /** Removes branch as in git rm-branch */
    public static void removeBranch(String branchName) {
        String currentBranchName = Utils.readObject(Utils.join(headFolder, "Head"), String.class);
        if(!Utils.join(branchesFolder, branchName).exists()){
            System.out.print(" A branch with that name does not exist.");
            System.exit(0);
        }
        if(currentBranchName.equals(branchName)){
            System.out.print("Cannot remove the current branch.");
            System.exit(0);
        }
        Utils.join(branchesFolder, branchName).delete();
    }

    /** Function for reset command */
    public static void reset(String commitID){
        HashMap<String, Commit> commitTree = Utils.readObject(Utils.join(commitFolder, "Commit Tree"), HashMap.class);
        String currentBranchName = Utils.readObject(Utils.join(headFolder, "Head"), String.class);
        String currentBranch = Utils.readObject(Utils.join(branchesFolder, currentBranchName), String.class);
        Commit currentCommit = commitTree.get(currentBranch);
        StagingArea stage = Utils.readObject(Utils.join(stagingAreaFolder, "Staging Area"), StagingArea.class);
        List<String> filesInCWD = Utils.plainFilenamesIn(CWD);
        if(commitID.length() < 40) {
            int commitIDlength = commitID.length();
            for (String id : commitTree.keySet()) {
                if(id.substring(0, commitIDlength).equals(commitID)) {
                    commitID = id;
                }
            }
        }
        if(!commitTree.containsKey(commitID)){
            System.out.print("No commit with that id exists.");
            System.exit(0);
        }
        Commit givenCommit = commitTree.get(commitID);
        for(int i = 0; i < filesInCWD.size(); i++){
            if(!currentCommit.getBlobs().containsKey(filesInCWD.get(i)) && givenCommit.getBlobs().containsKey(filesInCWD.get(i))){
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
            if(currentCommit.getBlobs().containsKey(filesInCWD.get(i)) && !givenCommit.getBlobs().containsKey(filesInCWD.get(i))){
                Utils.restrictedDelete(filesInCWD.get(i));
            }
        }
        for(String name : givenCommit.getBlobs().keySet()){
            checkout(commitID, name);
        }
        currentBranch = sha1(givenCommit);
        stage.erase();
        save(stagingAreaFolder, "Staging Area", stage);
        save(branchesFolder, currentBranchName, currentBranch);
    }
}
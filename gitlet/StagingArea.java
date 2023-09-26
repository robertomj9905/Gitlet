package gitlet;
import java.io.Serializable;
import java.util.HashMap;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

/** Represents a staging area object.
 *
 *  This class defines a staging area object by including
 *  relevant fields such as the stage for addition and the
 *  stage for removal. Additionally, relevant methods such as
 *  methods that add and remove files from either stage exist to define
 *  the behavior of the staging area.
 *
 *  @author Roberto Moron Jimenez
 */
public class StagingArea implements Serializable {

    /** Stores files staged for addition */
    private HashMap<String, String> addition;

    /** Stores files staged for removal*/
    private HashMap<String, String> removal;

    /** A staging area constructor containing a HashMap for the addition stage and one for the removal stage.
     * For each HashMap, key = fileName and value = sha1.*/
    public StagingArea(){
        addition = new HashMap<String, String>();
        removal = new HashMap<String, String>();
    }

    /** Clears the staging area */
    public void erase() {
        addition = new HashMap<String, String>();
        removal = new HashMap<String, String>();
    }

    /** Adds the file to the staging area for addition */
    public void stageToAddition(String fileName, String sha1){
        addition.put(fileName, sha1);
    }

    /** Adds the file to the staging area for removal */
    public void stageToRemoval(String fileName, String sha1){
        removal.put(fileName, sha1);
    }

    /** Returns the staging area HashMap for addition */
    public HashMap<String, String> getAdditionStage(){
        return this.addition;
    }

    /** Returns the staging area HashMap for removal */
    public HashMap<String, String> getRemovalStage(){
        return this.removal;
    }

    /** Checks if the identified staging area (i.e. for addition or removal) contains the sha1 value passed in */
    public boolean stageContainsSHA1(String sha1, boolean isAddition){
        if(isAddition){
            return this.addition.containsValue(sha1);
        }
        return this.removal.containsValue(sha1);
    }

    /** Removes a file from the identified StagingArea */
    public void removeFromStage(String filename, boolean isAddition){
        if(isAddition){
            this.addition.remove(filename);
        }
        this.removal.remove(filename);
    }

}


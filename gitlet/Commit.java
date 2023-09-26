package gitlet;
import java.util.*;
import java.text.*;
import java.util.Date;
import java.io.Serializable;
import java.util.HashMap;

/** Represents a gitlet commit object.
 *
 *  This class defines a commit by including relevant instance variables such as
 *  message, which stores the message of a commit, timestamp, which stores the time
 *  a commit was made, and blobs, a HashMap which stores the names and sha1 values
 *  of the files of the commit. Additionally, the class contains methods that define
 *  the behavior of a commit, such as getBlobs(), which returns the blobs of a commit.
 *
 *  @author Roberto Moron Jimenez
 */
public class Commit implements Serializable {

    /** The message of this Commit. */
    private String message;
    private String timestamp;
    private String parentCommit;

    /** Stores the file names and sha1 values of the files that were committed with this commit.
     * Keys are file names and values are sha1s */
    private HashMap<String, String> blobs;

    /** Creates the initial commit with message "initial commit" and Unix Epoch Date (Thu Jan 1 00:00:00 1970) */
    public Commit(){
        this.message = "initial commit";
        this.timestamp = createTimeStamp(new Date(0));
        blobs = new HashMap<String, String>();
        parentCommit = null;
    }

    /** Creates a commit constructor with an inputted message and timestamp when created */
    public Commit(String message, String parentCommit, HashMap<String, String> blobs){
        this.message = message;
        this.timestamp = createTimeStamp(new Date());
        this.blobs = blobs;
        this.parentCommit = parentCommit;
    }

    /** Returns the timestamp of a commit in the proper format. */
    public String createTimeStamp(Date date){
        SimpleDateFormat s = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z");
        s.setTimeZone(TimeZone.getTimeZone("PST"));
        String time = s.format(date);
        return time;
    }

    /** Returns a SHA1 ID for this Commit */
    public String createCommitID() {
        return Utils.sha1(Utils.serialize(this));
    }

    /** Returns the blob HashMap of this commit */
    public HashMap<String, String> getBlobs(){
        return this.blobs;
    }

    /** Adds the HashMap of blobs sent in to a commit object's blob HashMap */
    public void addBlobs(HashMap<String, String> added){
        added.forEach((k,v) -> this.blobs.put(k,v));
    }

    /** Removes all the key/value pairs of a commit object's blob Hashmap that correspond to the key/value pairs of the HashMap of blobs sent in */
    public void removeBlobs(HashMap<String, String> removal){
        removal.forEach((k,v) -> this.blobs.remove(k, v));
    }


    /** Returns Timestamp of Commit */
    public String getTimeStamp(){
        return this.timestamp;
    }

    /** Returns message of Commit */
    public String getMessage(){
        return this.message;
    }

    /** Returns parentSHA1 of Commit */
    public String getParentSHA1(){
        return this.parentCommit;
    }

}
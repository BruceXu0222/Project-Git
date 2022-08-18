package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

/** Stage class.
 * @author Bruce Xu
 */
@SuppressWarnings("unchecked")
public class Stage implements Serializable {
    /** the hashmap that records mapping between filename and corresponding blob sha1code in stage added. */
    private HashMap<String, String> added;
    /** the hashmap that records mapping between filename and corresponding blob sha1code in stage deleted. */
    private HashMap<String, String> deleted;
    /** the directory to store stage added. */
    private final File stageadded;
    /** the directory to store stage deleted. */
    private final File stagedeleted;

    /** Constructor. */
    public Stage() {
        added = new HashMap<>();
        deleted = new HashMap<>();
        stageadded = new File(".gitlet/.Stage/.added");
        stagedeleted = new File(".gitlet/.Stage/.deleted");
    }

    /** get the hashmap for stage added. */
    public HashMap<String, String> getadded() {
        return this.added;
    }

    /** get the hashmap for stage deleted. */
    public HashMap<String, String> getdeleted() {
        return this.deleted;
    }

    /** add file to stage added.
     * @param filename **the filename in string to be added**
     */
    public void toadd(String filename) {
        File toread = new File(filename);
        String contentread = Utils.readContentsAsString(toread);
        Blob blobtoadd = new Blob(filename, contentread);
        added.put(filename, blobtoadd.getsha1code());
        Utils.writeObject(stageadded, added);
    }

    /** add file to stage deleted.
     * @param filename **the filename in string to be deleted**
     */
    public void todelete(String filename) {
        File head = new File(".gitlet/.Head");
        Head currenthead = Utils.readObject(head, Head.class);
        Commit currentcommit = Utils.readObject
                (new File(".gitlet/.Commit/"
                        + currenthead.getHeadcommit()), Commit.class);
        String blobsha1 = currentcommit.getBloblist().get(filename);
        deleted.put(filename, blobsha1);
        Utils.writeObject(stagedeleted, deleted);
    }

    /** get the current staging area from stored files. */
    public void getcurrentstage() {
        added = Utils.readObject(stageadded, HashMap.class);
        deleted = Utils.readObject(stagedeleted, HashMap.class);
    }

    /** remove file from stage added.
     * @param filename **the filename in string to be removed**
     */
    public void removeblobadded(String filename) {
        added.remove(filename);
    }

    /** remove file from stage deleted.
     * @param filename **the filename in string to be removed**
     */
    public void removeblobdeleted(String filename) {
        deleted.remove(filename);
    }

    /** check if a file exists in stage added.
     * @param filename **the filename to check in string**
     */
    public boolean containfileadded(String filename) {
        if (added.size() == 0) {
            return false;
        } else {
            return added.containsKey(filename);
        }
    }

    /** check if a file exists in stage deleted.
     * @param filename **the filename to check in string**
     */
    public boolean containfiledeleted(String filename) {
        if (deleted.size() == 0) {
            return false;
        } else {
            return deleted.containsKey(filename);
        }
    }
}

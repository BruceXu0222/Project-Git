package gitlet;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/** Commit class.
 * @author Bruce Xu
 */
public class Commit implements Serializable {
    /** commit message in string. */
    private String message;
    /** sha1code of the commit. */
    private String sha1code;
    /** parent of the commit in string. */
    private String parent;
    /** hashmap to record the mapping between filename and blob sha1code. */
    private final HashMap<String, String> bloblist;
    /** timestamp of the commit. */
    private Date timestamp = new Date();

    /** Constructor.
     * @param messagein **commit message**
     * @param parentin **commit parent in string**
     */
    public Commit(String messagein, String parentin) {
        this.message = messagein;
        this.parent = parentin;
        this.bloblist = new HashMap<>();
        if (parent == null) {
            this.timestamp = new Date(0);
        }
        this.sha1code = Utils.sha1(message, Utils.serialize(timestamp));
    }

    /** get the commit message as string. */
    public String getMessage() {
        return this.message;
    }

    /** get the commit timestamp as string. */
    public String getTimestamp() {
        String pattern = "EEE MMM dd HH:mm:ss yyyy ZZZZZ";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(timestamp);
    }

    /** get the parent of this commit as string. */
    public String getParent() {
        return this.parent;
    }

    /** get the sha1code of this commit as string. */
    public String getSha1Code() {
        return this.sha1code;
    }

    /** get the hashmap to record the mapping between filename and blob sha1code. */
    public HashMap<String, String> getBloblist() {
        return this.bloblist;
    }

    /** modify the commit message.
     * @param messagein **new message to replace the old one**
     */
    public void modifymessage(String messagein) {
        this.message = messagein;
    }

    /** modify the timestamp of the commit. */
    public void modifytimestamp() {
        this.timestamp = new Date();
    }

    /** modify the parent of the commit.
     * @param commit **new parent commit**
     */
    public void modifyparent(Commit commit) {
        this.parent = commit.getSha1Code();
    }

    /** modify the hashmap to record the mapping between filename and blob sha1code.
     * @param stage **the stage used to modify bloblist**
     */
    public void modifyblob(Stage stage) {
        ArrayList<String> toremove = new ArrayList<>();
        for (String key: bloblist.keySet()) {
            if (stage.getadded().containsKey(key)) {
                bloblist.replace(key, stage.getadded().get(key));
            }
            if (stage.getdeleted().containsKey(key)) {
                toremove.add(key);
            }
        }
        for (String key: toremove) {
            bloblist.remove(key);
        }
        for (String key2: stage.getadded().keySet()) {
            if (!bloblist.containsKey(key2)) {
                bloblist.put(key2, stage.getadded().get(key2));
            }
        }
    }

    /** recalculate sha1code of the commit. */
    public void recalsha1() {
        this.sha1code = Utils.sha1(Utils.serialize(message),
                Utils.serialize(timestamp), Utils.serialize(parent),
                Utils.serialize(bloblist));
    }

    /** write the commit to a new file. */
    public void writecommit() {
        File thiscommit = new File(".gitlet/.Commit/" + sha1code);
        Utils.writeObject(thiscommit, this);
    }

    /** get the commit with the given commit sha1code.
     * @param name **the selected commit sha1code in string**
     */
    public Commit getcommit(String name) {
        File commitdir = new File(".gitlet/.Commit/" + name);
        return Utils.readObject(commitdir, Commit.class);
    }

    /** check if the commit contains a file.
     * @param filename **the filename to be checked**
     */
    public boolean containfile(String filename) {
        if (bloblist.size() == 0) {
            return false;
        } else {
            return bloblist.containsKey(filename);
        }
    }

}


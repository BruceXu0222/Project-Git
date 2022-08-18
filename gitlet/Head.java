package gitlet;
import java.io.Serializable;

/** Head class.
 * @author Bruce Xu
 */
public class Head implements Serializable {
    /** the sha1code of the head commit. */
    private final String commitsha1;

    /** Constructor.
     * @param sha1code **the sha1code of the commit that is set as head**
     */
    public Head(String sha1code) {
        this.commitsha1 = sha1code;
    }

    /** get the sha1code of head commit as string. */
    public String getHeadcommit() {
        return commitsha1;
    }
}

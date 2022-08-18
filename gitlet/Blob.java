package gitlet;

import java.io.Serializable;

/** Blob class.
 * @author Bruce Xu
 */
public class Blob implements Serializable {
    /** sha1code for the blob. */
    private final String sha1code;
    /** content as string for the blob. */
    private final String content;

    /** Constructor.
     * @param filenamein **filename as string**
     * @param contentin **content as string**
     */
    public Blob(String filenamein, String contentin) {
        this.content = contentin;
        this.sha1code = Utils.sha1(filenamein, content);
    }

    /** get the content of this blob as string. */
    public String getcontent() {
        return content;
    }

    /** get the sha1code of this blob as string. */
    public String getsha1code() {
        return sha1code;
    }

}

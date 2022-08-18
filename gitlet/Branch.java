package gitlet;

import java.io.Serializable;
import java.util.HashMap;

/** Branch class.
 * @author Bruce Xu
 */
public class Branch implements Serializable {
    /** The hashmap to record mapping between branchname as string and the commit sha1code as string. */
    private final HashMap<String, String> branches;
    /** The branchname of the current branch as string . */
    private String branchnow;

    /** Constructor. */
    public Branch() {
        branches = new HashMap<>();
        branchnow = "master";
    }

    /** add new branches to the hashmap.
     * @param branchname **branchname as string**
     * @param commitsha1 **commit sha1code as string**
     */
    public void addbranch(String branchname, String commitsha1) {
        branches.put(branchname, commitsha1);
    }

    /** delete existing branches from the hashmap.
     * @param branchname **branchname as string**
     */
    public void deletebranch(String branchname) {
        branches.remove(branchname);
    }

    /** get the hashmap to record mapping between branchname as string and the commit sha1code as string. */
    public HashMap<String, String> getBranches() {
        return branches;
    }

    /** get the current branch as string. */
    public String getBranchnow() {
        return branchnow;
    }

    /** modify existing branch mapping with new commit sha1code.
     * @param branchname **branchname as string**
     * @param commitsha1 **new commit sha1code as string**
     */
    public void modifybranch(String branchname, String commitsha1) {
        branches.replace(branchname, commitsha1);
    }

    /** modify the current branch.
     * @param branchname **new branchname for the current branch as string**
     */
    public void modifybranchnow(String branchname) {
        branchnow = branchname;
    }

    /** check if a branch exists.
     * @param branchname **branchname to test as string**
     */
    public boolean containbranch(String branchname) {
        return branches.containsKey(branchname);
    }
}

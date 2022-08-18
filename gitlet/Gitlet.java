package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/** Gitlet class.
 * @author Bruce Xu
 */
public class Gitlet {

    /** Commit at present. */
    private Commit currentcommit;
    /** Head at present. */
    private Head currenthead;
    /** Stage at present. */
    private Stage currentstage;
    /** Branch at present. */
    private Branch currentbranch;

    /** Get the current working directory. */
    private final File cwd = new File(System.getProperty("user.dir"));

    /** Commit folder directory. */
    private final File commitfolder = new File(".gitlet/.Commit");
    /** Branch file directory. */
    private final File branch = new File(".gitlet/.Branch");
    /** Blob folder directory. */
    private final File blobfolder = new File(".gitlet/.Blob");
    /** Stage folder directory. */
    private final File stagefolder = new File(".gitlet/.Stage");
    /** Head file directory. */
    private final File head = new File(".gitlet/.Head");
    /** Staged for addition file directory. */
    private final File added = new File(".gitlet/.Stage/.added");
    /** Staged for deletion file directory. */
    private final File deleted = new File(".gitlet/.Stage/.deleted");


    /** Init operation. */
    public void init() throws IOException {
        File gitlet = new File(".gitlet");
        if (gitlet.exists()) {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
        } else {
            gitlet.mkdir();

            commitfolder.mkdir();
            branch.createNewFile();
            blobfolder.mkdir();
            stagefolder.mkdir();
            head.createNewFile();
            added.createNewFile();
            deleted.createNewFile();

            currentstage = new Stage();
            currentcommit = new Commit("initial commit", null);
            currentbranch = new Branch();
            currentbranch.addbranch("master", currentcommit.getSha1Code());
            currenthead = new Head(currentcommit.getSha1Code());

            File initialcommit = new File(".gitlet/.Commit/"
                    + currentcommit.getSha1Code());
            initialcommit.createNewFile();

            Utils.writeObject(initialcommit, currentcommit);
            Utils.writeObject(branch, currentbranch);
            Utils.writeObject(head, currenthead);
            Utils.writeObject(added, currentstage.getadded());
            Utils.writeObject(deleted, currentstage.getdeleted());
        }
    }

    /** Commit operation.
     * @param message **The commit message as string**
     */
    public void commit(String message) {
        currenthead = Utils.readObject(head, Head.class);
        currentcommit = Utils.readObject
                (new File(".gitlet/.Commit/"
                        + currenthead.getHeadcommit()), Commit.class);
        currentbranch = Utils.readObject(branch, Branch.class);
        currentstage = new Stage();
        currentstage.getcurrentstage();
        if (currentstage.getadded().
                equals(new HashMap<String, String>())
                && currentstage.getdeleted().
                equals(new HashMap<String, String>())) {
            System.out.println("No changes added to the commit.");
            return;
        }
        Commit updatedcommit = currentcommit;
        updatedcommit.modifymessage(message);
        updatedcommit.modifytimestamp();
        updatedcommit.modifyblob(currentstage);
        updatedcommit.modifyparent(currentcommit);
        updatedcommit.recalsha1();
        updatedcommit.writecommit();
        Head updatedhead = new Head(updatedcommit.getSha1Code());
        Utils.writeObject(head, updatedhead);
        currentbranch.modifybranch(currentbranch.getBranchnow(),
                updatedcommit.getSha1Code());
        Utils.writeObject(branch, currentbranch);
        currentstage = new Stage();
        Utils.writeObject(added, currentstage.getadded());
        Utils.writeObject(deleted, currentstage.getdeleted());
    }

    /** Find operation.
     * @param message **The find message as string**
     */
    public void find(String message) {
        boolean finded = false;
        for (String commitname: Objects.requireNonNull
                (Utils.plainFilenamesIn(commitfolder))) {
            Commit current = Utils.readObject
                    (new File(".gitlet/.Commit/"
                            + commitname), Commit.class);
            if (current.getMessage().equals(message)) {
                System.out.println(commitname);
                finded = true;
            }
        }
        if (!finded) {
            System.out.println("Found no commit with that message.");
        }
    }

    /** Log operation. */
    public void log() {
        currenthead = Utils.readObject(head, Head.class);
        currentcommit = Utils.readObject
                (new File(".gitlet/.Commit/"
                        + currenthead.getHeadcommit()), Commit.class);
        Commit tracking = currentcommit;

        while (tracking != null) {
            System.out.println("===");
            System.out.println("commit " + tracking.getSha1Code());
            System.out.println("Date: " + tracking.getTimestamp());
            System.out.println(tracking.getMessage());
            System.out.println();
            if (tracking.getParent() == null) {
                break;
            }
            tracking = tracking.getcommit(tracking.getParent());
        }
    }

    /** Global_log operation. */
    public void globallog() {
        for (String commitname: Objects.requireNonNull
                (Utils.plainFilenamesIn(commitfolder))) {
            Commit current = Utils.readObject
                    (new File(".gitlet/.Commit/"
                            + commitname), Commit.class);
            System.out.println("===");
            System.out.println("commit " + current.getSha1Code());
            System.out.println("Date: " + current.getTimestamp());
            System.out.println(current.getMessage());
            System.out.println();
        }
    }

    /** Add operation.
     * @param filename **Filename to add as string**
     */
    public void add(String filename) throws IOException {
        File toadd = new File(filename);
        if (!toadd.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        Blob temp = new Blob(filename, Utils.readContentsAsString(toadd));
        currenthead = Utils.readObject(head, Head.class);
        currentcommit = Utils.readObject
                (new File(".gitlet/.Commit/"
                        + currenthead.getHeadcommit()), Commit.class);
        currentbranch = Utils.readObject(branch, Branch.class);
        currentstage = new Stage();
        currentstage.getcurrentstage();

        if (!currentcommit.containfile(filename)) {
            addhelper(currentstage, filename, temp);
        } else {
            if (currentcommit.getBloblist().
                    get(filename).equals(temp.getsha1code())) {
                if (currentstage.containfileadded(filename)) {
                    currentstage.removeblobadded(filename);
                    Utils.writeObject(added, currentstage.getadded());
                } else if (currentstage.containfiledeleted(filename)) {
                    currentstage.removeblobdeleted(filename);
                    Utils.writeObject(deleted, currentstage.getdeleted());
                }
            } else {
                addhelper(currentstage, filename, temp);
            }
        }
    }

    /** Add helper.
     * @param stage **The stage at the time of staged for addition operation**
     * @param filename **The filename as string to be added for stage**
     * @param blob **The blob that contains the content for addition**
     */
    public void addhelper(Stage stage, String filename, Blob blob)
            throws IOException {
        File toaddblob = new File(".gitlet/.Blob/"
                + blob.getsha1code());
        if (stage.containfileadded(filename)) {
            stage.removeblobadded(filename);
        }
        stage.toadd(filename);
        Utils.writeObject(added, stage.getadded());
        toaddblob.createNewFile();
        Utils.writeContents(toaddblob, blob.getcontent());
    }

    /** Remove operation.
     * @param filename **The filename as string to be deleted for stage**
     */
    public void rm(String filename) {
        File torm = new File(filename);
        currenthead = Utils.readObject(head, Head.class);
        currentcommit = Utils.readObject
                (new File(".gitlet/.Commit/"
                        + currenthead.getHeadcommit()), Commit.class);
        currentstage = new Stage();
        currentstage.getcurrentstage();
        if ((!currentstage.containfileadded(filename))
                && (!currentcommit.containfile(filename))) {
            System.out.println("No reason to remove the file.");
            return;
        }
        if (currentstage.containfileadded(filename)) {
            currentstage.removeblobadded(filename);
            Utils.writeObject(added, currentstage.getadded());
        }
        if (currentcommit.containfile(filename)) {
            currentstage.todelete(filename);
            Utils.writeObject(deleted, currentstage.getdeleted());
            Utils.restrictedDelete(torm);
        }
    }

    /** Status operation. */
    public void status() {
        if (!new File(".gitlet").exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        currentbranch = Utils.readObject(branch, Branch.class);
        System.out.println("=== Branches ===");
        Set<String> branchset = currentbranch.getBranches().keySet();
        List<String> branchlist = new ArrayList<>(branchset);
        Collections.sort(branchlist);
        StringBuilder pattern = new StringBuilder();
        for (String s: branchlist) {
            if (s.equals(currentbranch.getBranchnow())) {
                pattern.append("*");
            }
            pattern.append(s);
            pattern.append("\n");
        }
        System.out.println(pattern);
        currentstage = new Stage();
        currentstage.getcurrentstage();
        System.out.println("=== Staged Files ===");
        for (String key: currentstage.getadded().keySet()) {
            System.out.println(key);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String key: currentstage.getdeleted().keySet()) {
            System.out.println(key);
        }
        statusextra();
    }

    /** Status operation continued. */
    public void statusextra() {
        currentstage = new Stage();
        currentstage.getcurrentstage();
        currenthead = Utils.readObject(head, Head.class);
        currentcommit = Utils.readObject
                (new File(".gitlet/.Commit/"
                        + currenthead.getHeadcommit()), Commit.class);
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String filename: Objects.requireNonNull
                (Utils.plainFilenamesIn(cwd))) {
            String contentindirectory = Utils.readContentsAsString
                    (new File(filename));
            if ((currentcommit.containfile(filename)
                    && (!Utils.readContentsAsString
                            (new File(".gitlet/.Blob/"
                                    + currentcommit.getBloblist().
                                    get(filename))).
                    equals(contentindirectory))
                    && (!currentstage.containfileadded(filename)))
                    || ((currentstage.containfileadded(filename))
                    && (!Utils.readContentsAsString
                            (new File(".gitlet/.Blob/"
                                    + currentstage.getadded().get(filename))).
                    equals(contentindirectory)))) {
                System.out.println(filename + " (modified)");
            }
        }
        StringBuilder processed = new StringBuilder();
        for (String filename: currentstage.getadded().keySet()) {
            if (!new File(filename).exists()) {
                if (!processed.toString().contains(filename)) {
                    System.out.println(filename + " (deleted)");
                    processed.append(filename);
                }
            }
        }
        for (String filename: currentcommit.getBloblist().keySet()) {
            if (!currentstage.containfiledeleted(filename)
                    && !new File(filename).exists()) {
                if (!processed.toString().contains(filename)) {
                    System.out.println(filename + " (deleted)");
                    processed.append(filename);
                }
            }
        }
        System.out.println();
        System.out.println("=== Untracked Files ===");
        for (String filename: Objects.requireNonNull
                (Utils.plainFilenamesIn(cwd))) {
            if ((!currentstage.containfileadded(filename))
                    && (!currentcommit.containfile(filename))) {
                System.out.println(filename);
            }
        }
    }

    /** Checkout recent commit operation.
     * @param filename **The filename as string to checkout in the most recent commit**
     */
    public void checkout(String filename) {
        currenthead = Utils.readObject(head, Head.class);
        currentcommit = Utils.readObject
                (new File(".gitlet/.Commit/"
                        + currenthead.getHeadcommit()), Commit.class);
        checkout(currentcommit.getSha1Code(), filename);
    }

    /** Checkout specific commit operation.
     * @param commitsha1 **The sha1code for the commit of the checkout operation**
     * @param filename **The filename as string to checkout in the given commit**
     */
    public void checkout(String commitsha1, String filename) {
        boolean hascommit = false;
        for (String commitid: Objects.requireNonNull
                (Utils.plainFilenamesIn(commitfolder))) {
            if (commitid.contains(commitsha1)) {
                hascommit = true;
                commitsha1 = commitid;
            }
        }
        if (!hascommit) {
            System.out.println("No commit with that id exists");
            return;
        }
        File selectedcommit = new File(".gitlet/.Commit/" + commitsha1);
        if (!Utils.readObject(selectedcommit, Commit.class).
                containfile(filename)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        Commit thiscommit = Utils.readObject(selectedcommit, Commit.class);
        String selectedsha1 = thiscommit.getBloblist().get(filename);
        File selectedblob = new File(".gitlet/.Blob/" + selectedsha1);
        String toreplace = Utils.readContentsAsString(selectedblob);
        Utils.writeContents(new File(filename), toreplace);
    }

    /** Checkout branch operation.
     * @param branchname **The branchname as string to checkout its head commit**
     */
    public void checkout2(String branchname) throws IOException {
        currenthead = Utils.readObject(head, Head.class);
        currentbranch = Utils.readObject(branch, Branch.class);
        currentcommit = Utils.readObject(
                new File(".gitlet/.Commit/" + currenthead.getHeadcommit()),
                Commit.class);
        boolean havebranch = false;
        for (String thisbranch: currentbranch.getBranches().keySet()) {
            if (thisbranch.equals(branchname)) {
                havebranch = true;
                break;
            }
        }
        if (!havebranch) {
            System.out.println("No such branch exists.");
            return;
        }
        if (currentbranch.getBranchnow().equals(branchname)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        String targetsha1 = currentbranch.getBranches().get(branchname);
        Commit targetcommit = Utils.readObject
                (new File(".gitlet/.Commit/" + targetsha1), Commit.class);
        for (String filename: Objects.requireNonNull
                (Utils.plainFilenamesIn(cwd))) {
            if (!currentcommit.containfile(filename)
                    && targetcommit.containfile(filename)) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                return;
            }
        }
        for (String filename: targetcommit.getBloblist().keySet()) {
            String blobsha1 = targetcommit.getBloblist().get(filename);
            String content = Utils.readContentsAsString
                    (new File(".gitlet/.Blob/" + blobsha1));
            File thisfile = new File(filename);
            if (!thisfile.exists()) {
                thisfile.createNewFile();
            }
            Utils.writeContents(thisfile, content);
        }
        for (String filename: currentcommit.getBloblist().keySet()) {
            File thisfile = new File(filename);
            if (!targetcommit.containfile(filename)) {
                Utils.restrictedDelete(thisfile);
            }
        }
        currentbranch.modifybranchnow(branchname);
        Utils.writeObject(branch, currentbranch);
        currenthead = new Head(targetcommit.getSha1Code());
        Utils.writeObject(head, currenthead);
        currentstage = new Stage();
        Utils.writeObject(added, currentstage.getadded());
        Utils.writeObject(deleted, currentstage.getdeleted());
    }

    /** Branch operation.
     * @param branchname **The branchname as string to add a new branch**
     */
    public void branch(String branchname) {
        currentbranch = Utils.readObject(branch, Branch.class);
        for (String filename: currentbranch.getBranches().keySet()) {
            if (filename.equals(branchname)) {
                System.out.println("A branch with that name already exists.");
                return;
            }
        }
        currenthead = Utils.readObject(head, Head.class);
        currentcommit = Utils.readObject
                (new File(".gitlet/.Commit/"
                        + currenthead.getHeadcommit()), Commit.class);
        currentbranch.addbranch(branchname, currentcommit.getSha1Code());
        Utils.writeObject(branch, currentbranch);
    }

    /** Rmbranch operation.
     * @param branchname **The branchname as string to delete the given branch**
     */
    public void rmbranch(String branchname) {
        currentbranch = Utils.readObject(branch, Branch.class);
        if (!currentbranch.getBranches().containsKey(branchname)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (currentbranch.getBranchnow().equals(branchname)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        currentbranch.deletebranch(branchname);
        Utils.writeObject(branch, currentbranch);
    }

    /** Reset operation.
     * @param commitsha1 **The sha1code for the commit we wish to reset to**
     */
    public void reset(String commitsha1) throws IOException {
        File targetdir = new File(".gitlet/.Commit/" + commitsha1);
        if (!targetdir.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        currentbranch = Utils.readObject(branch, Branch.class);
        currenthead = Utils.readObject(head, Head.class);
        currentcommit = Utils.readObject
                (new File(".gitlet/.Commit/"
                        + currenthead.getHeadcommit()), Commit.class);
        currentstage = new Stage();
        Commit targetcommit = Utils.readObject(targetdir, Commit.class);
        for (String filename: Objects.requireNonNull
                (Utils.plainFilenamesIn(cwd))) {
            if (!currentcommit.containfile(filename)
                    && targetcommit.containfile(filename)) {
                System.out.println("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
                return;
            }
        }
        String branchnow = currentbranch.getBranchnow();
        currentbranch.addbranch("helper", targetcommit.getSha1Code());
        Utils.writeObject(branch, currentbranch);
        checkout2("helper");
        currentbranch.modifybranchnow(branchnow);
        currentbranch.deletebranch("helper");
        currentbranch.modifybranch(branchnow, targetcommit.getSha1Code());
        Utils.writeObject(branch, currentbranch);
        currenthead = new Head(targetcommit.getSha1Code());
        Utils.writeObject(head, currenthead);
    }

    /** Merge operation.
     * @param branchname **The branchname as string to merge with the current branch**
     */
    public void merge(String branchname) throws IOException {
        currentstage = new Stage();
        currentstage.getcurrentstage();
        if (!currentstage.getadded().equals(new HashMap<String, String>())
                || !currentstage.getdeleted().
                equals(new HashMap<String, String>())) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        currentbranch = Utils.readObject(branch, Branch.class);
        if (!currentbranch.containbranch(branchname)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (currentbranch.getBranchnow().equals(branchname)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        String givensha1 = currentbranch.getBranches().get(branchname);
        Commit givencommit = Utils.readObject
                (new File(".gitlet/.Commit/" + givensha1), Commit.class);
        currenthead = Utils.readObject(head, Head.class);
        currentcommit = Utils.readObject(new File(".gitlet/.Commit/"
                + currenthead.getHeadcommit()), Commit.class);
        for (String filename: Objects.requireNonNull
                (Utils.plainFilenamesIn(cwd))) {
            if (!currentcommit.containfile(filename)
                    && givencommit.containfile(filename)) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                return;
            }
        }
        Commit splitcommit = getsplit(currentcommit, givencommit);
        if (splitcommit.equals(givencommit)) {
            System.out.println("Given branch is an "
                    + "ancestor of the current branch.");
            return;
        }
        if (splitcommit.equals(currentcommit)) {
            checkout2(branchname);
            System.out.println("Current branch fast-forwarded.");
            return;
        }
        mergenoconflict(splitcommit, currentcommit, givencommit);
        commit("Merged " + branchname + " into "
                + currentbranch.getBranchnow() + ".");
    }

    /** Merge without conflict, subpart of merge.
     * @param split **The commit where the two branch split**
     * @param current **The current head commit**
     * @param given **The head commit of the given branch**
     */
    public void mergenoconflict(Commit split, Commit current, Commit given)
            throws IOException {
        for (String filename: split.getBloblist().keySet()) {
            String splitsha1 = split.getBloblist().get(filename);
            if (current.containfile(filename)) {
                String currentsha1 = current.getBloblist().get(filename);
                if (splitsha1.equals(currentsha1)) {
                    if (!given.containfile(filename)) {
                        rm(filename);
                    } else {
                        String givensha1 = given.getBloblist().get(filename);
                        if (!splitsha1.equals(givensha1)) {
                            checkout(given.getSha1Code(), filename);
                            add(filename);
                        }
                    }
                }
            }
        }
        for (String filename: given.getBloblist().keySet()) {
            if ((!split.containfile(filename))
                    && (!current.containfile(filename))) {
                checkout(given.getSha1Code(), filename);
                add(filename);
            }
        }
    }
    /** Get the split commit.
     * @param current **The current head commit**
     * @param given **The head commit of the given branch**
     */
    public Commit getsplit(Commit current, Commit given) {
        ArrayList<String> currentlist = new ArrayList<>();
        ArrayList<String> givenlist = new ArrayList<>();
        String splitsha1 = "";
        while (current.getParent() != null) {
            currentlist.add(current.getSha1Code());
            current = Utils.readObject(new File(".gitlet/.Commit/"
                    + current.getParent()), Commit.class);

        }
        while (given.getParent() != null) {
            givenlist.add(given.getSha1Code());
            given = Utils.readObject(new File(".gitlet/.Commit/"
                    + given.getParent()), Commit.class);
        }
        Collections.reverse(currentlist);
        Collections.reverse(givenlist);
        int smaller = Math.min(currentlist.size(), givenlist.size());
        for (int i = 0; i < smaller; i++) {
            if (currentlist.get(i).equals(givenlist.get(i))) {
                splitsha1 = currentlist.get(i);
            }
        }
        return current.getcommit(splitsha1);
    }

}

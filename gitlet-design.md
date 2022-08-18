# Gitlet Design Document

**Name**: Bruce Xu

## Classes and Data Structures
### Gitlet
####Variables
1. Commits: a hashmap that stores all commits
2. Branches: a hashmap that stores all branches
3. Directory: a string that marks the directory gitlet is working
4. Stagingarea: a stage that records the staging status for both added and removed files
5. Workingdirectory: a file ends in ".gitlet" at the location Directory
6. Headcommit: a string of the object of the headcommit
7. Headbranch: a string of the object of the headbranch

### Stage
####Variables
1. Stageadded
2. Stageremoved

### Commit
####Variables
1. Mysha: a string of SHA-1 identifier of this commit
2. Psha: a string of SHA-1 identifier of my parent commit
3. Timestamp
4. Logmessage
5. Parentset: a hashmap that stores all blobs of the parent commit
6. Myblob: a hashmap that stores all blobs of this commit

### Blob
####Variables
1. Mysha: a string of SHA-1 identifier of the commit of this blob
2. Filename: a string of the name of the blob file
3. Directory: the address of the blob file
4. Content: the content of the directory

### Serialization


## Algorithms
### Gitlet
1. Gitlet(): the class constructor, set the directory as the current system directory, set the workingdirectory as directory.gitlet. Set the commit, branch, stagingarea, headcommit, and headbranch to the information of this commit
2. Init(), Commit(String message), rm(String name), add(String name), log(), globallog(), find(String message), status(), checkout(String branchname), checkout(String filename), checkout(String SHA-identifier), branch(String branchname), rmbranch(String branchname), reset(String SHA-identifier)

### Stage
1. Stage(): the class constructor, creates empty space for both Stageadded and Stageremoved
2. Clear(): resets the staging area, clear all the records for stageadded and stageremoved

### Commit
1. Commit(String message, HashMap<String, Blob> currentfiles, HashMap<String, Blob> parentfiles, String Psha): the class constructor, sets mysha to the SHA-1 identifier of this commit, set psha to the SHA-1 identifier of my parent commit, set timestamp, logmessage, set parentset, myblob accordingly
2. Gettime(): get the happening time of the commit


### Blob
1. Blob(String filename, String location): the class constructor, set mysha to the SHA-1 identifier of the commit of this blob, set the filename to the filename provided, set the directory to the file address provided
2. Equals(Object another): compared the content of two blobs

### Serialization
1. Read(File file): read the content of the file
2. Write(File file, Object object): write object as the content of the file



## Persistence
In order to persist the settings of Gitlet class, we need to save the state of Gitlet after each operations on the Gitlet class. To do this,
1. Write the gitlet file into disk, we can serialize them into bytes that we can eventually write to a special named file on disk. This can be done by the Write method in Class Serialization.
2. Write the Commits hashmap and Branches hashmap into disk. This can be done by the Write method in Class Serialization. We will make sure that the Gitlet class implements the Serializable interface.





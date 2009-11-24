package utd.pallet.classification;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import utd.pallet.classification.MalletTextDataTrainer.TrainerObject;
import utd.pallet.data.RDFUtils;
import cc.mallet.classify.Classifier;
import cc.mallet.types.InstanceList;

/**
 * @author sharath
 * 
 */
public class FetchDirData {

    private List<File> rootDirFileList = new ArrayList<File>();

    /**
     * 
     */
    public FetchDirData(ArrayList<String> rootDirectories)
            throws NullPointerException {

        if (rootDirectories == null)
            throw new NullPointerException(
                    "Null specified for directories to be parsed");

        for (int i = 0; i < rootDirectories.size(); i++) {
            File dir = new File(rootDirectories.get(i));
            rootDirFileList.add(dir);
        }

    }

    /**
     * @param aDirectory
     * @param directory
     * @throws FileNotFoundException
     * @throws IllegalArgumentException
     * @throws NullPointerException
     * @throws Exception
     */
    private void CheckValidityofDirectory(File aDirectory)
            throws FileNotFoundException, IllegalArgumentException {
        if (aDirectory == null) {
            throw new IllegalArgumentException("Directory should not be null.");
        }
        if (!aDirectory.exists()) {
            throw new FileNotFoundException("Directory does not exist: "
                    + aDirectory);
        }
        if (!aDirectory.isDirectory()) {
            throw new IllegalArgumentException("Is not a directory: "
                    + aDirectory);
        }
        if (!aDirectory.canRead()) {
            throw new IllegalArgumentException("Directory cannot be read: "
                    + aDirectory);
        }
    }

    /**
     * @param file
     * @return
     * @throws Exception
     */
    private InstanceList FetchDatafromFile(File file,
            TrainerObject trainerObject,String classificationPredicate) throws Exception, NullPointerException {

        InstanceList iList = null;
        // System.out.println(file.toString());
try{
        String fileContents = null;
        try {
            fileContents = org.apache.commons.io.FileUtils
                    .readFileToString(file);
        } catch (IOException e) {
            // TODO Auto-generated catch block
          throw e;
        }

        Classifier prevClassifier = null;
        // System.out.println(fileContents);
        if (trainerObject != null)
            prevClassifier = (Classifier) trainerObject.getClassifier();

        iList = RDFUtils.convertRDFToInstanceList(fileContents, prevClassifier,classificationPredicate);
}catch(Exception e)
{
	
	throw e;
}
        return iList;
    }

    /**
     * @param directory
     * @return
     * @throws NullPointerException
     * @throws Exception
     */
    private InstanceList ParseDirectory(File directory,
            TrainerObject prevTrainerObj,String classificationPredicate) throws IllegalArgumentException,
            FileNotFoundException,Exception {

        File[] dirContents = directory.listFiles();

        InstanceList iList = null;
        for (File filesinDir : dirContents) {
            if (filesinDir.isFile()) {
                try {
                    iList = FetchDatafromFile(filesinDir, prevTrainerObj,classificationPredicate);
                } catch (Exception e) {
                    
                	throw e;
                    
                }
            } else {
                try {
                    CheckValidityofDirectory(filesinDir);
                } catch (IllegalArgumentException nE) {
                    throw nE;
                } catch (FileNotFoundException e) {
                    throw e;
                }
                catch(Exception e)
                {
                	throw e;
                }

                iList = ParseDirectory(filesinDir, prevTrainerObj,classificationPredicate);
                // iList.addAll(tList);
            }
        }

        return iList;
    }

    /**
     * 
     * @throws NullPointerException
     * @throws Exception
     * @return
     */
    // trainer = null for first time training
    public InstanceList ParseDirectoryList(TrainerObject trainer,String classificationPredicate)
            throws Exception {
        InstanceList iList = null;

        // Parse thru all directories in the list
        for (int i = 0; i < rootDirFileList.size(); i++) {
            // If file then extract data
            if (((File) this.rootDirFileList.get(i)).isFile()) {

                InstanceList tList = null;
                try {
                    iList = FetchDatafromFile((File) this.rootDirFileList
                            .get(i), trainer,classificationPredicate);

                    File fl = (File) this.rootDirFileList.get(i);

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    
                    throw e;
                }

            } else {
                // Validate whether it is a directory and has read permission
                try {
                    CheckValidityofDirectory((File) this.rootDirFileList.get(i));
                } catch (IllegalArgumentException iAE) {
                    throw iAE;
                } catch (FileNotFoundException e) {
                    throw e;
                }
                catch(Exception e)
                {
                	throw e;
                }

                InstanceList tList = null;
                try {
                    tList = ParseDirectory((File) this.rootDirFileList.get(i),
                            trainer,classificationPredicate);
                } catch (IllegalArgumentException e) {
                    throw e;
                } catch (FileNotFoundException e) {
                    throw e;
                }
                catch(Exception e)
                {
                	throw e;
                }

                if (iList == null)
                    iList = tList;
                else
                    iList.addAll(tList);
            }
        }

        return iList;
    }

    public static void main(String[] args) throws Exception  {

        ArrayList<String> dirList = new ArrayList<String>();
        dirList.add("/home/sharath/websemantic_lab/data");

        FetchDirData fd = new FetchDirData(dirList);
          String classificationPredicate=null;
        InstanceList iList = null;
        try {
            iList = fd.ParseDirectoryList(null,classificationPredicate);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            throw e;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            throw e;
        }

//        System.out.println("Size - " + iList.size());
    }
}

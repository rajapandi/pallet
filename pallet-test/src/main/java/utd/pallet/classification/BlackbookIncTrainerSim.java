package utd.pallet.classification;

import java.util.ArrayList;

import utd.pallet.classification.MalletTextDataTrainer.TrainerObject;
import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.classify.NaiveBayesTrainer;
import cc.mallet.types.InstanceList;

/**
 * Class that simulates Incremental Training.
 * 
 */
public class BlackbookIncTrainerSim {

    /**
     * Instance List that is used for training the classifier.
     */
    InstanceList iList = null;
    /**
     * Wrapper for Classifier
     */
    TrainerObject trnObj = null;

    /**
     * 
     */
    public BlackbookIncTrainerSim() {

    }

    /**
     * @param sourceName
     *            Source folder from which the training data is fetched for
     *            training.
     * @param trainerObj
     *            Trainer Object on which the data is to be trained.
     * @param classificationPredicate
     *            classificationPredicate that needs to be searched in the
     *            files.
     * @throws Exception
     */
    public void fetchData(ArrayList<String> sourceName,
            TrainerObject trainerObj, String classificationPredicate)
            throws Exception {

        FetchDirData dataToMallet = new FetchDirData(sourceName);

        try {
            iList = dataToMallet.ParseDirectoryList(trainerObj,
                    classificationPredicate);
        } catch (Exception e) {

            throw e;
        }
    }

    /**
     * @param prevTrainer
     *            Trainer that needs to be incrementally trained.
     * @return New instance of TrainerObject
     * @throws NullPointerException
     * @throws Exception
     */

    @SuppressWarnings("unchecked")
    public TrainerObject bbIncTrain(ClassifierTrainer prevTrainer)
            throws NullPointerException, Exception {

        MalletTextDataTrainer dataTrainer = new MalletTextDataTrainer();

        try {
            trnObj = dataTrainer.trainIncremental((NaiveBayesTrainer)prevTrainer, iList);
        } catch (NullPointerException ne) {
            throw ne;
        } catch (Exception e) {
            throw e;
        }

        return trnObj;
    }

    /**
     * @param filename
     *            Filename to be used to persist the data.
     * @throws Exception
     */
    public void SaveClassifier(String filename) throws Exception {

        if (filename == null || this.trnObj == null)
            throw new NullPointerException(
                    "Either the training data is not yet trained or filename is null");

        BlackbookSimUtils.saveTrainerObject(this.trnObj, filename);
    }
}

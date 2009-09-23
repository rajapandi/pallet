package utd.pallet.classification;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import utd.pallet.classification.MalletTextDataTrainer.TrainerObject;
import cc.mallet.types.InstanceList;

public class BlackbookTrainingSimulator {

    InstanceList iList = null;
    FetchDirData dataToMallet = null;
    TrainerObject trainerObject = null;

    public BlackbookTrainingSimulator() {
    }

    // trainer == null for first time training
    public void fetchData(ArrayList<String> sourceName, TrainerObject trainerObj)
            throws NullPointerException, IllegalArgumentException {

        dataToMallet = new FetchDirData(sourceName);

        try {
            iList = dataToMallet.ParseDirectoryList(trainerObj);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @param trainingAlgorithm
     * @return
     * @throws NullPointerException
     * @throws Exception
     */
    public TrainerObject train(int trainingAlgorithm)
            throws NullPointerException, Exception {

        MalletTextDataTrainer trainer = new MalletTextDataTrainer();

        if (iList == null)
            throw new Exception(
                    "Training cannot be dont on empty Data Set, fetch Data before training");

        try {
            this.trainerObject = trainer.train(iList, trainingAlgorithm);
        } catch (NullPointerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw e;
        }

        return this.trainerObject;
    }

    public void saveTrainer(String filename) throws IOException, Exception {

        int size;

        if (filename == null || this.trainerObject == null)
            throw new NullPointerException(
                    "Either the training data is not yet trained or filename is null");

        BlackbookSimUtils.saveTrainerObject(this.trainerObject, filename);
    }
}

package utd.pallet.classification;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import utd.pallet.classification.MalletTextDataTrainer.TrainerObject;
import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.types.InstanceList;

public class BlackbookIncTrainerSim {

    InstanceList iList = null;
    TrainerObject trnObj = null;

    public BlackbookIncTrainerSim() {

    }

    public void fetchData(ArrayList<String> sourceName, TrainerObject trainerObj)
            throws NullPointerException, IllegalArgumentException,
            FileNotFoundException {

        FetchDirData dataToMallet = new FetchDirData(sourceName);

        try {
            iList = dataToMallet.ParseDirectoryList(trainerObj);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw e;
        }
    }

    public TrainerObject bbIncTrain(ClassifierTrainer prevTrainer)
            throws NullPointerException, Exception {

        MalletTextDataTrainer dataTrainer = new MalletTextDataTrainer();

        try {
            trnObj = dataTrainer.trainIncremental(prevTrainer, iList);
        } catch (NullPointerException ne) {
            // TODO Auto-generated catch block
            ne.printStackTrace();
            throw ne;
        } catch (Exception e) {
            throw e;
        }

        return trnObj;
    }

    public void SaveClassifier(String filename) throws Exception {

        int size;

        if (filename == null || this.trnObj == null)
            throw new NullPointerException(
                    "Either the training data is not yet trained or filename is null");

        BlackbookSimUtils.saveTrainerObject(this.trnObj, filename);
    }
}

package utd.pallet.classification;

import java.util.ArrayList;

import utd.pallet.classification.MalletTextDataTrainer.TrainerObject;
import utd.pallet.data.MalletAccuracyVector;
import cc.mallet.classify.Classification;
import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.types.InstanceList;

public class BlackBookSimulator {

    private static final int BB_TRAIN = 0x00;
    private static final int BB_CLASSIFY_STANDALONE = 0x01;
    private static final int BB_CLASSIFY_WITH_CURR_TRAINED_DATA = 0x02;
    private static final int BB_CLASSIFY_WITH_INC_TRAINED_DATA = 0x03;
    private static final int BB_INC_TRAIN = 0x04;
    private static final int BB_VALIDATE = 0x05;
    private static final int BB_DEFAULT_OPTION = 0xFF;

    public static final String NAIVE_BAYES = "NaiveBayes";

    public BlackBookSimulator() {

    }

    /**
     * @param trainDataSrc
     */
    public static TrainerObject processTrainCommand(
            ArrayList<String> trainDataSrcs, int trainingAlgorithm,
            String opFilename) {

        String OpResourceName;

        BlackbookTrainingSimulator bbTrainer = new BlackbookTrainingSimulator();

        try {
            bbTrainer.fetchData(trainDataSrcs, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        TrainerObject trainerObj = null;
        try {
            trainerObj = bbTrainer.train(trainingAlgorithm);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            bbTrainer.saveTrainer(opFilename);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return trainerObj;
    }

    public static TrainerObject processIncTrainCommand(
            ClassifierTrainer classifierTrainer,
            ArrayList<String> trainDataSrcs, String opFilename,
            TrainerObject trainerObject) {

        BlackbookIncTrainerSim incTrainer = new BlackbookIncTrainerSim();

        try {
            incTrainer.fetchData(trainDataSrcs, trainerObject);
        } catch (Exception e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }

        TrainerObject trnObject = null;
        try {
            trnObject = incTrainer.bbIncTrain(classifierTrainer);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        try {
            incTrainer.SaveClassifier(opFilename);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return trnObject;
    }

    private static int getTrainingAlgo(String algorithm) {

        int trainingAlgorithm = -1;

        if (algorithm == NAIVE_BAYES)
            return MalletTextDataTrainer.NAIVE_BAYES;

        return trainingAlgorithm;
    }

    // Classifier Stand Alone
    private static ArrayList<MalletAccuracyVector> processClassifyStandAloneCommand(
            String trainerObjectFilename, ArrayList<String> dataSrc)
            throws Exception {

        TrainerObject trainerObject = null;
        try {
            trainerObject = BlackbookSimUtils
                    .fetchTrainerObject(trainerObjectFilename);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw e;
        }

        FetchDirData fetchdata = new FetchDirData(dataSrc);

        InstanceList dataList = fetchdata.ParseDirectoryList(trainerObject);
        MalletTextClassify classifier = new MalletTextClassify();
        ArrayList<Classification> cl = classifier.classify(trainerObject
                .getClassifier(), dataList);

        return classifier.getAccuracyVectors();
    }

    private static ArrayList<MalletAccuracyVector> processClassifyWithTraining(
            ArrayList<String> trainDataSrcs, int trainingAlgorithm,
            String trainerDestFile, ArrayList<String> testDataSrc)
            throws Exception {

        processTrainCommand(trainDataSrcs, trainingAlgorithm, trainerDestFile);

        return processClassifyStandAloneCommand(trainerDestFile, testDataSrc);
    }

    private static ArrayList<MalletAccuracyVector> processClassifyWithIncTraining(
            String trainerObjSrcFile, ArrayList<String> trainDataSrcs,
            String opFilename, ArrayList<String> testDataSrc) throws Exception {

        TrainerObject trainerObject = null;
        try {
            trainerObject = BlackbookSimUtils
                    .fetchTrainerObject(trainerObjSrcFile);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw e;
        }

        trainerObject = processIncTrainCommand(trainerObject.getTrainer(),
                trainDataSrcs, opFilename, trainerObject);

        return processClassifyStandAloneCommand(opFilename, testDataSrc);

    }

    public static void validateTrainCmd(ArrayList<Object> optionList)
            throws Exception {

        if (optionList.size() != 3)
            throw new Exception("Training data cannot be empty");

        if (!(optionList.get(0) instanceof ArrayList)) {
            throw new Exception("Incorrect format for train command");
        }

        if (!(optionList.get(1) instanceof String)) {
            throw new Exception("Incorrect format for train command");
        }

        if (!(optionList.get(2) instanceof String)) {
            throw new Exception("Incorrect format for train command");
        }
    }

    public static void validateIncTrainCmd(ArrayList<Object> optionList)
            throws Exception {
        try {
            validateTrainCmd(optionList);
        } catch (Exception e) {
            throw e;
        }
    }

    // <TestDataSet> <TrainerObjectFilepath>
    public static void validateClassifyStandAloneCmd(
            ArrayList<Object> optionList) throws Exception {

        if (optionList.size() != 2)
            throw new Exception("Training data cannot be empty");

        if (!(optionList.get(0) instanceof ArrayList)) {
            throw new Exception("Incorrect format for train command");
        }

        if (!(optionList.get(1) instanceof String)) {
            throw new Exception("Incorrect format for train command");
        }
    }

    // <TrainingDataList> <algo> <DestTrainerFile> <TestDataList>
    public static void validateClassifyWithTrainCmd(ArrayList<Object> optionList)
            throws Exception {
        if (optionList.size() != 4)
            throw new Exception("Training data cannot be empty");

        if (!(optionList.get(0) instanceof ArrayList)) {
            throw new Exception("Incorrect format for train command");
        }

        if (!(optionList.get(1) instanceof String)) {
            throw new Exception("Incorrect format for train command");
        }

        if (!(optionList.get(2) instanceof String)) {
            throw new Exception("Incorrect format for train command");
        }

        if (!(optionList.get(3) instanceof ArrayList)) {
            throw new Exception("Incorrect format for train command");
        }
    }

    // <trainingDataSrc> <TrainerObjectSrc(filename)> <DestTrainerObjectpath>
    // <TestDataSet>
    public static void validateClassifyWithIncTrainCmd(
            ArrayList<Object> optionList) throws Exception {
        try {
            validateClassifyWithTrainCmd(optionList);
        } catch (Exception e) {
            throw e;
        }
    }

    private static void processCommand(int cmd, ArrayList<Object> optionList)
            throws IllegalArgumentException, NullPointerException, Exception {
        switch (cmd) {
        case BB_TRAIN:
            try {
                validateTrainCmd(optionList);
            } catch (Exception e) {
                throw e;
            }
            ArrayList<String> dataSet = (ArrayList) optionList.get(0);
            int trainingAlgorithm = getTrainingAlgo((String) optionList.get(1));
            if (trainingAlgorithm == -1)
                throw new Exception("Incorrect format for train command");
            String opFilename = (String) optionList.get(2);
            processTrainCommand(dataSet, trainingAlgorithm, opFilename);

            break;
        case BB_INC_TRAIN:
            try {
                validateIncTrainCmd(optionList);
            } catch (Exception e) {
                throw e;
            }
            dataSet = (ArrayList) optionList.get(0);
            TrainerObject trainObj = null;
            try {
                trainObj = BlackbookSimUtils
                        .fetchTrainerObject((String) optionList.get(1));
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
            opFilename = (String) optionList.get(2);
            processIncTrainCommand(trainObj.getTrainer(), dataSet, opFilename,
                    trainObj);
            break;
        case BB_CLASSIFY_STANDALONE:
            try {
                validateClassifyStandAloneCmd(optionList);
            } catch (Exception e) {
                throw e;
            }
            dataSet = (ArrayList) optionList.get(0);
            processClassifyStandAloneCommand((String) optionList.get(1),
                    dataSet);
            break;
        case BB_CLASSIFY_WITH_CURR_TRAINED_DATA:
            try {
                validateClassifyWithTrainCmd(optionList);
            } catch (Exception e) {
                throw e;
            }
            dataSet = (ArrayList) optionList.get(0);
            trainingAlgorithm = getTrainingAlgo((String) optionList.get(1));
            if (trainingAlgorithm == -1)
                throw new Exception("Incorrect format for train command");
            processClassifyWithTraining((ArrayList) optionList.get(0),
                    trainingAlgorithm, (String) optionList.get(2),
                    (ArrayList) optionList.get(3));

            break;
        case BB_CLASSIFY_WITH_INC_TRAINED_DATA:
            try {
                validateClassifyWithIncTrainCmd(optionList);
            } catch (Exception e) {
                throw e;
            }

            processClassifyWithIncTraining((String) optionList.get(1),
                    (ArrayList) optionList.get(0), (String) optionList.get(2),
                    (ArrayList) optionList.get(3));
            break;
        case BB_VALIDATE:
            break;
        default:
            break;
        }
    }

    /**
     * @param args
     */
    // Keep all methods as static
    public static void main(String[] args) {
        // TODO Auto-generated method stub

        // File file = new File(
        // "/home/sharath/websemantic_lab/pallet-parent/pallet-test/1253511423514.rdf"
        // );

        String opFilename = "./testTrainer.rdf";

        ArrayList<String> dirList = new ArrayList<String>();
        dirList.add("/home/sharath/websemantic_lab/data/");

        ArrayList<Object> optionList = new ArrayList<Object>();
        String algo = BlackBookSimulator.NAIVE_BAYES;

        optionList.add(dirList);
        optionList.add(algo);
        optionList.add("./testnew.rdf");

        try {
            BlackBookSimulator.processCommand(BB_TRAIN, optionList);
        } catch (IllegalArgumentException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (NullPointerException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }
}

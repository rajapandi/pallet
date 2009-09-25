package utd.pallet.classification;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import utd.pallet.classification.MalletTextDataTrainer.TrainerObject;
import utd.pallet.data.JenaModelFactory;
import utd.pallet.data.MalletAccuracyVector;
import utd.pallet.data.RDFUtils;
import cc.mallet.classify.Classification;
import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.types.InstanceList;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileUtils;

public class BlackBookSimulator {
	 private static org.apache.log4j.Logger log = Logger
     .getLogger(BlackBookSimulator.class);
	 private static final Property BEST_LABEL = ModelFactory
     .createDefaultModel()
     .createProperty(
             "http://marathonminds.com//MalletClassification//#hasBestLabel");
	 private static final Property HAS_VALUE = ModelFactory
     .createDefaultModel()
     .createProperty(
             "http://marathonminds.com//MalletClassification//#hasValue");
    public BlackBookSimulator() {

    }

    /**
     * @param trainDataSrc
     */
    private static TrainerObject processTrainCommand(
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

    private static TrainerObject processIncTrainCommand(
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

    public static int getTrainingAlgo(String algorithm) throws Exception {

        int trainingAlgorithm = MalletTextDataTrainer.ALGO_UNASSIGNED;

        if (algorithm.equals(CommandLineParser.NAIVE_BAYES))
            trainingAlgorithm = MalletTextDataTrainer.NAIVE_BAYES;

        else if (algorithm.equals(CommandLineParser.MAX_ENT))
            trainingAlgorithm = MalletTextDataTrainer.MAX_ENT;

        else if (algorithm.equals(CommandLineParser.DECISION_TREES))
            trainingAlgorithm = MalletTextDataTrainer.DECISION_TREES;

        else if (algorithm.equals(CommandLineParser.C_45))
            trainingAlgorithm = MalletTextDataTrainer.C_45;

        else if (algorithm.equals(CommandLineParser.BALANCED_WINNOW))
            trainingAlgorithm = MalletTextDataTrainer.BALANCED_WINNOW;

        else if (algorithm.equals(CommandLineParser.RANK_MAX_ENT))
            trainingAlgorithm = MalletTextDataTrainer.RANK_MAX_ENT;

        else if (algorithm.equals(CommandLineParser.NAIVE_BAYES_EM))
            trainingAlgorithm = MalletTextDataTrainer.NAIVE_BAYES_EM;

        else if (algorithm.equals(CommandLineParser.MAX_ENT_GE))
            trainingAlgorithm = MalletTextDataTrainer.MAX_ENT_GE;

        else if (algorithm.equals(CommandLineParser.MC_MAX_ENT))
            trainingAlgorithm = MalletTextDataTrainer.MC_MAX_ENT;
        else
            throw new Exception("Invalid Training Algorithm Specified");

        return trainingAlgorithm;
    }

    private static void ValidateClassification(MalletTextClassify classifier,
            String validationDataSrc, ArrayList<Classification> clList,String classificationProperty)
            throws Exception {
    	Model model=null;
    	int numberOfCorrectlyClassifiedInstances=0;
        try {
            model = RDFUtils.createModelWithClassifications(classifier
                    .getAccuracyVectors(), clList);
            log.error("model of classifications is: "
                    + JenaModelFactory.serializeModel(model,
                            FileUtils.langNTriple));
        } catch (Exception e) {
            throw e;
        }
try
{
        Model answerModel = JenaModelFactory.rdf2Model(validationDataSrc);
        ResIterator rts = answerModel.listSubjects();
        Property ClassificationProperty=ModelFactory.createDefaultModel().createProperty(classificationProperty);
       while(rts.hasNext())
       {
    	Resource res =rts.next();
    	if (res.hasProperty(ClassificationProperty)) 
    	{
    		String originalLabel=res.getProperty(ClassificationProperty)
            .getObject().toString();
    		 NodeIterator stmt = model.listObjectsOfProperty(res,
                     BEST_LABEL);
    		 RDFNode bestLabel = stmt.next();
    		 Resource resourceBestLabel = ModelFactory.createDefaultModel()
             .createResource(bestLabel.toString());
    		 if (model.contains(resourceBestLabel, HAS_VALUE, originalLabel))
    		 {
    			 log.error(res +" is correctly classified to " + originalLabel);
    			 numberOfCorrectlyClassifiedInstances++;
    		 }
    		 else
    		 {
    			 log.error(res + " is incorrectly classified to " + bestLabel.toString());
    		 }
    		
    	}
    	
       }
       ResIterator rts1 = answerModel.listSubjects();
   	int numberOfTotalInstances=0;
   	while(rts1.hasNext())
   	{
   		Resource r=rts1.next();
   		if(r.hasProperty(ClassificationProperty))
   			numberOfTotalInstances++;
   	}
      log.error("Number of Correctly Classsified Instances are "+ numberOfCorrectlyClassifiedInstances);
      log.error("Total number of Instances are "+ numberOfTotalInstances);
       
}catch(Exception e)
{
	e.printStackTrace();
	throw e;
}
}

    // Classifier Stand Alone
    private static ArrayList<MalletAccuracyVector> processClassifyStandAloneCommand(
            String trainerObjectFilename, ArrayList<String> dataSrc,
            String validationDataSrc,String classificationProperty) throws Exception {

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

        ValidateClassification(classifier, validationDataSrc, cl,classificationProperty);

        return classifier.getAccuracyVectors();
    }

    private static ArrayList<MalletAccuracyVector> processClassifyWithTraining(
            ArrayList<String> trainDataSrcs, int trainingAlgorithm,
            String trainerDestFile, ArrayList<String> testDataSrc,
            String validationDataSrc,String classificationProperty) throws Exception {

        processTrainCommand(trainDataSrcs, trainingAlgorithm, trainerDestFile);

        return processClassifyStandAloneCommand(trainerDestFile, testDataSrc,
                validationDataSrc,classificationProperty);
    }

    private static ArrayList<MalletAccuracyVector> processClassifyWithIncTraining(
            String trainerObjSrcFile, ArrayList<String> trainDataSrcs,
            String opFilename, ArrayList<String> testDataSrc,
            String validationDataSrc,String classificationProperty) throws Exception {

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

        return processClassifyStandAloneCommand(opFilename, testDataSrc,
                validationDataSrc,classificationProperty);

    }

    // <TestDataSet> <TrainerObjectFilepath>

    // <TrainingDataList> <algo> <DestTrainerFile> <TestDataList>

    // <trainingDataSrc> <TrainerObjectSrc(filename)> <DestTrainerObjectpath>
    // <TestDataSet>

    private static void processCommand(CommandLineParser optionList)
            throws Exception {

        int cmd = optionList.getCommand();
        switch (cmd) {
        case CommandLineParser.BB_TRAIN:
            if (!(optionList instanceof Train))
                throw new Exception("Invalid command");

            Train train = (Train) optionList;
            processTrainCommand(train.getDataSet(), train
                    .getTrainingAlgorithm(), train.getOutputFileName());

            break;
        case CommandLineParser.BB_INC_TRAIN:

            if (!(optionList instanceof Train))
                throw new Exception("Invalid command");

            IncrementalTraining it = (IncrementalTraining) optionList;
            processIncTrainCommand(it.getTrainingObject().getTrainer(), it
                    .getDataSet(), it.getOutputFileName(), it
                    .getTrainingObject());
            break;
        case CommandLineParser.BB_CLASSIFY_STANDALONE:

            ClassifyStandAlone csa = (ClassifyStandAlone) optionList;
            processClassifyStandAloneCommand(csa.getTrainerObjectFileName(),
                    csa.getDataSource(), csa.getValidationDataSrc(),csa.getClassificationProperty());
            break;
        case CommandLineParser.BB_CLASSIFY_WITH_CURR_TRAINED_DATA:

            ClassifyWithCurrentTrainedData cwctd = (ClassifyWithCurrentTrainedData) optionList;
            processClassifyWithTraining(cwctd.getTestDataSrcs(), cwctd
                    .getTrainingAlgorithm(), cwctd.getTrainerDestFile(), cwctd
                    .getTestDataSrcs(), cwctd.getValidationDataSrc(),cwctd.getClassificationProperty());
            break;
        case CommandLineParser.BB_CLASSIFY_WITH_INC_TRAINED_DATA:

            ClassifyWithIncrementalTrainedData cwitd = (ClassifyWithIncrementalTrainedData) optionList;
            processClassifyWithIncTraining(cwitd.getTrainerSrcFile(), cwitd
                    .getTrainDataSrcs(), cwitd.getOutputFile(), cwitd
                    .getTestDataSrcs(), cwitd.getValidationDataSrc(),cwitd.getClassificationProperty());
            break;
        case CommandLineParser.BB_VALIDATE:
            break;
        default:
            break;
        }
    }

    /**
     * @param args
     */
    // Keep all methods as static
    public static void main(String[] args) throws Exception {

        try {
            ArrayList<CommandLineParser> commands = CommandLineParser
                    .argumentParser(args);

            for (int i = 0; i < commands.size(); i++) {
                CommandLineParser clp = commands.get(i);
                if (!(clp instanceof CommandLineParser))
                    throw new Exception("Invalid Command format");

                processCommand(clp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

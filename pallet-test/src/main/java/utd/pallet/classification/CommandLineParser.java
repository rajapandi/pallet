package utd.pallet.classification;

import java.util.ArrayList;
import java.util.StringTokenizer;

import utd.pallet.classification.MalletTextDataTrainer.TrainerObject;

/**
 * The Train class is used to represent the arguments for the Training of the
 * classifier
 * 
 */
class Train extends CommandLineParser {

    /**
     * It represent the dataSet
     */
    public ArrayList<String> dataSet = new ArrayList<String>();
    /**
     * It represents training algorithm
     */
    public int trainingAlgorithm;
    /**
     * It represent the outputfile which is used to save the training
     * classifier.
     */
    public String outputFileName;
    /**
     * It represents the classification property.
     */
    public String classificationProperty;

    /**
     * @return dataset.
     */
    public ArrayList<String> getDataSet() {
        return dataSet;
    }

    /**
     * @return classificationProperty
     */
    public String getClassificationProperty() {
        return classificationProperty;
    }

    /**
     * @return trainingAlgorithm
     */
    public Integer getTrainingAlgorithm() {
        return trainingAlgorithm;
    }

    /**
     * @return outputFileName
     */
    public String getOutputFileName() {
        return outputFileName;
    }
}

/**
 * This class represents the arguments for the Incremental Training
 * 
 */
class IncrementalTraining extends CommandLineParser {
    /**
     * It represents the trainer object.
     */
    public TrainerObject trainingObject;
    /**
     * It represents the dataset.
     */
    public ArrayList<String> dataSet = new ArrayList<String>();
    /**
     * It represents the outputfile name where the classifier will be stored.
     */
    public String outputFileName;
    /**
     * It represents the classification property.
     */
    public String classificationProperty;

    /**
     * @return classificationProperty
     */
    public String getClassificationProperty() {
        return classificationProperty;
    }

    /**
     * @return Trainer Object.
     */
    public TrainerObject getTrainingObject() {
        return trainingObject;
    }

    /**
     * @return dataSet
     */
    public ArrayList<String> getDataSet() {
        return dataSet;
    }

    /**
     * @return output file name
     */
    public String getOutputFileName() {
        return outputFileName;
    }

}

/**
 *This class represents the arguments for classify stand alone.
 * 
 */
class ClassifyStandAlone extends CommandLineParser {
    /**
     * It represents the source of the data . Data source can contains
     * directoris and files.
     * 
     */
    public ArrayList<String> dataSrc = new ArrayList<String>();
    /**
     * It represents the trainer object file name.
     */
    public String trainerObjectFileName;
    /**
     * It represent the Model which is used to validate the classifer model.
     */
    public String validationDataSrc;
    /**
     * It represents the classification property on the basis of which
     * classification is done.
     */
    public String classificationProperty;

    /**
     * @return : It returns the data source.
     */
    public ArrayList<String> getDataSource() {
        return dataSrc;
    }

    /**
     * @return It return the trainer object file name.
     */
    public String getTrainerObjectFileName() {
        return trainerObjectFileName;
    }

    /**
     * @return : It returns the validation data source
     */
    public String getValidationDataSrc() {
        return validationDataSrc;
    }

    /**
     * @return : It return the classification property.
     */
    public String getClassificationProperty() {
        return classificationProperty;
    }
}

/**
 * It represents the arguments to classify with current trained data.
 * 
 */
class ClassifyWithCurrentTrainedData extends CommandLineParser {
    /**
     * It represents the trained data sources.
     */
    public ArrayList<String> trainDataSrcs = new ArrayList<String>();
    /**
     * It represents the training algorithm
     */
    public Integer trainingAlgorithm;
    /**
     * It represents the destination file for the trainer.
     */
    public String trainerDestFile;
    /**
     * It represents the source for the test data.
     */
    public ArrayList<String> testDataSrcs = new ArrayList<String>();
    /**
     * It represents the source of validation data.
     * 
     */
    public String validationDataSrc;
    /**
     * It represents the classification property.
     */
    public String classificationProperty;

    /**
     * @return : It returns the trained data source.
     * 
     */
    public ArrayList<String> getTrainDataSrcs() {
        return trainDataSrcs;
    }

    /**
     * @return : It returns the classification property
     */
    public String getClassificationProperty() {
        return classificationProperty;
    }

    /**
     * @return : It returns the classification property
     */
    public String getValidationDataSrc() {
        return validationDataSrc;
    }

    /**
     * @return : It returns the training algorithm
     */
    public Integer getTrainingAlgorithm() {
        return trainingAlgorithm;
    }

    /**
     * @return : It returns the test data source.
     */
    public ArrayList<String> getTestDataSrcs() {
        return testDataSrcs;
    }

    /**
     * @return : It returns the trained destination file.
     */
    public String getTrainerDestFile() {
        return trainerDestFile;
    }
}

/**
 * This class represents the arguments for
 * "classify with incremental trained data"
 * 
 */
class ClassifyWithIncrementalTrainedData extends CommandLineParser {
    /**
     * It represents the test data sources
     */
    public ArrayList<String> testDataSrcs = new ArrayList<String>();
    /**
     * It represents the trainee source file.
     */
    public String trainerSrcFile;
    /**
     * It represents the trained data source.
     */
    public ArrayList<String> trainDataSrcs = new ArrayList<String>();
    /**
     * It represents the output file.
     */
    public String outputFile;
    /**
     * It represents the source for validation data.
     */
    public String validationDataSrc;
    /**
     * It represents the classification property.
     */
    public String classificationProperty;

    /**
     * @return : It returns the validation data source.
     */
    public String getValidationDataSrc() {
        return validationDataSrc;
    }

    /**
     * @return : It returns the classification property.
     */
    public String getClassificationProperty() {
        return classificationProperty;
    }

    /**
     * @return It returns the test data sources.
     */
    public ArrayList<String> getTestDataSrcs() {
        return testDataSrcs;
    }

    /**
     * @return : It returns the trained data source.
     */
    public ArrayList<String> getTrainDataSrcs() {
        return trainDataSrcs;
    }

    /**
     * @return : It returns the output file .
     */
    public String getOutputFile() {
        return outputFile;
    }

    /**
     * @return : It returns the trained source file.
     */
    public String getTrainerSrcFile() {
        return trainerSrcFile;
    }

}

/**
 * commandLineParser is a class which will parse the command line arguments .
 * for eg Train-dir C://user,d://home NaiveBayes a.txt@Train-file b.txt,c.txt
 * MaxEnt d.txt@Validation-dir d://user,c://user There are three commands. The
 * parser will convert it like firstToken=Train-dir ArrayList contains C://user
 * d://user ThirdToken=NaiveBayes fourthToken a.txt. All these are added to one
 * object of commandLineParser. Therefore all the commands are in an ArrayList
 * of commandLineParser.Please give @ between two commands and , between two
 * files and directory name.
 */
public class CommandLineParser {

    /**
     * For only training.
     */
    public static final int BB_TRAIN = 0x00;
    /**
     * For classify stand alone.
     */
    public static final int BB_CLASSIFY_STANDALONE = 0x01;
    /**
     * For classify with current trained data.
     */
    public static final int BB_CLASSIFY_WITH_CURR_TRAINED_DATA = 0x02;
    /**
     * For classify with incremental trained data.
     */
    public static final int BB_CLASSIFY_WITH_INC_TRAINED_DATA = 0x03;
    /**
     * For Incremental training.
     */
    public static final int BB_INC_TRAIN = 0x04;
    /**
     * For Validatation
     */
    public static final int BB_VALIDATE = 0x05;
    /**
     * For default option
     */
    public static final int BB_DEFAULT_OPTION = 0xFF;

    /**
     * For Naive bayes.
     */
    public static final String NAIVE_BAYES = "NaiveBayes";
    /**
     * For MAX_INT
     */
    public static final String MAX_ENT = "MaxEnt";
    /**
     * For Decision Trees
     */
    public static final String DECISION_TREES = "DecisionTrees";
    /**
     * For C45
     */
    public static final String C_45 = "C45";
    /**
     * For balanced Winnow
     */
    public static final String BALANCED_WINNOW = "BalancedWinnow";
    /**
     * For RANK_MAX_ENT
     */
    public static final String RANK_MAX_ENT = "RankMaxEnt";
    /**
     * For Naive Bayes EM
     */
    public static final String NAIVE_BAYES_EM = "NaiveBayesEM";
    /**
     * For MAX ENT GE
     */
    public static final String MAX_ENT_GE = "MaxEntGE";
    /**
     * FOR MC MAX ENT
     */
    public static final String MC_MAX_ENT = "MCMaxEnt";

    /**
     * It takes the firstToken(Train-dir)
     */
    protected int commandName = BB_DEFAULT_OPTION;

    /**
     * @return It returns the command name.
     */
    public int getCommand() {
        return commandName;
    }

    /**
     * @param args
     *            : It takes the arguments from the user.
     * @return : It returns the ArrayList.
     * @throws Exception
     */
    public static ArrayList<CommandLineParser> argumentParser(String args[])
            throws Exception {

        int totalCommands = args.length;

        int k = 0;
        String str = "";
        int p1 = totalCommands;
        while (p1 != 0) {

            str = str + args[k] + " ";
            k++;
            p1--;
        }
        System.out.println(str);
        StringTokenizer commandLineArguments = new StringTokenizer(str, "@");
        ArrayList<CommandLineParser> commandParser = new ArrayList<CommandLineParser>();

        while (commandLineArguments.hasMoreTokens()) {
            StringTokenizer completeCommand = new StringTokenizer(
                    commandLineArguments.nextToken(), " ");
            int size = completeCommand.countTokens();
            System.out.println(size);

            String cmd = completeCommand.nextToken();
            if (cmd.equals("TRAIN")) {
                if (size != 5) {
                    throw new Exception("Incorrect format for training command");
                }
                Train train = new Train();
                train.commandName = BB_TRAIN;
                StringTokenizer argumentSecond = new StringTokenizer(
                        completeCommand.nextToken(), ",");
                while (argumentSecond.hasMoreTokens()) {
                    train.dataSet.add(argumentSecond.nextToken());
                }

                String s1 = completeCommand.nextToken();
                train.trainingAlgorithm = BlackBookSimulator
                        .getTrainingAlgo(s1);
                if (train.trainingAlgorithm == -1)
                    throw new Exception("Incorrect Training Algorithm");
                String s2 = completeCommand.nextToken();
                train.outputFileName = s2;
                String s3 = completeCommand.nextToken();
                train.classificationProperty = s3;
                commandParser.add(train);
                continue;
            } else if (cmd.equals("INC_TRAIN")) {
                if (size != 5) {
                    throw new Exception(
                            "Incorrect format for incremental training");
                }
                IncrementalTraining it = new IncrementalTraining();
                it.commandName = BB_INC_TRAIN;
                StringTokenizer argumentSecond = new StringTokenizer(
                        completeCommand.nextToken(), ",");
                while (argumentSecond.hasMoreTokens()) {
                    it.dataSet.add(argumentSecond.nextToken());
                }
                String s1 = completeCommand.nextToken();

                try {
                    it.trainingObject = BlackbookSimUtils
                            .fetchTrainerObject(s1);
                } catch (Exception e) {

                    throw e;
                }
                String s2 = completeCommand.nextToken();
                it.outputFileName = s2;
                String s3 = completeCommand.nextToken();
                it.classificationProperty = s3;
                commandParser.add(it);
                continue;
            } else if (cmd.equals("CLASSIFY_SD")) {
                if (size != 5) {
                    throw new Exception(
                            "Incorrect format for classifying standalone");
                }
                ClassifyStandAlone csa = new ClassifyStandAlone();
                csa.commandName = BB_CLASSIFY_STANDALONE;
                StringTokenizer argumentSecond = new StringTokenizer(
                        completeCommand.nextToken(), ",");
                while (argumentSecond.hasMoreTokens()) {
                    csa.dataSrc.add(argumentSecond.nextToken());
                }
                String s1 = completeCommand.nextToken();
                csa.trainerObjectFileName = s1;
                commandParser.add(csa);
                String s2 = completeCommand.nextToken();
                csa.validationDataSrc = s2;
                String s3 = completeCommand.nextToken();
                csa.classificationProperty = s3;
                continue;

            } else if (cmd.equals("CLASSIFY")) {
                if (size != 7) {
                    throw new Exception(
                            "Incorrect format for classify with current trained data");
                }
                ClassifyWithCurrentTrainedData cwctd = new ClassifyWithCurrentTrainedData();
                cwctd.commandName = BB_CLASSIFY_WITH_CURR_TRAINED_DATA;
                StringTokenizer argumentSecond = new StringTokenizer(
                        completeCommand.nextToken(), ",");
                while (argumentSecond.hasMoreTokens()) {
                    cwctd.trainDataSrcs.add(argumentSecond.nextToken());
                }
                StringTokenizer thirdArgument = new StringTokenizer(
                        completeCommand.nextToken(), ",");
                while (argumentSecond.hasMoreTokens()) {
                    cwctd.testDataSrcs.add(thirdArgument.nextToken());
                }
                String s1 = completeCommand.nextToken();
                cwctd.trainingAlgorithm = BlackBookSimulator
                        .getTrainingAlgo(s1);
                if (cwctd.trainingAlgorithm == -1)
                    throw new Exception("Incorrect Training Algorithm");
                String s2 = completeCommand.nextToken();
                cwctd.trainerDestFile = s2;
                String s3 = completeCommand.nextToken();
                cwctd.validationDataSrc = s3;
                String s4 = completeCommand.nextToken();
                cwctd.classificationProperty = s4;
                commandParser.add(cwctd);
            } else if (cmd.equals("CLASSIFY_INC")) {
                if (size != 6) {
                    throw new Exception(
                            "Incorrect format for classify with incremental trained data");
                }
                ClassifyWithIncrementalTrainedData cwitd = new ClassifyWithIncrementalTrainedData();
                cwitd.commandName = BB_CLASSIFY_WITH_INC_TRAINED_DATA;
                StringTokenizer argumentSecond = new StringTokenizer(
                        completeCommand.nextToken(), ",");
                while (argumentSecond.hasMoreTokens()) {
                    cwitd.testDataSrcs.add(argumentSecond.nextToken());
                }
                StringTokenizer thirdArgument = new StringTokenizer(
                        completeCommand.nextToken(), ",");
                while (argumentSecond.hasMoreTokens()) {
                    cwitd.trainDataSrcs.add(thirdArgument.nextToken());
                }
                String s1 = completeCommand.nextToken();
                cwitd.trainerSrcFile = s1;
                String s2 = completeCommand.nextToken();
                cwitd.outputFile = s2;
                String s3 = completeCommand.nextToken();
                cwitd.validationDataSrc = s3;
                String s4 = completeCommand.nextToken();
                cwitd.classificationProperty = s4;
                commandParser.add(cwitd);

                continue;
            } else {
                throw new IllegalArgumentException("Invalid Command");
            }
        }
        return commandParser;

    }
}

package utd.pallet.classification;

import java.util.ArrayList;
import java.util.StringTokenizer;

import utd.pallet.classification.MalletTextDataTrainer.TrainerObject;

class Train extends CommandLineParser {

    public ArrayList<String> dataSet = new ArrayList<String>();
    public Integer trainingAlgorithm;
    public String outputFileName;

    public ArrayList<String> getDataSet() {
        return dataSet;
    }

    public Integer getTrainingAlgorithm() {
        return trainingAlgorithm;
    }

    public String getOutputFileName() {
        return outputFileName;
    }
}

class IncrementalTraining extends CommandLineParser {
    public TrainerObject trainingObject;
    public ArrayList<String> dataSet = new ArrayList<String>();
    public String outputFileName;

    public TrainerObject getTrainingObject() {
        return trainingObject;
    }

    public ArrayList<String> getDataSet() {
        return dataSet;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

}

class ClassifyStandAlone extends CommandLineParser {
    public ArrayList<String> dataSrc = new ArrayList<String>();
    public String trainerObjectFileName;
    public String validationDataSrc;
    public String classificationProperty;

    public ArrayList<String> getDataSource() {
        return dataSrc;
    }

    public String getTrainerObjectFileName() {
        return trainerObjectFileName;
    }

    public String getValidationDataSrc() {
        return validationDataSrc;
    }
    public String getClassificationProperty()
    {
    	return classificationProperty;
    }
}

class ClassifyWithCurrentTrainedData extends CommandLineParser {
    public ArrayList<String> trainDataSrcs = new ArrayList<String>();
    public Integer trainingAlgorithm;
    public String trainerDestFile;
    public ArrayList<String> testDataSrcs = new ArrayList<String>();
    public String validationDataSrc;
    public String classificationProperty;
    public ArrayList<String> getTrainDataSrcs() {
        return trainDataSrcs;
    }
    public String getClassificationProperty()
    {
    	return classificationProperty;
    }
    public String getValidationDataSrc() {
        return validationDataSrc;
    }

    public Integer getTrainingAlgorithm() {
        return trainingAlgorithm;
    }

    public ArrayList<String> getTestDataSrcs() {
        return testDataSrcs;
    }

    public String getTrainerDestFile() {
        return trainerDestFile;
    }
}

class ClassifyWithIncrementalTrainedData extends CommandLineParser {
    public ArrayList<String> testDataSrcs = new ArrayList<String>();
    public String trainerSrcFile;
    public ArrayList<String> trainDataSrcs = new ArrayList<String>();
    public String outputFile;
    public String validationDataSrc;
    public String classificationProperty;
    public String getValidationDataSrc() {
        return validationDataSrc;
    }
    public String getClassificationProperty()
    {
    	return classificationProperty;
    }
    public ArrayList<String> getTestDataSrcs() {
        return testDataSrcs;
    }

    public ArrayList<String> getTrainDataSrcs() {
        return trainDataSrcs;
    }

    public String getOutputFile() {
        return outputFile;
    }

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

    public static final int BB_TRAIN = 0x00;
    public static final int BB_CLASSIFY_STANDALONE = 0x01;
    public static final int BB_CLASSIFY_WITH_CURR_TRAINED_DATA = 0x02;
    public static final int BB_CLASSIFY_WITH_INC_TRAINED_DATA = 0x03;
    public static final int BB_INC_TRAIN = 0x04;
    public static final int BB_VALIDATE = 0x05;
    public static final int BB_DEFAULT_OPTION = 0xFF;

    public static final String NAIVE_BAYES = "NaiveBayes";
    public static final String MAX_ENT = "MaxEnt";
    public static final String DECISION_TREES = "DecisionTrees";
    public static final String C_45 = "C45";
    public static final String BALANCED_WINNOW = "BalancedWinnow";
    public static final String RANK_MAX_ENT = "RankMaxEnt";
    public static final String NAIVE_BAYES_EM = "NaiveBayesEM";
    public static final String MAX_ENT_GE = "MaxEntGE";
    public static final String MC_MAX_ENT = "MCMaxEnt";

    /**
     * It takes the firstToken(Train-dir)
     */
    protected int commandName = BB_DEFAULT_OPTION;

    public int getCommand() {
        return commandName;
    }

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
                if (size != 4) {
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
                commandParser.add(train);
                continue;
            } else if (cmd.equals("INC_TRAIN")) {
                if (size != 4) {
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
                    e.printStackTrace();
                    throw e;
                }
                String s2 = completeCommand.nextToken();
                it.outputFileName = s2;
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

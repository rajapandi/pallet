package utd.pallet.classification;

import java.util.ArrayList;
import java.util.StringTokenizer;

import utd.pallet.classification.MalletTextDataTrainer.TrainerObject;



class Train extends commandLineParser
{
	public String commandName;	
	public ArrayList<String> dataSet = new ArrayList<String>();
	public Integer trainingAlgorithm;
	public String outputFileName;
	 public ArrayList<String> getDataSet()
	    {
	    	return dataSet;
	    }
	 public String getCommand()
	    {
	    	return commandName;
	    }
	 public Integer getTrainingAlgorithm()
	    {
	    	return trainingAlgorithm;
	    }
	 public String getOutputFileName()
	    {
	    return outputFileName;	
	    }
}


class incrementalTraining extends commandLineParser
{
	public String commandName;
	public TrainerObject trainingObject;
	public ArrayList<String> dataSet = new ArrayList<String>();
	public String outputFileName;
	public String getCommand()
    {
    	return commandName;
    }
	public TrainerObject getTrainingObject()
    {
    	return trainingObject;
    }
	 public ArrayList<String> getDataSet()
	{
	    	return dataSet;
	}
	 public String getOutputFileName()
	{
	    return outputFileName;	
	}

}

class classifyStandAlone extends commandLineParser
{   public String commandName; 
	public ArrayList<String> dataSrc = new ArrayList<String>();
	public String trainerObjectFileName;
	public ArrayList<String> getDataSource()
	{
	    	return dataSrc;
	}
	public String getCommand()
    {
    	return commandName;
    }
	public String getTrainerObjectFileName() 
	{
		return trainerObjectFileName;
	}
}

class classifyWithCurrentTrainedData extends commandLineParser
{
	    public String commandName; 
		public ArrayList<String> trainDataSrcs = new ArrayList<String>();
		public Integer trainingAlgorithm;
		public String trainerDestFile;
		public ArrayList<String> testDataSrcs = new ArrayList<String>();
		public String getCommand()
	    {
	    	return commandName;
	    }
		public ArrayList<String> getTrainDataSrcs()
		{
		    	return trainDataSrcs;
		}
		public Integer getTrainingAlgorithm()
		{
			return trainingAlgorithm;
		}
		public ArrayList<String> getTestDataSrcs()
		{
		    	return testDataSrcs;
		}
		public String getTrainerDestFile()
	    {
	    	return trainerDestFile;
	    }
}

class classifyWithIncrementalTrainedData extends commandLineParser 
{
	public String commandName;	
	public ArrayList<String> testDataSrcs = new ArrayList<String>();
	public String trainerSrcFile;
	public ArrayList<String> trainDataSrcs = new ArrayList<String>();
	public String outputFile;
	public String getCommand()
    {
    	return commandName;
    }
	public ArrayList<String> getTestDataSrcs()
	{
	    	return testDataSrcs;
	}
	public ArrayList<String> getTrainDataSrcs()
	{
		return trainDataSrcs;
	}
	public String getOutputFile()
    {
    	return outputFile;
    }
	public String getTrainerSrcFile()
    {
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
public class commandLineParser {
    /**
     * It takes the firstToken(Train-dir)
     */
    public String commandName;
    
    public String getCommand()
    {
    	return commandName;
    }
    
   
    public static ArrayList<commandLineParser> argumentParser(String args[])throws Exception {
        // public static void main(String args[]) {
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
        ArrayList<commandLineParser> commandParser = new ArrayList<commandLineParser>();

        while (commandLineArguments.hasMoreTokens()) {
            StringTokenizer completeCommand = new StringTokenizer(
                    commandLineArguments.nextToken(), " ");
            int size = completeCommand.countTokens();
            System.out.println(size);
            commandLineParser ok = new commandLineParser();
            ok.commandName= null;
            
            ok.commandName = completeCommand.nextToken();
            if(ok.getCommand().equals("Train"))
            {
            	if(size!=4)
            	{
            		throw new Exception("Incorrect format for training command");
            	}
            	Train train=new Train();
            	train.commandName=ok.getCommand();
            	StringTokenizer argumentSecond = new StringTokenizer(
                        completeCommand.nextToken(), ",");  
                        while (argumentSecond.hasMoreTokens()) {
                            train.dataSet.add(argumentSecond.nextToken());
                        }
                       
                            String s1 = completeCommand.nextToken();
                            train.trainingAlgorithm = BlackBookSimulator.getTrainingAlgo(s1);
                            if (train.trainingAlgorithm == -1)
                                throw new Exception("Incorrect Training Algorithm");
                            String s2 = completeCommand.nextToken();
                            train.outputFileName = s2;
                            commandParser.add(train);          
            continue;
            }
            
            if(ok.getCommand().equals("INC_TRAIN"))
            {
            	if(size!=4)
            	{
            		throw new Exception("Incorrect format for incremental training");
            	}
            	incrementalTraining it=new incrementalTraining();
            	it.commandName=ok.getCommand();
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
                 it.outputFileName=s2;
                 commandParser.add(it);
            continue;
            }
            if(ok.getCommand().equals("CLASSIFY_STANDALONE"))
            {
            	if(size!=3)
            	{
            		throw new Exception("Incorrect format for classifying standalone");
            	}
            	classifyStandAlone csa=new classifyStandAlone();
            	csa.commandName=ok.getCommand();
            	StringTokenizer argumentSecond = new StringTokenizer(
                        completeCommand.nextToken(), ",");  
                        while (argumentSecond.hasMoreTokens()) {
                            csa.dataSrc.add(argumentSecond.nextToken());
                        }
            	String s1 = completeCommand.nextToken();
                csa.trainerObjectFileName=s1;
                commandParser.add(csa);
                continue;
            }
            if(ok.getCommand().equals("CLASSIFY_WITH_CURR_TRAINED_DATA"))
            {
            	if(size!=5)
            	{
            		throw new Exception("Incorrect format for classify with current trained data");
            	}
            	classifyWithCurrentTrainedData cwctd=new classifyWithCurrentTrainedData();
            	cwctd.commandName=ok.getCommand();
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
            	 cwctd.trainingAlgorithm = BlackBookSimulator.getTrainingAlgo(s1);
                 if (cwctd.trainingAlgorithm == -1)
                     throw new Exception("Incorrect Training Algorithm");
                 String s2 = completeCommand.nextToken();
                 cwctd.trainerDestFile =s2;
                 commandParser.add(cwctd);
            }
            if(ok.getCommand().equals("CLASSIFY_WITH_INCREMENTAL_TRAIN_DATA"))
            {
            	if(size!=5)
            	{
            		throw new Exception("Incorrect format for classify with incremental trained data");
            	}
            	classifyWithIncrementalTrainedData cwitd=new classifyWithIncrementalTrainedData();
            	cwitd.commandName=ok.getCommand();
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
                                cwitd.trainerSrcFile=s1;
                                String s2 = completeCommand.nextToken();
                                cwitd.outputFile=s2;
                                commandParser.add(cwitd);
            continue;
            
            }
           
          
        }
        return commandParser;

    }
}

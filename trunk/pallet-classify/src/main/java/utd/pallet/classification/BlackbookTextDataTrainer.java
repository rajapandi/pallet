package utd.pallet.classification;

import cc.mallet.classify.Classifier;
import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.classify.NaiveBayesTrainer;
import cc.mallet.types.InstanceList;

/**
 * Creates specified Trainer
 * 
 * @author sharath
 *
 */
public class BlackbookTextDataTrainer {
	
//	private ClassifierTrainer 	trainer 		= null;
//	private Classifier			classifier 		= null;
	private int					trainer_algo 	= ALGO_UNASSIGNED;
	private InstanceList		list_2b_trained = null;
	private boolean				init_done 		= false;

	public static final int ALGO_UNASSIGNED = 0x00;
	public static final int NAIVEBAYES 		= 0x01;
	public static final int MAXENT	 		= 0x02;
	public static final int DECISIONTREES 	= 0x03;
	public static final int C45				= 0x04;
	
	/**
	 * To be used only for incremental train
	 */
	public BlackbookTextDataTrainer () {
		//throw 
	}
	
	/**
	 * 
	 * @param listToTrain - Instance list that needs to be used for training (o/p from data import)
	 * @param trainerAlgorithm - algorithm to be chosen to train the data
	 * @param copy: if true clone instance list 
	 */
	public BlackbookTextDataTrainer (InstanceList listToTrain, int trainerAlgorithm, boolean copy) {
		
		int algo = trainerAlgorithm;
		if (algo == ALGO_UNASSIGNED)
			algo = NAIVEBAYES;

		this.trainer_algo = algo;
		if (copy == false)
			this.list_2b_trained = listToTrain;			
		else 
			this.list_2b_trained = (InstanceList)listToTrain.clone();
		
		init_done = true;
	}
	
	/**
	 * 
	 * @param listToTrain
	 * @param trainerAlgorithm
	 */
	public BlackbookTextDataTrainer (InstanceList listToTrain, int trainerAlgorithm) {
		this (listToTrain, trainerAlgorithm, false);
	}
	
	/**
	 * 
	 * @param listToTrain
	 */
	public BlackbookTextDataTrainer (InstanceList listToTrain) {
		this (listToTrain, NAIVEBAYES, false);
	}
	
	@SuppressWarnings("unchecked")
	private int getTrainerAlgo (ClassifierTrainer trainer) throws java.lang.NullPointerException {
		
		if (trainer == null)
			throw new NullPointerException ("Trainer not initialized");
					
		int trainerAlgo = ALGO_UNASSIGNED;
		
		if (trainer instanceof cc.mallet.classify.NaiveBayesTrainer)
			trainerAlgo = NAIVEBAYES;
		else if (trainer instanceof cc.mallet.classify.MaxEntTrainer)
			trainerAlgo = MAXENT;
		else if (trainer instanceof cc.mallet.classify.DecisionTreeTrainer)
			trainerAlgo = DECISIONTREES;
		else if (trainer instanceof cc.mallet.classify.C45Trainer)
			trainerAlgo = C45;
				
		return trainerAlgo;
		
	}

	/*
	 * Creates trainer from the factory. 
	 */
	@SuppressWarnings("unchecked")
	private ClassifierTrainer CreateTrainer (int trainingAlgo)  throws java.lang.Exception {
	
		ClassifierTrainer trainer = null;
		
		switch (trainingAlgo) {
		case NAIVEBAYES:
			trainer = BlackbookTrainerFactory.CreateNaiveBayesTrainer();
			break;
		case MAXENT:
			trainer = BlackbookTrainerFactory.CreateMaxEntTrainer();
			break;
		case DECISIONTREES:
			trainer = BlackbookTrainerFactory.CreateDecisionTreeTrainer();
			break;
		case C45:
			trainer = BlackbookTrainerFactory.CreateC45Trainer();
			break;
		default:
			throw new Exception ("Unknown Trainer Algorithm");
		}
				
		return trainer;				
	}
	
	
	/**
	 * 
	 * @param trainer
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Classifier train (ClassifierTrainer trainer) throws java.lang.Exception {
		if (trainer == null && init_done == false)
			throw new Exception("Instance list and trainer algorithm is not initialized");

		int trainerAlgo;
		if (trainer != null)
			trainerAlgo = this.getTrainerAlgo(trainer);
		else
		{
			trainerAlgo = this.trainer_algo;
			trainer = this.CreateTrainer(trainerAlgo);
		}

		// Incremental training for naive bayes
		Classifier cl = null;
		if (trainerAlgo == NAIVEBAYES && init_done == false)
		{
			NaiveBayesTrainer tr = (NaiveBayesTrainer)trainer;
			cl = tr.trainIncremental(this.list_2b_trained);
		}
		else
			cl = trainer.train(this.list_2b_trained);
		
		return cl;
	}
}



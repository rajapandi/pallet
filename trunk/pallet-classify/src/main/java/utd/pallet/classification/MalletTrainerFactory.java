package utd.pallet.classification;

import cc.mallet.classify.AdaBoostTrainer;
import cc.mallet.classify.BaggingTrainer;
import cc.mallet.classify.BalancedWinnowTrainer;
import cc.mallet.classify.C45Trainer;
import cc.mallet.classify.ClassifierEnsembleTrainer;
import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.classify.DecisionTreeTrainer;
import cc.mallet.classify.MCMaxEntTrainer;
import cc.mallet.classify.MaxEntGETrainer;
import cc.mallet.classify.MaxEntTrainer;
import cc.mallet.classify.NaiveBayesEMTrainer;
import cc.mallet.classify.NaiveBayesTrainer;
import cc.mallet.classify.RankMaxEntTrainer;



/**
 * Factory to create specified Mallet Trainer.
 * 
 */
@SuppressWarnings("unused")
public class MalletTrainerFactory {
	/**
	 * Creates NaiveBayes Trainer, currently creates trainer with default constructor.
	 * @return Instance of NaiveBayesTrainer.
	 */
	@SuppressWarnings("unchecked")
	public static ClassifierTrainer CreateNaiveBayesTrainer () {		
		return new NaiveBayesTrainer ();
	}
	
	/**
	 * Creates MaxEntTrainer, currently creates trainer with default constructor.
	 * @return Instance of MaxEntTrainer.
	 */
	@SuppressWarnings("unchecked")
	public static ClassifierTrainer CreateMaxEntTrainer () {		
		return new MaxEntTrainer ();
	}

	/**
	 * Creates DecisionTreeTrainer, currently creates trainer with default constructor.
	 * @return Instance of DecisionTreeTrainer.
	 */
	@SuppressWarnings("unchecked")
	public static ClassifierTrainer CreateDecisionTreeTrainer () {
		return new DecisionTreeTrainer ();
	}
	
	/**
	 * Creates C45Trainer, currently creates trainer with default constructor.
	 * @return Instance of C45Trainer.
	 */	
	@SuppressWarnings("unchecked")
	public static ClassifierTrainer CreateC45Trainer () {
		return new C45Trainer ();
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Creates BalancedWinnowTrainer, currently creates trainer with default constructor.
	 * @return Instance of BalancedWinnowTrainer.
	 */
	public static ClassifierTrainer CreateBalancedWinnowTrainer () {
		return new BalancedWinnowTrainer ();
	}
	
	/**
	 * Creates MaxEntGETrainer, currently creates trainer with default constructor.
	 * @return Instance of MaxEntGETrainer.
	 */
	@SuppressWarnings("unchecked")
	public static ClassifierTrainer CreateMaxEntGETrainer () {
		return new MaxEntGETrainer ();
	}
	
	/**
	 * Creates RankMaxEntTrainer, currently creates trainer with default constructor.
	 * @return Instance of RankMaxEntTrainer.
	 */
	@SuppressWarnings("unchecked")
	public static ClassifierTrainer CreateRankMaxEntTrainer () {	
		return new RankMaxEntTrainer ();
	}
	
	/**
	 * Creates NaiveBayesEMTrainer, currently creates trainer with default constructor.
	 * @return Instance of NaiveBayesEMTrainer.
	 */
	@SuppressWarnings("unchecked")
	public static ClassifierTrainer CreateNaiveBayesEMTrainer () {
		return new NaiveBayesEMTrainer ();
	}
	
	/**
	 * Creates MCMaxEntTrainer, currently creates trainer with default constructor.
	 * @return Instance of MCMaxEntTrainer.
	 */
	@SuppressWarnings("unchecked")
	public static ClassifierTrainer CreateMCMAxEntTrainer () {
		return new MCMaxEntTrainer ();
	}
	
	// Yet to be done
	/*
	public static ClassifierTrainer CreateClassifierEnsembleTrainer () {
		
		return new ClassifierEnsembleTrainer ();
	}
	*/	
	
	//public static ClassifierTrainer CreateMaxE	
}


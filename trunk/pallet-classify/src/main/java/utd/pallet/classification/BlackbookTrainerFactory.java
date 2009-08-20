package utd.pallet.classification;

import cc.mallet.classify.C45Trainer;
import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.classify.DecisionTreeTrainer;
import cc.mallet.classify.MaxEntTrainer;
import cc.mallet.classify.NaiveBayesTrainer;



/**
 * Factory to create Mallet Trainer
 * 
 * @author Sharath
 */
public class BlackbookTrainerFactory {
	
	@SuppressWarnings("unchecked")
	public static ClassifierTrainer CreateNaiveBayesTrainer () {		
		return new NaiveBayesTrainer ();
	}
	
	@SuppressWarnings("unchecked")
	public static ClassifierTrainer CreateMaxEntTrainer () {		
		return new MaxEntTrainer ();
	}

	@SuppressWarnings("unchecked")	
	public static ClassifierTrainer CreateDecisionTreeTrainer () {
		return new DecisionTreeTrainer ();
	}
	
	@SuppressWarnings("unchecked")	
	public static ClassifierTrainer CreateC45Trainer () {
		return new C45Trainer ();
	}

	//public static ClassifierTrainer CreateMaxE	
}

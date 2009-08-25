package utd.pallet.classification;

import java.io.Serializable;

import cc.mallet.classify.Classification;
import cc.mallet.classify.Classifier;
//import cc.mallet.classify.ClassifierTrainer;
//import cc.mallet.types.InstanceList;

/**
 * classifies text document using the specified classifier. 
 * 
 * 
 *
 */

@SuppressWarnings({ "serial" })
public class MalletTextClassify implements Serializable {
	private Classifier 			classifier = null;
	private Object		listToClassify = null;
	
	
	/**
	 * 
	 * @param classifier
	 * @param listToClassify
	 */
	public MalletTextClassify (Classifier classifier, Object listToClassify) {
		this.listToClassify = listToClassify;
		this.classifier = classifier;
	}
	
	/**
	 * Classifies the data on specified algorithm. 
	 * @return Classification - Classification object that needs to be converted to jena model before saving.
	 */
	public Classification classify () {
		return classifier.classify((Object)this.listToClassify);
	}
	
}

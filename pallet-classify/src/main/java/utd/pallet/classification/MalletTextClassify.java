package utd.pallet.classification;

import java.io.Serializable;

import cc.mallet.classify.Classification;
import cc.mallet.classify.Classifier;
import cc.mallet.classify.Trial;
import cc.mallet.pipe.Noop;
import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
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
	Trial trial = null;
		
	/**
	 * 
	 * @param classifier
	 * @param listToClassify
	 */
	public MalletTextClassify () {}
	
	/**
	 * Classifies the data on specified algorithm. 
	 * @return Classification - Classification object that needs to be converted to jena model before saving.
	 * @throws Exception 
	 */
	public Classification classify (Classifier classifier, Object listToClassify) throws Exception {
		//return classifier.classify((Object)this.listToClassify);
						
		if ( classifier == null ) {
			throw new NullPointerException ("");
		}
		
		if (listToClassify == null ) {
			throw new NullPointerException ("");
		}
		
		if (listToClassify instanceof InstanceList) {
			trial = new Trial(classifier, (InstanceList) listToClassify);			
		} else if (listToClassify instanceof String) {			
			Pipe pipe = classifier.getInstancePipe();
			InstanceList list = new InstanceList (new Noop ());
			Instance instance = pipe.instanceFrom(new Instance (listToClassify, null,null,null));			
			list.add(instance);			 
			trial = new Trial (classifier, list);			
		} else if (listToClassify instanceof Instance) {
			throw new Exception ("Yet to be implemented");
		} else {		
			throw new Exception ("Unsupported data type for classification");
		}
					
		//for (int i = 0; i < trial.size(); i++)
		Classification cl = trial.get(0);
				
		return cl;		
	}
	
	public double getAccuracy () throws java.lang.NullPointerException {
		
		throw new NullPointerException ("Yet to be implemented");		
	}
	
}

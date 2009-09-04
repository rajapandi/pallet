package utd.pallet.classification;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import cc.mallet.classify.Classification;
import cc.mallet.classify.Classifier;
import cc.mallet.classify.Trial;
import cc.mallet.pipe.Noop;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.iterator.ArrayIterator;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Labeling;
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
	public ArrayList<Classification> classify (Classifier classifier, Object listToClassify) 
														throws Exception, NullPointerException {
		Pipe 			pipe		= null;
		Instance 		instance 	= null;
		InstanceList 	iList 		= null;
		
		if ( classifier == null ) {
			throw new NullPointerException ("Invalid Classifier object");
		}
		
		pipe = classifier.getInstancePipe();
		if (listToClassify == null ) {
			throw new NullPointerException ("");
		}
		
		if (listToClassify instanceof InstanceList) {
			
			trial = new Trial(classifier, (InstanceList) listToClassify);
			
		} else if (listToClassify instanceof String) {
						
			iList = new InstanceList (new Noop ());
			instance = pipe.instanceFrom(new Instance (listToClassify, null,null,null));			
			iList.add(instance);			 
			trial = new Trial (classifier, iList);
			
		} else if (listToClassify instanceof Instance) {
			Instance testInstance = (Instance) listToClassify; 			
			iList = new InstanceList (new Noop ());
			iList.add(testInstance);			 
			trial = new Trial (classifier, iList);			
			
		} else if (listToClassify instanceof String[]){
						
			iList = new InstanceList (pipe);			
			iList.addThruPipe(new ArrayIterator ((String [])listToClassify, 
					pipe.getTargetAlphabet().lookupObject(0)));
			
			trial = new Trial (classifier, iList);
		} else {
			
			throw new Exception ("Unsupported data type for classification");
		}

		ArrayList<Classification> aList = new ArrayList<Classification>();
		
		for (int i = 0; i < trial.size(); i++) {
			aList.add(trial.get(i));
		}
		
		//Classification cl = trial.get(0);				
		return aList;		
	}
	
	/**
	 * Returns list of Accuracy vector.
	 * Vector represented as list of <object,accuracy> pair where accuracy is the accuracy associated with the object.
	 * @return 
	 */
	ArrayList <HashMap<Object, Double>> getAccuracyVectors () throws Exception, NullPointerException {
				
		ArrayList <HashMap<Object, Double>>  accVectorList = new ArrayList <HashMap<Object, Double>> ();		
		MalletAccuracyVector accuracyVector = new MalletAccuracyVector ();
		
		for (int i = 0; i < trial.size(); i++) {
			Classification cl = this.trial.get(i);
			try {
				accuracyVector.setAccuracy(cl);
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw (e);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw (e);
			}
			HashMap<Object, Double> vector = null;
			try {
				vector = accuracyVector.getAccuracyVector();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw (e);
			}	
			
			accVectorList.add(vector);
		}
		
		return accVectorList;
	}
	
	//ArrDouble getAccuracy 	
	// Not in a shape to be tested
	// Is separate class necessary? -> What if accuracy of previously saved classification is required.
	/**
	 * 
	 */
	public class MalletAccuracyVector {
		
		// Is Object type to be changed to Labelings? - if labelings then cannot use hashmap 
		HashMap<Object, Double> accuracyVector = new HashMap<Object, Double> ();
		/**
		 * 
		 */
		public MalletAccuracyVector () {};
		
		/**
		 * 
		 * @param classification
		 * @throws java.lang.NullPointerException
		 * @throws java.lang.Exception
		 */
		public void setAccuracy (Classification classification) 
						throws java.lang.NullPointerException, java.lang.Exception {
			if (classification == null)
				throw new NullPointerException ("Invalid Classification object");
		
			//accuracyVector.put(key, value);
			//classification.
			throw new Exception ("Yet to be implemented");
			
		}
		
		/**
		 * 
		 * @return
		 * @throws java.lang.Exception
		 */
		public HashMap<Object, Double> getAccuracyVector ()  throws java.lang.Exception {			
			if (accuracyVector.isEmpty() == true)
				throw new Exception ("Empty Accuracy Vector");
			
			return accuracyVector;
		}
		
		/**
		 * 
		 * @return
		 * @throws java.lang.Exception
		 */
		public double getAccuracy () throws java.lang.Exception {
			
			throw new Exception ("Yet to be implemented");		
		}
		
		/**
		 * 
		 * @return
		 * @throws java.lang.Exception
		 */
		public Labeling getLabel () throws java.lang.Exception {
			throw new Exception ("Yet to be implemented");		
		}		
	}	
}


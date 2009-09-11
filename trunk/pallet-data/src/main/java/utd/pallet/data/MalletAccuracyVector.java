package utd.pallet.data;

import java.util.HashMap;

import cc.mallet.classify.Classification;


/**
 * 
 * 
 */
public class MalletAccuracyVector {
	
	Object source = null;
	Object name = null;
	String bestLabel = null;
	HashMap<String, Double> labelAccuracyVector = new HashMap<String, Double> ();
	Classification cl = null;
	
	/**
	 * 
	 */
	public MalletAccuracyVector () {};
	
	/**
	 * Initializes the classification for which the accuracy vector as to be calculated.
	 * @param classification
	 * @throws java.lang.NullPointerException
	 * @throws java.lang.Exception
	 */
	public void setAccuracy (Classification classification) 
					throws java.lang.NullPointerException, java.lang.Exception {
		if (classification == null)
			throw new NullPointerException ("Invalid Classification object");
	
		cl = classification;
		source = classification.getInstance().getSource();
		name = classification.getInstance().getName();
		bestLabel = classification.getLabeling().getBestLabel().toString();
		
		// FeatureVector fv = (FeatureVector) classification.getInstance().getData();
		//int []indices = new int [fv.getIndices().length];
		//Alphabet dict = fv.getAlphabet();
		
		for (int i = 0; i < classification.getLabelVector().getAlphabet().size(); i++) {
			labelAccuracyVector.put (classification.getLabelVector().getAlphabet().lookupObject(i).toString(),
									classification.getLabelVector().value(i));
			//labelAccuracyVector.put(dict.lookupObject(indices[i]).toString(), value);
			
			/*System.out.println ("Label - " + 
						classification.getLabelVector().getAlphabet().lookupObject(i).toString() + 
						",Value  - " + classification.getLabelVector().value(i));*/
		}
		//System.out.println ("---------------------------");
	}
	

	/**
	 * 
	 * @return Hashmap
	 * @throws java.lang.Exception
	 */
	public HashMap<String, Double> getAccuracyVector ()  throws java.lang.Exception {			
		if (labelAccuracyVector.isEmpty() == true)
			throw new Exception ("Empty Accuracy Vector");
					
		return labelAccuracyVector;
	}
	
	/**
	 * 
	 * @return
	 * @throws java.lang.Exception
	 */
	public double getAccuracy () throws java.lang.NullPointerException {
		
		if (cl == null)
			throw new NullPointerException ("Classification not set");
		int index = cl.getLabeling().getBestLabel().getBestIndex();
		return cl.getLabeling().value(index);
	}
	
	/**
	 * 
	 * @return
	 * @throws java.lang.Exception
	 */
	public String getBestLabel () throws java.lang.NullPointerException {
		if (cl == null)
			throw new NullPointerException ("Classification not set");			
		return cl.getLabeling().getBestLabel().toString();
	}
	
	/**
	 * 
	 * @return
	 * @throws java.lang.NullPointerException
	 */
	public Object getSource () throws java.lang.NullPointerException {
		if (cl == null)
			throw new NullPointerException ("Classification not set");
		
		return this.source; // can be null
	}
	
	/**
	 * 
	 * @return
	 * @throws java.lang.NullPointerException
	 */
	public Object getName () throws java.lang.NullPointerException {
		if (cl == null)
			throw new NullPointerException ("Classification not set");			
		return this.name; // can be null			
	}
}
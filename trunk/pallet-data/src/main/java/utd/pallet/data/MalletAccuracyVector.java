package utd.pallet.data;

import java.util.HashMap;

import cc.mallet.classify.Classification;

/**
 * This class contains the methods which are used to get the source ,name and
 * other Important information about Mallet Instances. This class also tells
 * about the accuracy vector and best Label after the classification.
 * 
 */
public class MalletAccuracyVector {

    /**
     * It represents the source of the Mallet Instances
     */
    Object source = null;
    /**
     * It represents the name of the Mallet Instances
     */
    Object name = null;
    /**
     * It represent the best Label associated with particular Mallet Instances
     * after classification.
     */
    String bestLabel = null;
    /**
     * It represents the label Accuracy Vector.
     */
    HashMap<String, Double> labelAccuracyVector = new HashMap<String, Double>();
    /**
	 * 
	 */
    Classification cl = null;

    /**
	 * 
	 */
    public MalletAccuracyVector() {
    };

    /**
     * Initializes the classification for which the accuracy vector as to be
     * calculated.
     * 
     * @param classification
     *            : It takes the classification object.
     * @throws java.lang.NullPointerException
     * @throws java.lang.Exception
     */
    public void setAccuracy(Classification classification)
            throws java.lang.NullPointerException, java.lang.Exception {
        if (classification == null)
            throw new NullPointerException("Invalid Classification object");

        cl = classification;
        source = classification.getInstance().getSource();
        name = classification.getInstance().getName();
        bestLabel = classification.getLabeling().getBestLabel().toString();

        // FeatureVector fv = (FeatureVector)
        // classification.getInstance().getData();
        // int []indices = new int [fv.getIndices().length];
        // Alphabet dict = fv.getAlphabet();

        for (int i = 0; i < classification.getLabelVector().getAlphabet()
                .size(); i++) {
            labelAccuracyVector.put(classification.getLabelVector()
                    .getAlphabet().lookupObject(i).toString(), classification
                    .getLabelVector().value(i));
            // labelAccuracyVector.put(dict.lookupObject(indices[i]).toString(),
            // value);

            /*
             * System.out.println ("Label - " +
             * classification.getLabelVector().getAlphabet
             * ().lookupObject(i).toString() + ",Value  - " +
             * classification.getLabelVector().value(i));
             */
        }
        // System.out.println ("---------------------------");
    }

    /**
     * 
     * @return Hashmap : This HashMap contains the Accuracy Vector.
     * @throws java.lang.Exception
     */
    public HashMap<String, Double> getAccuracyVector()
            throws java.lang.Exception {
        if (labelAccuracyVector.isEmpty() == true)
            throw new Exception("Empty Accuracy Vector");

        return labelAccuracyVector;
    }

    /**
     * 
     * @return labeling : It returns the accuracy for particular Label.
     * @throws java.lang.NullPointerException
     */
    public double getAccuracy() throws java.lang.NullPointerException {

        if (cl == null)
            throw new NullPointerException("Classification not set");
        int index = cl.getLabeling().getBestLabel().getBestIndex();
        return cl.getLabeling().value(index);
    }

    /**
     * 
     * @return String : It returns the best Label of the classification for
     *         particular Mallet Instances.
     * @throws java.lang.NullPointerException
     */
    public String getBestLabel() throws java.lang.NullPointerException {
        if (cl == null)
            throw new NullPointerException("Classification not set");
        return cl.getLabeling().getBestLabel().toString();
    }

    /**
     * 
     * @return Object
     * @throws java.lang.NullPointerException
     */
    public Object getSource() throws java.lang.NullPointerException {
        if (cl == null)
            throw new NullPointerException("Classification not set");

        return this.source; // can be null
    }

    /**
     * 
     * @return Object
     * @throws java.lang.NullPointerException
     */
    public Object getName() throws java.lang.NullPointerException {
        if (cl == null)
            throw new NullPointerException("Classification not set");
        return this.name; // can be null
    }
}
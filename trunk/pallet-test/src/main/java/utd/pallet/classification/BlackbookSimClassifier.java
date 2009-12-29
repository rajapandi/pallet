package utd.pallet.classification;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import utd.pallet.data.MalletAccuracyVector;
import utd.pallet.data.RDFUtils;
import cc.mallet.classify.Classification;
import cc.mallet.classify.Classifier;
import cc.mallet.types.InstanceList;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Classifier Simulator.
 * 
 */
public class BlackbookSimClassifier {

    /**
     * 
     */
    private Classifier classifier = null;
    /**
     * 
     */
    private ArrayList<Classification> classificationList = null;
    /**
     * 
     */
    private ArrayList<MalletAccuracyVector> accVectorList = null;

    /**
     * 
     */
    BlackbookSimClassifier() {
    }

    /**
     * @param SourceName
     *            Filename from which the classifier needs to be fetched.
     * @throws FileNotFoundException
     *             when specified file does not exists.
     * @throws IOException
     *             when specified file cannot be accessed.
     * @throws Exception
     */
    private void getClassifier(String SourceName) throws FileNotFoundException,
            IOException, Exception {

        try {
            classifier = BlackbookSimUtils.getClassifier(SourceName);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block

            throw e;
        } catch (IOException e) {
            // TODO Auto-generated catch block

            throw e;
        } catch (Exception e) {
            // TODO Auto-generated catch block

            throw e;
        }
    }

    /**
     * @param classifierSrc
     *            filename from which the classifier is to be fetched.
     * @param dataToBeClassified
     *            Object of Instance List that needs to be classified.
     * @throws FileNotFoundException
     *             when specified file does not exists.
     * @throws IOException
     * @throws Exception
     */
    public void classifyData(String classifierSrc,
            InstanceList dataToBeClassified) throws FileNotFoundException,
            IOException, Exception {

        try {
            this.getClassifier(classifierSrc);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block

            throw e;
        } catch (IOException e) {
            // TODO Auto-generated catch block

            throw e;
        } catch (Exception e) {
            // TODO Auto-generated catch block

            throw e;
        }
        MalletTextClassify pClassifier = new MalletTextClassify();

        try {
            this.classificationList = pClassifier.classify(classifier,
                    dataToBeClassified);
        } catch (Exception e) {
            throw e;
        }

        this.accVectorList = pClassifier.getAccuracyVectors();

    }

    /**
     * @return ArrayList of Classification object.
     * @throws NullPointerException
     *             when invoked before classifying the data.
     */
    public ArrayList<Classification> getClassificationList()
            throws NullPointerException {

        if (this.classificationList == null)
            throw new NullPointerException("Data not yet Classified");

        return this.classificationList;
    }

    /**
     * @return ArrayList of Accuracy Vector.
     * @throws NullPointerException
     *             when invoked before classiying data.
     * @see MalletAccuracyVector
     */
    public ArrayList<MalletAccuracyVector> getAccuracyVectorList()
            throws NullPointerException {

        if (this.accVectorList == null)
            throw new NullPointerException("Data not yet Classified");
        return accVectorList;
    }

    /**
     * @param classifiedDataId
     *            File name to be used for persisting the classifier.
     * @throws IOException
     * @throws Exception
     */
    public void persistClassifiedData(String classifiedDataId)
            throws IOException, Exception {

        String strClassifier = "";
        try {
            strClassifier = BlackbookSimUtils
                    .convertClassifierToRDF(classifier);
        } catch (Exception e) {
            // TODO Auto-generated catch block

            throw e;
        }

        File file = new File(classifiedDataId);
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
        } catch (IOException e1) {
            // TODO Auto-generated catch block

            throw e1;
        }
        BufferedWriter buffWriter = new BufferedWriter(writer);

        try {
            buffWriter.write(strClassifier);
        } catch (IOException e) {
            // TODO Auto-generated catch block

            throw e;
        }

        // RDF2MalletInstances.
    }

    /**
     * @param validationDataSrc
     *            Data source against which the classified data is to be
     *            validated.
     * @throws Exception
     */
    public void validateData(String validationDataSrc) throws Exception {

        @SuppressWarnings("unused")
        Model answerModel = null;
        try {
            answerModel = RDFUtils.rdf2JenaModel(validationDataSrc);
        } catch (Exception e) {
            // TODO Auto-generated catch block

            throw e;
        }
    }
}

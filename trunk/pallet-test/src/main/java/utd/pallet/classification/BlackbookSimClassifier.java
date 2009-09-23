package utd.pallet.classification;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import utd.pallet.data.JenaModelFactory;
import utd.pallet.data.MalletAccuracyVector;
import cc.mallet.classify.Classification;
import cc.mallet.classify.Classifier;
import cc.mallet.types.InstanceList;

import com.hp.hpl.jena.rdf.model.Model;

public class BlackbookSimClassifier {

    private Classifier classifier = null;
    private ArrayList<Classification> classificationList = null;
    private ArrayList<MalletAccuracyVector> accVectorList = null;

    BlackbookSimClassifier() {
    }

    private void getClassifier(String SourceName) throws FileNotFoundException,
            IOException, Exception {

        try {
            classifier = BlackbookSimUtils.getClassifier(SourceName);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw e;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw e;
        }
    }

    public void classifyData(String classifierSrc,
            InstanceList dataToBeClassified) throws FileNotFoundException,
            IOException, Exception {

        try {
            this.getClassifier(classifierSrc);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw e;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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

    public ArrayList<Classification> getClassificationList()
            throws NullPointerException {

        if (this.classificationList == null)
            throw new NullPointerException("Data not yet Classified");

        return this.classificationList;
    }

    public ArrayList<MalletAccuracyVector> getAccuracyVectorList()
            throws NullPointerException {

        if (this.accVectorList == null)
            throw new NullPointerException("Data not yet Classified");
        return accVectorList;
    }

    public void persistClassifiedData(String classifiedDataId)
            throws IOException, Exception {

        String strClassifier = "";
        try {
            strClassifier = BlackbookSimUtils
                    .convertClassifierToRDF(classifier);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw e;
        }

        File file = new File(classifiedDataId);
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            throw e1;
        }
        BufferedWriter buffWriter = new BufferedWriter(writer);

        try {
            buffWriter.write(strClassifier);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw e;
        }

        // RDF2MalletInstances.
    }

    public void validateData(String validationDataSrc) throws Exception {

        Model answerModel = null;
        try {
            answerModel = JenaModelFactory.rdf2Model(validationDataSrc);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw e;
        }
    }
}

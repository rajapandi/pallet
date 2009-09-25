package utd.pallet.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import cc.mallet.classify.Classification;
import cc.mallet.classify.Classifier;
import cc.mallet.types.InstanceList;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class RDFUtils {

    public static Model createModelWithClassifications (
            ArrayList<MalletAccuracyVector> accVector,

            ArrayList<Classification> classificationList) throws Exception {
        @SuppressWarnings("unused")
        Model rdfModel = ModelFactory.createDefaultModel();
        try {

            String uriAddress = "http://marathonminds.com//MalletClassification//";

            int i = 0;
            for (MalletAccuracyVector av : accVector) {
                String name = (String) av.getName();
                HashMap<String, Double> labelVector = av.getAccuracyVector();
                Iterator<String> iterator = labelVector.keySet().iterator();
                String bestLabel = av.getBestLabel();
                double accuracy = av.getAccuracy();
                String sAccuracy = "" + accuracy;

                Resource originalResource = rdfModel.createResource(name);

                // Best label to which the test data was classified
                Resource bestLabelResource = rdfModel.createResource(uriAddress
                        + bestLabel + i);
                Property bestLabelProperty = rdfModel.createProperty(uriAddress
                        + "#hasBestLabel");
                originalResource.addProperty(bestLabelProperty,
                        bestLabelResource);

                // Accuracy for best
                Resource bestAccResource = rdfModel.createResource(uriAddress
                        + sAccuracy + i);
                Property bestAccuracy = rdfModel.createProperty(uriAddress
                        + "#hasAccuracy");
                originalResource.addProperty(bestAccuracy, bestAccResource);

                while (iterator.hasNext()) {
                    String Label = iterator.next();
                    Double confidencValue = labelVector.get(Label);
                    Property malletConfidenceValue = rdfModel
                            .createProperty(uriAddress
                                    + "#MalletConfidenceValue");
                    Resource labelResource = rdfModel.createResource(uriAddress
                            + Label + i);
                    Property associatedWith = rdfModel
                            .createProperty(uriAddress + "#associatedWith");
                    Property hasValue = rdfModel.createProperty(uriAddress
                            + "#hasValue");
                    labelResource.addProperty(malletConfidenceValue,
                            confidencValue.toString());

                    labelResource.addProperty(associatedWith, originalResource);
                    labelResource.addProperty(hasValue, Label);
                }

                i++;

            }
            return rdfModel;
        } catch (Exception e) {
           throw e;
        }

        
    }

    private static final Property CLASSIFICATION_PROPERTY = ModelFactory
            .createDefaultModel().createProperty(
                    "http://blackbook.com/terms#STAT_EVENT");

    public static InstanceList convertRDFToInstanceList(String rdf,
            Classifier prevClassifier,String classificationProperty) throws Exception {
    	ByteArrayOutputStream bos = null;
    	try{
        
       
            bos = RDF2MalletInstances.convertRDFWithLabels(rdf,
                    classificationProperty, prevClassifier);
       
    	}
            catch(Exception e)
            {
            	
            	throw e;
            }
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
          
        InstanceList iList = (InstanceList) ois.readObject();
        
    	
        return iList;
    }
}

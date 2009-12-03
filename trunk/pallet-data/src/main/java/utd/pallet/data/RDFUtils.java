package utd.pallet.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import utd.pallet.data.MalletAccuracyVector;
import utd.pallet.data.RDF2MalletInstances;
import cc.mallet.classify.Classification;
import cc.mallet.classify.Classifier;
import cc.mallet.types.InstanceList;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * This class is used to display the final classifying Model.
 * 
 */
public class RDFUtils {

    /**
     * dummyURI is used to provide the default URI to all the resources and
     * properties of the final classified Model.
     */
    public static String dummyURI = "http://localhost:8443/blackbook/malletModel";
    /**
     * The property on the basis of which classification is done.
     */
    public static final Property CLASSIFICATION_PROPERTY = ModelFactory
            .createDefaultModel().createProperty(
                    "http://blackbook.com/terms#STAT_EVENT");
    
	private static org.apache.log4j.Logger logger = Logger.getLogger(RDFUtils.class);

    /**
     * @param accVector
     *            : It represents accuracy vector.
     * @param classificationList
     *            : It contains the classification Instances.
     * @return : It returns the final classified Model.
     * @throws Exception
     */
    public static Model createModelWithClassifications(
            ArrayList<MalletAccuracyVector> accVector,

            ArrayList<Classification> classificationList) throws Exception {

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

    /**
     * @param rdf
     *            :The model in the rdf format.
     * @param prevClassifier
     *            : The instances of already trained Classifier.
     * @param classificationProperty
     *            : The Classification Property.
     * @return : It returns the InstanceList.
     * @throws Exception
     */
    public static InstanceList convertRDFToInstanceList(String rdf,
            Classifier prevClassifier, String classificationProperty)
            throws Exception {
        ByteArrayOutputStream bos = null;
        try {

            bos = RDF2MalletInstances.convertRDFWithLabelsSerializable(rdf,
                    classificationProperty, prevClassifier);

        } catch (Exception e) {

            throw e;
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);

        InstanceList iList = (InstanceList) ois.readObject();

        return iList;
    }
    
    
    /**
     * Returns a map containing all the URI resources mapped to a space separated
     * string with literal values.
     */
    public static HashMap<String,String> getResource2ObjectsMap(Model model) {
    	
		HashMap<String, String> resData = new HashMap<String, String>();
		String propertyData = " ";
		ResIterator res = model.listSubjects();

		while (res.hasNext()) {
			Resource resourceOriginal = res.next();
			Resource newResource = null;
			try {
				newResource = findSuperNode(resourceOriginal, model);
			} catch (IllegalStateException e) {
				continue;
			}

			logger.debug("DATA OF LOCAL RESOURCE " + resourceOriginal.toString()
					+ "IS ATTACHED TO THE DATA OF ORIGINAL RESOURCE "
					+ newResource.toString());

			StmtIterator stmtItr = resourceOriginal.listProperties();
			propertyData = " ";
			while (stmtItr.hasNext()) {
				Statement s = stmtItr.next();
				if (s.getObject().isLiteral()) {

					propertyData = propertyData + " "
							+ s.getObject().toString() + " ";

				}
			}
			/**
			 * If Resource already there in the Map ,then get the previous data.
			 * and add the new data.
			 */
			if (resData.containsKey(newResource.toString())) {
				String slrt = resData.get(newResource.toString());
				slrt = slrt + " " + propertyData;
				resData.put(newResource.toString(), slrt);
			}
			/**
			 * If Resource is not already in the Map then add in the Map with
			 * current data.
			 */
			else {
				resData.put(newResource.toString(), propertyData);
			}
		}
		
		return resData;
    }
    
    
	/**
	 * @param r
	 *            It takes the Resource which is not URI(or Blank node)
	 * @param model
	 *            Jena Model
	 * @return It return the original Resource of the passed Blank node.
	 */
	public static Resource findSuperNode(Resource r, Model model) {

		if (r.isURIResource() && !r.isAnon()) {
			return r;
		}

		StmtIterator stmt = model.listStatements((Resource) null,
				(Property) null, r);
		if (stmt.hasNext()) {
			Statement s = stmt.next();
			return findSuperNode(s.getSubject(), model);
		} else {
			throw new IllegalStateException(
					"Model contains no statements with the resource " + r
							+ " as an object.");
		}
	}
}

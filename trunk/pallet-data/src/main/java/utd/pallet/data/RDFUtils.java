package utd.pallet.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import cc.mallet.classify.Classification;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * RDFUtils class is used to generate the final classified model.
 * 
 */
public class RDFUtils {
    /**
     * It refers to the Best Label associated with the data
     */
    private final static String HAS_BEST_LABEL = "#hasBestLabel";
    /**
     * It refers to the confidence value associated with the data for particular
     * label
     */
    private final static String MALLET_CONFIDENCE_VALUE = "#malletConfidenceValue";
    /**
     * The refers to the label association with the data.
     */
    private final static String ASSOCIATED_WITH = "#associatedWith";
    /**
     * It shows the real value of the Label.
     */
    private final static String HAS_VALUE = "#hasValue";
    /**
     * It is default uriAddress attach to each resource and Property.
     */
    private final static String uriAddress = "http://marathonminds.com//MalletClassification//";

    /**
     * @param accVector
     *            : It contains the accuracy vector corresponding to instances.
     * @param classificationList
     *            : It contains classification list.
     * @return Classified Model.
     */
    public static Model createModelWithClassifications(
            ArrayList<MalletAccuracyVector> accVector,

            ArrayList<Classification> classificationList) {

        Model rdfModel = ModelFactory.createDefaultModel();

        try {

            int i = 0;
            for (MalletAccuracyVector av : accVector) {
                String name = (String) av.getName();
                HashMap<String, Double> labelVector = av.getAccuracyVector();
                Iterator<String> iterator = labelVector.keySet().iterator();
                String bestLabel = av.getBestLabel();
                String bestLabelRemoveSpaces = "";
                StringTokenizer removeSpaces = new StringTokenizer(bestLabel);
                while (removeSpaces.hasMoreTokens()) {
                    bestLabelRemoveSpaces = bestLabelRemoveSpaces
                            + removeSpaces.nextToken();
                }

                Resource originalResource = rdfModel.createResource(name);
                Resource bestLabelResource = rdfModel.createResource(uriAddress
                        + bestLabelRemoveSpaces + i);
                Property bestLabelProperty = rdfModel.createProperty(uriAddress
                        + HAS_BEST_LABEL);
                originalResource.addProperty(bestLabelProperty,
                        bestLabelResource);
                while (iterator.hasNext()) {
                    String Label = iterator.next();
                    // String uriLabel=Label.trim();
                    StringTokenizer uriLabel = new StringTokenizer(Label);
                    String uriLabelRemoveSpaces = "";
                    while (uriLabel.hasMoreTokens()) {
                        uriLabelRemoveSpaces = uriLabelRemoveSpaces
                                + uriLabel.nextToken();
                    }

                    Double confidencValue = labelVector.get(Label);
                    Property malletConfidenceValue = rdfModel
                            .createProperty(uriAddress
                                    + MALLET_CONFIDENCE_VALUE);
                    Resource labelResource = rdfModel.createResource(uriAddress
                            + uriLabelRemoveSpaces + i);
                    Property associatedWith = rdfModel
                            .createProperty(uriAddress + ASSOCIATED_WITH);
                    Property hasValue = rdfModel.createProperty(uriAddress
                            + HAS_VALUE);
                    labelResource.addProperty(malletConfidenceValue,
                            confidencValue.toString());
                    labelResource.addProperty(associatedWith, originalResource);
                    labelResource.addProperty(hasValue, Label);
                }
                i++;

            }
            return rdfModel;
        } catch (Exception e) {
            System.out.println(e);
        }

        return rdfModel;
    }

}

package utd.pallet.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
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

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * This class is used to display the final classifying Model.
 * 
 */
public class RDFUtils {

	public static final String MALLET_NAMESPACE = "http://marathonminds.com/Mallet/";

	private static org.apache.log4j.Logger logger = Logger
			.getLogger(RDFUtils.class);

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

			int i = 0;
			for (MalletAccuracyVector av : accVector) {

				// original resource that was classified.
				String name = (String) av.getName();
				Resource origRes = rdfModel.createResource(name);

				// Best label to which the test data was classified
				String bestLabel = av.getBestLabel();
				String bestAccuracy = av.getAccuracy() + "";
				Resource malletClassEvent = rdfModel
						.createResource(MALLET_NAMESPACE
								+ "malletClassifcation" + System.nanoTime()
								+ "" + i);
				Property bestLabelProperty = rdfModel
						.createProperty(MALLET_NAMESPACE
								+ "#hasBestClassification");
				rdfModel.add(addMalletClassification(origRes, malletClassEvent,
						bestLabelProperty, bestLabel, bestAccuracy));

				// add information/scores of other classifications for
				// comparison
				HashMap<String, Double> labelVector = av.getAccuracyVector();
				Iterator<String> iterator = labelVector.keySet().iterator();
				while (iterator.hasNext()) {
					String label = iterator.next();

					String confidenceValue = labelVector.get(label).toString();
					Resource classRes = createMalletResource(rdfModel, i);
					Property hasClassProp = rdfModel
							.createProperty(MALLET_NAMESPACE
									+ "#hasOtherClassification");

					rdfModel.add(addMalletClassification(malletClassEvent,
							classRes, hasClassProp, label, confidenceValue));

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
	public static InstanceList convertSerializedRDFToInstanceList(String rdf,
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

	public static InstanceList convertRDFToInstanceList(Model rdf,
			Property classProp, Classifier prevClassifier) throws Exception {
		// get converted data
		logger.info("	converting rdf with labels " + classProp
				+ " to instance list.");
		InstanceList iList = RDF2MalletInstances
				.trainingDataIntoMalletInstanceList(rdf, classProp, prevClassifier);
		logger.info("	number of instances retrieved from RDF: " + iList.size());

		return iList;
	}

	/**
	 * Returns a map containing all the URI resources mapped to a space
	 * separated string with literal values.
	 */
	public static HashMap<String, String> getResource2ObjectsMap(Model model) {

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

			logger.debug("DATA OF LOCAL RESOURCE "
					+ resourceOriginal.toString()
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

	private static Model addMalletClassification(Resource origRes,
			Resource malletRes, Property malletProperty, String classification,
			String accuracy) {
		Model model = ModelFactory.createDefaultModel();

		// add classification
		model.add(malletRes, malletProperty, classification);
		// and make this the label of this resource
		model
				.add(malletRes, RDFS.label, "Mallet Classified: "
						+ classification);

		// add accuracy for classification
		Property accuracyProp = getMalletAccuracyProperty(model);
		model.add(malletRes, accuracyProp, accuracy);
		// and make this the description of this resource
		model.add(malletRes, RDFS.comment, accuracy);

		// associate to original resource
		Property malletClassProp = getMalletClassificationProperty(model);
		model.add(origRes, malletClassProp, malletRes);

		return model;
	}

	public static Property getMalletClassificationProperty(Model model) {
		return model.createProperty(MALLET_NAMESPACE
				+ "#malletClassificationEvent");
	}

	public static Property getMalletAccuracyProperty(Model model) {
		return model.createProperty(MALLET_NAMESPACE + "#hasAccuracy");
	}

	public static Resource createMalletResource(Model model, int index) {
		return model.createResource(MALLET_NAMESPACE + "malletClassifcation"
				+ System.nanoTime() + "" + index);
	}

	public static Resource createMalletTrainedModelResource(Model model,
			int index) {
		return model.createResource(MALLET_NAMESPACE + "malletTrainedModel"
				+ System.nanoTime() + "" + index);
	}

	public static Model convertClassifierToRDF(Classifier classifier)
			throws Exception {
		Model model = ModelFactory.createDefaultModel();
		Resource res = RDFUtils.createMalletTrainedModelResource(model, 0);
		Property prop = OWL.hasValue;

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(classifier);

		Literal obj = model.createTypedLiteral(bos.toByteArray());

		Statement stmt = model.createLiteralStatement(res, prop, obj);
		model.add(stmt);

		logger.info("converted classifier to rdf");

		return model;
	}

	public static Classifier convertRDFToClassifier(Model model)
			throws Exception {

		StmtIterator stmtItr = model.listStatements((Resource) null,
				OWL.hasValue, (RDFNode) null);
		Statement onlyStmt = stmtItr.nextStatement();

		ByteArrayInputStream bisLiteral = new ByteArrayInputStream(
				(byte[]) onlyStmt.getLiteral().getValue());
		ObjectInputStream ois = new ObjectInputStream(bisLiteral);
		Classifier classifier = (Classifier) ois.readObject();

		return classifier;

	}
}
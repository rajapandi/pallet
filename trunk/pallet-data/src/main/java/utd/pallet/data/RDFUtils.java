package utd.pallet.data;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import utd.pallet.data.MalletAccuracyVector;
import utd.pallet.data.RDF2MalletInstances;
import cc.mallet.classify.Classification;
import cc.mallet.classify.Classifier;
import cc.mallet.pipe.Noop;
import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
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
import com.hp.hpl.jena.shared.BadURIException;
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
	public static Model createJenaModelWithClassifications(
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
	 * convert the rdf model into a list of instances that does have the correct
	 * label (specified literal associated with the classProp).
	 * 
	 * @param model
	 *            - the model to convert.
	 * @param classProp
	 *            - the property marking the correct label
	 * @param prevClassifier
	 *            - the classifier which has the pipe we need for processing.
	 * @return
	 * @throws Exception
	 */
	public static InstanceList convertJenaModelToInstanceList(Model model,
			Property classProp, Classifier prevClassifier) throws Exception {
		// get converted data
		logger.info("	converting rdf with labels " + classProp
				+ " to instance list.");
		try {

			if (model == null || model.isEmpty()) {
				throw new IllegalArgumentException(
						"model cannot be blank or null");
			}
		} catch (Exception e) {

			logger.error(e.toString());

			throw e;
		}

		if (classProp != null) {
			try {
				new URL(classProp.toString());
			} catch (Exception e) {

				logger.error(e.toString() + "   " + classProp
						+ " is not in the proper format of the RDF predicate");

				throw new BadURIException(classProp
						+ " is not in the proper format of the RDF predicate");

			}
		}

		logger.info("Getting resource 2 object map");
		HashMap<String, String> resData = RDFUtils
				.getResource2ObjectsMap(model);

		logger.info("getting instances from resource object map");
		List<Instance> instBeforeProcessing = RDF2MalletInstances
				.getInstancesFromResourceObjectMap(resData, model, classProp);

		logger.info("processing all instances");
		InstanceList instances = RDF2MalletInstances.getProcessedInstances(
				instBeforeProcessing, prevClassifier);

		logger.info("	number of instances retrieved from RDF: "
				+ instances.size());

		return instances;
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

	public static Model convertClassifierToJenaModel(Classifier classifier)
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

	public static Classifier convertJenaModelToClassifier(Model model)
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

	/**
	 * @param modelAsString
	 *            : The classifying data as model
	 * @param classifier
	 *            : The trained classifier
	 * @return : ByteArrayOutputStream Serialized InstanceList containing
	 *         classifying data
	 * @throws Exception
	 */
	public final static InstanceList convertJenaModel2InstanceList(
			Model incomingModel, Classifier classifier) throws Exception {

		InstanceList classifyingDataIntoMalletInstances = new InstanceList(
				new Noop());
		Pipe newPipe = null;

		try {
			if (classifier == null)
				throw new IllegalArgumentException(
						"classifier cannot be blank or null");
		} catch (Exception e) {
			logger.error(e.toString());
			throw e;
		}

		HashMap<String, String> resData = RDFUtils
				.getResource2ObjectsMap(incomingModel);

		Instance i = null;
		Iterator<String> resItr = resData.keySet().iterator();
		newPipe = classifier.getInstancePipe();
		while (resItr.hasNext()) {
			Resource re = incomingModel.createResource(resItr.next());
			String str = resData.get(re.toString());
			if (str == null)
				continue;
			i = newPipe.instanceFrom(new Instance(str, newPipe
					.getTargetAlphabet().lookupObject(0).toString(), re
					.toString(), "RDF/XML"));
			logger.debug("NAME:: " + i.getName().toString());
			logger.debug("DATA::\n" + i.getData().toString());
			logger.debug("TARGET:: " + i.getTarget().toString());
			logger.debug("SOURCE:: " + i.getSource().toString());

			classifyingDataIntoMalletInstances.add(i);
		}

		return classifyingDataIntoMalletInstances;

	}

	
    /**
     * @param model
     *            : It receives the Model which is to be converted into String
     * @param format
     *            : It receives the format of the jena Model
     * @return : It returns the Jena model in String form.
     * @throws Exception
     *             : It throws an exception
     */
    static public String serializeJenaModel(Model model, String format)
            throws Exception {
        try {

            StringWriter sw = new StringWriter();
            model.write(sw, format);
            String rdf = sw.toString();
            return rdf;

        } catch (Exception e) {
            throw e;
        }

    }
    
    /**
     * @param model
     *            : It receives the Model which is to be converted into String
     * @param format
     *            : It receives the format of the jena Model
     * @return : It returns the Jena model in String form.
     * @throws Exception
     *             : It throws an exception
     */
    static public Model deserializeJenaModel(String rdf)
            throws Exception {
		Model model = null;
		try {
			if (StringUtils.isBlank(rdf))
				throw new IllegalArgumentException(
						"model cannot be blank or null");
			InputStream is = new ByteArrayInputStream(rdf
					.getBytes("UTF-8"));
			model = ModelFactory.createDefaultModel();
			model.read(is, "");
		} catch (Exception e) {
			logger.error(e.toString());
			throw e;
		}

		return model;
    }

    /**
     * @param fileName
     *            Name of the input RDF file name
     * @return It returns Jena model
     * @throws Exception
     */
    static public Model rdf2JenaModel(String fileName) throws Exception {
        Model model = null;
        try {

            File fread = new File(fileName);
            FileReader fr = new FileReader(fread);
            BufferedReader br = new BufferedReader(fr);
            model = ModelFactory.createDefaultModel();
            model.read(br, null, "RDF/XML");
        } catch (Exception e) {
            throw e;
        }
        return model;
    }

}

package pallet.blackbook.workflow;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import pallet.test.util.TestUtilities;

import utd.pallet.classification.MalletTextClassify;
import utd.pallet.classification.MalletTextDataTrainer;
import utd.pallet.classification.MalletTextDataTrainer.TrainerObject;
import utd.pallet.data.JenaModelFactory;
import utd.pallet.data.MalletAccuracyVector;
import utd.pallet.data.RDF2MalletInstances;
import utd.pallet.data.RDFUtils;
import blackbook.algorithm.api.AlgorithmResponse;
import blackbook.algorithm.api.RDFFormat;
import blackbook.algorithm.api.RDFRequest;
import blackbook.algorithm.api.RDFResponse;
import blackbook.algorithm.api.RDFRetrievalRequest;
import blackbook.algorithm.api.VoidParameter;
import blackbook.ejb.client.algorithm.proxy.AlgorithmManagerProxyIfc;
import blackbook.ejb.client.metadata.proxy.MetadataManagerProxyIfc;
import blackbook.metadata.api.AlgorithmMetadata;
import blackbook.metadata.api.DataSourceMetadata;
import cc.mallet.classify.Classification;
import cc.mallet.classify.Classifier;
import cc.mallet.types.InstanceList;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.vocabulary.OWL;

public class BlackbookWorkflowSimulator {

	/**
	 * log is created for the purpose of logging
	 */
	private static Logger log = Logger
			.getLogger(BlackbookWorkflowSimulator.class);

	/** the RDF retrieval algorithm */
	private static final String JENA_RETRIEVE = "Jena Retrieve";

	private static final Property BEST_LABEL = ModelFactory
			.createDefaultModel()
			.createProperty(
					"http://marathonminds.com//MalletClassification//#hasBestLabel");

	private static final Property HAS_VALUE = ModelFactory
			.createDefaultModel()
			.createProperty(
					"http://marathonminds.com//MalletClassification//#hasValue");

	private static final Property CLASSIFICATION_PROPERTY = ModelFactory
			.createDefaultModel().createProperty(
					"http://blackbook.com/terms#STAT_EVENT");

	private static final Property ASSOCIATED_WITH = ModelFactory
			.createDefaultModel()
			.createProperty(
					"http://marathonminds.com//MalletClassification//#associatedWith");

	public static void main(String[] args) throws Exception {

		log.setLevel(Level.INFO);

		// get data from file
		String rdfTrainString = null;
		log.error("Getting RDF train data.");
		rdfTrainString = getRDFFromBlackbook(false);

		// get converted data
		log.error("Creating mallet instance list from data.");
		InstanceList trainingList = convertRDFToInstanceList(rdfTrainString);

		// train mallet model
		log.error("Getting trained model using instance list.");
		TrainerObject trnObj = trainMalletModel(trainingList);

		// get classifier as rdf
		log.error("Converting trained model to rdf for storage.");
		String classifierToPersistAsRDF = convertClassifierToRDF(trnObj
				.getClassifier());

		// save classifier in blackbook temp data source
		log.error("Storing trained classifier.");
		String trainedModelDS = null;
		trainedModelDS = persistTrainedModel2Blackbook(classifierToPersistAsRDF);

		// retrieve classifier from blackbook data source
		log.error("Retrieving stored classifier");
		String classifierRetrievedAsRDF = null;
		classifierRetrievedAsRDF = getTrainedModelFromBlackbook(trainedModelDS);

		// convert rdf back to mallet classifier
		log.error("Converting trained model from rdf to mallet classifier");
		Classifier classifier = convertRDFToClassifier(classifierRetrievedAsRDF);

		log.error("Getting test data.");
		String testModel = getRDFFromBlackbook(true);

		log.error("Classifying data.");
		Model classifiedModel = bbClassify(testModel, classifier);

		log.error("Retrieving answer model.");
		Model answerModel = ModelFactory.createDefaultModel();
		String answers = getRDFFromBlackbook(false);
		ByteArrayInputStream bis = new ByteArrayInputStream(answers.getBytes());
		answerModel.read(bis, null);

		log.error("Producing accuracy model.");
		produceReport(classifiedModel, answerModel);
	}

	private static InstanceList convertRDFToMalletData(String rdf)
			throws Exception {

		// get converted data
		ByteArrayOutputStream bos = RDF2MalletInstances.convertRDFWithLabelsSerializable(
				rdf, CLASSIFICATION_PROPERTY.getURI(), null);
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bis);

		InstanceList iList = (InstanceList) ois.readObject();
		log.error("number of instances retrieved from RDF: " + iList.size());

		return iList;
	}

	private static InstanceList convertRDFToInstanceList(String rdf)
			throws Exception {
		// get converter
		// RDF2MalletInstances conv = new RDF2MalletInstances();

		// get converted data
		ByteArrayOutputStream bos = RDF2MalletInstances.convertRDFWithLabelsSerializable(
				rdf, CLASSIFICATION_PROPERTY.getURI(), null);
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bis);

		InstanceList iList = (InstanceList) ois.readObject();
		log.error("number of instances retrieved from RDF: " + iList.size());

		return iList;
	}

	private static TrainerObject trainMalletModel(InstanceList iList) {
		MalletTextDataTrainer bTrainer = new MalletTextDataTrainer();

		TrainerObject trnObj = null;
		try {
			trnObj = bTrainer.train(iList, MalletTextDataTrainer.NAIVE_BAYES);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return trnObj;
	}

	private static String convertClassifierToRDF(Classifier classifier)
			throws Exception {
		Model model = ModelFactory.createDefaultModel();
		Resource res = model.createResource(new URL(
				"http://localhost:8443/blackbook/malletModel_"
						+ System.currentTimeMillis()).toString());
		Property prop = OWL.hasValue;

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(classifier);

		Literal obj = model.createTypedLiteral(bos.toByteArray());

		Statement stmt = model.createLiteralStatement(res, prop, obj);
		model.add(stmt);

		String ret = JenaModelFactory.serializeModel(model, "RDF/XML");

		log.error("converted classifier to rdf:");

		return ret;
	}

	private static Classifier convertRDFToClassifier(String rdf)
			throws Exception {

		Model model = ModelFactory.createDefaultModel();
		ByteArrayInputStream bisModel = new ByteArrayInputStream(rdf.getBytes());
		model.read(bisModel, "RDF/XML");

		StmtIterator stmtItr = model.listStatements((Resource) null,
				OWL.hasValue, (RDFNode) null);
		Statement onlyStmt = stmtItr.nextStatement();

		ByteArrayInputStream bisLiteral = new ByteArrayInputStream(
				(byte[]) onlyStmt.getLiteral().getValue());
		ObjectInputStream ois = new ObjectInputStream(bisLiteral);
		Classifier classifier = (Classifier) ois.readObject();

		return classifier;

	}

	private static Model bbClassify(String rdf, Classifier classifier)
			throws Exception {

		ByteArrayOutputStream bos = RDF2MalletInstances
				.convertRDFWithoutLabels(rdf, classifier);
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bis);

		InstanceList iList = (InstanceList) ois.readObject();
		MalletTextClassify malletClassifier = new MalletTextClassify();

		ArrayList<Classification> classifications = malletClassifier.classify(
				classifier, iList);
		ArrayList<MalletAccuracyVector> mAccVectorList = malletClassifier
				.getAccuracyVectors();

		// FIXME needs confidence values as well - Created an empty static
		// method in RDFUtils(in pallet-data)
		// project. - sj
		Model model = RDFUtils.createModelWithClassifications(mAccVectorList,
				classifications);

		return model;
	}

	private static String getRDFFromBlackbook(boolean testData)
			throws Exception {

		String rdf = "";

		/**
		 * Web service proxy
		 */
		AlgorithmManagerProxyIfc proxy = null;
		proxy = TestUtilities.getAlgorithmManagerEndpoint();

		// Make the test data source interactive...
		MetadataManagerProxyIfc mdm = TestUtilities
				.getMetadataManagerEndpoint();

		Collection<DataSourceMetadata> dsms = mdm
				.getAllDatasources(TestUtilities.TESTADMIN_PUBLICKEY);
		DataSourceMetadata dsm = null;
		for (DataSourceMetadata thisdsm : dsms) {
			// System.err.println(thisdsm.getName());
			if (thisdsm.getName().trim().equals("Monterey")) {
				dsm = thisdsm;
			}
		}

		RDFRetrievalRequest<VoidParameter> rr = new RDFRetrievalRequest<VoidParameter>();
		// get needed predicates
		Set<String> predicates = new HashSet<String>();
		if (!testData) {
			predicates.add("http://blackbook.com/terms#STAT_EVENT");
		}
		predicates.add("http://blackbook.com/terms#incidentDescription");
		rr.setPredicates(predicates);
		rr.setDataSource(dsm.getName());
		rr.setRdfFormat(RDFFormat.RDF_XML.toString());
		// rr.setUris(uris);

		AlgorithmResponse aResponse = proxy.execute(
				TestUtilities.TESTADMIN_PUBLICKEY, JENA_RETRIEVE, rr);
		RDFResponse rdfResponse = ((RDFResponse) aResponse);

		rdf = rdfResponse.getRdf();

		if (rdf.length() < 1000) {
			log.error("rdf retrieval summary: " + rdf);
		} else {
			log.error("rdf retrieval summary: " + rdf.substring(0, 999));
		}

		return rdf;
	}

	private static String persistTrainedModel2Blackbook(String trainedModelAsRDF)
			throws Exception {

		/**
		 * Web service proxy
		 */
		AlgorithmManagerProxyIfc proxy = null;
		proxy = TestUtilities.getAlgorithmManagerEndpoint();

		// Make the test data source interactive...
		MetadataManagerProxyIfc mdm = TestUtilities
				.getMetadataManagerEndpoint();

		String dsName = "malletTrainedModel" + System.currentTimeMillis();
		mdm.createAssertionsDS(TestUtilities.TESTADMIN_PUBLICKEY, dsName);

		DataSourceMetadata dsm = TestUtilities.getMetadataManagerEndpoint()
				.getDataSourceMetadataByName(TestUtilities.TESTADMIN_PUBLICKEY,
						dsName);

		Set<String> namespace = new HashSet<String>();
		namespace.add("urn:webServiceTest:");
		dsm.setNamespace(namespace);

		AlgorithmMetadata alg = TestUtilities.getMetadataManagerEndpoint()
				.getAlgorithmMetadata(TestUtilities.TESTADMIN_PUBLICKEY,
						"Jena Expand");
		alg.setClassName("blackbook.algorithm.jena.JenaExpand");
		alg.setName("Jena Expand");
		dsm.setExpandAlgorithm(alg);
		AlgorithmMetadata alg2 = TestUtilities.getMetadataManagerEndpoint()
				.getAlgorithmMetadata(TestUtilities.TESTADMIN_PUBLICKEY,
						"Jena and Lucene Persist");
		alg.setClassName("blackbook.algorithm.jena.JenaAndLucenePersist");
		alg.setName("Jena and Lucene Persist");
		dsm.setPersistAlgorithm(alg2);

		TestUtilities.getMetadataManagerEndpoint().modifyMetadataObject(
				TestUtilities.TESTADMIN_PUBLICKEY, dsm);

		proxy.executePersist(TestUtilities.TESTADMIN_PUBLICKEY,
				new RDFRequest<VoidParameter>(trainedModelAsRDF, dsName,
						RDFFormat.RDF_XML.toString()));

		return dsName;
	}

	private static String getTrainedModelFromBlackbook(String dsName) {

		String rdf = "";

		try {
			/**
			 * Web service proxy
			 */
			AlgorithmManagerProxyIfc proxy = null;
			proxy = TestUtilities.getAlgorithmManagerEndpoint();

			// Make the test data source interactive...
			MetadataManagerProxyIfc mdm = TestUtilities
					.getMetadataManagerEndpoint();

			Collection<DataSourceMetadata> dsms = mdm
					.getAllDatasources(TestUtilities.TESTADMIN_PUBLICKEY);
			DataSourceMetadata dsm = null;
			for (DataSourceMetadata thisdsm : dsms) {
				// System.err.println(thisdsm.getName());
				if (thisdsm.getName().trim().equals(dsName)) {
					dsm = thisdsm;
				}
			}

			RDFRetrievalRequest<VoidParameter> rr = new RDFRetrievalRequest<VoidParameter>();
			// get needed predicates
			Set<String> predicates = new HashSet<String>();
			predicates.add(OWL.hasValue.toString());
			rr.setPredicates(predicates);
			rr.setDataSource(dsm.getName());
			rr.setRdfFormat(RDFFormat.RDF_XML.toString());

			AlgorithmResponse aResponse = proxy.execute(
					TestUtilities.TESTADMIN_PUBLICKEY, JENA_RETRIEVE, rr);
			RDFResponse rdfResponse = ((RDFResponse) aResponse);

			rdf = rdfResponse.getRdf();

			log.error("rdf retrieval summary: " + rdf.substring(0, 1000));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return rdf;
	}

	private static void produceReport(Model classifiedModel, Model answerModel)
			throws Exception {
		log.error("model of classifications is: "
				+ JenaModelFactory.serializeModel(classifiedModel,
						FileUtils.langNTriple).substring(0, 1000));
		log.error("model of answers is: "
				+ JenaModelFactory.serializeModel(answerModel,
						FileUtils.langNTriple).substring(0, 1000));

		StmtIterator stmts = classifiedModel.listStatements((Resource) null,
				BEST_LABEL, (RDFNode) null);
		int numCorrect = 0;
		int numNotFound = 0;
		int numIncorrect = 0;
		int total = 0;
		while (stmts.hasNext()) {
			Statement stmt = stmts.nextStatement();
			Resource r = stmt.getObject().as(Resource.class);
			NodeIterator nItr = classifiedModel.listObjectsOfProperty(r,
					HAS_VALUE);
			String labeledAs = null;
			if (nItr.hasNext()) {
				labeledAs = nItr.next().asNode().getLiteralValue().toString();
//				log.error("labeled as: " + labeledAs);
			}

			NodeIterator objItr = answerModel.listObjectsOfProperty(stmt.getSubject(),
					CLASSIFICATION_PROPERTY);

			if (objItr.hasNext()) {
				String actualValue = objItr.next().asNode().getLiteralValue()
						.toString();
				// log.error("actual value is: " + actualValue);
				if (actualValue.equalsIgnoreCase(labeledAs)) {
					numCorrect++;
				} else {
					log.error("for uri: " + r.getURI() + " thought it was " + labeledAs + " but actual value is: " + actualValue);
					numIncorrect++;
				}
			} else {
				numNotFound++;
			}
			total++;
		}

		log.error("total entities is: " + total + ".  classified " + numCorrect
				+ " correctly and " + (numIncorrect) + " incorrectly.  "
				+ numNotFound + " were not found in the answer model.");
	}
}

package pallet.algorithm.blackbook;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pallet.blackbook.util.BlackbookUtil;
import security.ejb.client.User;
import utd.pallet.classification.MalletTextDataTrainer;
import utd.pallet.classification.MalletTextDataTrainer.TrainerObject;
import utd.pallet.data.RDF2MalletInstances;
import workflow.ejb.client.annotations.Execute;
import blackbook.algorithm.api.Algorithm;
import blackbook.algorithm.api.DataSourceRequest;
import blackbook.algorithm.api.VoidParameter;
import blackbook.algorithm.api.VoidResponse;
import blackbook.exception.BlackbookSystemException;
import blackbook.jena.util.JenaModelCache;
import cc.mallet.classify.Classifier;
import cc.mallet.types.InstanceList;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.OWL;

/**
 * Remove all materialized results that are isolated. This is accomplished by
 * checking for outgoing and incoming references to each resource in the
 * supplied model.
 */
public class MalletTrain implements
		Algorithm<DataSourceRequest<VoidParameter>, VoidResponse> {

	/** logger */
	private static Log logger = LogFactory.getLog(MalletTrain.class);

	private static final Property CLASSIFICATION_PROPERTY = ModelFactory
			.createDefaultModel().createProperty(
					"http://blackbook.com/terms#STAT_EVENT");

	/**
	 * @param user
	 * @param request
	 * @return DataSourceResponse
	 * @throws BlackbookSystemException
	 * @see blackbook.algorithm.api.Algorithm#execute(security.ejb.client.User,
	 *      blackbook.algorithm.api.AlgorithmRequest)
	 */
	@Execute
	public VoidResponse execute(User user,
			DataSourceRequest<VoidParameter> request)
			throws BlackbookSystemException {
		if (user == null) {
			throw new BlackbookSystemException("'user' cannot be null.");
		}

		if (request == null) {
			throw new BlackbookSystemException("'parameters' cannot be null.");
		}

		request.validate();

		// this class caches models to allow for multiple opens among multiple
		// URIs
		JenaModelCache modelCache = new JenaModelCache();
		try {

			Model sourceModel = modelCache.getModelByName(request
					.getSourceDataSource(), user);

			// get converted data
			logger.info("Creating mallet instance list from data.");
			InstanceList trainingList = convertRDFToInstanceList(sourceModel);

			// train mallet model
			logger.info("Getting trained model using instance list.");
			TrainerObject trnObj = trainMalletModel(trainingList);

			// get classifier as rdf
			logger.info("Converting trained model to rdf for storage.");
			Model trainedModel = convertClassifierToRDF(trnObj
					.getClassifier());

			// save classifier in blackbook temp data source
			logger.info("Storing trained classifier.");
			String dsName = "MalletTrainedModel" + System.currentTimeMillis();
			Model assertionsModel = BlackbookUtil.persist2BlackbookAssertions(trainedModel, dsName, "urn:mallet:", user);
			assertionsModel.close();
			trainedModel.close();

		} catch (Exception e) {
			e.printStackTrace();
			throw new BlackbookSystemException("Unable to execute algorithm :"
					+ this.getClass().getName(), e);
		} finally {
			modelCache.closeAll();
		}
		
		return VoidResponse.getInstance();
	}

	private static InstanceList convertRDFToInstanceList(Model rdf)
			throws Exception {
		// get converter
		// RDF2MalletInstances conv = new RDF2MalletInstances();

		// get converted data
		logger.info("	converting rdf with labels " + CLASSIFICATION_PROPERTY
				+ " to instance list.");
		InstanceList iList = RDF2MalletInstances
				.trainingDataIntoMalletInstanceList(rdf,
						CLASSIFICATION_PROPERTY, null);
		logger.info("	number of instances retrieved from RDF: " + iList.size());

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

	private static Model convertClassifierToRDF(Classifier classifier)
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

		logger.info("converted classifier to rdf:");

		return model;
	}
}

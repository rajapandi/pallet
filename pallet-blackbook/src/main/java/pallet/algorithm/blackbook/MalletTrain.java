package pallet.algorithm.blackbook;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pallet.blackbook.util.BlackbookUtil;
import security.ejb.client.User;
import utd.pallet.classification.MalletTextDataTrainer;
import utd.pallet.classification.MalletTextDataTrainer.TrainerObject;
import utd.pallet.data.RDFUtils;
import workflow.ejb.client.annotations.Execute;
import blackbook.algorithm.api.Algorithm;
import blackbook.algorithm.api.DataSourceRequest;
import blackbook.algorithm.api.StringParameter;
import blackbook.algorithm.api.VoidResponse;
import blackbook.exception.BlackbookSystemException;
import blackbook.jena.util.JenaModelCache;
import cc.mallet.types.InstanceList;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;


public class MalletTrain implements
		Algorithm<DataSourceRequest<StringParameter>, VoidResponse> {

	/** logger */
	private static Log logger = LogFactory.getLog(MalletTrain.class);

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
			DataSourceRequest<StringParameter> request)
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
			logger.info("Creating mallet instance list from data, using classification parameter: " + request.getParameters().getParameter());
			Property classProp = sourceModel.createProperty(request.getParameters().getParameter());
			InstanceList trainingList = RDFUtils.convertJenaModelToInstanceList(sourceModel, classProp, null);

			// train mallet model
			logger.info("Getting trained model using instance list.");
			TrainerObject trnObj = trainMalletModel(trainingList);

			// get classifier as rdf
			logger.info("Converting trained model to rdf for storage.");
			Model trainedModel = RDFUtils.convertClassifierToJenaModel(trnObj
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
}

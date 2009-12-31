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

/**
 * A Blackbook/Mallet algorithm for training a mallet model.
 * The StringParameter should contain the predicate to use to retrieve
 * the "label" or "classification" of each entity.
 * NaiveBayes is the only currently supported algorithm.
 */
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
		long startTime = System.currentTimeMillis();
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
			logger.info("\tcreating mallet instance list from data, using classification parameter: " + request.getParameters().getParameter());
			long t1 = System.currentTimeMillis();
			Property classProp = sourceModel.createProperty(request.getParameters().getParameter());
			InstanceList trainingList = RDFUtils.convertJenaModelToInstanceList(sourceModel, classProp, null);
			logger.info("\tmallet instance list creation took: " + (System.currentTimeMillis()-t1)/1000 + " sec.");

			// train mallet model
			logger.info("\tgetting trained model using instance list.");
			long t2 = System.currentTimeMillis();
			TrainerObject trnObj = trainMalletModel(trainingList);
			logger.info("\ttraining took: " + (System.currentTimeMillis()-t2)/1000 + " sec.");

			// get classifier as rdf
			logger.info("\tconverting trained model to rdf for storage.");
			long t3 = System.currentTimeMillis();
			Model trainedModel = RDFUtils.convertClassifierToJenaModel(trnObj
					.getClassifier());
			logger.info("\tconverting classifier to rdf took: " + (System.currentTimeMillis()-t3)/1000 + " sec.");

			// save classifier in blackbook temp data source
			logger.info("\tstoring trained classifier.");
			long t4 = System.currentTimeMillis();
			String dsName = "MalletTrainedModel" + System.currentTimeMillis();
			Model assertionsModel = BlackbookUtil.persist2BlackbookAssertions(trainedModel, dsName, "urn:mallet:", user);
			logger.info("\tpersisting trained model as rdf into blackbook took: " + (System.currentTimeMillis()-t4)/1000 + " sec.");
			
			assertionsModel.close();
			trainedModel.close();

			logger.info("\tmallet train algorithm took: " + (System.currentTimeMillis()-startTime)/1000 + " sec.");
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

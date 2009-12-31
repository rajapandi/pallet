package pallet.algorithm.blackbook;

import java.util.Set;

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
import cc.mallet.classify.Classifier;
import cc.mallet.classify.NaiveBayes;
import cc.mallet.classify.NaiveBayesTrainer;
import cc.mallet.types.InstanceList;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;

/**
 * A Blackbook/Mallet algorithm for incrementally training a pre-existing
 * mallet model.  The StringParameter should contain the predicate to use
 * to retrieve the "label" or "classification" of each entity.
 * NaiveBayes is the only currently supported algorithm.
 */
public class MalletTrainIncremental implements
		Algorithm<DataSourceRequest<StringParameter>, VoidResponse> {

	/** logger */
	private static Log logger = LogFactory.getLog(MalletTrainIncremental.class);

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
			
			long t1 = System.currentTimeMillis();
			Set<String> prevTrainedModels = request.getAssertionsDataSources();
			Classifier prevClassifier = null;
			if(prevTrainedModels.size() == 0) {
				throw new BlackbookSystemException("must provide at least one assertions data source name that contains a trained model, in order for me to incrementally train it.");
			} else {
				for(String tModel : prevTrainedModels) {
					logger.info("\tretrieving previously trained model: " + tModel);
					Model currentModel = modelCache.getModelByName(tModel, user);
					prevClassifier = RDFUtils.convertJenaModelToClassifier(currentModel);
					break;
				}
			}
			logger.info("\tretrieving original trained model from blackbook took: " + (System.currentTimeMillis()-t1)/1000 + " sec.");

			// get converted data
			logger.info("\tcreating mallet instance list from data.");
			long t2 = System.currentTimeMillis();
			Property classProp = sourceModel.createProperty(request.getParameters().getParameter());
			InstanceList trainingList = RDFUtils.convertJenaModelToInstanceList(sourceModel, classProp, prevClassifier);
			logger.info("\tcreation of instance list took: " + (System.currentTimeMillis()-t2)/1000 + " sec.");

			// train mallet model
			logger.info("\tincrementally training model using instance list.");
			long t3 = System.currentTimeMillis();
			NaiveBayes nbClassifier = (NaiveBayes) prevClassifier;
			NaiveBayesTrainer naiveBayesTrainer = new NaiveBayesTrainer(nbClassifier);
			TrainerObject trnObj = incrementallyTrainMalletModel(naiveBayesTrainer, trainingList);
			logger.info("\tincremental training took: " + (System.currentTimeMillis()-t3)/1000 + " sec.");

			// get classifier as rdf
			logger.info("\tconverting trained model to rdf for storage.");
			long t4 = System.currentTimeMillis();
			Model trainedModel = RDFUtils.convertClassifierToJenaModel(trnObj
					.getClassifier());
			logger.info("\tconversion of classifier to rdf took: " + (System.currentTimeMillis()-t4)/1000 + " sec.");

			// save classifier in blackbook temp data source
			logger.info("\tstoring trained classifier.");
			long t5 = System.currentTimeMillis();
			String dsName = "MalletTrainedModel" + System.currentTimeMillis();
			Model assertionsModel = BlackbookUtil.persist2BlackbookAssertions(trainedModel, dsName, "urn:mallet:", user);
			logger.info("\tstoring of classifier took: " + (System.currentTimeMillis()-t5)/1000 + " sec.");
			
			assertionsModel.close();
			trainedModel.close();

		} catch (Exception e) {
			e.printStackTrace();
			throw new BlackbookSystemException("Unable to execute algorithm :"
					+ this.getClass().getName(), e);
		} finally {
			modelCache.closeAll();
		}
		
		logger.info("Mallet incremental training took: " + (System.currentTimeMillis()-startTime)/1000 + " sec.");
		return VoidResponse.getInstance();
	}
	
	public static TrainerObject incrementallyTrainMalletModel(NaiveBayesTrainer prevTrainer, InstanceList listToTrain) {
		MalletTextDataTrainer bTrainer = new MalletTextDataTrainer();

		TrainerObject trnObj = null;
		try {
			trnObj = bTrainer.trainIncremental(prevTrainer, listToTrain);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return trnObj;
	}
}

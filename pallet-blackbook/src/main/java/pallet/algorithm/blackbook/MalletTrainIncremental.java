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
			
			Set<String> prevTrainedModels = request.getAssertionsDataSources();
			Classifier prevClassifier = null;
			if(prevTrainedModels.size() == 0) {
				throw new BlackbookSystemException("must provide at least one assertions data source name that contains a trained model, in order for me to incrementally train it.");
			} else {
				for(String tModel : prevTrainedModels) {
					logger.info("	retrieving previously trained model: " + tModel);
					Model currentModel = modelCache.getModelByName(tModel, user);
					prevClassifier = RDFUtils.convertJenaModelToClassifier(currentModel);
					break;
				}
			}

			// get converted data
			logger.info("Creating mallet instance list from data.");
			Property classProp = sourceModel.createProperty(request.getParameters().getParameter());
			InstanceList trainingList = RDFUtils.convertJenaModelToInstanceList(sourceModel, classProp, prevClassifier);

			// train mallet model
			logger.info("incrementally training model using instance list.");
			NaiveBayes nbClassifier = (NaiveBayes) prevClassifier;
			NaiveBayesTrainer naiveBayesTrainer = new NaiveBayesTrainer(nbClassifier);
			TrainerObject trnObj = incrementallyTrainMalletModel(naiveBayesTrainer, trainingList);

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

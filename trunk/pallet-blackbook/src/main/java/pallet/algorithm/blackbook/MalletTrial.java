package pallet.algorithm.blackbook;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import security.ejb.client.User;
import utd.pallet.data.RDFUtils;
import workflow.ejb.client.annotations.Execute;
import blackbook.algorithm.api.Algorithm;
import blackbook.algorithm.api.DataSourceRequest;
import blackbook.algorithm.api.StringParameter;
import blackbook.algorithm.api.VoidResponse;
import blackbook.exception.BlackbookSystemException;
import blackbook.jena.util.JenaModelCache;
import cc.mallet.classify.Classifier;
import cc.mallet.classify.Trial;
import cc.mallet.types.InstanceList;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;


public class MalletTrial implements
		Algorithm<DataSourceRequest<StringParameter>, VoidResponse> {

	/** logger */
	private static Log logger = LogFactory.getLog(MalletTrial.class);

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
					prevClassifier = RDFUtils.convertRDFToClassifier(currentModel);
					break;
				}
			}

			// get converted data
			logger.info("Creating mallet instance list from data.");
			Property classProp = sourceModel.createProperty(request.getParameters().getParameter());
			InstanceList trialList = RDFUtils.convertRDFToInstanceList(sourceModel, classProp, prevClassifier);

			// trial
			logger.info("running trial report.");
			Trial trial = new Trial(prevClassifier,trialList);
			String report = produceReport(trial, trialList);
			logger.info("report being returned is: " + report);

		} catch (Exception e) {
			e.printStackTrace();
			throw new BlackbookSystemException("Unable to execute algorithm :"
					+ this.getClass().getName(), e);
		} finally {
			modelCache.closeAll();
		}
		
		return VoidResponse.getInstance();
	}
	
	public static String produceReport(Trial trial, InstanceList trialList) {
		String report = "";
		
		report = report + "\nAccuracy: " + trial.getAccuracy();
		report = report + "\nAvg. Rank: " + trial.getAverageRank();
		
		for(int i = 0; i < trialList.size(); i++) {
			report = report + "\nReviewing instance: " + i;
			report = report + "\n\tF1: " + trial.getF1(trialList.get(i).getLabeling());
			report = report + "\n\tPrecision: " + trial.getPrecision(trialList.get(i).getLabeling());
			report = report + "\n\tRecall: " + trial.getRecall(trialList.get(i).getLabeling());
		}

		return report;
	}
}

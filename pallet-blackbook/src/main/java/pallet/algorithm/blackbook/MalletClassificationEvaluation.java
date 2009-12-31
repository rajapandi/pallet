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
import cc.mallet.types.InstanceList;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;

/**
 * A Blackbook/Mallet algorithm to evaluate how well a mallet classifier has performed.
 */
public class MalletClassificationEvaluation implements
		Algorithm<DataSourceRequest<StringParameter>, VoidResponse> {

	/** logger */
	private static Log logger = LogFactory.getLog(MalletClassificationEvaluation.class);

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
		String report = "";
		try {

			Model sourceModel = modelCache.getModelByName(request
					.getSourceDataSource(), user);
			
			logger.info("\tgetting previously trained mallet model");
			long t2 = System.currentTimeMillis();
			Set<String> prevTrainedModels = request.getAssertionsDataSources();
			Classifier classifier = null;
			if(prevTrainedModels.size() == 0) {
				throw new BlackbookSystemException("must provide at least one assertions data source name that contains a trained model, in order for me to incrementally train it.");
			} else {
				for(String tModel : prevTrainedModels) {
					logger.info("	retrieving previously trained model: " + tModel);
					Model currentModel = modelCache.getModelByName(tModel, user);
					classifier = RDFUtils.convertJenaModelToClassifier(currentModel);
					break;
				}
			}
			logger.info("\tretrieval of previously trained mallet model took: " + (System.currentTimeMillis()-t2)/1000 + " sec.");

			// get converted data
			logger.info("\tcreating mallet instance list from data.");
			long t3 = System.currentTimeMillis();
			Property classProp = sourceModel.createProperty(request.getParameters().getParameter());
			InstanceList trialList = RDFUtils.convertJenaModelToInstanceList(sourceModel, classProp, classifier);
			logger.info("\tinstance list retrieval took: " + (System.currentTimeMillis()-t3)/1000 + " sec.");

			// trial
			logger.info("\trunning classification evaluation.");
			long t4 = System.currentTimeMillis();
			report = produceReport(classifier, trialList);
			logger.info("\tevaluation took: " + (System.currentTimeMillis()-t4)/1000 + " sec.");

		} catch (Exception e) {
			e.printStackTrace();
			throw new BlackbookSystemException("Unable to execute algorithm :"
					+ this.getClass().getName(), e);
		} finally {
			modelCache.closeAll();
		}
		logger.info("Mallet classification evaluation took: " + (System.currentTimeMillis()-startTime)/1000 + " sec.");
		
		//TODO THIS IS A HACK... SHOULD NOT THROW AN EXCEPTION...
		//only doing this so that the blackbook interface can see the report
		if(true) {
			throw new BlackbookSystemException(report);
		}
		return VoidResponse.getInstance();
	}
	
	/**
	 * produce a report with accuracy, avg. rank, etc. for this classifier and a
	 * list of results with answers.
	 * @param classifier
	 * @param trialList
	 * @return the report
	 */
	public static String produceReport(Classifier classifier, InstanceList trialList) {
		String report = "";
		
		if(trialList == null || classifier == null || trialList.size() == 0) {
			report = "the instance list was empty";
		}
		else {
			report = report + "\nNumber of instances: " + trialList.size();
			report = report + "\nNumber labeled correctly: " + (trialList.size()*classifier.getAccuracy(trialList));
			report = report + "\nAccuracy: " + classifier.getAccuracy(trialList);
			report = report + "\nAvg. Rank: " + classifier.getAverageRank(trialList);
			logger.info(report);
			
			
			for(int i = 0; i < trialList.size(); i++) {
				String subreport = "";
				subreport = subreport + "\nReviewing instance: " + trialList.get(i).getTarget() + " with best label: " + classifier.classify(trialList.get(i)).getLabeling().getBestValue();
				subreport = subreport + "\n\tF1: " + classifier.getF1(trialList, trialList.get(i).getLabeling());
				subreport = subreport + "\n\tPrecision: " + classifier.getPrecision(trialList, trialList.get(i).getLabeling());
				subreport = subreport + "\n\tRecall: " + classifier.getRecall(trialList, trialList.get(i).getLabeling());
				report = report + subreport;
			}
		}

		return report;
	}
}

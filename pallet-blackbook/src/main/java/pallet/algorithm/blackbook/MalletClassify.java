package pallet.algorithm.blackbook;

import java.util.ArrayList;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pallet.blackbook.util.BlackbookUtil;

import security.ejb.client.User;
import utd.pallet.classification.MalletTextClassify;
import utd.pallet.data.MalletAccuracyVector;
import utd.pallet.data.RDFUtils;
import workflow.ejb.client.annotations.Execute;
import blackbook.algorithm.api.Algorithm;
import blackbook.algorithm.api.DataSourceRequest;
import blackbook.algorithm.api.DataSourceResponse;
import blackbook.algorithm.api.VoidParameter;
import blackbook.exception.BlackbookSystemException;
import blackbook.jena.util.JenaModelCache;
import blackbook.metadata.manager.MetadataManagerFactory;
import cc.mallet.classify.Classification;
import cc.mallet.classify.Classifier;
import cc.mallet.types.InstanceList;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * A Blackbook/Mallet algorithm for classifying a data set.
 * All of the triples in the data source request will be used to
 * determine the classification.
 * NaiveBayes is the only currently supported algorithm.
 */
public class MalletClassify implements
		Algorithm<DataSourceRequest<VoidParameter>, DataSourceResponse> {

	/** logger */
	private static Log logger = LogFactory.getLog(MalletClassify.class);

	/**
	 * @param user
	 * @param request
	 * @return DataSourceResponse
	 * @throws BlackbookSystemException
	 * @see blackbook.algorithm.api.Algorithm#execute(security.ejb.client.User,
	 *      blackbook.algorithm.api.AlgorithmRequest)
	 */
	@Execute
	public DataSourceResponse execute(User user,
			DataSourceRequest<VoidParameter> request)
			throws BlackbookSystemException {
		long startTime = System.currentTimeMillis();
		if (user == null) {
			throw new BlackbookSystemException("'user' cannot be null.");
		}

		if (request == null) {
			throw new BlackbookSystemException("'parameters' cannot be null.");
		}

		request.validate();

		String destinationDataSource = MetadataManagerFactory.getInstance()
				.createTemporaryDS(request.getDestinationDataSource(),
						request.getDestinationModelType());

		// this class caches models to allow for multiple opens among multiple
		// URIs
		JenaModelCache modelCache = new JenaModelCache();
		try {
			Model sourceModel = modelCache.getModelByName(request.getSourceDataSource()
					, user);

			long t1 = System.currentTimeMillis();
			Set<String> aDsNames = request.getAssertionsDataSources();
			Model trainedModel = null;
			if(aDsNames != null && aDsNames.size() > 0) {
				String aName = aDsNames.iterator().next();
				logger.info("\topening assertions model to retrieve mallet trained model: " + aName);
				trainedModel = modelCache.getModelByName(aName, user);
			} else {
				throw new IllegalStateException("no assertions data source to retrieve trained model from.");
			}
			logger.info("\tretrieving trained model rdf from assertions took: " + (System.currentTimeMillis()-t1)/1000 + " sec.");

			// convert rdf back to mallet classifier
			logger
					.error("\tconverting trained model from rdf to mallet classifier");
			long t2 = System.currentTimeMillis();
			Classifier classifier = RDFUtils.convertJenaModelToClassifier(trainedModel);
			logger.info("\tconversion of rdf to trained model took: " + (System.currentTimeMillis()-t2)/1000 + " sec.");

			logger.error("\tclassifying data of size: " + sourceModel.size());
			long t3 = System.currentTimeMillis();
			Model classifiedModel = bbClassify(sourceModel, classifier);
			logger.info("\tclassified model size is: " + classifiedModel.size());
			logger.info("\tclassifying took: " + (System.currentTimeMillis()-t3)/1000 + " sec.");
			
			String dsName = "MalletClassifiedModel" + System.currentTimeMillis();
			long t4 = System.currentTimeMillis();
			Model assertionsModel = BlackbookUtil.persist2BlackbookAssertions(classifiedModel, dsName, RDFUtils.MALLET_NAMESPACE, user);
			logger.info("\tpersisting classification results into blackbook took: " + (System.currentTimeMillis()-t4)/1000 + " sec.");

			long t5 = System.currentTimeMillis();
			Model destinationModel = modelCache.getModelByName(destinationDataSource, user);
			destinationModel.add(sourceModel);
			destinationModel.add(assertionsModel);
			logger.info("\tpersisting combined (traditional and classifications) into a temp ds took: " + (System.currentTimeMillis()-t5)/1000 + " sec.");
			
			assertionsModel.close();
			
		} catch (Exception e) {
			throw new BlackbookSystemException("Unable to execute algorithm :"
					+ request.getSourceDataSource(), e);
		} finally {
			modelCache.closeAll();
		}

		logger.info("Mallet classification algorithm took: " + (System.currentTimeMillis()-startTime)/1000 + " sec.");
		return new DataSourceResponse(destinationDataSource);
	}

	/**
	 * Classify the content in the model provided, using the classifier provided.
	 * The model returned is the full model (original and classification content.
	 * @param model
	 * @param classifier
	 * @return
	 * @throws Exception
	 */
	private static Model bbClassify(Model model, Classifier classifier)
			throws Exception {

		InstanceList iList = RDFUtils
				.convertJenaModel2InstanceList(model, classifier);
		 
		MalletTextClassify malletClassifier = new MalletTextClassify();

		ArrayList<Classification> classifications = malletClassifier.classify(
				classifier, iList);
		ArrayList<MalletAccuracyVector> mAccVectorList = malletClassifier
				.getAccuracyVectors();

		model = RDFUtils.createJenaModelWithClassifications(mAccVectorList,
				classifications);

		return model;
	}

}

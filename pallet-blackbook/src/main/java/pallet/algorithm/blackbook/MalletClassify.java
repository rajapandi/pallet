package pallet.algorithm.blackbook;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pallet.blackbook.util.BlackbookUtil;

import security.ejb.client.User;
import utd.pallet.classification.MalletTextClassify;
import utd.pallet.data.MalletAccuracyVector;
import utd.pallet.data.RDF2MalletInstances;
import utd.pallet.data.RDFUtils;
import workflow.ejb.client.annotations.Execute;
import blackbook.algorithm.api.Algorithm;
import blackbook.algorithm.api.DataSourceRequest;
import blackbook.algorithm.api.DataSourceResponse;
import blackbook.algorithm.api.VoidParameter;
import blackbook.exception.BlackbookSystemException;
import blackbook.jena.util.JenaModelCache;
import blackbook.jena.util.JenaUtils;
import blackbook.metadata.manager.MetadataManagerFactory;
import cc.mallet.classify.Classification;
import cc.mallet.classify.Classifier;
import cc.mallet.types.InstanceList;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;

/**
 * Remove all materialized results that are isolated. This is accomplished by
 * checking for outgoing and incoming references to each resource in the
 * supplied model.
 */
public class MalletClassify implements
		Algorithm<DataSourceRequest<VoidParameter>, DataSourceResponse> {

	/** logger */
	private static Log logger = LogFactory.getLog(MalletClassify.class);

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
	public DataSourceResponse execute(User user,
			DataSourceRequest<VoidParameter> request)
			throws BlackbookSystemException {
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

			Set<String> aDsNames = request.getAssertionsDataSources();
			Model trainedModel = null;
			if(aDsNames != null && aDsNames.size() > 0) {
				String aName = aDsNames.iterator().next();
				logger.info("opening assertions model to retrieve mallet trained model: " + aName);
				trainedModel = modelCache.getModelByName(aName, user);
			} else {
				throw new IllegalStateException("no assertions data source to retrieve trained model from.");
			}

			// convert rdf back to mallet classifier
			logger
					.error("Converting trained model from rdf to mallet classifier");
			Classifier classifier = convertRDFToClassifier(trainedModel);

			logger.error("Classifying data of size: " + sourceModel.size());
			Model classifiedModel = bbClassify(sourceModel, classifier);
			logger.info("classified model size is: " + classifiedModel.size());
			
			String dsName = "MalletClassifiedModel" + System.currentTimeMillis();
			Model assertionsModel = BlackbookUtil.persist2BlackbookAssertions(classifiedModel, dsName, RDFUtils.MALLET_NAMESPACE, user);

			Model destinationModel = modelCache.getModelByName(destinationDataSource, user);
			destinationModel.add(sourceModel);
			destinationModel.add(assertionsModel);
			
			assertionsModel.close();
			
		} catch (Exception e) {
			throw new BlackbookSystemException("Unable to execute algorithm :"
					+ request.getSourceDataSource(), e);
		} finally {
			modelCache.closeAll();
		}

		return new DataSourceResponse(destinationDataSource);
	}

	private static Classifier convertRDFToClassifier(Model model)
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

	private static Model bbClassify(Model model, Classifier classifier)
			throws Exception {

		InstanceList iList = RDF2MalletInstances
				.convertModel2InstanceList(model, classifier);
		 
		MalletTextClassify malletClassifier = new MalletTextClassify();

		ArrayList<Classification> classifications = malletClassifier.classify(
				classifier, iList);
		ArrayList<MalletAccuracyVector> mAccVectorList = malletClassifier
				.getAccuracyVectors();

		model = RDFUtils.createModelWithClassifications(mAccVectorList,
				classifications);

		return model;
	}

}

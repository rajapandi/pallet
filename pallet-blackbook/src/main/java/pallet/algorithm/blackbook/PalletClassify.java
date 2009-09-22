package pallet.algorithm.blackbook;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import security.ejb.client.User;
import workflow.ejb.client.annotations.Execute;
import blackbook.algorithm.api.Algorithm;
import blackbook.algorithm.api.DataSourceHelperSourceRequest;
import blackbook.algorithm.api.DataSourceResponse;
import blackbook.algorithm.api.DataSourceSetRequest;
import blackbook.algorithm.api.VoidParameter;
import blackbook.exception.BlackbookSystemException;
import blackbook.jena.factory.JenaModelFactory;
import blackbook.metadata.manager.MetadataManagerFactory;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * This class combines two RDF models passed in as parameters.
 */
public class PalletClassify
		implements
		Algorithm<DataSourceHelperSourceRequest<VoidParameter>, DataSourceResponse> {
	/** logger */
	private static Log logger = LogFactory.getLog(PalletClassify.class);

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
			DataSourceHelperSourceRequest<VoidParameter> request)
			throws BlackbookSystemException {
		if (user == null) {
			throw new BlackbookSystemException("'user' cannot be null.");
		}

		if (request == null) {
			throw new BlackbookSystemException("'parameters' cannot be null.");
		}

		request.validate();

		String destinationDataSource = MetadataManagerFactory
				.getUpdatableInstance().createTemporaryDS(
						request.getDestinationDataSource(), true);

		// open destination model.
		Model destinationModel = null;
		Model sourceModel = null;
		Model trainedModel = null;
		try {
			destinationModel = JenaModelFactory.openModelByName(
					destinationDataSource, user);

			// add contents of each source model to destination model.
			String sourceDS = request.getSourceDataSource();
			sourceModel = JenaModelFactory.openModelByName(sourceDS, user);

			trainedModel = JenaModelFactory.openModelByName(request.getHelperDataSource(),user);
			
			//TODO get mallet instances from sourceModel
			
			//TODO get classifier model from trainedModel
			
			//TODO classify the sourceModel, writing it to the destinationModel
			
			sourceModel.close();
			trainedModel.close();
			

			// null out closed model so an additional close is not attempted
			// via finally.
			sourceModel = null;
			trainedModel = null;

		} catch (BlackbookSystemException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Unable to execute algorithm.", e);
			throw new BlackbookSystemException("Unable to execute algorithm.");
		} finally {
			if (destinationModel != null) {
				destinationModel.close();
			}

			if (sourceModel != null) {
				sourceModel.close();
			}
			
			if(trainedModel != null) {
				trainedModel.close();
			}
		}

		return new DataSourceResponse(destinationDataSource);
	}
}

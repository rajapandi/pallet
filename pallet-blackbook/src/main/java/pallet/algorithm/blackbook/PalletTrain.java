package pallet.algorithm.blackbook;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import security.ejb.client.User;
import workflow.ejb.client.annotations.Execute;
import blackbook.algorithm.api.Algorithm;
import blackbook.algorithm.api.DataSourceHelperSourceRequest;
import blackbook.algorithm.api.DataSourceRequest;
import blackbook.algorithm.api.DataSourceResponse;
import blackbook.algorithm.api.DataSourceSetRequest;
import blackbook.algorithm.api.VoidParameter;
import blackbook.exception.BlackbookSystemException;
import blackbook.jena.factory.JenaModelFactory;
import blackbook.metadata.manager.MetadataManagerFactory;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * This class trains a mallet model from rdf data.
 */
public class PalletTrain
		implements
		Algorithm<DataSourceRequest<VoidParameter>, DataSourceResponse> {
	/** logger */
	private static Log logger = LogFactory.getLog(PalletTrain.class);

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

		String destinationDataSource = MetadataManagerFactory
				.getUpdatableInstance().createTemporaryDS(
						request.getDestinationDataSource(), true);

		// open destination model.
		Model destinationModel = null;
		Model sourceModel = null;

		try {
			destinationModel = JenaModelFactory.openModelByName(
					destinationDataSource, user);

			//get data to train on.
			String sourceDS = request.getSourceDataSource();
			sourceModel = JenaModelFactory.openModelByName(sourceDS, user);

			
			//TODO get mallet instances from sourceModel
			
			//TODO create classifier model from mallet instances
			
			//TODO write classifier to the destinationModel
			
			sourceModel.close();
			

			// null out closed model so an additional close is not attempted
			// via finally.
			sourceModel = null;

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
			
		}

		return new DataSourceResponse(destinationDataSource);
	}
}

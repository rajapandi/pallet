package pallet.algorithm.blackbook;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import security.ejb.client.User;
import utd.pallet.classification.MalletTextDataTrainer;
import utd.pallet.classification.MalletUtils;
import utd.pallet.classification.MalletTextDataTrainer.TrainerObject;
import utd.pallet.data.RDFUtils;
import workflow.ejb.client.annotations.Execute;
import blackbook.algorithm.api.Algorithm;
import blackbook.algorithm.api.DataSourceRequest;
import blackbook.algorithm.api.DataSourceResponse;
import blackbook.algorithm.api.VoidParameter;
import blackbook.exception.BlackbookSystemException;
import blackbook.jena.factory.JenaModelFactory;
import blackbook.metadata.manager.MetadataManagerFactory;
import cc.mallet.types.InstanceList;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * This class trains a mallet model from rdf data.
 */
public class PalletTrain implements
        Algorithm<DataSourceRequest<VoidParameter>, DataSourceResponse> {

    // To be fetched from the request parameter
    private static String dummyURI = RDFUtils.dummyURI;
    private static Property dummyClassificationProperty = RDFUtils.CLASSIFICATION_PROPERTY;
    private static String dummyModelLang = "RDF/XML";
    private static int dummyAlgo = MalletTextDataTrainer.NAIVE_BAYES;

    /** logger */
    private static Log logger = LogFactory.getLog(PalletTrain.class);

    private static TrainerObject trainModel(Model sourceModel,
            Property classificationProperty, String modelLang,
            int trainingAlgorithm) throws BlackbookSystemException {

        TrainerObject trnObj = null;

        if (sourceModel == null)
            throw new BlackbookSystemException("Source Model cannot be null");

        if (classificationProperty == null)
            throw new BlackbookSystemException(
                    "Classification Property cannot be null");

        if (trainingAlgorithm == MalletTextDataTrainer.ALGO_UNASSIGNED)
            throw new BlackbookSystemException("Invaled Training Algorithm");

        // Convert data Model to string.
        String rdfDataAsString = null;
        try {
            rdfDataAsString = utd.pallet.data.JenaModelFactory.serializeModel(
                    sourceModel, modelLang);
        } catch (Exception e) {
            throw new BlackbookSystemException(e.getMessage());
        }

        // Convert the data in string format to instance list.
        InstanceList iList = null;
        try {
            iList = RDFUtils.convertRDFToInstanceList(rdfDataAsString, null,
                    classificationProperty.getURI());
        } catch (Exception e) {
            throw new BlackbookSystemException(e.getMessage());
        }

        MalletTextDataTrainer trainer = new MalletTextDataTrainer();
        // Algorithm to be fetched from Request
        try {
            trnObj = trainer.train(iList, trainingAlgorithm);
        } catch (NullPointerException e) {
            throw new BlackbookSystemException(e.getMessage());
        } catch (Exception e) {
            throw new BlackbookSystemException(e.getMessage());
        }

        return trnObj;
    }

    /*
     * @param user
     * 
     * @param request
     * 
     * @return DataSourceResponse
     * 
     * @throws BlackbookSystemException
     * 
     * @see blackbook.algorithm.api.Algorithm#execute(security.ejb. client.User,
     * blackbook.algorithm.api.AlgorithmRequest)
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

            // get data to train on.
            String sourceDS = request.getSourceDataSource();
            // request.
            sourceModel = JenaModelFactory.openModelByName(sourceDS, user);

            // TODO get mallet instances from sourceModel
            // dummy Property and dummy lang to be replaced by the one fetched
            // from request.
            TrainerObject trnObj = trainModel(sourceModel,
                    dummyClassificationProperty, dummyModelLang, dummyAlgo);

            // TODO write classifier to the destinationModel
            Statement stmt = MalletUtils.convertTrainertoRDFStatement(
                    destinationModel, trnObj, dummyClassificationProperty
                            .getURI());
            destinationModel.add(stmt);

            sourceModel.close();
            // null out closed model so an additional close is not attempted
            // via finally.
            sourceModel = null;

            destinationModel.close();
            destinationModel = null;

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

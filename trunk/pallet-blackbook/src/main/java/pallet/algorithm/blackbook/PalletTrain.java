package pallet.algorithm.blackbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import security.ejb.client.User;
import utd.pallet.classification.MalletTextDataTrainer;
import utd.pallet.classification.MalletTextDataTrainer.TrainerObject;
import utd.pallet.data.RDF2MalletInstances;

import workflow.ejb.client.annotations.Execute;
import blackbook.algorithm.api.Algorithm;

import blackbook.algorithm.api.DataSourceRequest;
import blackbook.algorithm.api.DataSourceResponse;

import blackbook.algorithm.api.VoidParameter;
import blackbook.exception.BlackbookSystemException;
import blackbook.jena.factory.JenaModelFactory;
import blackbook.metadata.manager.MetadataManagerFactory;

import cc.mallet.classify.Classifier;
import cc.mallet.types.InstanceList;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.OWL;


/**
 * This class trains a mallet model from rdf data.
 */
public class PalletTrain
		implements
		Algorithm<DataSourceRequest<VoidParameter>, DataSourceResponse> {
	/** logger */
	private static Log logger = LogFactory.getLog(PalletTrain.class);
	private static final Property CLASSIFICATION_PROPERTY = ModelFactory
    .createDefaultModel().createProperty(
            "http://blackbook.com/terms#STAT_EVENT");
	private static InstanceList getMalletInstances(Model sourceModel)throws Exception
	{   
		String stringSourceModel=utd.pallet.data.JenaModelFactory.serializeModel(sourceModel, "RDF/XML");
		ByteArrayOutputStream bos = RDF2MalletInstances.convertRDFWithLabels(
                stringSourceModel, CLASSIFICATION_PROPERTY.getURI(), null);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);

        InstanceList iList = (InstanceList) ois.readObject();
        logger.error("number of instances retrieved from RDF: " + iList.size());
        return iList;
	}
private static TrainerObject getClassifierModel(InstanceList iList)throws Exception
{
	 MalletTextDataTrainer bTrainer = new MalletTextDataTrainer();

     TrainerObject trnObj = null;
     
         trnObj = bTrainer.train(iList, MalletTextDataTrainer.NAIVE_BAYES);
    return trnObj;
}
private static String convertClassifierToRDF(Classifier classifier)throws Exception
{
	 Model model = ModelFactory.createDefaultModel();
     Resource res = model.createResource(new URL(
             "http://localhost:8443/blackbook/malletModel_"
                     + System.currentTimeMillis()).toString());
     Property prop = OWL.hasValue;

     ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
     ObjectOutputStream oos = new ObjectOutputStream(bos1);
     oos.writeObject(classifier);

     Literal obj = model.createTypedLiteral(bos1.toByteArray());

     Statement stmt = model.createLiteralStatement(res, prop, obj);
     model.add(stmt);

     String ret = utd.pallet.data.JenaModelFactory.serializeModel(model, "RDF/XML");
     return ret;
}


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

			/*
			 * get mallet instances from sourceModel
			 */
			
			String sourceDS = request.getSourceDataSource();
			sourceModel = JenaModelFactory.openModelByName(sourceDS, user);
            InstanceList iList=getMalletInstances(sourceModel);
			
	        /*
	         * create classifier model from mallet instances
	         */
            TrainerObject trnObj=getClassifierModel(iList);
	       
           /*
            * write classifier to the destinationModel. 
            */
	       String classifierToPersistAsRDF=convertClassifierToRDF(trnObj.getClassifier());
	       InputStream is = new ByteArrayInputStream(classifierToPersistAsRDF
                   .getBytes("UTF-8"));
	       destinationModel.read(is, "");
	        logger.error("converted classifier to rdf:");
			
			
			
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

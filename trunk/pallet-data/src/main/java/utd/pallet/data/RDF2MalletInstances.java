/**
 * The following program is used to convert the data in the RDF format to Mallet Instances.
 */
package utd.pallet.data;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;

import cc.mallet.classify.Classifier;
import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.FeatureSequence2FeatureVector;
import cc.mallet.pipe.Input2CharSequence;
import cc.mallet.pipe.Noop;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.Target2Label;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.BadURIException;

/**
 * RDF2MalletInstances class is used to convert the RDF data into Mallet
 * Instances.The Algorithm which is followed here is 1) Iterate through each
 * resource of the RDF 2) If Resource is not uri resource or Blank node then
 * find the Original Resource of the Blank node. 3) list all the properties of
 * the Resource . 4)If the object value of the property is literal then add it
 * to the String .5)Store the data into HashMap having the key as the Resource
 * 6) If while iterating through the resources we get the same resource (because
 * resource of blank node ) which is already in the HashMap then append the new
 * data. 7) Finally,we add each instance to the List , process the instance
 * through the Pipe and add all the instances to the instanceList.
 * 
 * 
 * 
 */
public class RDF2MalletInstances {

	/**
	 * log is created for the purpose of logging
	 */

	private static org.apache.log4j.Logger log = Logger
			.getLogger(RDF2MalletInstances.class);

	/**
	 * Appender is created to append the log in the file.
	 */
	private static WriterAppender appender = null;
	/**
	 * BufferedWriter is created for the log file.
	 */
	private static BufferedWriter bufferWriter = null;
	/**
	 * Pipe is intialized to get all the pipes of Mallet
	 */
	Pipe pipe;
	static {
		try {
			File logFile = new File("RDFMalletInstances.log");
			FileWriter logFileWriter = new FileWriter(logFile);
			bufferWriter = new BufferedWriter(logFileWriter);
			appender = new WriterAppender(new PatternLayout(), bufferWriter);
			log.addAppender(appender);
			log.setLevel((Level) Level.INFO);
		} catch (Exception e) {

		}
	}

	/**
	 * RDF2MalletInstances constructor is called which calls buildpipe method.
	 */

	public RDF2MalletInstances() {

		pipe = buildPipe();
	}

	/**
	 * @return Instance of SerialPipes is returned. BuildPipe method is used to
	 *         add all the Mallet's pipes into one ArrayList.
	 */
	public Pipe buildPipe() {
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
		pipeList.add(new Input2CharSequence("UTF-8"));
		Pattern tokenPattern = Pattern.compile("[//\\./\\:\\p{L}\\p{N}_-]+");
		pipeList.add(new CharSequence2TokenSequence(tokenPattern));
		pipeList.add(new TokenSequenceLowercase());
		pipeList.add(new TokenSequenceRemoveStopwords(false, false));
		pipeList.add(new TokenSequence2FeatureSequence());
		pipeList.add(new Target2Label());
		pipeList.add(new FeatureSequence2FeatureVector());
		return new SerialPipes(pipeList);
	}

	/**
	 * @param modelAsString
	 *            : The classifying data as model
	 * @param classifier
	 *            : The trained classifier
	 * @return : ByteArrayOutputStream Serialized InstanceList containing
	 *         classifying data
	 * @throws Exception
	 */
	public final static ByteArrayOutputStream convertRDFWithoutLabels(
			String modelAsString, Classifier classifier) throws Exception {
		Model incomingModel = null;
		ByteArrayOutputStream bos = null;
		InstanceList classifyingDataIntoMalletInstances = new InstanceList(
				new Noop());
		Pipe newPipe = null;
		try {
			if (StringUtils.isBlank(modelAsString))
				throw new IllegalArgumentException(
						"model cannot be blank or null");
			InputStream is = new ByteArrayInputStream(modelAsString
					.getBytes("UTF-8"));
			incomingModel = ModelFactory.createDefaultModel();
			incomingModel.read(is, "");
		} catch (Exception e) {
			log.error(e.toString());
			throw e;
		}

		try {
			if (classifier == null)
				throw new IllegalArgumentException(
						"classifier cannot be blank or null");
		} catch (Exception e) {
			log.error(e.toString());
			throw e;
		}
		HashMap<String, String> resData = new HashMap<String, String>();
		String propertyData = " ";
		ResIterator res = incomingModel.listSubjects();
		while (res.hasNext()) {
			Resource resourceOriginal = res.next();
			Resource newResource = null;
			try {
				newResource = RDFUtils.findSuperNode(resourceOriginal, incomingModel);
			} catch (IllegalStateException e) {
				continue;
			}
			log.debug("DATA OF LOCAL RESOURCE " + resourceOriginal.toString()
					+ "IS ATTACHED TO THE DATA OF ORIGINAL RESOURCE "
					+ newResource.toString());
			StmtIterator stmt = resourceOriginal.listProperties();
			propertyData = " ";
			while (stmt.hasNext()) {
				Statement s = stmt.next();
				if (s.getObject().isLiteral()) {
					propertyData = propertyData + " "
							+ s.getObject().toString() + " ";
				}
			}
			if (resData.containsKey(newResource.toString())) {
				String slrt = resData.get(newResource.toString());
				slrt = slrt + " " + propertyData;
				resData.put(newResource.toString(), slrt);
			} else {
				resData.put(newResource.toString(), propertyData);
			}
		}
		Instance i = null;
		res = incomingModel.listSubjects();
		newPipe = classifier.getInstancePipe();
		while (res.hasNext()) {
			Resource re = res.next();
			String str = resData.get(re.toString());
			if (str == null)
				continue;
			i = newPipe.instanceFrom(new Instance(str, newPipe
					.getTargetAlphabet().lookupObject(0).toString(), re
					.toString(), "RDF/XML"));
			log.debug("NAME:: " + i.getName().toString());
			log.debug("DATA::\n" + i.getData().toString());
			log.debug("TARGET:: " + i.getTarget().toString());
			log.debug("SOURCE:: " + i.getSource().toString());

			classifyingDataIntoMalletInstances.add(i);
		}
		try {
			bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(classifyingDataIntoMalletInstances);
		} catch (Exception e) {
			log.error(e.toString());
			throw e;
		}
		return bos;
	}
	
	
	/**
	 * @param modelAsString
	 *            : The classifying data as model
	 * @param classifier
	 *            : The trained classifier
	 * @return : ByteArrayOutputStream Serialized InstanceList containing
	 *         classifying data
	 * @throws Exception
	 */
	public final static InstanceList convertModel2InstanceList(
			Model incomingModel, Classifier classifier) throws Exception {

		InstanceList classifyingDataIntoMalletInstances = new InstanceList(
				new Noop());
		Pipe newPipe = null;

		try {
			if (classifier == null)
				throw new IllegalArgumentException(
						"classifier cannot be blank or null");
		} catch (Exception e) {
			log.error(e.toString());
			throw e;
		}
		
		HashMap<String, String> resData = RDFUtils.getResource2ObjectsMap(incomingModel);
			
		Instance i = null;
		Iterator<String> resItr = resData.keySet().iterator();
		newPipe = classifier.getInstancePipe();
		while (resItr.hasNext()) {
			Resource re = incomingModel.createResource(resItr.next());
			String str = resData.get(re.toString());
			if (str == null)
				continue;
			i = newPipe.instanceFrom(new Instance(str, newPipe
					.getTargetAlphabet().lookupObject(0).toString(), re
					.toString(), "RDF/XML"));
			log.debug("NAME:: " + i.getName().toString());
			log.debug("DATA::\n" + i.getData().toString());
			log.debug("TARGET:: " + i.getTarget().toString());
			log.debug("SOURCE:: " + i.getSource().toString());

			classifyingDataIntoMalletInstances.add(i);
		}

		return classifyingDataIntoMalletInstances;
		
	}
	

	/**
	 * @param model
	 *            Jena Model
	 * @param classificationPredicate
	 *            : The Predicate on the basis of which classification done.
	 * @param prevClassifier
	 *            : null for general training, previously trained instance of
	 *            ClassifierTrainer on incremental training.
	 * @return This method returns InstanceList
	 * @throws Exception
	 */

	// Classifier trainer as an argument - Reqd for incremental training of data
	// - 09-21-2009, SJ
	public final static InstanceList trainingDataIntoMalletInstanceList(
			Model model, Property classificationPredicate,
			Classifier prevClassifier) throws Exception {

		try {

			if (model == null || model.isEmpty()) {
				throw new IllegalArgumentException(
						"model cannot be blank or null");
			}
		} catch (Exception e) {

			log.error(e.toString());

			throw e;
		}

		if (classificationPredicate != null) {
			try {
				new URL(classificationPredicate.toString());
			} catch (Exception e) {

				log.error(e.toString() + "   " + classificationPredicate
						+ " is not in the proper format of the RDF predicate");

				throw new BadURIException(classificationPredicate
						+ " is not in the proper format of the RDF predicate");

			}
		}

		log.info("Getting resource 2 object map");
		HashMap<String, String> resData = RDFUtils.getResource2ObjectsMap(model);
		
		log.info("getting instances from resource object map");
		List<Instance> instBeforeProcessing = getInstancesFromResourceObjectMap(resData,model,classificationPredicate);

		log.info("processing all instances");
		InstanceList instances = getProcessedInstances(instBeforeProcessing,prevClassifier);


		return instances;
	}

	/**
	 * 
	 * @param modelAsString
	 *            :It takes model as String
	 * @param classificationPredicate
	 *            : It takes classification predicate as String
	 * @param prevTrainer
	 *            : Null for first time training, instance of classifierTrainer
	 *            for subsequent incremental training.
	 * @return ByteArrayOutputStream :It return the serialized form of the
	 *         InstnaceList.
	 * 
	 * @throws Exception
	 */
	public final static ByteArrayOutputStream convertRDFWithLabelsSerializable(
			String modelAsString, String classificationPredicate,
			Classifier prevTrainer) throws Exception {

		InstanceList il = null;
		Model incomingModel = null;

		try {

			if (StringUtils.isBlank(modelAsString))
				throw new IllegalArgumentException(
						"model cannot be blank or null");
			InputStream is = new ByteArrayInputStream(modelAsString
					.getBytes("UTF-8"));
			incomingModel = ModelFactory.createDefaultModel();
			incomingModel.read(is, "");
		} catch (Exception e) {

			log.error(e.toString());

			throw e;
		}

		try {
			if (StringUtils.isBlank(classificationPredicate)) {
				throw new IllegalArgumentException(
						"Classification Predicate cannot be  blank or null");
			}
		} catch (IllegalArgumentException e) {

			log.error(e.toString());

			throw e;
		}

		try {

			@SuppressWarnings("unused")
			URL checkProperty = new URL(classificationPredicate);

		} catch (Exception e) {

			log.error(e.toString() + "   " + classificationPredicate
					+ " is not in the proper format of the RDF predicate");

			throw new BadURIException(classificationPredicate
					+ " is not in the proper format of the RDF predicate");
		}

		ByteArrayOutputStream bos = null;
		try {

			Property property = incomingModel
					.getProperty(classificationPredicate);

			il = trainingDataIntoMalletInstanceList(incomingModel, property,
					prevTrainer);
			bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(il);

			log
					.debug("The RDF data is converted into Mallet Instances and the  instance list is serialized as a String and return");

		} catch (Exception e) {

			log.error(e.toString());

			throw e;
		}

		return bos;
	}
	
	
	/**
	 * Get mallet instances from resource/object map, applying the predicate value from the model.
	 * @param resData
	 * @param model
	 * @param classificationPredicate
	 * @return
	 */
	private static List<Instance> getInstancesFromResourceObjectMap(HashMap<String,String> resData, Model model, Property classificationPredicate){
		List<Instance> instBeforeProcessing = new ArrayList<Instance>();
		Instance i = null;
		
		/**
		 * Again Start iterating through each resource
		 */
		Iterator<String> resItr = resData.keySet().iterator();
		while (resItr.hasNext()) {
			Resource re = model.createResource(resItr.next());
			String str = resData.get(re.toString());
			/**
			 * Get the data from the Map associated with each Resource.If data
			 * has the specified property then only we need to create instance.
			 */
			if (str == null)
				continue;
			if (re.getProperty(classificationPredicate) != null) {

				/**
				 * An Mallet Instance is created with data as the data
				 * associated with Resource.Target will be the object value of
				 * the specified property .Name will be the name of the
				 * resource. Source is the through which we get the data.
				 */

				i = new Instance(str, re.getProperty(classificationPredicate)
						.getObject().toString(), re.toString(),
						"Sample RDF/XML");

				instBeforeProcessing.add(i);
			}
		}
		
		if (instBeforeProcessing.size() == 0) {
			log.error("No resource in the given RDF contains the predicate ");
		}
		
		return instBeforeProcessing;
	}
	
	
	private static InstanceList getProcessedInstances(List<Instance> instBeforeProcessing, Classifier prevClassifier) {
		Iterator<Instance> ip = instBeforeProcessing.iterator();
		/**
		 * A BuildPipe method is called ,which creates the pipelist.
		 */

		InstanceList instances = null;
		if (prevClassifier == null) {
			RDF2MalletInstances rdf2MalletInstances = new RDF2MalletInstances();
			instances = new InstanceList(rdf2MalletInstances.pipe);
		} else {
			Pipe pipe = prevClassifier.getInstancePipe();
			instances = new InstanceList(pipe);
		}
		/**
		 * The data is processed through the Pipe which is associated with
		 * Mallet InstanceList.
		 */
		instances.addThruPipe(ip);
		Iterator<Instance> ipc = instances.iterator();
		try {
			while (ipc.hasNext()) {
				Instance io = ipc.next();
				log.debug("NAME:: " + io.getName().toString());
				log.debug("DATA::\n" + io.getData().toString());
				log.debug("TARGET:: " + io.getTarget().toString());
				log.debug("SOURCE:: " + io.getSource().toString());

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return instances;
	}
	
}

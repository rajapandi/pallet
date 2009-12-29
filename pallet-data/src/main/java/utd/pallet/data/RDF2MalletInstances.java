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
	 * Get mallet instances from resource/object map, applying the predicate value from the model.
	 * @param resData
	 * @param model
	 * @param classificationPredicate
	 * @return
	 */
	public static List<Instance> getInstancesFromResourceObjectMap(HashMap<String,String> resData, Model model, Property classificationPredicate){
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
	
	
	public static InstanceList getProcessedInstances(List<Instance> instBeforeProcessing, Classifier prevClassifier) {
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

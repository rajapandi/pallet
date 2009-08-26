/**
 * The following program is used to convert the data in the RDF format to Mallet Instances.
 */
package utd.pallet.data;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.StringWriter;
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

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.FeatureSequence2FeatureVector;
import cc.mallet.pipe.Input2CharSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.PrintInputAndTarget;
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
 * RDF_2_Mallet_Instances class is used to convert the RDF data into Mallet
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
     * Pipe instance is created which is used to process the data.
     * 
     */

    private static org.apache.log4j.Logger log = Logger
            .getLogger(RDF2MalletInstances.class);
    /**
     * Appender for logger is created.
     */
    private static WriterAppender appender = null;
    /**
     * BufferedWriter is created for the log file.
     */
    private static BufferedWriter bw = null;
    /**
     * 
     */
    Pipe pipe;
    static {
        try {
            File logFile = new File("RDFMalletInstances.log");
            FileWriter logfileWriter = new FileWriter(logFile);
            bw = new BufferedWriter(logfileWriter);
            appender = new WriterAppender(new PatternLayout(), bw);
            log.addAppender(appender);
            log.setLevel((Level) Level.DEBUG);
        } catch (Exception e) {

        }
    }
    /**
     * Jena Model is intianlized
     */

    /**
     * An List is created(Instances_before_processing), to add all the instances
     * before processing through them pipe.
     */
    public static List<Instance> Inst_bef_proce = new ArrayList<Instance>();

    /**
     * RDF_2_Mallet_Instances constructor is called which calls buildpipe
     * method.
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
        pipeList.add(new PrintInputAndTarget());
        return new SerialPipes(pipeList);
    }

    /**
     * @param r
     *            It takes the Resource which is not URI(or Blank node)
     * @param model
     *            Jena Model
     * @return It return the original Resource of the passed Blank node.
     */
    private final Resource findSuperNode(Resource r, Model model) {

        StmtIterator stmt1 = model.listStatements();
        while (stmt1.hasNext()) {
            Statement s1 = stmt1.next();
            RDFNode rdf1 = s1.getObject();
            if (r.isURIResource()) {
                break;
            }
            if (rdf1.toString().equals(r.toString())) {
                r = s1.getSubject();
                stmt1 = model.listStatements();
                continue;
            }
        }
        return r;

    }

    /**
     * @param model
     *            Jena Model
     * @param classificationPredicate
     *            : The Predicate on the basis of which classification done
     * @return This method returns InstanceList
     * @throws Exception
     */
    public final InstanceList executeAlgorithm(Model model,
            Property classificationPredicate) throws Exception {

        try {

            if (model == null) {
                throw new IllegalArgumentException("model cannot be null");
            }
        } catch (Exception e) {

            log.debug(e.toString());

            throw e;
        }

        try {
            if (classificationPredicate == null) {
                throw new IllegalArgumentException(
                        "Classification Predicate cannot be  blank");
            }
        } catch (IllegalArgumentException e) {

            log.debug(e.toString());

            throw e;
        }

        try {
            Model m1 = ModelFactory.createDefaultModel();
            Property p = classificationPredicate;
            m1.createResource().addProperty(p, "test");
            m1.write(System.out);

        } catch (Exception e) {

            log.debug(e.toString() + classificationPredicate
                    + " is not in the proper format of the RDF predicate");

            throw new BadURIException(classificationPredicate
                    + " is not in the proper format of the RDF predicate");

        }

        HashMap<String, String> Res_data = new HashMap<String, String>();
        String s_data = " ";
        ResIterator res = model.listSubjects();

        while (res.hasNext()) {
            Resource r_original = res.next();
            Resource r1 = findSuperNode(r_original, model);

            log.debug("DATA OF LOCAL RESOURCE " + r_original.toString()
                    + "IS ATTACHED TO THE DATA OF ORIGINAL RESOURCE "
                    + r1.toString());

            StmtIterator stmt = r_original.listProperties();
            s_data = " ";
            while (stmt.hasNext()) {
                Statement s1 = stmt.next();
                if (s1.getObject().isLiteral()) {

                    s_data = s_data + " " + s1.getObject().toString() + " ";

                }
            }
            /**
             * If Resource already there in the Map ,then get the previous data.
             * and add the new data.
             */
            if (Res_data.containsKey(r1.toString())) {
                String slrt = Res_data.get(r1.toString());
                slrt = slrt + " " + s_data;
                Res_data.put(r1.toString(), slrt);
            }
            /**
             * If Resource is not already in the Map then add in the Map with
             * current data.
             */
            else {
                Res_data.put(r1.toString(), s_data);
            }
        }
        Instance i = null;
        /**
         * Again Start iterating through each resource
         */
        res = model.listSubjects();
        while (res.hasNext()) {
            Resource re = res.next();
            String str = Res_data.get(re.toString());
            /**
             * Get the data from the Map associated with each Resource.If data
             * has the property http://blackbook.com/terms#STAT_EVENT then only
             * we need to create instance.
             */
            if (re.getProperty(classificationPredicate) != null) {

                /**
                 * An Mallet Instance is created with data as the data
                 * associated with Resource.Target will be the object value of
                 * the property http://blackbook.com/terms#STAT_EVENT.Name will
                 * be the name of the resource. Source is the file through which
                 * we get the data.
                 */

                i = new Instance(str, re.getProperty(classificationPredicate)
                        .getObject().toString(), re.toString(), "File");

                Inst_bef_proce.add(i);
            }
        }
        try {
            if (Inst_bef_proce.size() == 0) {
                throw new IllegalArgumentException(
                        "No resource in the given RDF contains the predicate "
                                + classificationPredicate);
            }
        } catch (IllegalArgumentException e) {

            log.debug("No resource in the given RDF contains the predicate ");

            throw e;
        }
        Iterator<Instance> ip = Inst_bef_proce.iterator();
        /**
         * A BuildPipe method is called ,which creates the pipelist.
         */
        RDF2MalletInstances ob1 = new RDF2MalletInstances();
        InstanceList instances = new InstanceList(ob1.pipe);
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

            log.debug(e.toString());

            throw e;

        }

        return instances;
    }

    /**
     * 
     * @param modelAsString
     * @param classificationPredicate
     * @return InstanceList
     * @throws Exception
     */
    public final String executeAlgorithmSerializable(String modelAsString,
            String classificationPredicate) throws Exception {

        InstanceList il = null;
        Model incomingModel = null;

        try {

            if (StringUtils.isBlank(modelAsString))
                throw new IllegalArgumentException("model cannot be blank");
            InputStream is = new ByteArrayInputStream(modelAsString
                    .getBytes("UTF-8"));
            incomingModel = ModelFactory.createDefaultModel();
            incomingModel.read(is, "");
        } catch (Exception e) {

            log.debug(e.toString());

            throw e;
        }

        try {
            if (StringUtils.isBlank(classificationPredicate)) {
                throw new IllegalArgumentException(
                        "Classification Predicate cannot be  blank");
            }
        } catch (IllegalArgumentException e) {

            log.debug(e.toString());

            throw e;
        }

        try {
            Model m1 = ModelFactory.createDefaultModel();
            Property p = m1.createProperty(classificationPredicate);
            m1.createResource().addProperty(p, "test");
            m1.write(System.out);

        } catch (BadURIException e) {

            log.debug(e.toString() + classificationPredicate
                    + " is not in the proper format of the RDF predicate");

            throw new BadURIException(classificationPredicate
                    + " is not in the proper format of the RDF predicate");
        }
        StringWriter sw = null;
        try {

            Property p1 = incomingModel.getProperty(classificationPredicate);
            il = executeAlgorithm(incomingModel, p1);
            String stringInstanceList = il.toString();
            sw = new StringWriter();
            sw.write(stringInstanceList);

            log
                    .debug("The RDF data is converted into Mallet Instances and the  instance list is serialized as a String and return");

        } catch (Exception e) {

            log.debug(e.toString());

            throw e;
        }

        return sw.toString();
    }

    /**
     * @param args
     *            CommandLine Arguments
     * */

}

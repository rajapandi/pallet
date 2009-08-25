/**
 * The following program is used to convert the data in the RDF format to Mallet Instances.The first 
 * command line argument(args[0]) will be the path of the Input RDF file. The Second Command Line 
 * argument will be the Output .txt(args[1]) file.
 */
package utd.pallet.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

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
 * @author pralabh
 * 
 */
public class RDF_2_Mallet_Instances {
    /**
     * Pipe instance is created which is used to process the data.
     */
    Pipe pipe;

    /**
     * Jena Model is intianlized
     */
    static Model model = null;
    /**
     * An List is created(Instances_before_processing), to add all the instances
     * before processing through them pipe.
     */
    public static List<Instance> Inst_bef_proce = new ArrayList<Instance>();

    /**
     * RDF_2_Mallet_Instances constructor is called which calls buildpipe
     * method.
     */
    public RDF_2_Mallet_Instances() {
        pipe = buildpipe();
    }

    /**
     * @return Instance of SerialPipes is returned. BuildPipe method is used to
     *         add all the Mallet's pipes into one ArrayList.
     */
    public Pipe buildpipe() {
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
     * @return It return the original Resource of the passed Blank node.
     */
    public static Resource findsupernode(Resource r) {
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
     * @param args
     *            CommandLine Arguments
     */
    public static void main(String args[]) {
        File fread = null;
        File fwrite = null;
        String s_data = " ";
        /**
         * Map which store Resource as a Key and Resource's data as a Value
         * 
         */
        HashMap<String, String> Res_data = new HashMap<String, String>();
        FileReader fr = null;
        BufferedReader br = null;
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            model = ModelFactory.createDefaultModel();
            fread = new File(args[0]);
            fwrite = new File(args[1]);
            fr = new FileReader(fread);
            br = new BufferedReader(fr);
            fw = new FileWriter(fwrite);
            bw = new BufferedWriter(fw);

            model.read(br, null, "RDF/XML");
            /**
             * The object of Property STAT_EVENT is used as the Target in the
             * Mallet Instances.
             * 
             */
            Property p1 = model
                    .getProperty("http://blackbook.com/terms#STAT_EVENT");
            ResIterator res = model.listSubjects();

            while (res.hasNext()) {
                Resource r_original = res.next();
                Resource r1 = findsupernode(r_original);

                System.out.println("DATA OF LOCAL RESOURCE "
                        + r_original.toString()
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
                 * If Resource already there in the Map ,then get the previous
                 * data. and add the new data.
                 */
                if (Res_data.containsKey(r1.toString())) {
                    String slrt = Res_data.get(r1.toString());
                    slrt = slrt + " " + s_data;
                    Res_data.put(r1.toString(), slrt);
                }
                /**
                 * If Resource is not already in the Map then add in the Map
                 * with current data.
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
                 * Get the data from the Map associated with each Resource.If
                 * data has the property http://blackbook.com/terms#STAT_EVENT
                 * then only we need to create instance.
                 */
                if (re.getProperty(p1) != null) {

                    /**
                     * An Mallet Instance is created with data as the data
                     * associated with Resource.Target will be the object value
                     * of the property
                     * http://blackbook.com/terms#STAT_EVENT.Name will be the
                     * name of the resource. Source is the file through which we
                     * get the data.
                     */

                    i = new Instance(str, re.getProperty(p1).getObject()
                            .toString(), re.toString(), args[0]);

                    Inst_bef_proce.add(i);

                }
            }

        } catch (Exception e) {
            System.out.println(e);
        }
        Iterator<Instance> ip = Inst_bef_proce.iterator();
        /**
         * A BuildPipe method is called ,which creates the pipelist.
         */
        RDF_2_Mallet_Instances ob1 = new RDF_2_Mallet_Instances();
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
                System.out.println("NAME:: " + io.getName().toString());
                System.out.println("DATA::\n" + io.getData().toString());
                System.out.println("TARGET:: " + io.getTarget().toString());
                System.out.println("SOURCE:: " + io.getSource().toString());

                bw.newLine();
                bw.write("NAME:: " + io.getName().toString());
                bw.newLine();
                bw.write("DATA::\n" + io.getData().toString());
                bw.newLine();
                bw.write("TARGET:: " + io.getTarget().toString());
                bw.newLine();
                bw.write("SOURCE:: " + io.getSource().toString());
                bw.newLine();

            }
            bw.close();

        } catch (Exception e) {
            System.out.println(e);
        }

    }

}

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
        File f = null;
        File f1 = null;
        String s12 = " ";
        HashMap<String, String> m1 = new HashMap<String, String>();
        FileReader fr = null;
        BufferedReader br = null;
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            model = ModelFactory.createDefaultModel();
            f = new File("Montery2RDF.rdf");
            f1 = new File("Mallet_Instances.txt");

            fr = new FileReader(f);
            br = new BufferedReader(fr);
            fw = new FileWriter(f1);
            bw = new BufferedWriter(fw);
            model.read(br, null, "RDF/XML");

            Property p1 = model
                    .getProperty("http://blackbook.com/terms#STAT_EVENT");
            ResIterator res = model.listSubjects();

            while (res.hasNext()) {
                Resource r = res.next();

                Resource r1 = findsupernode(r);

                System.out.println("DATA OF LOCAL RESOURCE " + r.toString()
                        + "IS ATTACHED TO THE DATA OF ORIGINAL RESOURCE "
                        + r1.toString());

                StmtIterator stmt = r.listProperties();
                s12 = " ";
                while (stmt.hasNext()) {
                    Statement s1 = stmt.next();
                    if (s1.getObject().isLiteral()) {

                        s12 = s12 + " " + s1.getObject().toString() + " ";

                    }
                }
                if (m1.containsKey(r1.toString())) {
                    String slrt = m1.get(r1.toString());
                    slrt = slrt + " " + s12;
                    m1.put(r1.toString(), slrt);
                } else {
                    m1.put(r1.toString(), s12);
                }
            }
            Instance i = null;

            res = model.listSubjects();
            while (res.hasNext()) {
                Resource re = res.next();
                String str = m1.get(re.toString());

                if (re.getProperty(p1) != null) {

                    i = new Instance(str, re.getProperty(p1).getObject()
                            .toString(), re.toString(), "Montery2RDF.rdf");

                    Inst_bef_proce.add(i);

                }
            }

        } catch (Exception e) {
            System.out.println(e);
        }
        Iterator<Instance> ip = Inst_bef_proce.iterator();
        RDF_2_Mallet_Instances ob1 = new RDF_2_Mallet_Instances();
        InstanceList instances = new InstanceList(ob1.pipe);
        instances.addThruPipe(ip);
        Iterator<Instance> ipc = instances.iterator();
        try {
            while (ipc.hasNext()) {
                Instance io = ipc.next();
                System.out.println(io.getName());
                System.out.println(io.getTarget());
                System.out.println(io.getData());
                System.out.println(io.getSource());

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

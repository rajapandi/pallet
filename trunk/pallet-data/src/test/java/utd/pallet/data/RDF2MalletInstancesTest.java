package utd.pallet.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.VCARD;

/**
 * testRDF2MalletInstances is a class which perform five junit test cases on
 * RDF2MalletInstances.
 * 
 */
public class RDF2MalletInstancesTest extends TestCase {

    /**
     * rdf2MalletInstances1 is an Instance of RDF2MalletInstances
     */
    private RDF2MalletInstances rdf2MalletInstances1;
    /**
     * rdf2MalletInstances2 is an Instance of RDF2MalletInstances
     */
    private RDF2MalletInstances rdf2MalletInstances2;
    /**
     * * rdf2MalletInstances3 is an Instance of RDF2MalletInstances
     */
    private RDF2MalletInstances rdf2MalletInstances3;
    /**
     * ob4 is an Instance of RDF2MalletInstances
     */
    private RDF2MalletInstances rdf2MalletInstances4;
    /**
     * rdf2MalletInstances5 is an Instance of RDF2MalletInstances
     */
    private RDF2MalletInstances rdf2MalletInstances5;

    /**
     * rdf2MalletInstances6 is an Instance of RDF2MalletInstances
     */
    private RDF2MalletInstances rdf2MalletInstances6;

    /**
     * ob7 is an Instance of RDF2MalletInstances
     */
    private RDF2MalletInstances rdf2MalletInstances7;

    /**
     * rdf2MalletInstances8 is an Instance of RDF2Mallet Instances
     */
    private RDF2MalletInstances rdf2MalletInstances8;

    /**
     * @return Instance of TestSuite
     */
    static Test suite() {
        return new TestSuite(RDF2MalletInstancesTest.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */

    protected void setUp() {

        try {
            super.setUp();
            rdf2MalletInstances1 = new RDF2MalletInstances();
            rdf2MalletInstances2 = new RDF2MalletInstances();
            rdf2MalletInstances3 = new RDF2MalletInstances();
            rdf2MalletInstances4 = new RDF2MalletInstances();
            rdf2MalletInstances5 = new RDF2MalletInstances();
            rdf2MalletInstances6 = new RDF2MalletInstances();
            rdf2MalletInstances7 = new RDF2MalletInstances();
            rdf2MalletInstances8 = new RDF2MalletInstances();
        } catch (Exception e) {

        }

    }

    /*
     * (non-Javadoc) * @see junit.framework.TestCase#tearDown()
     */

    protected void tearDown() throws Exception {

        super.tearDown();
        rdf2MalletInstances1 = null;
        rdf2MalletInstances2 = null;
        rdf2MalletInstances3 = null;
        rdf2MalletInstances4 = null;
        rdf2MalletInstances5 = null;
        rdf2MalletInstances6 = null;
        rdf2MalletInstances7 = null;
        rdf2MalletInstances8 = null;

    }

    /**
     * This checks for the case when model is null
     */

    public void testExecuteAlgorithmNullModel() {

        try {
            rdf2MalletInstances1.executeAlgorithm(null, null);
        } catch (Exception e) {
            assertNotNull(e.getMessage());

        }
    }

    /**
     * This checks for the blank model.
     */
    public void testExecuteAlgorithmBlankModel() {

        try {

            Model model = ModelFactory.createDefaultModel();
            Property sampleProperty = model
                    .createProperty("http://www.w3.org/2001/vcard-rdf/3.0#CATEGORIES");
            rdf2MalletInstances2.executeAlgorithm(model, sampleProperty);
        } catch (Exception e) {
            assertNotNull(e.getMessage());

        }
    }

    /**
     * This checks for the case when the classification predicate is null.
     */

    public void testExecuteAlgorithmNullProperty() {
        try {
            Model model = ModelFactory.createDefaultModel();
            Resource resource = model
                    .createResource("http://somewhere/JohnSmith");
            resource.addProperty(VCARD.FN, "John Smith");

            rdf2MalletInstances3.executeAlgorithm(model, null);

        } catch (Exception e) {

            assertNotNull(e.getMessage());
        }

    }

    /**
     * This checks for the case when the classificationpredicate is in proper
     * URI format correct but it does not exist in the model
     */

    public void testExecuteAlgorithmModelMissingClassification() {
        try {
            Model model = ModelFactory.createDefaultModel();

            Resource johnSmith = model
                    .createResource("http://somewhere/JohnSmith");
            johnSmith.addProperty(VCARD.FN, "John Smith");
            johnSmith.addProperty(VCARD.N, model.createResource().addProperty(
                    VCARD.Given, "John").addProperty(VCARD.Family, "Smith"));

            Property p = model
                    .createProperty("http://www.w3.org/2001/vcard-rdf/3.0#CATEGORIES");
            rdf2MalletInstances4.executeAlgorithm(model, p);
        } catch (Exception e) {
            assertNotNull(e.getMessage());

        }
    }

    /**
     * This function checks executeAlgorithmSerializable when both model(in
     * String) form and classification predicate in String form are null.
     */

    public void testExecuteAlgorithmSerializableNullModelString() {

        try {

            rdf2MalletInstances5.executeAlgorithmSerializable(null, null);
        } catch (Exception e) {
            assertNotNull(e);
        }

    }

    /**
     * The function checks executeAlgorithmSerializable when the model is Empty
     */

    public void testExecuteAlgorithmSerializableEmptyModelString() {
        try {

            rdf2MalletInstances6.executeAlgorithmSerializable("",
                    "http://www.w3.org/2001/vcard-rdf/3.0#CATEGORIES");
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    /**
     * The function checks executeAlgorithmSerializable when the predicate is
     * not in correct URI form.
     */
    public void testExecuteAlgorithmSerializableMalformedString() {
        try {
            Model model = ModelFactory.createDefaultModel();
            Resource resource = model
                    .createResource("http://somewhere/JohnSmith");
            resource.addProperty(VCARD.FN, "John Smith");
            String modelSerialized = JenaModelFactory.serializeModel(model,
                    "RDF/XML");
            rdf2MalletInstances7.executeAlgorithmSerializable(modelSerialized,
                    "HIJK");
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    /**
     * This function checks for the correct functionality of RDF2MalletInstances
     * class. executeAlgorithmSerializableis checked when all the input is
     * correct.
     */
    public void testExecuteAlgorithmSerializableCorrectModel() {
        ByteArrayOutputStream bout = null;

        try {
            Model model = ModelFactory.createDefaultModel();
            Resource johnSmith = model
                    .createResource("http://somewhere/JohnSmith");
            johnSmith.addProperty(VCARD.FN, "John Smith");
            johnSmith.addProperty(VCARD.CATEGORIES, "General");
            johnSmith.addProperty(VCARD.N, model.createResource().addProperty(
                    VCARD.Given, "John").addProperty(VCARD.Family, "Smith"));

            Resource paulKit = model.createResource("http://somewhere/PaulKit");
            paulKit.addProperty(VCARD.FN, "Paul Kit");
            paulKit.addProperty(VCARD.CATEGORIES, "Special");
            paulKit.addProperty(VCARD.N, model.createResource().addProperty(
                    VCARD.Given, "Paul").addProperty(VCARD.Family, "Kit"));

            String modelSerialized = JenaModelFactory.serializeModel(model,
                    "RDF/XML");
            bout = rdf2MalletInstances8.executeAlgorithmSerializable(
                    modelSerialized,
                    "http://www.w3.org/2001/vcard-rdf/3.0#CATEGORIES");
        } catch (Exception e) {
            fail(e.toString());
        }
        try {
            ByteArrayInputStream bin = new ByteArrayInputStream(bout
                    .toByteArray());
            ObjectInputStream oos = new ObjectInputStream(bin);
            Object o = oos.readObject();
            InstanceList instances = (InstanceList) o;
            Iterator<Instance> ipc = instances.iterator();
            Instance io = ipc.next();
            /**
             * This conditions check the name and target of the first instance .
             * Moreover it checks the size of instancelist If all the things are
             * as expected then assertTrue will be return.
             * 
             */

            assertTrue((io.getName().toString()
                    .equals("http://somewhere/JohnSmith"))
                    && (io.getTarget().toString().equals("General"))
                    && ((instances.size() == 2)));

        } catch (Exception e) {
            fail(e.toString());
        }

    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());

    }
}

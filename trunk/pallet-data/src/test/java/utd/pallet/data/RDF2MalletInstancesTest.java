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
 * RDF2MalletInstancesTest is a class which perform Eight junit test cases on
 * RDF2MalletInstances.
 * 
 */
public class RDF2MalletInstancesTest extends TestCase {

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

        } catch (Exception e) {

        }

    }

    /*
     * (non-Javadoc) * @see junit.framework.TestCase#tearDown()
     */

    protected void tearDown() throws Exception {

        super.tearDown();

    }

    /**
     * This checks for the case when model is null
     */

    public void testExecuteAlgorithmNullModel() {
        boolean exceptionThrown = false;
        try {
            RDF2MalletInstances.trainingDataIntoMalletInstanceList(null, null);
        } catch (Exception e) {
            exceptionThrown = true;

        }
        assertTrue(exceptionThrown);
    }

    /**
     * This checks for the blank model.
     */
    public void testExecuteAlgorithmBlankModel() {
        boolean exceptionThrown = false;
        try {

            Model model = ModelFactory.createDefaultModel();
            Property sampleProperty = model
                    .createProperty("http://www.w3.org/2001/vcard-rdf/3.0#CATEGORIES");
            RDF2MalletInstances.trainingDataIntoMalletInstanceList(model,
                    sampleProperty);
        } catch (Exception e) {
            exceptionThrown = true;

        }
        assertTrue(exceptionThrown);
    }

    /**
     * This checks for the case when the classification predicate is null.
     */

    public void testExecuteAlgorithmNullProperty() {
        boolean exceptionThrown = false;
        try {
            Model model = ModelFactory.createDefaultModel();
            Resource resource = model
                    .createResource("http://somewhere/JohnSmith");
            resource.addProperty(VCARD.FN, "John Smith");

            RDF2MalletInstances.trainingDataIntoMalletInstanceList(model, null);

        } catch (Exception e) {

            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    /**
     * This checks for the case when the classificationpredicate is in proper
     * URI format correct but it does not exist in the model
     */

    public void testExecuteAlgorithmModelMissingClassification() {
        boolean exceptionThrown = false;
        try {
            Model model = ModelFactory.createDefaultModel();

            Resource johnSmith = model
                    .createResource("http://somewhere/JohnSmith");
            johnSmith.addProperty(VCARD.FN, "John Smith");
            johnSmith.addProperty(VCARD.N, model.createResource().addProperty(
                    VCARD.Given, "John").addProperty(VCARD.Family, "Smith"));

            Property p = model
                    .createProperty("http://www.w3.org/2001/vcard-rdf/3.0#CATEGORIES");
            RDF2MalletInstances.trainingDataIntoMalletInstanceList(model, p);
        } catch (Exception e) {
            exceptionThrown = true;

        }
        assertTrue(exceptionThrown);
    }

    /**
     * This function checks executeAlgorithmSerializable when both model(in
     * String) form and classification predicate in String form are null.
     */

    public void testExecuteAlgorithmSerializableNullModelString() {
        boolean exceptionThrown = false;
        try {

            RDF2MalletInstances.convertRDFWithLabels(null, null);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    /**
     * The function checks executeAlgorithmSerializable when the model is Empty
     */

    public void testExecuteAlgorithmSerializableEmptyModelString() {
        boolean exceptionThrown = false;
        try {

            RDF2MalletInstances.convertRDFWithLabels("",
                    "http://www.w3.org/2001/vcard-rdf/3.0#CATEGORIES");
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    /**
     * The function checks executeAlgorithmSerializable when the predicate is
     * not in correct URI form.
     */
    public void testExecuteAlgorithmSerializableMalformedString() {
        boolean exceptionThrown = false;
        try {
            Model model = ModelFactory.createDefaultModel();
            Resource resource = model
                    .createResource("http://somewhere/JohnSmith");
            resource.addProperty(VCARD.FN, "John Smith");
            String modelSerialized = JenaModelFactory.serializeModel(model,
                    "RDF/XML");
            RDF2MalletInstances.convertRDFWithLabels(modelSerialized, "HIJK");
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
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
            bout = RDF2MalletInstances.convertRDFWithLabels(modelSerialized,
                    "http://www.w3.org/2001/vcard-rdf/3.0#CATEGORIES");
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

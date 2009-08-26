package utd.pallet.data;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import utd.pallet.data.JenaModelFactory;
import utd.pallet.data.RDF2MalletInstances;
import utd.pallet.data.Rdf2JenaModel;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;

/**
 * testRDF2MalletInstances is a class which perform five junit test cases on
 * RDF2MalletInstances.
 * 
 */
public class RDF2MalletInstancesTest extends TestCase {

    /**
     * ob1 is an Instance of RDF2MalletInstances
     */
    private RDF2MalletInstances ob1;
    /**
     * ob2 is an Instance of RDF2MalletInstances
     */
    private RDF2MalletInstances ob2;
    /**
     * * ob3 is an Instance of RDF2MalletInstances
     */
    private RDF2MalletInstances ob3;
    /**
     * ob4 is an Instance of RDF2MalletInstances
     */
    private RDF2MalletInstances ob4;
    /**
     * ob5 is an Instance of RDF2MalletInstances
     */
    private RDF2MalletInstances ob5;

    /**
     * ob6 is an Instance of RDF2MalletInstances
     */
    private RDF2MalletInstances ob6;

    /**
     * ob7 is an Instance of RDF2MalletInstances
     */
    private RDF2MalletInstances ob7;

    /**
     * ob8 is an Instance of RDF2Mallet Instances
     */
    private RDF2MalletInstances ob8;

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
            ob1 = new RDF2MalletInstances();
            ob2 = new RDF2MalletInstances();
            ob3 = new RDF2MalletInstances();
            ob4 = new RDF2MalletInstances();
            ob5 = new RDF2MalletInstances();
            ob6 = new RDF2MalletInstances();
            ob7 = new RDF2MalletInstances();
            ob8 = new RDF2MalletInstances();
        } catch (Exception e) {

        }

    }

    /*
     * (non-Javadoc) * @see junit.framework.TestCase#tearDown()
     */

    protected void tearDown() throws Exception {

        super.tearDown();
        ob1 = null;
        ob2 = null;
        ob3 = null;
        ob4 = null;
        ob5 = null;
        ob6 = null;
        ob7 = null;

    }

    /**
     * This checks for the case when both model and classification predicate is
     * null
     */

    public void testExecuteAlgorithmNullModel() {

        try {
            ob1.executeAlgorithm(null, null);
        } catch (Exception e) {
            assertNotNull(e.getMessage());

        }
    }

    /**
     * This checks for the blank model.
     */
    public void testExecuteAlgorithmBlankModel() {

        try {
            Model m = ModelFactory.createDefaultModel();
            ob2.executeAlgorithm(m, null);
        } catch (Exception e) {
            assertNotNull(e.getMessage());

        }
    }

    /**
     * This checks for the case when the classification predicate is null.
     */

    public void testExecuteAlgorithmNullProperty() {
        try {
            Model model = Rdf2JenaModel
                    .rdf2Model("C:\\Users\\pralabh\\workspace2\\Mallet1\\Montery2RDF.rdf");
            ob3.executeAlgorithm(model, null);
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
            Model model = Rdf2JenaModel
                    .rdf2Model("C:\\Users\\pralabh\\workspace2\\Mallet1\\Montery2RDF.rdf");
            Property p = model
                    .createProperty("http://blackbook.com/terms#STAT_EVENT1");
            ob4.executeAlgorithm(model, p);
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

            ob5.executeAlgorithmSerializable(null, null);
        } catch (Exception e) {
            assertNotNull(e);
        }

    }

    /**
     * The function checks executeAlgorithmSerializable when the model (in
     * String) form is null.
     */

    public void testExecuteAlgorithmSerializableEmptyModelString() {
        try {
            Model model = ModelFactory.createDefaultModel();
            String modelSerialized = JenaModelFactory.serializeModel(model,
                    "RDF/XML");
            ob6.executeAlgorithmSerializable(modelSerialized,
                    "http://blackbook.com/terms#STAT_EVENT");
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
            Model m = Rdf2JenaModel
                    .rdf2Model("C:\\Users\\pralabh\\workspace2\\Mallet1\\Montery2RDF.rdf");
            String modelSerialized = JenaModelFactory.serializeModel(m,
                    "RDF/XML");
            ob7.executeAlgorithmSerializable(modelSerialized, "HIJK");
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    /**
     * ------------------------------------------------------------------------
     * -- ----------------------------------------------------------------------
     * -- ---- This function checks for the correct functionality of
     * RDF2MalletInstances class. executeAlgorithmSerializableis checked when
     * all the input is correct.
     */
    public void testExecuteAlgorithmSerializableCorrectModel() {
        String il = null;

        try {
            Model m = Rdf2JenaModel
                    .rdf2Model("C:\\Users\\pralabh\\workspace2\\Mallet1\\Montery2RDF.rdf");
            String modelSerialized = JenaModelFactory.serializeModel(m,
                    "RDF/XML");
            il = ob8.executeAlgorithmSerializable(modelSerialized,
                    "http://blackbook.com/terms#STAT_EVENT");
        } catch (Exception e) {

        }
        assertNotNull(il);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());

    }
}

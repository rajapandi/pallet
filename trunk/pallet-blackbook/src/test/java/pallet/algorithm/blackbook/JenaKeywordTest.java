package pallet.algorithm.blackbook;

import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import pallet.algorithm.api.AlgorithmQuery2URISetTest;
import pallet.algorithm.blackbook.JenaKeyword;
import pallet.test.util.TestUtilities;

import blackbook.algorithm.api.QueryRequest;
import blackbook.algorithm.api.VoidParameter;
import blackbook.exception.BlackbookSystemException;
import blackbook.jena.factory.JenaModelFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.VCARD;

/**
 * test
 */
public class JenaKeywordTest extends AlgorithmQuery2URISetTest {

    /** anonymous node */
    private Resource anon = null;

    /** data access */
    private String da;

    /** model */
    private Model model;

    /** r1 */
    private Resource r1;

    /** r2 */
    private Resource r2;

    /** r3 */
    private Resource r3;

    /** r4 */
    private Resource r4;

    /** r5 */
    private Resource r5;

    /** r6 */
    private Resource r6;

    /** r7 */
    private Resource r7;

    /** r8 */
    private Resource r8;

    /** r9 */
    private Resource r9;

    /** uris */
    private Set<String> uris;

    /**
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();

        da = "test";
        testAlgorithm = new JenaKeyword();
    }

    /**
     * Blank keyword string test.
     * 
     * @throws BlackbookSystemException
     */
    @Test
    public void testBlankKeywordString() throws BlackbookSystemException {
        da = "test5";
        uris = testAlgorithm.execute(testUser,
                new QueryRequest<VoidParameter>(" ", da, null)).getUris();
        Assert.assertNotNull(uris);
        Assert.assertEquals(0, uris.size());
    }

    /**
     * Null data access test.
     * 
     * @throws BlackbookSystemException
     */
    public void testNullDataSourceMetadata() throws BlackbookSystemException {
        uris = testAlgorithm.execute(testUser,
                new QueryRequest<VoidParameter>(null, "xyz", null)).getUris();
        Assert.assertNotNull(uris);
        Assert.assertTrue(uris.isEmpty());
    }

    /**
     * Null keyword string test.
     * 
     * @throws BlackbookSystemException
     */
    @Test
    public void testNullKeywordString() throws BlackbookSystemException {
        uris = testAlgorithm.execute(testUser,
                new QueryRequest<VoidParameter>(null, da, null)).getUris();
        Assert.assertNotNull(uris);
        Assert.assertTrue(uris.isEmpty());
    }

    /**
     * @throws Exception
     */
    @Test
    public void testSingleKeyword() throws Exception {
        String datasourceName = "test5";

        model = JenaModelFactory.openModelByName(datasourceName, TestUtilities
                .getUser());
        model.removeAll();

        // will match
        r1 = model.createResource("urn:test:r1");
        r1.addProperty(VCARD.Family, "value");

        // will match
        r2 = model.createResource("urn:test:r2");
        r2.addProperty(VCARD.Given, "value");

        // will NOT match - "value" != "blah value blah"
        r3 = model.createResource("urn:test:r3");
        r3.addProperty(VCARD.Family, "blah value blah");

        // will NOT match - "value" != "value blah"
        r4 = model.createResource("urn:test:r4");
        r4.addProperty(VCARD.Family, "value blah");

        // will NOT match - "value" != "blah value"
        r5 = model.createResource("urn:test:r5");
        r5.addProperty(VCARD.Family, "blah value");

        // will NOT match - "value" != "vAlUe"
        r6 = model.createResource("urn:test:r6");
        r6.addProperty(VCARD.Family, "vAlUe");

        // will NOT match - "value" != "xyzvalueabc"
        r7 = model.createResource("urn:test:r7");
        r7.addProperty(VCARD.Family, "xyzvalueabc");

        // will match
        r8 = model.createResource("urn:test:r8");
        anon = model.createResource();
        r8.addProperty(VCARD.N, anon);
        anon.addProperty(VCARD.Family, "value");

        // will NOT match - FN is not specified as part of the
        // keywordAttributes in MetadataManagerFromMemory
        r9 = model.createResource("urn:test:r9");
        r9.addProperty(VCARD.FN, "value");

        uris = testAlgorithm.execute(testUser,
                new QueryRequest<VoidParameter>("value", datasourceName, null))
                .getUris();

        Assert.assertNotNull(uris);
        Assert.assertEquals(3, uris.size());
        Assert.assertTrue(uris.contains("urn:test:r1"));
        Assert.assertTrue(uris.contains("urn:test:r2"));
        Assert.assertFalse(uris.contains("urn:test:r3"));
        Assert.assertFalse(uris.contains("urn:test:r4"));
        Assert.assertFalse(uris.contains("urn:test:r5"));
        Assert.assertFalse(uris.contains("urn:test:r6"));
        Assert.assertFalse(uris.contains("urn:test:r7"));
        Assert.assertTrue(uris.contains("urn:test:r8"));
        Assert.assertFalse(uris.contains("urn:test:r9"));
    }

    /**
     * @throws Exception
     */
    @Test
    public void testTwoKeywords() throws Exception {
        String datasourceName = "test5";

        model = JenaModelFactory.openModelByName(datasourceName, TestUtilities
                .getUser());
        model.removeAll();

        // will NOT match - FN is not specified as part of the
        // keywordAttributes in MetadataManagerFromMemory
        r1 = model.createResource("urn:test:r1");
        r1.addProperty(VCARD.FN, "john smith");

        // will match
        r2 = model.createResource("urn:test:r2");
        r2.addProperty(VCARD.Given, "john smith");

        // will match
        r3 = model.createResource("urn:test:r3");
        r3.addProperty(VCARD.Family, "john smith");

        // will match
        r4 = model.createResource("urn:test:r4");
        r4.addProperty(VCARD.Family, "john");

        // will match
        r5 = model.createResource("urn:test:r5");
        r5.addProperty(VCARD.Family, "smith");
        r5.addProperty(VCARD.Given, "john");

        // will match
        r6 = model.createResource("urn:test:r6");
        anon = model.createResource();
        anon.addProperty(VCARD.Family, "smith");
        r6.addProperty(VCARD.N, anon);
        anon = model.createResource();
        anon.addProperty(VCARD.Given, "john");
        r6.addProperty(VCARD.N, anon);

        uris = testAlgorithm.execute(
                testUser,
                new QueryRequest<VoidParameter>("john smith \"john smith\"",
                        datasourceName, null)).getUris();

        Assert.assertNotNull(uris);
        Assert.assertEquals(5, uris.size());
        Assert.assertFalse(uris.contains("urn:test:r1"));
        Assert.assertTrue(uris.contains("urn:test:r2"));
        Assert.assertTrue(uris.contains("urn:test:r3"));
        Assert.assertTrue(uris.contains("urn:test:r4"));
        Assert.assertTrue(uris.contains("urn:test:r5"));
        Assert.assertTrue(uris.contains("urn:test:r6"));
    }
}

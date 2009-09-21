package pallet.algorithm.api;

import org.junit.Before;
import org.junit.Test;

import blackbook.algorithm.api.DataSourceRequest;
import blackbook.algorithm.api.VoidParameter;
import blackbook.algorithm.api.VoidResponse;
import blackbook.exception.BlackbookSystemException;
import blackbook.jena.factory.JenaModelFactory;
import blackbook.metadata.manager.MetadataManagerFactory;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * test
 */
public abstract class AlgorithmDS2VoidTest extends
        AlgorithmTest<DataSourceRequest<VoidParameter>, VoidResponse> {

    /** test model */
    protected Model testModel;

    /** temporary data source name used for testing */
    protected String testModelName;

    /**
     * Prepare the test fixture. NB: Implementing classes must initialize
     * "testAlgorithm" to actual algorithm under test
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();

        testModelName = MetadataManagerFactory.getUpdatableInstance()
                .createTemporaryDS(null, false);
        testModel = JenaModelFactory.openModelByName(testModelName, testUser);
    }

    /**
     * Test null user
     * 
     * @throws BlackbookSystemException
     */
    @Test(expected = BlackbookSystemException.class)
    public void testExecuteEmptySourceDS() throws BlackbookSystemException {
        testAlgorithm.execute(testUser,
                new DataSourceRequest<VoidParameter>(""));
    }

    /**
     * Test null user
     * 
     * @throws BlackbookSystemException
     */
    @Test(expected = BlackbookSystemException.class)
    public void testExecuteNullSourceDS() throws BlackbookSystemException {
        testAlgorithm.execute(testUser, new DataSourceRequest<VoidParameter>(
                null));
    }
}

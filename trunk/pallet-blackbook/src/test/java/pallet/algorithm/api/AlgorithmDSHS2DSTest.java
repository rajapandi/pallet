package pallet.algorithm.api;

import org.junit.Before;
import org.junit.Test;

import blackbook.algorithm.api.DataSourceHelperSourceRequest;
import blackbook.algorithm.api.DataSourceResponse;
import blackbook.algorithm.api.VoidParameter;
import blackbook.exception.BlackbookSystemException;
import blackbook.jena.util.JenaModelFactory;
import blackbook.metadata.manager.MetadataManagerFactory;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * test
 */
public abstract class AlgorithmDSHS2DSTest extends
        AlgorithmTest<DataSourceHelperSourceRequest<VoidParameter>, DataSourceResponse> {
    /**
     * First test model.
     */
    protected Model model1;

    /**
     * helper test model
     */
    protected Model helperModel;

    /**
     * First test model identifier
     */
    private String modelName1;

    /**
     * Combined test model identifier
     */
    protected String helperModelName;


    /**
     * Prepare the test fixture NB: implementing classes must assign value to
     * testAlgorithm
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();

        modelName1 = MetadataManagerFactory.getModelInstance()
                .createTemporaryDS(null, null);
        model1 = JenaModelFactory.openModelByName(modelName1, testUser);
        helperModelName = MetadataManagerFactory.getModelInstance()
                .createTemporaryDS(null, null);
        helperModel = JenaModelFactory.openModelByName(helperModelName,
                testUser);
    }

    /**
     * Test null input sources list.
     * 
     * @throws BlackbookSystemException
     */
    @Test(expected = BlackbookSystemException.class)
    public void testNullSourceDataSource() throws BlackbookSystemException {
        testAlgorithm
                .execute(testUser, new DataSourceHelperSourceRequest<VoidParameter>(
                        null));
    }

    /**
     * Test empty input sources list.
     * 
     * @throws BlackbookSystemException
     */
    @Test(expected = BlackbookSystemException.class)
    public void testEmptySourceDataSource() throws BlackbookSystemException {
        testAlgorithm.execute(testUser,
                new DataSourceHelperSourceRequest<VoidParameter>(modelName1));
    }
}

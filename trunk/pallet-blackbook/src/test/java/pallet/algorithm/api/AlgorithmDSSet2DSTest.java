package pallet.algorithm.api;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import blackbook.algorithm.api.DataSourceResponse;
import blackbook.algorithm.api.DataSourceSetRequest;
import blackbook.algorithm.api.VoidParameter;
import blackbook.exception.BlackbookSystemException;
import blackbook.jena.factory.JenaModelFactory;
import blackbook.metadata.manager.MetadataManagerFactory;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * test
 */
public abstract class AlgorithmDSSet2DSTest extends
        AlgorithmTest<DataSourceSetRequest<VoidParameter>, DataSourceResponse> {
    /**
     * First test model.
     */
    protected Model model1;

    /**
     * Second test model
     */
    protected Model model2;

    /**
     * Combined test model
     */
    protected Model combinedModel;

    /**
     * First test model identifier
     */
    private String modelName1;

    /**
     * Second test model identifier
     */
    private String modelName2;

    /**
     * Combined test model identifier
     */
    protected String combinedModelName;

    /**
     * Source model list
     */
    protected Set<String> sourceModels;

    /**
     * Prepare the test fixture NB: implementing classes must assign value to
     * testAlgorithm
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();

        modelName1 = MetadataManagerFactory.getUpdatableInstance()
                .createTemporaryDS(null, false);
        model1 = JenaModelFactory.openModelByName(modelName1, testUser);
        modelName2 = MetadataManagerFactory.getUpdatableInstance()
                .createTemporaryDS(null, false);
        model2 = JenaModelFactory.openModelByName(modelName2, testUser);
        combinedModelName = MetadataManagerFactory.getUpdatableInstance()
                .createTemporaryDS(null, false);
        combinedModel = JenaModelFactory.openModelByName(combinedModelName,
                testUser);
        sourceModels = new HashSet<String>();
        sourceModels.add(modelName1);
        sourceModels.add(modelName2);
    }

    /**
     * Test null input sources list.
     * 
     * @throws BlackbookSystemException
     */
    @Test(expected = BlackbookSystemException.class)
    public void testNullSourceDataSources() throws BlackbookSystemException {
        testAlgorithm
                .execute(testUser, new DataSourceSetRequest<VoidParameter>(
                        null, combinedModelName));
    }

    /**
     * Test empty input sources list.
     * 
     * @throws BlackbookSystemException
     */
    @Test(expected = BlackbookSystemException.class)
    public void testEmptySourceDataSources() throws BlackbookSystemException {
        sourceModels.clear();
        testAlgorithm.execute(testUser,
                new DataSourceSetRequest<VoidParameter>(sourceModels,
                        combinedModelName));
    }
}

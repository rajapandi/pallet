package pallet.algorithm.api;

import org.junit.Before;
import org.junit.Test;

import blackbook.algorithm.api.DataSourceRequest;
import blackbook.algorithm.api.URISetResponse;
import blackbook.algorithm.api.VoidParameter;
import blackbook.exception.BlackbookSystemException;

/**
 * test
 */
public abstract class AlgorithmDS2URISetTest extends
        AlgorithmTest<DataSourceRequest<VoidParameter>, URISetResponse> {
    /**
     * Prepare the test fixture. NB: Implementing classes must initialize
     * "testAlgorithm" to actual algorithm under test
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();
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

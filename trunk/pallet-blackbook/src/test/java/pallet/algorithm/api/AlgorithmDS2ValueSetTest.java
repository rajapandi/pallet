package pallet.algorithm.api;

import org.junit.Before;
import org.junit.Test;

import blackbook.algorithm.api.DataSourceRequest;
import blackbook.algorithm.api.ValueSetResponse;
import blackbook.algorithm.api.VoidParameter;
import blackbook.exception.BlackbookSystemException;

/**
 * test
 */
public abstract class AlgorithmDS2ValueSetTest extends
        AlgorithmTest<DataSourceRequest<VoidParameter>, ValueSetResponse> {

    /**
     * Prepare the test fixture NB: implementing classes must assign value to
     * testAlgorithm
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

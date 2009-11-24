package pallet.algorithm.api;

import org.junit.Before;
import org.junit.Test;

import pallet.test.util.TestUtilities;

import security.ejb.client.User;
import blackbook.algorithm.api.Algorithm;
import blackbook.algorithm.api.AlgorithmRequest;
import blackbook.algorithm.api.AlgorithmResponse;
import blackbook.exception.BlackbookSystemException;

/**
 * Abstract base test class for all algorithms.
 * 
 * @param <REQUEST>
 *            name of the class used to contain the algorithm request values
 * 
 * @param <RESPONSE>
 *            name of the class used to contain the algorithm return values
 */
public abstract class AlgorithmTest<REQUEST extends AlgorithmRequest<?>, RESPONSE extends AlgorithmResponse> {

    /**
     * The algorithm under test. Value must be assigned by implementing tests.
     */
    protected Algorithm<REQUEST, RESPONSE> testAlgorithm = null;

    /** test user */
    protected User testUser;

    /**
     * Prepare the test fixture. NB: Implementing classes must initialize
     * "testAlgorithm" to actual algorithm under test
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
//        TestUtilities.prepareForTesting();
        testUser = TestUtilities.getUser();
    }

    /**
     * Test null user
     * 
     * @throws BlackbookSystemException
     */
    @Test(expected = BlackbookSystemException.class)
    public void testExecuteNullRequest() throws BlackbookSystemException {
        testAlgorithm.execute(testUser, null);
    }

    /**
     * Test null user
     * 
     * @throws BlackbookSystemException
     */
    @Test(expected = BlackbookSystemException.class)
    public void testExecuteNullUser() throws BlackbookSystemException {
        testAlgorithm.execute(null, null);
    }
}

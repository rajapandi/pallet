package pallet.algorithm.api;

import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import blackbook.algorithm.api.URISetRequest;
import blackbook.algorithm.api.VoidParameter;
import blackbook.algorithm.api.VoidResponse;
import blackbook.exception.BlackbookSystemException;

/**
 * test
 */
public abstract class AlgorithmURISet2VoidTest extends
        AlgorithmTest<URISetRequest<VoidParameter>, VoidResponse> {

    /**
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * @throws BlackbookSystemException
     */
    @Test(expected = BlackbookSystemException.class)
    public void testEmptyDestinationDataSource()
            throws BlackbookSystemException {
        testAlgorithm.execute(testUser, new URISetRequest<VoidParameter>(
                new HashSet<String>(), null, ""));
    }

    /**
     * @throws BlackbookSystemException
     */
    @Test(expected = BlackbookSystemException.class)
    public void testNullDestinationDataSource() throws BlackbookSystemException {
        testAlgorithm.execute(testUser, new URISetRequest<VoidParameter>(
                new HashSet<String>(), null, null));
    }

    /**
     * @throws BlackbookSystemException
     */
    @Test(expected = BlackbookSystemException.class)
    public void testNullURIs() throws BlackbookSystemException {
        testAlgorithm.execute(testUser, new URISetRequest<VoidParameter>(null,
                null, null));
    }
}

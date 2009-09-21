package pallet.algorithm.api;

import org.junit.Before;
import org.junit.Test;

import blackbook.algorithm.api.URISetRequest;
import blackbook.algorithm.api.URISetResponse;
import blackbook.algorithm.api.VoidParameter;
import blackbook.exception.BlackbookSystemException;

/**
 * test
 */
public abstract class AlgorithmURISet2URISetTest extends
        AlgorithmTest<URISetRequest<VoidParameter>, URISetResponse> {

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
    public void testNullURIs() throws BlackbookSystemException {
        testAlgorithm.execute(testUser, new URISetRequest<VoidParameter>(null,
                null, null));
    }
}

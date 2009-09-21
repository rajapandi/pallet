package pallet.algorithm.api;

import org.junit.Before;
import org.junit.Test;

import blackbook.algorithm.api.QueryRequest;
import blackbook.algorithm.api.URISetResponse;
import blackbook.algorithm.api.VoidParameter;
import blackbook.exception.BlackbookSystemException;

/**
 * test
 */
public abstract class AlgorithmQuery2URISetTest extends
        AlgorithmTest<QueryRequest<VoidParameter>, URISetResponse> {

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
    public void testBlankSourceDS() throws BlackbookSystemException {
        testAlgorithm.execute(testUser, new QueryRequest<VoidParameter>(null,
                "", null));
    }

    /**
     * @throws BlackbookSystemException
     */
    @Test(expected = BlackbookSystemException.class)
    public void testNullSourceDS() throws BlackbookSystemException {
        testAlgorithm.execute(testUser, new QueryRequest<VoidParameter>(null,
                null, null));
    }
}

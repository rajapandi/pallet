package pallet.algorithm.api;

import org.junit.Before;
import org.junit.Test;

import blackbook.algorithm.api.RDFResponse;
import blackbook.algorithm.api.RDFRetrievalRequest;
import blackbook.algorithm.api.VoidParameter;
import blackbook.exception.BlackbookSystemException;

/**
 * test
 */
public abstract class AlgorithmRDF2RDFTest extends
        AlgorithmTest<RDFRetrievalRequest<VoidParameter>, RDFResponse> {

    /**
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
        testAlgorithm
                .execute(testUser, new RDFRetrievalRequest<VoidParameter>("", null));
    }

    /**
     * Test null user
     * 
     * @throws BlackbookSystemException
     */
    @Test(expected = BlackbookSystemException.class)
    public void testExecuteNullSourceDS() throws BlackbookSystemException {
        testAlgorithm.execute(testUser, new RDFRetrievalRequest<VoidParameter>(null,
                null));
    }

    /**
     * Test null user
     * 
     * @throws BlackbookSystemException
     */
    @Test(expected = BlackbookSystemException.class)
    public void testExecuteBadFormat() throws BlackbookSystemException {
        testAlgorithm.execute(testUser, new RDFRetrievalRequest<VoidParameter>(null,
                null, null, "XYZ"));
    }
}

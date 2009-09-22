package pallet.algorithm.blackbook;

import org.junit.Before;
import org.junit.Test;

import pallet.algorithm.api.AlgorithmDSHS2DSTest;
import blackbook.algorithm.api.DataSourceHelperSourceRequest;
import blackbook.algorithm.api.VoidParameter;
import blackbook.exception.BlackbookSystemException;

/**
 * test
 */
public class PalletClassifyTest extends AlgorithmDSHS2DSTest {

    /**
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();

        testAlgorithm = new PalletClassify();
    }



    /**
     * Null data access test.
     * 
     * @throws BlackbookSystemException
     */
    public void testNullDataSourceMetadata() throws BlackbookSystemException {
    	testAlgorithm.execute(testUser,
                new DataSourceHelperSourceRequest<VoidParameter>(null, "xyz", null));
    }

    /**
     * Null keyword string test.
     * 
     * @throws BlackbookSystemException
     */
    @Test
    public void testNullHelperSource() throws BlackbookSystemException {

    }

    //TODO fill in tests
}

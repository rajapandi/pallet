/**
 * 
 */
package utd.pallet.classification;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.junit.After;
import org.junit.Before;

import cc.mallet.classify.ClassifierTrainer;

/**
 * Tests the correctness of the Trainer Created from the Factory
 *
 */
@SuppressWarnings("unchecked")
public class MalletTrainerFactoryTest extends TestCase {

	public MalletTrainerFactoryTest (String name)
	{
		super (name);
	}	
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	static Test suite ()
	{
		return new TestSuite (MalletTrainerFactoryTest.class);
	}
	
	/**
	 * Tests the validity of NaiveBayesTrainer Creation.
	 * 
	 */
	public void testCreateNaiveBayesTrainer () {
		ClassifierTrainer trainer = MalletTrainerFactory.CreateNaiveBayesTrainer();
		assertTrue(trainer instanceof cc.mallet.classify.NaiveBayesTrainer);	
	}
	
	/**
	 * Tests the validity of MaxEntTrainer Creation.
	 */
	public void testCreateMaxEntTrainer () {
		ClassifierTrainer trainer = MalletTrainerFactory.CreateMaxEntTrainer();
		assertTrue(trainer instanceof cc.mallet.classify.MaxEntTrainer);			
	}
	
	/**
	 * Tests the validity of DecisionTreeTrainer Creation. 
	 */
	public void testCreateDecisionTreeTrainer () {
		ClassifierTrainer trainer = MalletTrainerFactory.CreateDecisionTreeTrainer();
		assertTrue(trainer instanceof cc.mallet.classify.DecisionTreeTrainer);		
	}
	
	/**
	 * Tests the validity of C45Trainer Creation.
	 */
	public void testCreateC45Trainer () {
		ClassifierTrainer trainer = MalletTrainerFactory.CreateC45Trainer();
		assertTrue(trainer instanceof cc.mallet.classify.C45Trainer);
	}
	
	/**
	 * Tests the validity of BalancedWinnowTrainer Creation.
	 */
	public void testCreateBalancedWinnowTrainer () {
		ClassifierTrainer trainer = MalletTrainerFactory.CreateBalancedWinnowTrainer();
		assertTrue(trainer instanceof cc.mallet.classify.BalancedWinnowTrainer);
	}
	
	/**
	 * Tests the validity of MaxEntGETrainer Creation.
	 */
	public void testCreateMaxEntGETrainer () {
		ClassifierTrainer trainer = MalletTrainerFactory.CreateMaxEntGETrainer();
		assertTrue(trainer instanceof cc.mallet.classify.MaxEntGETrainer);
	}
	
	/**
	 * Tests the validity of RankMaxEntTrainer Creation.
	 */
	public void testCreateRankMaxEntTrainer () {
		ClassifierTrainer trainer = MalletTrainerFactory.CreateRankMaxEntTrainer();
		assertTrue(trainer instanceof cc.mallet.classify.RankMaxEntTrainer);		
	}
	
	/**
	 * Tests the validity of NaiveBayesEMTrainer Creation.
	 */
	public void testCreateNaiveBayesEMTrainer () {
		ClassifierTrainer trainer = MalletTrainerFactory.CreateNaiveBayesEMTrainer();
		assertTrue(trainer instanceof cc.mallet.classify.NaiveBayesEMTrainer);
	}
	
	/*public void testCreateMCMAxEntTrainer () {
		ClassifierTrainer trainer = MalletTrainerFactory.CreateMCMaxEntTrainer();
		
		System.out.println ("Class - " + trainer.getClass());
		
		//assertTrue(trainer instanceof cc.mallet.classify.MCMaxEntTrainer);	
	}*/
		
	public static void main (String[] args)
	{
		junit.textui.TestRunner.run (suite());
	}		
}

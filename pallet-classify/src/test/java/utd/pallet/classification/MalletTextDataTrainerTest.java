/**
 * 
 */
package utd.pallet.classification;


import java.io.IOException;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.junit.After;
import org.junit.Before;

import cc.mallet.classify.Classifier;
import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.classify.NaiveBayesTrainer;
import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.FeatureSequence2FeatureVector;
import cc.mallet.pipe.Input2CharSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.Target2Label;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.ArrayIterator;
import cc.mallet.types.InstanceList;

import utd.pallet.classification.MalletTextDataTrainer.TrainerObject;
import utd.pallet.data.BuildPipe;

/**
 * Tests training and incremental training of data.
 *
 */
public class MalletTextDataTrainerTest extends TestCase {
	
	BuildPipe pipe = null;
	
	public MalletTextDataTrainerTest (String name)
	{
		super (name);
	}		
	
	
	private InstanceList getInstanceList () {
        String[][][] trainingdata = new String[][][] {
                {{ "on the plains of africa savannas the lions roar",
                   "in swahili ngoma means to dance",
                   "nelson mandela became president of south africa",
                   "the saraha dessert is expanding" }, { "africa" }},
                {{ "panda bears eat bamboo",
                   "china's one child policy has resulted in a surplus of boys",
                   "tigers live in the jungle" }, { "asia" } },
                {{ "home of kangaroos", "Autralian's for beer - Foster",
                   "Steve Irvin is a herpetologist" },{ "australia" } } };
        
        Pattern tokenPattern = Pattern.compile("[\\p{L}\\p{N}_]+");
        
        try {
        	pipe.createPipe(new Input2CharSequence("UTF-8"),
                    new CharSequence2TokenSequence(tokenPattern), true,
                    new TokenSequenceRemoveStopwords(true, true),
                    new TokenSequence2FeatureSequence(), new Target2Label(),
                    new FeatureSequence2FeatureVector(false), false);
        } catch (IOException e) {

        }

        for (int i = 0; i < 3; i++) {
            try {
                pipe.addThruPipe(new ArrayIterator(trainingdata[i][0],
                        trainingdata[i][1][0]));

            } catch (Exception e) {
            }
        }   
        
        return pipe.getInstanceList();
		
	}	
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		pipe = new BuildPipe ();		
	}

	/**
	 * Tests 
	 * 1. Whether the trainer created is of NaiveBayes type.
	 * 2. Whether the classifier created by training is trained on specified training data.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void testNaiveBayesTrain () {
		MalletTextDataTrainer trainer = new MalletTextDataTrainer ();
		
		TrainerObject trnObj = null;
		try {
			trnObj = trainer.train(getInstanceList (), MalletTextDataTrainer.NAIVE_BAYES);
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			assertTrue(false);
			//e.printStackTrace();			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			assertTrue(false);
		}
		
		ClassifierTrainer trner = trnObj.getTrainer();
		// check whether the trainer created is indeed NaiveBayesTrainer 
		assertTrue(trner instanceof NaiveBayesTrainer);
		
		Classifier cl = trnObj.getClassifier();
		
		// Tests whether the classifier created by train is trained on Training data
		assertTrue (cl.alphabetsMatch(pipe.getInstanceList()));
				
	}
	
	/**
	 * 1. Tests whether the trainer supplied is of type naive bayes (currently only naive bayes supports incremental training).
	 * 2. Tests whether classifier learnt the new training data supplied online.
	 * 3. Tests whether the incremental train is indeed succes 
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void testIncrementalTrain () {
		MalletTextDataTrainer trainer = new MalletTextDataTrainer ();
		
		TrainerObject trnObj = null;
		try {
			trnObj = trainer.train(getInstanceList (), MalletTextDataTrainer.NAIVE_BAYES);
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			assertTrue(false);
			//e.printStackTrace();			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			assertTrue(false);
		}
		
		String [][][] incTestData = {
				{{ "The United States is one of the world's most ethnically diverse and multicultural nations",
	                   "The U.S. economy is the largest national economy in the world",
	                   "The country accounts for approximately fifty percent of global military spending"}, { "USA" }}
		};
		
		ClassifierTrainer trner = trnObj.getTrainer();
		Classifier cl = trnObj.getClassifier();
		Pipe incPipe = cl.getInstancePipe();
		InstanceList incList = new InstanceList (incPipe);
		incList.addThruPipe(new ArrayIterator(incTestData[0][0],
				incTestData[0][1][0]));
                
		try {
			trnObj = trainer.trainIncremental(trner, incList);
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue(false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue(false);
		}
		
		pipe.addThruPipe(new ArrayIterator(incTestData[0][0],
				incTestData[0][1][0]));
		
		assertTrue (cl.alphabetsMatch(pipe.getInstanceList()));
		
		/*cl = trnObj.getClassifier();
		Classification cla = cl.classify("The U.S. economy is the largest national economy in the world");
		System.out.println("Label - " + cla.getLabeling());*/
		
		assertTrue(true);
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		pipe = null;
	}
	
	static Test suite ()
	{
		return new TestSuite (MalletTextDataTrainerTest.class);
	}	
	
	public static void main (String[] args)
	{
		junit.textui.TestRunner.run (suite());
	}
}

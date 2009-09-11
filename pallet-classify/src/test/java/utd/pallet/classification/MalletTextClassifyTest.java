package utd.pallet.classification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import utd.pallet.classification.MalletTextClassify;
import utd.pallet.classification.MalletTextDataTrainer;
import utd.pallet.classification.MalletTextDataTrainer.TrainerObject;
import utd.pallet.data.BuildPipe;
import cc.mallet.classify.Classification;
import cc.mallet.classify.Classifier;
import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.FeatureSequence2FeatureVector;
import cc.mallet.pipe.Input2CharSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.Target2Label;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.ArrayIterator;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.LabelAlphabet;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 *
 *
 */
public class MalletTextClassifyTest extends TestCase {
	
	private InstanceList iList;
	
	// Set of test data that needs to be classified to label "africa".
	private String [] testObj = {"nelson mandela never eats lions", 
						 "Lion is the king of savannas", 
						 "Swahili is an african language"};
	
	// Set of test data that needs to be classified to "asia".
	private String [] testObjAsia = {"tigers are kings in asian jungle",
							 "Panda bears under threat of extinction since bamboo is cut down for paper",
							 "China is the dominant country in asian continent"};
	
	// Set of test data that needs to be classified to "australia".
	private String [] testObjAus = {"Foster beers are manufactured in Australia",
							   "Kangaroos and Cricket are native of Australia",
							   "Steve Irvin died when he was on a mission"};
	
	// Set of Training data
    private String[][][] trainingdata = new String[][][] {
            {{ "on the plains of africa savannas the lions roar",
               "in swahili ngoma means to dance",
               "nelson mandela became president of south africa",
               "the saraha dessert is expanding" }, { "africa" }},
            {{ "panda bears eat bamboo",
               "china's one child policy has resulted in a surplus of boys",
               "China and India are emerging super powers",
               "tigers live in the jungle" }, { "asia" } },
            {{ "home of kangaroos", "Autralian's for beer - Foster",
               "Steve Irvin is a herpetologist" },{ "australia" } } };
	
	public MalletTextClassifyTest (String name)
	{
		super (name);
	}
	
	/*
	 * Fetches the Instance list of Training data.
	 */
	private InstanceList GetInstanceList (BuildPipe pipe) {
        
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

	/*
	 * Trains the classifier on training data.
	 */
	private TrainerObject train (int algo) {
		BuildPipe pipe = new BuildPipe ();
    	iList = GetInstanceList(pipe);
    	MalletTextDataTrainer bTrainer = new MalletTextDataTrainer ();
    	TrainerObject trnObj = null;
    	
    	try {
    		trnObj = bTrainer.train(iList, algo);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return trnObj;
	}
	
	
	/**
	 * Tests classification, tests behavior on different labels and different set of test data. 
	 */
    public void testMalletClassification () {
    	    	    
    	Classifier classifier = null;    	
    	TrainerObject trnObj = train (MalletTextDataTrainer.NAIVE_BAYES);
		
		classifier = trnObj.getClassifier();
		MalletTextClassify bClassifier = new MalletTextClassify ();
				
		ArrayList<Classification> classificationList = null;
		ArrayList<Classification> classificationListAsia = null;
		ArrayList<Classification> classificationListAus = null;
		try {
			classificationList = bClassifier.classify(classifier, testObj);
			classificationListAsia = bClassifier.classify(classifier, testObjAsia);
			classificationListAus = bClassifier.classify(classifier, testObjAus);
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue (false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue (false);
		}
		
		if (classificationList.size() != testObj.length || 
			classificationListAsia.size() != testObjAsia.length ||
			classificationListAus.size() != testObjAus.length)
			assertTrue (false);
		
		// Test for label africa
		for (int i = 0; i < classificationList.size();i++) {
			//System.out.println("Best Label - " + classificationList.get(i).getLabeling().getBestLabel());
										
			assertTrue (classificationList.get(i).getLabeling().getBestLabel()
					== ((LabelAlphabet)iList.getTargetAlphabet()).lookupLabel("africa"));
		}
		
		// Test for label asia
		for (int i = 0; i < classificationListAsia.size();i++) {
			//System.out.println("--Best Label - " + classificationList.get(i).getLabeling().getBestLabel());
										
			assertTrue (classificationListAsia.get(i).getLabeling().getBestLabel()
					== ((LabelAlphabet)iList.getTargetAlphabet()).lookupLabel("asia"));
		}
		
		// Test for label australia
		for (int i = 0; i < classificationListAus.size();i++) {
			//System.out.println("--Best Label - " + classificationList.get(i).getLabeling().getBestLabel());
										
			assertTrue (classificationListAus.get(i).getLabeling().getBestLabel()
					== ((LabelAlphabet)iList.getTargetAlphabet()).lookupLabel("australia"));
		}		
    }
    

    /**
     * Tests Classification when test data is represented as String.
     */
    public void testClassifierWhenDataAsString () {
    	    	
    	Classifier classifier = null;    	
    	TrainerObject trnObj = train (MalletTextDataTrainer.NAIVE_BAYES);
		
		classifier = trnObj.getClassifier();
		MalletTextClassify bClassifier = new MalletTextClassify ();
		ArrayList <Classification> cl = null;		    			
		try {
			cl = bClassifier.classify(classifier, this.testObjAsia[0]);
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue (false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue (false);
		}
		
		// only one string is classified, number of classifications should be one.
		assertTrue(cl.size() == 1);
		//System.out.println ("Label - " + cl.get(0).getLabeling().getBestLabel());
		assertTrue (cl.get(0).getLabeling().getBestLabel() == 
			((LabelAlphabet)iList.getTargetAlphabet()).lookupLabel("asia"));		
		
    }
  
    /**
     * Tests Classification when test data is represented as Instance.
     */
    public void testClassifierWhenDataAsInstance () {    	
    	
    	Classifier classifier = null;    	
    	TrainerObject trnObj = train (MalletTextDataTrainer.NAIVE_BAYES);
		
		classifier = trnObj.getClassifier();
		Pipe p = classifier.getInstancePipe();
						
		MalletTextClassify bClassifier = new MalletTextClassify ();
		
		Instance testInstance = p.instanceFrom(new Instance (testObjAsia[0], null,null,null));
		ArrayList <Classification> cl = null;
		try {
			cl = bClassifier.classify(classifier, testInstance);
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue (false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue (false);
		}
		
		int i = 0;
		for (i = 0; i < cl.size(); i++) 
		{
			//System.out.println ("Label - " + cl.get(i).getLabeling().getBestLabel());						 
			assertTrue (cl.get(i).getLabeling().getBestLabel() == 
					((LabelAlphabet)iList.getTargetAlphabet()).lookupLabel("asia"));
				
		}    	
    	
    }
    
    /**
     * Tests classifier when Test data is empty string.
     * should classify test data to Default label(first label in the Training data). 
     */
    public void testClassifierOnEmptyString () {
    	
    	Classifier classifier = null;    	
    	TrainerObject trnObj = train (MalletTextDataTrainer.NAIVE_BAYES);
		
		classifier = trnObj.getClassifier();
		MalletTextClassify bClassifier = new MalletTextClassify ();
		
		String emptyString = "";
		ArrayList <Classification> cl = null;
		try {
			cl = bClassifier.classify(classifier, emptyString);
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue (false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue (false);
		}
		
		int i = 0;
		for (i = 0; i < cl.size(); i++) 
		{
			//System.out.println ("Label - " + cl.get(i).getLabeling().getBestLabel());
				assertTrue (cl.get(i).getLabeling().getBestLabel() == 
					((LabelAlphabet)iList.getTargetAlphabet()).lookupLabel("africa"));
		}					
    }
    
    /**
     * Tests classifier behavior when the test data has no relevance to the training data.
     * Should classify the test data to default label.
     */
    public void testClassifierOnInvalidData () {
    	String invalidTestData = "This string as no relevance to any of the training data";

    	Classifier classifier = null;    	
    	TrainerObject trnObj = train (MalletTextDataTrainer.NAIVE_BAYES);
		
		classifier = trnObj.getClassifier();
		MalletTextClassify bClassifier = new MalletTextClassify ();
		
		ArrayList <Classification> cl = null;
		try {
			cl = bClassifier.classify(classifier, invalidTestData);
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue (false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue (false);
		}
		
		int i = 0;
		for (i = 0; i < cl.size(); i++) 
		{
			// System.out.println ("Label - " + cl.get(i).getLabeling().getBestLabel());
			assertTrue (cl.get(i).getLabeling().getBestLabel() == 
					((LabelAlphabet)iList.getTargetAlphabet()).lookupLabel("africa"));
		}							
    }
    
        
	static Test suite ()
	{
		return new TestSuite (MalletTextClassifyTest.class);
	}	
	
	protected void setUp ()
	{
	}
	
	public static void main (String[] args)
	{
		junit.textui.TestRunner.run (suite());
	}	

}


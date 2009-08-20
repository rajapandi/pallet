package utd.pallet.classification;

import java.io.IOException;
import java.util.regex.Pattern;

import utd.pallet.classification.BlackbookTextClassify;
import utd.pallet.classification.BlackbookTextDataTrainer;
import utd.pallet.data.BuildPipe;
import cc.mallet.classify.Classification;
import cc.mallet.classify.Classifier;
import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.FeatureSequence2FeatureVector;
import cc.mallet.pipe.Input2CharSequence;
import cc.mallet.pipe.Target2Label;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.ArrayIterator;
import cc.mallet.types.InstanceList;
import cc.mallet.types.LabelAlphabet;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * @author sharath
 *
 */
public class testClassification extends TestCase {
	
	BuildPipe pipe = new BuildPipe ();	
	
	public testClassification (String name)
	{
		super (name);
	}
	
	
	private InstanceList GetInstanceList () {
        String[][][] trainingdata = new String[][][] {
                {{ "on the plains of africa the lions roar",
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
        	pipe.CreatePipe(new Input2CharSequence("UTF-8"),
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
        
        return pipe.GetInstanceList();
		
	}
	

    @SuppressWarnings("unchecked")
	public void testMalletClassification () {
    	InstanceList iList = GetInstanceList();
    	BlackbookTextDataTrainer bTrainer = new BlackbookTextDataTrainer (iList, 
    										BlackbookTextDataTrainer.NAIVEBAYES, 
    										false);
    	
    	ClassifierTrainer trainer = null;
    	Classifier classifier = null;
    	
    	try {
    		classifier = bTrainer.train(trainer);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		BlackbookTextClassify bClassifier = new BlackbookTextClassify (classifier, 
										"nelson mandela never eats lions");
		
		Classification bClassification = bClassifier.classify();
		
		assertTrue (bClassification.getLabeling().getBestLabel()
				== ((LabelAlphabet)iList.getTargetAlphabet()).lookupLabel("africa"));
		
		//trainer.
    }
	
	
	
	static Test suite ()
	{
		return new TestSuite (testClassification.class);
	}	
	
	protected void setUp ()
	{
	}
	
	public static void main (String[] args)
	{
		junit.textui.TestRunner.run (suite());
	}	

}

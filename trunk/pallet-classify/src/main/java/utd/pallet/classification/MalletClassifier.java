import java.io.IOException;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import cc.mallet.classify.Classification;
import cc.mallet.classify.Classifier;
import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.classify.MaxEntTrainer;
import cc.mallet.classify.NaiveBayesTrainer;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import cc.mallet.types.*;

/**
 * @author sharath
 *
 */
public class MalletClassifier {
	private BuildPipe bpipe = null;
	// private ClassifierTrainer trainer = null;
	private Classifier classifier = null;
	
	public static final int naivebayes = 0;
	public static final int maxent = 1;
	
	
	public void CreatePipe (Input2CharSequence charseq, CharSequence2TokenSequence tokenseq, 
			   boolean tokenseq2lowercase,  
			   TokenSequenceRemoveStopwords removestopwards,
			   TokenSequence2FeatureSequence tokenseq2featureseq, Target2Label target2label,
			   FeatureSequence2FeatureVector featseq2Featvector, boolean printinputandlabel) throws IOException {
		
		this.bpipe = new BuildPipe ();
		this.bpipe.CreatePipe(charseq, tokenseq, tokenseq2lowercase, removestopwards, 
						tokenseq2featureseq, target2label, featseq2Featvector, printinputandlabel);
	}

	public void addThruPipe (Iterator<Instance> iterator) throws java.lang.Exception {
		if (bpipe == null)
			throw new java.lang.NullPointerException ("Mallet Classifier, addThruPipe : pipe is not yet created");
		
		this.bpipe.addThruPipe(iterator);
	}
	
	// Creating trainer with constructor w/o parameter
	// Naive bayes and MaxEnt supports more than one constructor
	// What to do about that? -- sharath
	public void train (int algo) throws java.lang.Exception {
		ClassifierTrainer trainer = null;
		
		if (bpipe == null)
			throw new java.lang.NullPointerException (
					"Mallet Classifier, train : pipe is not yet created");
		
		switch (algo) {
			case MalletClassifier.naivebayes:
					trainer = new NaiveBayesTrainer ();
				break;
			case MalletClassifier.maxent:
					trainer = new MaxEntTrainer ();
				break;
			default:
				 throw new Exception ("Mallet Classifier, Train : Unsupported Training algorithm");
		}
		classifier = trainer.train(bpipe.GetInstanceList());
	}
	
	public Classification classify (Object sampledata)/* throws java.lang.Exception */{
		// if (bpipe == null)
		//	throw new java.lang.NullPointerException (
			//		"Mallet Classifier, classify : pipe is not yet created");
		
		return classifier.classify(sampledata);
	}
	
	// save and read are not yet tested - sharath
	public void saveClassifier (File classifierFile) throws java.lang.Exception {
		if (classifierFile == null)
			throw new Exception ("");
		
		ObjectOutputStream ops = new ObjectOutputStream (new FileOutputStream (classifierFile));
		ops.writeObject(this.classifier);
		ops.close();
	}
	
	public void readClassifier (File classifierFile) throws java.lang.Exception {
		if (classifierFile == null)
			throw new Exception ("");
		
		ObjectInputStream ips = new ObjectInputStream (new FileInputStream (classifierFile));
		classifier = null;
		this.classifier = (Classifier) ips.readObject();
		ips.close();
	}
}

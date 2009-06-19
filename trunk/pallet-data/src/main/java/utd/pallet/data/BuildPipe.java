import cc.mallet.pipe.FeatureSequence2FeatureVector;
import cc.mallet.pipe.Input2CharSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.PrintInputAndTarget;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.Target2Label;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import cc.mallet.types.*;

public class BuildPipe {

	private Pipe pipe = null;
	private InstanceList instancelist = null;
	
	public void CreatePipe (Input2CharSequence charseq, CharSequence2TokenSequence tokenseq, 
			   boolean tokenseq2lowercase,  
			   TokenSequenceRemoveStopwords removestopwards,
			   TokenSequence2FeatureSequence tokenseq2featureseq, Target2Label target2label,
			   FeatureSequence2FeatureVector featseq2Featvector, boolean printinputandlabel) throws IOException {
		
		if (charseq == null || tokenseq == null)
			 throw new IllegalArgumentException ("WrapperClassifier : Input2CharSequence or CharSequence2TokenSequence cannot be null");
			
		ArrayList pipelist = new ArrayList();
		
		boolean res = pipelist.add(charseq);
		// if (res == false)
		//	throw 
			
		pipelist.add(tokenseq);
		
		if (tokenseq2lowercase == true) {
			TokenSequenceLowercase lowercase =  new TokenSequenceLowercase ();
			pipelist.add(lowercase);
		}
		
		if (removestopwards != null)
			pipelist.add(removestopwards);
		
		if (tokenseq2featureseq != null)
			pipelist.add(tokenseq2featureseq);
		
		if (target2label != null)
			pipelist.add(target2label);
		
		if ((featseq2Featvector == null && tokenseq2featureseq != null) || 
				(featseq2Featvector != null && tokenseq2featureseq == null))
			throw new IllegalArgumentException (
					"WrapperClassifier,BuildPipe: Invalid FeatureSequence2FeatureVector and/or TokenSequence2FeatureSequence parameter");
		
		if (featseq2Featvector != null)
			pipelist.add(featseq2Featvector);
		
		if (printinputandlabel == true)
			pipelist.add(new PrintInputAndTarget("abc"));
		
		pipe = new SerialPipes(pipelist);
		this.instancelist = new InstanceList (pipe);
	}
	
	public void addThruPipe (Iterator<Instance> iterator) {		
		this.instancelist.addThruPipe(iterator);
	}
	
	// Required ???????????
	public InstanceList GetInstanceList () { return this.instancelist; }
}

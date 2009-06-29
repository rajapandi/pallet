package utd.pallet.classification;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;

import utd.pallet.data.BuildPipe;
import cc.mallet.classify.Classification;
import cc.mallet.classify.Classifier;
import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.classify.MaxEntTrainer;
import cc.mallet.classify.NaiveBayesTrainer;
import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.FeatureSequence2FeatureVector;
import cc.mallet.pipe.Input2CharSequence;
import cc.mallet.pipe.Target2Label;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.types.Instance;

/**
 * @author sharath
 * 
 */
public class MalletClassifier {
    /**
     * An instance of BuildPipe is created which will be used to call the
     * CreatePipe method of the BuildPipe class.
     **/
    private BuildPipe bpipe = null;

    /**
     *An instance of classifier is created which will be used for
     * classification
     * 
     */
    private Classifier classifier = null;

    /**
     * If zero is given as input than naivebayes algorthim will be applied
     */
    public static final int naivebayes = 0;
    /**
     * If zero is given as input than maxent will be applied
     */
    public static final int maxent = 1;

    /**
     * @param charseq
     *            An instance of Input2CharSequence Pipe
     * @param tokenseq
     *            An instance of CharSequence2TokenSequence Pipe
     * @param tokenseq2lowercase
     *            Boolean value.
     * @param removestopwards
     *            An instance of TokenSequenceRemoveStopwords Pipe
     * @param tokenseq2featureseq
     *            An instance of TokenSequence2FeatureSequence pipe
     * @param target2label
     *            An instance of Target2Label pipe
     * @param featseq2Featvector
     *            An instance of FeatureSequence2AugmentableFeatureVector pipe
     * @param printinputandlabel
     *            Boolean Value
     * @throws IOException
     */
    public void CreatePipe(Input2CharSequence charseq,
            CharSequence2TokenSequence tokenseq, boolean tokenseq2lowercase,
            TokenSequenceRemoveStopwords removestopwards,
            TokenSequence2FeatureSequence tokenseq2featureseq,
            Target2Label target2label,
            FeatureSequence2FeatureVector featseq2Featvector,
            boolean printinputandlabel) throws IOException {

        this.bpipe = new BuildPipe();
        this.bpipe.CreatePipe(charseq, tokenseq, tokenseq2lowercase,
                removestopwards, tokenseq2featureseq, target2label,
                featseq2Featvector, printinputandlabel);

    }

    /**
     * @param iterator
     * @throws Exception
     */
    public void addThruPipe(Iterator<Instance> iterator)
            throws java.lang.Exception {
        if (bpipe == null)
            throw new java.lang.NullPointerException(
                    "Mallet Classifier, addThruPipe : pipe is not yet created");

        this.bpipe.addThruPipe(iterator);
    }

    /**
     * @param algo
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public void train(int algo) throws java.lang.Exception {
        ClassifierTrainer trainer = null;

        if (bpipe == null)
            throw new java.lang.NullPointerException(
                    "Mallet Classifier, train : pipe is not yet created");

        switch (algo) {
        case MalletClassifier.naivebayes:
            trainer = new NaiveBayesTrainer();
            break;
        case MalletClassifier.maxent:
            trainer = new MaxEntTrainer();
            break;
        default:
            throw new Exception(
                    "Mallet Classifier, Train : Unsupported Training algorithm");
        }
        classifier = trainer.train(bpipe.GetInstanceList());
    }

    /**
     * @param sampledata
     * @return Classifier object is returned
     * */
    public Classification classify(Object sampledata) {
        return classifier.classify(sampledata);
    }

    /**
     * @param classifierFile
     * @throws Exception
     */

    public void saveClassifier(File classifierFile) throws java.lang.Exception {
        if (classifierFile == null)
            throw new Exception("");

        ObjectOutputStream ops = new ObjectOutputStream(new FileOutputStream(
                classifierFile));
        ops.writeObject(this.classifier);
        ops.close();
    }

    /**
     * @param classifierFile
     * @throws Exception
     */
    public void readClassifier(File classifierFile) throws java.lang.Exception {
        if (classifierFile == null)
            throw new Exception("");

        ObjectInputStream ips = new ObjectInputStream(new FileInputStream(
                classifierFile));
        classifier = null;
        this.classifier = (Classifier) ips.readObject();
        ips.close();
    }
}

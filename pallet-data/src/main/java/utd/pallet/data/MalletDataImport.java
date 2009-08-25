package utd.pallet.data;

import java.io.IOException;
import java.util.Iterator;

import cc.mallet.pipe.AugmentableFeatureVectorLogScale;
import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceRemoveHTML;
import cc.mallet.pipe.CharSequenceReplace;
import cc.mallet.pipe.FeatureSequence2AugmentableFeatureVector;
import cc.mallet.pipe.Input2CharSequence;
import cc.mallet.pipe.MakeAmpersandXMLFriendly;
import cc.mallet.pipe.Target2Label;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.types.Instance;

/**
 * @author Pralabh
 * 
 */
public class MalletDataImport {
    /**
     * An instance of BuildPipe1 is created which will be used to call the
     * CreatePipe method of the BuildPipe1 class.
     */
    private BuildPipe1 bpipe = null;

    /**
     * @param i2cs
     *            An instance of Input2CharSequence Pipe
     * @param csr
     *            An instance of CharSequenceReplace pipe
     * @param csrh
     *            An instance of CharSequenceRemoveHTML
     * @param cs2ts
     *            An instance of CharSequence2TokenSequence Pipe
     * @param tsrs
     *            An instance of TokenSequenceRemoveStopwords Pipe
     * @param maxf
     *            An instance of MakeAmpersandXMLFriendly Pipe
     * @param tokenseq2lowercase
     *            Boolean value.
     * @param ts2fs
     *            An instance of TokenSequence2FeatureSequence pipe
     * @param t2l
     *            An instance of Target2Label pipe
     * @param fs2afv
     *            An instance of FeatureSequence2AugmentableFeatureVector pipe
     * @param afv
     *            An instance of AugmentableFeatureVectorLogScale pipe
     * @param printinputandlabel
     *            Boolean Value
     * @throws IOException
     */
    public void createPipe(Input2CharSequence i2cs, CharSequenceReplace csr,
            CharSequenceRemoveHTML csrh, CharSequence2TokenSequence cs2ts,
            TokenSequenceRemoveStopwords tsrs, MakeAmpersandXMLFriendly maxf,
            boolean tokenseq2lowercase, TokenSequence2FeatureSequence ts2fs,
            Target2Label t2l, FeatureSequence2AugmentableFeatureVector fs2afv,
            AugmentableFeatureVectorLogScale afv, boolean printinputandlabel)
            throws IOException {

        this.bpipe = new BuildPipe1();
        this.bpipe
                .CreatePipe(i2cs, csr, csrh, cs2ts, tsrs, maxf,
                        tokenseq2lowercase, ts2fs, t2l, fs2afv, afv,
                        printinputandlabel);
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

}

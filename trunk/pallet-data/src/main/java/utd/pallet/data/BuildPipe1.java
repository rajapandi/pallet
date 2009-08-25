package utd.pallet.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import cc.mallet.pipe.AugmentableFeatureVectorLogScale;
import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceRemoveHTML;
import cc.mallet.pipe.CharSequenceReplace;
import cc.mallet.pipe.FeatureSequence2AugmentableFeatureVector;
import cc.mallet.pipe.Input2CharSequence;
import cc.mallet.pipe.MakeAmpersandXMLFriendly;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.PrintInputAndTarget;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.Target2Label;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

/**
 * @author pralabh
 * 
 */
public class BuildPipe1 {

    /**
     * A pipe instance is created
     */
    private Pipe pipe = null;
    /**
     * An InstanceList instance is created to add all the instances.
     */
    private InstanceList instancelist = null;

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
    @SuppressWarnings( { "unused" })
    public void CreatePipe(Input2CharSequence i2cs, CharSequenceReplace csr,
            CharSequenceRemoveHTML csrh, CharSequence2TokenSequence cs2ts,
            TokenSequenceRemoveStopwords tsrs, MakeAmpersandXMLFriendly maxf,
            boolean tokenseq2lowercase, TokenSequence2FeatureSequence ts2fs,
            Target2Label t2l, FeatureSequence2AugmentableFeatureVector fs2afv,
            AugmentableFeatureVectorLogScale afv, boolean printinputandlabel)
            throws IOException {

        ArrayList<Pipe> pipelist = new ArrayList<Pipe>();

        boolean res = pipelist.add(i2cs);

        if (csr != null)
            pipelist.add(csr);

        if (csrh != null)
            pipelist.add(csrh);

        if (cs2ts != null)
            pipelist.add(cs2ts);

        if (tsrs != null)
            pipelist.add(tsrs);

        if (maxf != null)
            pipelist.add(maxf);

        if (tokenseq2lowercase == true) {
            TokenSequenceLowercase lowercase = new TokenSequenceLowercase();
            pipelist.add(lowercase);
        }

        if (ts2fs != null)
            pipelist.add(ts2fs);

        if (t2l != null)
            pipelist.add(t2l);

        if ((fs2afv == null && ts2fs != null)
                || (fs2afv != null && ts2fs == null))
            throw new IllegalArgumentException(
                    "WrapperClassifier,BuildPipe: Invalid FeatureSequence2FeatureVector and/or TokenSequence2FeatureSequence parameter");

        if (fs2afv != null)
            pipelist.add(fs2afv);

        if (afv != null)
            pipelist.add(afv);

        if (printinputandlabel == true) {
            boolean add2 = pipelist.add(new PrintInputAndTarget());
        }

        pipe = new SerialPipes(pipelist);
        this.instancelist = new InstanceList(pipe);
    }

    /**
     * @param iterator
     *            It adds the instances to the Instance list.
     */
    public void addThruPipe(Iterator<Instance> iterator) {
        this.instancelist.addThruPipe(iterator);
    }

    /**
     * @return It return the Instancelist
     * 
     */
    public InstanceList GetInstanceList() {
        return this.instancelist;
    }
}
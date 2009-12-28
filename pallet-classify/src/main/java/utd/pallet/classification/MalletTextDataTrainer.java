package utd.pallet.classification;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cc.mallet.classify.Classifier;
import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.classify.NaiveBayes;
import cc.mallet.classify.NaiveBayesTrainer;
import cc.mallet.types.InstanceList;

/**
 * Creates classifier by training it on the data specified.
 * 
 * 
 * 
 */
public class MalletTextDataTrainer implements Serializable {

    /**
     * Used when no training algorithm is specified.
     */
    public static final int ALGO_UNASSIGNED = 0x00;

    /**
     * Naive Bayes Algorithm as trainer algorithm.
     */
    public static final int NAIVE_BAYES = 0x01;

    /**
     * Maximum Entropy Algorithm as trainer algorithm.
     */
    public static final int MAX_ENT = 0x02;

    /**
     * Decision trees as algorithm.
     */
    public static final int DECISION_TREES = 0x03;

    /**
     * Decision Trees with C4.5 as algorithm.
     */
    public static final int C_45 = 0x04;

    /**
     * BalancedWinnowing as algorithm.
     */
    public static final int BALANCED_WINNOW = 0x06;

    /**
     * Ranked Maximum Entropy as algorithm.
     */
    public static final int RANK_MAX_ENT = 0x07;

    /**
     * 
     */
    public static final int NAIVE_BAYES_EM = 0x08;

    /**
     * 
     */
    public static final int MAX_ENT_GE = 0x09;

    /**
     * 
     */
    public static final int MC_MAX_ENT = 0x0A;
    
    /** logger */
    private static Log logger = LogFactory.getLog(MalletTextDataTrainer.class);

    /**
     * Creates Trainer instance
     */
    public MalletTextDataTrainer() {
    }

    /**
     * Fetches the training algorithm to be used for incremental training based
     * on the instance of previous trainer.
     * 
     * @param trainer
     *            instance of ClassifierTrainer that is trained with data set
     *            using a particular algorithm.
     * 
     * @return Training algorithm used.
     * @throws java.lang.NullPointerException
     *             if trainer is null.
     * @throws java.lang.Exception
     *             if training algorithm is unknown.
     */
    @SuppressWarnings("unchecked")
    private int getTrainerAlgo(ClassifierTrainer trainer)
            throws java.lang.NullPointerException, java.lang.Exception {

        if (trainer == null)
            throw new IllegalArgumentException("Trainer not initialized because it is null");

        int trainerAlgo = ALGO_UNASSIGNED;

        if (trainer instanceof cc.mallet.classify.NaiveBayesTrainer)
            trainerAlgo = NAIVE_BAYES;
        else if (trainer instanceof cc.mallet.classify.MaxEntTrainer)
            trainerAlgo = MAX_ENT;
        else if (trainer instanceof cc.mallet.classify.DecisionTreeTrainer)
            trainerAlgo = DECISION_TREES;
        else if (trainer instanceof cc.mallet.classify.C45Trainer)
            trainerAlgo = C_45;
        else if (trainer instanceof cc.mallet.classify.BalancedWinnowTrainer)
            trainerAlgo = BALANCED_WINNOW;
        else if (trainer instanceof cc.mallet.classify.RankMaxEntTrainer)
            trainerAlgo = RANK_MAX_ENT;
        else if (trainer instanceof cc.mallet.classify.NaiveBayesEMTrainer)
            trainerAlgo = NAIVE_BAYES_EM;
        else if (trainer instanceof cc.mallet.classify.MaxEntGETrainer)
            trainerAlgo = MAX_ENT_GE;
        /*
         * else if (trainer instanceof cc.mallet.classify.MCMaxEntTrainer)
         * trainerAlgo = MC_MAX_ENT;
         */
        else
            throw new Exception("Unsupported algorithm");

        return trainerAlgo;

    }

    /**
     * Creates trainer from the factory based on the specified Training
     * algorithm.
     * 
     * @param trainingAlgo
     *            Algorithm that should be used to trainer.
     * 
     * @return Instance of ClassifierTrainer that should be used for Training.
     * @throws java.lang.Exception
     */
    @SuppressWarnings("unchecked")
    private ClassifierTrainer CreateTrainer(int trainingAlgo)
            throws java.lang.Exception {

        ClassifierTrainer trainer = null;

        switch (trainingAlgo) {
        case NAIVE_BAYES:
            trainer = MalletTrainerFactory.CreateNaiveBayesTrainer();
            break;
        case MAX_ENT:
            trainer = MalletTrainerFactory.CreateMaxEntTrainer();
            break;
        case DECISION_TREES:
            trainer = MalletTrainerFactory.CreateDecisionTreeTrainer();
            break;
        case C_45:
            trainer = MalletTrainerFactory.CreateC45Trainer();
            break;
        case BALANCED_WINNOW:
            trainer = MalletTrainerFactory.CreateBalancedWinnowTrainer();
            break;
        case RANK_MAX_ENT:
            trainer = MalletTrainerFactory.CreateRankMaxEntTrainer();
            break;
        case NAIVE_BAYES_EM:
            trainer = MalletTrainerFactory.CreateNaiveBayesEMTrainer();
            break;
        case MAX_ENT_GE:
            trainer = MalletTrainerFactory.CreateMaxEntGETrainer();
            break;
        /*
         * case MC_MAX_ENT: trainer =
         * MalletTrainerFactory.CreateMCMaxEntTrainer(); break;
         */
        default:
            throw new Exception("Unsupported Trainer Algorithm");
        }

        return trainer;
    }

    /**
     * Creates Trainer on the specified algorithm and Trains it with data
     * provided(as Instance Lists).
     * 
     * @param listToTrain
     *            Data that needs to be trained.
     * @param trainerAlgo
     *            Algorithm that needs to be used for training.
     * @return Instance of TrainerObject which contains the trainer that was
     *         trained and instance of classifier that was created by training
     *         of data.
     * @throws NullPointerException
     *             if InstanceList is null.
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public TrainerObject train(InstanceList listToTrain, int trainerAlgo)
            throws NullPointerException, Exception {

        if (listToTrain == null)
            throw new NullPointerException(
                    "Instance List to be trained is null");

        ClassifierTrainer trainer = null;
        try {
            trainer = CreateTrainer(trainerAlgo);
        } catch (Exception e) {

            throw new Exception("Failed to create Trainer");
        }
        Classifier cl = trainer.train(listToTrain);

        TrainerObject trnObject = new TrainerObject();
        try {
            trnObject.setTrainerObject(trainer, cl);
        } catch (Exception e) {

            throw new Exception("Failed to create TrainerObject");
        }

        return trnObject;
    }

    /**
     * Incrementally trains(updates) the trainer specified with the new set of
     * data specified.
     * 
     * @param prevTrainer
     *            Instance to Trainer that needs to be updated.
     * @param listToTrain
     *            New set of data with which the trainer specified needs to be
     *            trained.
     * @return Instance to TrainerObject that contains the updated trainer and
     *         the classifier.
     * @throws NullPointerException
     */
    @SuppressWarnings("unchecked")
    public TrainerObject trainIncremental(ClassifierTrainer<NaiveBayes> prevTrainer,
            InstanceList listToTrain) throws NullPointerException, Exception {

        if (prevTrainer == null)
            throw new IllegalArgumentException("cannot call incremental train on a null trainer");

        if (listToTrain == null) {
        	logger.warn("instance list was null, returning orginal trained model");
        }

        int trainerAlgo;
        try {
            trainerAlgo = getTrainerAlgo(prevTrainer);
        } catch (Exception e) {
        	throw new IllegalArgumentException("cannot call incremental train on a classifier trainer with no training algorithm");
        }

        Classifier cl = null;
        if (trainerAlgo == NAIVE_BAYES) {
            NaiveBayesTrainer nbTrainer = (NaiveBayesTrainer) prevTrainer;
            cl = nbTrainer.trainIncremental(listToTrain);
        } else {
            throw new IllegalArgumentException(
                    "Currently only Naive Bayes algorithm can be used for incremental training");
        }

        TrainerObject trnObject = new TrainerObject();

        try {
            trnObject.setTrainerObject(prevTrainer, cl);
        } catch (Exception e) {

            throw new Exception("Failed to create TrainerObject");
        }

        return trnObject;
    }

    @SuppressWarnings("unchecked")
    /*
     * Encapsulates the trainer and classifier created in the process of
     * Training the data on mallet classifiers.
     */
    public class TrainerObject implements Serializable {
        private Classifier classifier = null;
        private ClassifierTrainer trainer = null;

        public TrainerObject() {
        }

        /**
         * 
         * @param trainerCreated
         *            trainer that was created as a process of training the
         *            classifier.
         * @param cl
         *            Instance of classifier that was created due to training.
         * @throws java.lang.Exception
         */
        public void setTrainerObject(ClassifierTrainer trainerCreated, Classifier cl)
                throws java.lang.Exception {

            if (trainerCreated == null || cl == null)
                throw new Exception(
                        "Trainer or Classifier to be set is null pointer");

            trainer = trainerCreated;
            classifier = cl;
        }

        /**
         * 
         * @return Instance of trainer that was saved.
         */
        public ClassifierTrainer getTrainer() {
            return trainer;
        }

        /**
         * 
         * @return Instance of Classifier that was saved.
         */
        public Classifier getClassifier() {
            return classifier;
        }
    }
}

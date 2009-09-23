package utd.pallet.classification;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;

import utd.pallet.classification.MalletTextDataTrainer.TrainerObject;
import utd.pallet.data.JenaModelFactory;
import cc.mallet.classify.Classifier;
import cc.mallet.types.InstanceList;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;

/**
 * 
 *
 */
public class BlackbookSimUtils {

    public static String testURI = "http://localhost:8443/blackbook/malletModel";

    /**
     * @param rdf
     * @return
     * @throws Exception
     */
    public static Classifier convertRDFToClassifier(String rdf)
            throws Exception {

        Model model = ModelFactory.createDefaultModel();
        ByteArrayInputStream bisModel = new ByteArrayInputStream(rdf.getBytes());
        model.read(bisModel, "RDF/XML");

        StmtIterator stmtItr = model.listStatements((Resource) null,
                OWL.hasValue, (RDFNode) null);
        Statement onlyStmt = stmtItr.nextStatement();

        ByteArrayInputStream bisLiteral = new ByteArrayInputStream(
                (byte[]) onlyStmt.getLiteral().getValue());
        ObjectInputStream ois = new ObjectInputStream(bisLiteral);
        Classifier classifier = (Classifier) ois.readObject();

        return classifier;

    }

    public static TrainerObject convertRDFToTrainerObj(String rdf)
            throws Exception {

        Model model = ModelFactory.createDefaultModel();
        ByteArrayInputStream bisModel = new ByteArrayInputStream(rdf.getBytes());
        model.read(bisModel, "RDF/XML");

        StmtIterator stmtItr = model.listStatements((Resource) null,
                OWL.hasValue, (RDFNode) null);
        Statement onlyStmt = stmtItr.nextStatement();

        ByteArrayInputStream bisLiteral = new ByteArrayInputStream(
                (byte[]) onlyStmt.getLiteral().getValue());
        ObjectInputStream ois = new ObjectInputStream(bisLiteral);
        TrainerObject trainerObject = (TrainerObject) ois.readObject();

        return trainerObject;

    }

    public static TrainerObject trainMalletModel(InstanceList iList) {
        MalletTextDataTrainer bTrainer = new MalletTextDataTrainer();

        TrainerObject trnObj = null;
        try {
            trnObj = bTrainer.train(iList, MalletTextDataTrainer.NAIVE_BAYES);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return trnObj;
    }

    public static String convertClassifierToRDF(Classifier classifier)
            throws Exception {
        Model model = ModelFactory.createDefaultModel();
        Resource res = model.createResource(new URL(testURI).toString());
        Property prop = OWL.hasValue;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(classifier);

        Literal obj = model.createTypedLiteral(bos.toByteArray());

        Statement stmt = model.createLiteralStatement(res, prop, obj);
        model.add(stmt);

        String ret = JenaModelFactory.serializeModel(model, "RDF/XML");

        // log.error("converted classifier to rdf:");

        return ret;
    }

    public static String convertTrainerObjectToRDF(TrainerObject trnObj)
            throws Exception {

        Model model = ModelFactory.createDefaultModel();

        Resource res = model.createResource(new URL(testURI).toString());
        Property prop = OWL.hasValue;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(trnObj);

        Literal obj = model.createTypedLiteral(bos.toByteArray());

        Statement stmt = model.createLiteralStatement(res, prop, obj);
        model.add(stmt);

        String ret = JenaModelFactory.serializeModel(model, "RDF/XML");

        return ret;
    }

    public static void saveTrainerObject(TrainerObject trnObj, String fileName)
            throws Exception, NullPointerException {

        if (trnObj == null || fileName == null) {
            throw new NullPointerException("Invalid data to saveTrainerObject");
        }
        Model model = ModelFactory.createDefaultModel();
        Resource res = model.createResource(new URL(testURI).toString());
        Property prop = OWL.hasValue;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(trnObj);

        Literal obj = model.createTypedLiteral(bos.toByteArray());

        Statement stmt = model.createLiteralStatement(res, prop, obj);
        model.add(stmt);
        File file = new File(fileName);
        FileWriter writer = new FileWriter(file);
        BufferedWriter bWriter = new BufferedWriter(writer);

        System.out.println(fileName);
        model.write(writer, "RDF/XML");

    }

    public static TrainerObject fetchTrainerObject(String filename)
            throws Exception, NullPointerException, FileNotFoundException,
            ClassNotFoundException, IOException {

        File file = new File(filename);
        FileReader reader;
        try {
            reader = new FileReader(file);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw e;
        }
        BufferedReader bReader = new BufferedReader(reader);

        Model model = ModelFactory.createDefaultModel();
        model.read(bReader, testURI, "RDF/XML");

        StmtIterator iter = model.listStatements();
        Statement stmt = iter.nextStatement(); // get next statement
        RDFNode object = stmt.getObject(); // get the object

        byte[] data = (byte[]) object.asNode().getLiteralValue();
        ByteArrayInputStream ios = new ByteArrayInputStream(data);

        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(ios);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw e;
        }

        TrainerObject trnObj = null;
        try {
            trnObj = (TrainerObject) ois.readObject();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw e;
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw e;
        }

        return trnObj;
    }

    public static Classifier getClassifier(String SourceName)
            throws FileNotFoundException, IOException, Exception {
        Classifier classifier = null;

        File file = new File(SourceName);
        FileReader fileReader;
        try {
            fileReader = new FileReader(file);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw e;
        }

        BufferedReader reader = new BufferedReader(fileReader);

        String line;
        String rdfData = "";
        try {
            while (null != (line = reader.readLine())) {
                rdfData += line;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw e;
        }

        try {
            classifier = convertRDFToClassifier(rdfData);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw e;
        }

        return classifier;
    }
}

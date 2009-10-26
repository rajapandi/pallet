package utd.pallet.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringWriter;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * This is a utility class which contains two important static methods
 * 1)serializeModel(Model model, String format) : It returns the serialized form
 * of Jena Model in String format 2)rdf2Model(String fileName) : It returns the
 * Jena Model taking in input as the RDF/XML file.
 * 
 */
public class JenaModelFactory {

    /**
     * @param model
     *            : It receives the Model which is to be converted into String
     * @param format
     *            : It receives the format of the jena Model
     * @return : It returns the Jena model in String form.
     * @throws Exception
     *             : It throws an exception
     */
    static public String serializeModel(Model model, String format)
            throws Exception {
        try {

            StringWriter sw = new StringWriter();
            model.write(sw, format);
            String rdf = sw.toString();
            return rdf;

        } catch (Exception e) {
            throw e;
        }

    }

    /**
     * @param fileName
     *            Name of the input RDF file name
     * @return It returns Jena model
     * @throws Exception
     */
    static public Model rdf2Model(String fileName) throws Exception {
        Model model = null;
        try {

            File fread = new File(fileName);
            FileReader fr = new FileReader(fread);
            BufferedReader br = new BufferedReader(fr);
            model = ModelFactory.createDefaultModel();
            model.read(br, null, "RDF/XML");
        } catch (Exception e) {
            throw e;
        }
        return model;
    }
}
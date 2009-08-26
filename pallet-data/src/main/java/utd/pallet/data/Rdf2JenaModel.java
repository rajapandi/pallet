package utd.pallet.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * This class takes RDf model from the file and create the jena model.
 * 
 */
public class Rdf2JenaModel {
    /**
     * @param fileName
     *            It is the file which contains the data in RDF format.
     * @return Model which is created by reading the file.
     * @throws Exception
     *             This method can throws an Exception
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

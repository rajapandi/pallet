package utd.pallet.data;

import java.io.StringWriter;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * 
 * 
 */
public class JenaModelFactory {

    /**
     * @param model
     *            : It receives the Model which is to be converted into String
     * @param format
     *            : It receives the format of the jena Model
     * @return : It returns the jena model in String form.
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

}

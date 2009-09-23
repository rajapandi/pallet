package utd.pallet.classification;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;

/**
 * 
 * 
 */
public class MalletTBox {
    protected static final String uri = "http://example.org/#";

    public static String getURI() {
        return uri;
    }

    private static Model m = ModelFactory.createDefaultModel();

    public static final Property hasName = m.createProperty(uri, "hasName");
    public static final Property hasSrc = m.createProperty(uri, "hasSrc");
    public static final Property hasTarget = m.createProperty(uri, "hasTarget");
    public static final Property hasData = m.createProperty(uri, "hasData");
}

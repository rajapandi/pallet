package pallet.blackbook.util;

import java.util.HashSet;
import java.util.Set;

import security.ejb.client.User;
import utd.pallet.data.RDFUtils;
import blackbook.jena.util.JenaModelFactory;
import blackbook.metadata.api.AlgorithmMetadata;
import blackbook.metadata.api.DataSourceMetadata;
import blackbook.metadata.api.MetadataManagerIfc;
import blackbook.metadata.manager.MetadataManagerFactory;

import com.hp.hpl.jena.rdf.model.Model;

public class BlackbookUtil {

	public static Model persist2BlackbookAssertions(Model model,
			String dsName, User user) throws Exception {

		MetadataManagerIfc mm = MetadataManagerFactory.getInstance();
		mm.createAssertionsDS(dsName);

		DataSourceMetadata dsm = mm.getDataSourceMetadataByName(dsName);

		Set<String> namespace = new HashSet<String>();
		namespace.add(RDFUtils.MALLET_NAMESPACE);
		dsm.setNamespace(namespace);
		dsm.setMaxStatements(1000000);
		dsm.setMaxUris(1000000);

		AlgorithmMetadata alg = mm.getAlgorithmMetadata("Jena Expand");
		dsm.setExpandAlgorithm(alg);
		AlgorithmMetadata alg2 = mm.getAlgorithmMetadata("Jena and Lucene Persist");
		dsm.setPersistAlgorithm(alg2);

		mm.modifyMetadataObject(dsm);

		Model newModel = JenaModelFactory.openModelByName(dsName, user);
		newModel.add(model);
		
		return newModel;
	}

}

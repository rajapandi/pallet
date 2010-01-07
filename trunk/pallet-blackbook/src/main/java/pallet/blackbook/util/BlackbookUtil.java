package pallet.blackbook.util;

import java.util.HashSet;
import java.util.Set;

import security.ejb.client.User;
import blackbook.jena.util.JenaModelFactory;
import blackbook.metadata.api.AlgorithmMetadata;
import blackbook.metadata.api.DataSourceMetadata;
import blackbook.metadata.api.MetadataManagerIfc;
import blackbook.metadata.manager.MetadataManagerFactory;

import com.hp.hpl.jena.rdf.model.Model;

public class BlackbookUtil {

	/**
	 * persists the model provided to Blackbook using the data source name provided.
	 * the namespace will be set to the one provided.
	 * @param model
	 * @param dsName
	 * @param namespace
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public static Model persist2BlackbookAssertions(Model model,
			String dsName, String namespace, User user) throws Exception {

		MetadataManagerIfc mm = MetadataManagerFactory.getInstance();
		mm.createAssertionsDS(dsName);

		DataSourceMetadata dsm = mm.getDataSourceMetadataByName(dsName);

		Set<String> namespaces = new HashSet<String>();
		namespaces.add(namespace);
		dsm.setNamespace(namespaces);
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

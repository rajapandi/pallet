package pallet.blackbook.util;

import java.util.HashSet;
import java.util.Set;

import security.ejb.client.User;
import blackbook.jena.util.JenaModelFactory;
import blackbook.metadata.api.AlgorithmMetadata;
import blackbook.metadata.api.DataSourceMetadata;
import blackbook.metadata.manager.MetadataManagerFactory;

import com.hp.hpl.jena.rdf.model.Model;

public class BlackbookUtil {

	public static Model persist2BlackbookAssertions(Model model,
			String dsName, User user) throws Exception {

		MetadataManagerFactory.getInstance().createAssertionsDS(dsName);

		DataSourceMetadata dsm = MetadataManagerFactory.getInstance()
				.getDataSourceMetadataByName(dsName);

		Set<String> namespace = new HashSet<String>();
		namespace.add("urn:mallet:");
		dsm.setNamespace(namespace);

		AlgorithmMetadata alg = MetadataManagerFactory.getInstance()
				.getAlgorithmMetadata("Jena Expand");
		alg.setClassName("blackbook.algorithm.jena.JenaExpand");
		alg.setName("Jena Expand");
		dsm.setExpandAlgorithm(alg);
		AlgorithmMetadata alg2 = MetadataManagerFactory.getInstance()
				.getAlgorithmMetadata("Jena and Lucene Persist");
		alg.setClassName("blackbook.algorithm.jena.JenaAndLucenePersist");
		alg.setName("Jena and Lucene Persist");
		dsm.setPersistAlgorithm(alg2);

		MetadataManagerFactory.getInstance().modifyMetadataObject(dsm);

		Model newModel = JenaModelFactory.openModelByName(dsName, user);
		newModel.add(model);
		
		return newModel;
	}

}

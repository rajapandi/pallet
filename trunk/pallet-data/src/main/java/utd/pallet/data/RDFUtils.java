package utd.pallet.data;

import java.util.*;

import cc.mallet.classify.Classification;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class RDFUtils {
 
	public static Model createModelWithClassifications (ArrayList<MalletAccuracyVector>  accVector,
		
			ArrayList<Classification> classificationList) {
		@SuppressWarnings("unused")
		Model rdfModel = ModelFactory.createDefaultModel();
		try{
		
		String uriAddress="http://marathonminds.com//MalletClassification//";
		
		int i=0;
		for (MalletAccuracyVector av : accVector) {
			String name = (String)av.getName();
		    HashMap <String,Double> labelVector = av.getAccuracyVector ();
		    Iterator<String> iterator = labelVector.keySet().iterator();
		    String bestLabel = av.getBestLabel ();
		    
		    Resource originalResource=rdfModel.createResource(name);
			Resource bestLabelResource=rdfModel.createResource(uriAddress+bestLabel+i);
			Property bestLabelProperty=rdfModel.createProperty(uriAddress+"#hasBestLabel");
			originalResource.addProperty(bestLabelProperty, bestLabelResource);
			while(iterator.hasNext())
			{
				String Label=iterator.next();
				Double confidencValue=labelVector.get(Label);
				Property malletConfidenceValue=rdfModel.createProperty(uriAddress+"#MalletConfidenceValue");
				Resource labelResource=rdfModel.createResource(uriAddress+Label+i);
				Property associatedWith=rdfModel.createProperty(uriAddress+"#associatedWith");
				Property hasValue=rdfModel.createProperty(uriAddress+"#hasValue");
				labelResource.addProperty(malletConfidenceValue, confidencValue.toString());
			    labelResource.addProperty(associatedWith, originalResource);
			    labelResource.addProperty(hasValue, Label);
			}
			i++;
		    
			
		}
		return rdfModel;
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
		
		
		return rdfModel;		
	}
	
} 

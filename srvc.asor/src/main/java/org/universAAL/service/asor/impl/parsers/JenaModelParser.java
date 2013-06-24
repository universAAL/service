/**
 * 
 *  OCO Source Materials 
 *      © Copyright IBM Corp. 2012 
 *
 *      See the NOTICE file distributed with this work for additional 
 *      information regarding copyright ownership 
 *       
 *      Licensed under the Apache License, Version 2.0 (the "License"); 
 *      you may not use this file except in compliance with the License. 
 *      You may obtain a copy of the License at 
 *       	http://www.apache.org/licenses/LICENSE-2.0 
 *       
 *      Unless required by applicable law or agreed to in writing, software 
 *      distributed under the License is distributed on an "AS IS" BASIS, 
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *      See the License for the specific language governing permissions and 
 *      limitations under the License. 
 *
 */
package org.universAAL.service.asor.impl.parsers;

import impl.jena.OWLIndividualImpl;
import impl.jena.OWLKnowledgeBaseImpl;
import impl.jena.OWLOntologyImpl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.mindswap.owl.OWLKnowledgeBase;
import org.mindswap.owl.OWLValue;
import org.mindswap.owls.process.variable.Output;
import org.mindswap.query.ValueMap;
import org.mindswap.utils.URIUtils;

import com.hp.hpl.jena.n3.turtle.TurtleParseException;
import com.hp.hpl.jena.n3.turtle.TurtleReader;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;


/**
 * 
 *  @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 *	
 *  Sep 10, 2012
 *
 */
public class JenaModelParser {

	public static OntModel createModel(String resourceAsStr) {
		InputStream is = new ByteArrayInputStream(resourceAsStr.getBytes());
		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		
        RDFReader reader = new TurtleReader() ;
        try {
        	reader.read(model, is, "");
        } 
        catch (TurtleParseException ex) {
            ex.printStackTrace();    
        }
        
        return model;
	}

	public static void addOutputToMap(OntModel model, OWLKnowledgeBase owlKnowledgeBase,
			String outputURI, ValueMap<Output, OWLValue> map) {
		
		OWLOntologyImpl ontModel = new OWLOntologyImpl((OWLKnowledgeBaseImpl) owlKnowledgeBase, null, model);
		
		// Should be only one
		ResIterator iter = model.listSubjectsWithProperty(RDF.type, RDF.List);
		if(iter.hasNext()) {
        	// Transform to RDFList
			com.hp.hpl.jena.rdf.model.Resource resource = iter.next();
        	RDFList rdfList = null;
        	if (resource.canAs(RDFList.class)) {
        		rdfList = resource.as(RDFList.class);
        		// Create the output
            	Output output = owlKnowledgeBase.createOutput(URIUtils.createURI(outputURI));

            	// Transform to OWLValue list
            	List<OWLValue> owlValuesList = new ArrayList<OWLValue>();
            	ExtendedIterator<RDFNode> listElementsIt = rdfList.iterator();
            	while (listElementsIt.hasNext()) {
            		owlValuesList.add(new OWLIndividualImpl(ontModel, listElementsIt.next().asResource()));
            	}
            	// Add the last element to be nil
            	owlValuesList.add(new OWLIndividualImpl(ontModel, RDF.nil));
            	
            	// Create the list as OWLValue
            	OWLValue val = ontModel.createList(org.mindswap.owl.vocabulary.RDF.ListVocabulary, owlValuesList);
            	
            	// Add to the map
            	map.setValue(output, val);
        	} 
        }
	}
}

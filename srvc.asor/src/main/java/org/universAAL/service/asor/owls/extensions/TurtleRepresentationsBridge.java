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
package org.universAAL.service.asor.owls.extensions;

import impl.jena.OWLIndividualImpl;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.mindswap.owl.OWLEntity;
import org.mindswap.owl.OWLValue;
import org.mindswap.owls.process.execution.ExecutionContext;
import org.mindswap.owls.process.variable.Input;
import org.mindswap.owls.vocabulary.OWLS;
import org.mindswap.query.ValueMap;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelExtract;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StatementBoundaryBase;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;


/**
 * 
 *  @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 *	
 *  Aug 28, 2012
 *
 */
public class TurtleRepresentationsBridge {

	private static final String writerType = "TURTLE";
	
	private static final String process11URI = "http://www.daml.org/services/owl-s/1.1/Process.owl#";
	
	// Subjects to remove from the turtle
	private static final String[] subjectsAsStringToRemove = new String[] {
		process11URI + "Parameter"
	};

	
	// Properties to remove from the turtle
	private static final String[] propertiesAsStringToRemove =  new String[] {
		process11URI + "valueSpecifier",
		OWL.sameAs.toString(),
		OWL.equivalentProperty.toString(),
		OWL.equivalentClass.toString(),
		RDFS.subPropertyOf.toString(),
		OWL2.propertyDisjointWith.toString()
//		OWL2.propertyDisjointWith.toString(),
//		OWL2.differentFrom.toString()
	};
	
	// Objects to remove from the turtle
	private static final String[] objectsAsStringToRemove = new String[] { 
			"http://www.w3.org/2002/07/owl#Thing",
			process11URI + "Parameter",
			process11URI + "Binding",
			"http://www.w3.org/2003/11/swrl#Variable",
			OWL.Class.toString(),
			OWL.Nothing.toString(),
			OWL.ObjectProperty.toString()
	};
	
	private static final String[] statementsToRemoveFromLists = new String [] {
		"http://www.daml.org/services/owl-s/1.1/Process.owl#OutputBinding",
		"http://www.daml.org/services/owl-s/1.1/generic/Expression.owl#Expression"
	};
	
	public static <C extends ExecutionContext> String toTurtle(OWLIndividualImpl ind, C context) {
		// extract the blank node closure rooted at this' resource from the associated KB model
		final Model defaultModel = ModelFactory.createDefaultModel();
		final OntModel closureModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		
		final Model ontModel = ind.getKB().getImplementation();
		
		// Write the ontology to the closureModel
		new ModelExtract(new Boundary()).extractInto(closureModel, ind.getImplementation(), ontModel);
		
		final StringWriter sw = new StringWriter();
		final RDFWriter w = ontModel.getWriter(writerType);
		
		handleBindings(context, defaultModel, closureModel);
		
		// Filter subjects
		filterSubjects(closureModel, defaultModel);
		
		// Filter properties
		filterProperties(closureModel, defaultModel);
		
		// Filter objects
		filterObjects(closureModel, defaultModel);
		
		// filter the statements from the lists
		filterStatementsFromLists(closureModel, defaultModel);
		
		// Write it to the StringWriter
		w.write(closureModel, sw, null);
		
		String value = sw.toString();
		
		return value;
	}

	private static <C extends ExecutionContext> void handleBindings(C context, final Model defaultModel,
			final OntModel closureModel) {
		ValueMap<Input, OWLValue> inputsMap = context.getInputs();
		if (null != inputsMap) {
			Iterator<Entry<Input, OWLValue>> inputsMapIt = inputsMap.iterator();
			while (inputsMapIt.hasNext()) {
				Entry<Input, OWLValue> entry = inputsMapIt.next();
				
				// Get the resource (the target of the binding)
				Resource resource = (Resource) entry.getKey().getImplementation();
				String resourceURI = resource.getURI();
				
				// Search for all statements with object that is equal to the resourceURI
				handleBinding(defaultModel, closureModel, entry, resourceURI);
			}
		}
	}

	private static void handleBinding(
			final Model defaultModel,
			final OntModel closureModel, 
			Entry<Input, OWLValue> entry,
			String resourceURI) {
		
		String sourceURI = replaceObjectsWithBinding(defaultModel,
				closureModel, entry, resourceURI);
		
		replaceSubjectsWithBinding(defaultModel, closureModel, resourceURI,
				sourceURI);
	}

	private static String replaceObjectsWithBinding(final Model defaultModel,
			final OntModel closureModel, Entry<Input, OWLValue> entry,
			String resourceURI) {
		StmtIterator statementsIt = 
			closureModel.listStatements(null, OWL.hasValue, defaultModel.createResource(resourceURI));
		String sourceURI = ((OWLEntity) entry.getValue()).getURI().toString();
		List<Statement> statementsToAdd = new ArrayList<Statement>();
		List<Statement> statementsToRemove = new ArrayList<Statement>();
		while (statementsIt.hasNext()) {
			Statement statement = statementsIt.next();
			statementsToRemove.add(statement);
			statementsToAdd.add(
					closureModel.createStatement(
							statement.getSubject(), 
							statement.getPredicate(), 
							defaultModel.createResource(sourceURI)));
		}
		// Remove statements
		for (Statement statementToRemove : statementsToRemove) {
			closureModel.remove(statementToRemove);
		}
		// Add all statements
		for (Statement statementToAdd : statementsToAdd) {
			closureModel.add(statementToAdd);
		}
		return sourceURI;
	}
	
	private static void replaceSubjectsWithBinding(
			final Model defaultModel,
			final OntModel closureModel, 
			String resourceURI, 
			String sourceURI) {
		StmtIterator statementsIt = 
			closureModel.listStatements(defaultModel.createResource(resourceURI), (Property) null, (RDFNode) null);
		List<Statement> statementsToAdd = new ArrayList<Statement>();
		List<Statement> statementsToRemove = new ArrayList<Statement>();
		while (statementsIt.hasNext()) {
			Statement statement = statementsIt.next();
			statementsToRemove.add(statement);
			statementsToAdd.add(
					closureModel.createStatement(
							defaultModel.createResource(sourceURI), 
							statement.getPredicate(), 
							statement.getObject()));
		}
		// Remove statements
		for (Statement statementToRemove : statementsToRemove) {
			closureModel.remove(statementToRemove);
		}
		// Add all statements
		for (Statement statementToAdd : statementsToAdd) {
			closureModel.add(statementToAdd);
		}
	}
	
	private static void filterSubjects(OntModel closureModel, Model defaultModel) {
		for (String subjectToRemove : subjectsAsStringToRemove) {
			closureModel.removeAll(defaultModel.createResource(subjectToRemove), null, null);
		}
	}

	private static void filterProperties(OntModel closureModel, Model defaultModel) {
		for (String propertyToRemove : propertiesAsStringToRemove) {
			closureModel.removeAll(null, defaultModel.createProperty(propertyToRemove), null);
		}
	}
	
	private static void filterObjects(OntModel closureModel, Model defaultModel) {
		for (String objectToRemove : objectsAsStringToRemove) {
			closureModel.removeAll(null, null, defaultModel.createResource(objectToRemove));
		}
	}
	
	private static void filterStatementsFromLists(OntModel closureModel,
			Model defaultModel) {
		for (String objectURI : statementsToRemoveFromLists) {
			filterStatementsWithClassDefinitionFromLists(closureModel, defaultModel, objectURI);
		}
	}
	
	private static void filterStatementsWithClassDefinitionFromLists(
			OntModel closureModel, 
			Model defaultModel, 
			String objectURI) {
		ResIterator ni = 
			closureModel.listSubjectsWithProperty(RDF.first);
		while (ni.hasNext()) {
			Resource rs = ni.next();

			StmtIterator si =
				closureModel.listStatements(
						rs, 
						RDF.type, 
						defaultModel.createResource(objectURI));
			closureModel.remove(si);
			
//			ResIterator iter = closureModel.listSubjectsWithProperty(RDF.type, RDF.List);
//			iter.next();
			
		}
	}
	
	static final class Boundary extends StatementBoundaryBase {
		@Override
		public boolean stopAt(final Statement s) {
			boolean stop = false;
			
			final RDFNode stmtObject = s.getObject();
			
			if (stmtObject.isURIResource()) {
				String statementURI = stmtObject.asResource().getURI();
				
				stop = (statementURI.startsWith(OWL.getURI()) || 
						statementURI.startsWith(RDF.getURI()) || 
						statementURI.startsWith(OWLS.base));
			} else {
				stop = stmtObject.isLiteral();
			}
			
			return stop;
		}
	}
}

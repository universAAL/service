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
package org.universAAL.service.asor.owls.extensions.tests;

import java.io.IOException;

import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.service.asor.impl.ASORServiceMngr;


/**
 * 
 *  @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 *	
 *  Aug 28, 2012
 *
 */
public class TestTurtleRepresentationBridge {

	private final static String defaultFilePath = "d:/ServiceRequest.txt";
	
	public static void testTurtleFromFile() {
		testTurtleFromFile(defaultFilePath);
	}
	
	public static void testTurtleFromFile(String filePath) {
		String fileContent = "";
		try {
			fileContent = FileReader.readFile(filePath);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		ServiceRequest sr = 
			(ServiceRequest) ASORServiceMngr.getInstance().getMessageContentSerializer().deserialize(fileContent);
		System.out.println("Serialize after De-serialize:");
		System.out.println(ASORServiceMngr.getInstance().getMessageContentSerializer().serialize(sr));
		
		System.out.println(sr.hashCode());
	}
	
	public static void testTurtleParserWorkingOne() {
		String serviceRequestAsStr = new StringBuffer().
		append("@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n").
		append("@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n").
		append("@prefix list: <http://www.daml.org/services/owl-s/1.2/generic/ObjectList.owl#> .\n").
		append("@prefix owl: <http://www.w3.org/2002/07/owl#> .\n").
		append("@prefix expr: <http://www.daml.org/services/owl-s/1.2/generic/Expression.owl#> .\n").
		append("@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n").
		append("@prefix process11: <http://www.daml.org/services/owl-s/1.1/Process.owl#> .\n").
		append("@prefix pvn: <http://ontology.universAAL.org/uAAL.owl#> .\n").
		append("@prefix ns: <http://ontology.igd.fhg.de/LightingConsumer.owl#> .\n").
		append("@prefix : <http://ontology.universAAL.org/uAALASOR.owl#> .\n").	
		append(":GetLampsServiceRequest a pvn:ServiceRequest ;\n").
		append("pvn:requiredResult [\n").
		append("process11:withOutput [\n").
//		append("[\n").
		append("a process11:OutputBinding ;\n").
		append("process11:toParam ns:controlledLamps ;\n").
		append("process11:valueForm \"\"\"\n").
		append("@prefix : <http://ontology.universAAL.org/Service.owl#> .\n").
		append("_:BN000000 a :PropertyPath ;\n").
		append(":thePath (\n").
		append("<http://ontology.universaal.org/Lighting.owl#controls>\n").
		append(") .\n").
		append("\"\"\"^^<http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral>\n").
		append("] ;\n").
//		append(") ;\n").
		append("a process11:Result\n").
		append("] ;\n").
		append("pvn:requestedService [\n").
		append("a <http://ontology.universaal.org/Lighting.owl#Lighting>\n").
		append("] .\n").
		append("ns:controlledLamps a process11:Output .\n").toString();
		
		ServiceRequest sr = 
			(ServiceRequest) ASORServiceMngr.getInstance().getMessageContentSerializer().deserialize(serviceRequestAsStr);
		System.out.println("Serialize after De-serialize:");
		System.out.println(ASORServiceMngr.getInstance().getMessageContentSerializer().serialize(sr));
		
		System.out.println(sr.hashCode());
	}
	
	public static void testTurtleParser() {
		String serviceRequestAsStr = new StringBuffer().
		append("@prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n").
		append("@prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n").
		append("@prefix owl:     <http://www.w3.org/2002/07/owl#> .\n").
		append("@prefix xsd:     <http://www.w3.org/2001/XMLSchema#> .\n").
		append("@prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> .\n").

		append("<d:/universAAL/workspaces/ServiceOrchestrator/ASOR_Scripts/TestASORCallService.ttl#GetLampsServiceRequest>\n").
		append("a       <http://ontology.universAAL.org/uAAL.owl#ServiceRequest> ;\n").
		append("<http://ontology.universAAL.org/uAAL.owl#requestedService>\n").
		append("[ a       <http://ontology.universaal.org/Lighting.owl#Lighting>\n").
		append("] ;\n").
		append("<http://ontology.universAAL.org/uAAL.owl#requiredResult>\n").
		append("[ a       <http://www.daml.org/services/owl-s/1.1/Process.owl#Result> ;\n").
		append("<http://www.daml.org/services/owl-s/1.1/Process.owl#withOutput>\n").
		append("[ a       <http://www.daml.org/services/owl-s/1.1/Process.owl#OutputBinding> ;\n").
		append("<http://www.daml.org/services/owl-s/1.1/Process.owl#toParam>\n").
		append("[ a       <http://www.daml.org/services/owl-s/1.1/Process.owl#Output>\n").
		append("] ;\n").
		append("<http://www.daml.org/services/owl-s/1.1/Process.owl#valueForm>\n").
		append("\"\"\"\n").
		append("@prefix : <http://ontology.universAAL.org/Service.owl#> .\n").
		append("_:BN000000 a :PropertyPath ;\n").
		append(":thePath (\n").
		append("<http://ontology.universaal.org/Lighting.owl#controls>\n").
		append(") .\n").
		append("\"\"\"^^rdf:XMLLiteral\n").
		append("]\n").toString();

		
		ServiceRequest sr = 
			(ServiceRequest) ASORServiceMngr.getInstance().getMessageContentSerializer().deserialize(serviceRequestAsStr);
		System.out.println("Serialize after De-serialize:");
		System.out.println(ASORServiceMngr.getInstance().getMessageContentSerializer().serialize(sr));
		
		System.out.println(sr.hashCode());
	}
	
	public static void testTurtleParserWorkingOne2() {
		String serviceRequestAsStr = new StringBuffer().
		append("@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n").
		append("@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n").
		append("@prefix list: <http://www.daml.org/services/owl-s/1.2/generic/ObjectList.owl#> .\n").
		append("@prefix owl: <http://www.w3.org/2002/07/owl#> .\n").
		append("@prefix expr: <http://www.daml.org/services/owl-s/1.2/generic/Expression.owl#> .\n").
		append("@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n").
		append("@prefix process11: <http://www.daml.org/services/owl-s/1.1/Process.owl#> .\n").
		append("@prefix pvn: <http://ontology.universAAL.org/uAAL.owl#> .\n").
		append("@prefix ns: <http://ontology.igd.fhg.de/LightingConsumer.owl#> .\n").
		append("@prefix : <http://ontology.universAAL.org/uAALASOR.owl#> .\n").	
		append(":GetLampsServiceRequest a pvn:ServiceRequest ;\n").
		append("pvn:requiredResult [\n").
		append("process11:withOutput (\n").
		append("[\n").
		append("a process11:OutputBinding ;\n").
		append("process11:toParam ns:controlledLamps ;\n").
		append("process11:valueForm \"\"\"\n").
		append("@prefix : <http://ontology.universAAL.org/Service.owl#> .\n").
		append("_:BN000000 a :PropertyPath ;\n").
		append(":thePath (\n").
		append("<http://ontology.universaal.org/Lighting.owl#controls>\n").
		append(") .\n").
		append("\"\"\"^^<http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral>\n").
		append("]\n").
		append(") ;\n").
		append("a process11:Result\n").
		append("] ;\n").
		append("pvn:requestedService [\n").
		append("a <http://ontology.universaal.org/Lighting.owl#Lighting>\n").
		append("] .\n").
		append("ns:controlledLamps a process11:Output .\n").toString();
		
		ServiceRequest sr = 
			(ServiceRequest) ASORServiceMngr.getInstance().getMessageContentSerializer().deserialize(serviceRequestAsStr);
		System.out.println("Serialize after De-serialize:");
		System.out.println(ASORServiceMngr.getInstance().getMessageContentSerializer().serialize(sr));
		
		System.out.println(sr.hashCode());
	}
}

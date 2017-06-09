/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package org.universAAL.service.asor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.universAAL.middleware.owl.OntClassInfo;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.ontology.asor.LanguageClassifier;
import org.universAAL.ontology.asor.ScriptEngine;

@SuppressWarnings("restriction")
public class EngineInfo {

	public static List<ScriptEngine> getEngines() {
		ScriptEngineManager scriptManager = new ScriptEngineManager();
		List<ScriptEngineFactory> scriptFactories = scriptManager.getEngineFactories();

		List<ScriptEngine> retVal = new ArrayList<ScriptEngine>(scriptFactories.size());

		for (ScriptEngineFactory factory : scriptFactories) {
			ScriptEngine se = new ScriptEngine();

			se.setName(factory.getEngineName());
			se.setVersion(factory.getEngineVersion());
			se.setLanguageVersion(factory.getLanguageVersion());
			se.setMimeTypes(factory.getMimeTypes().toArray(new String[0]));
			se.setFileExtensions(factory.getExtensions().toArray(new String[0]));

			// find the language classifier
			OntClassInfo oci = OntologyManagement.getInstance().getOntClassInfo(LanguageClassifier.MY_URI);
			if (oci != null) {
				Resource[] instances = oci.getInstances();
				for (Resource r : instances) {
					if (!(r instanceof LanguageClassifier))
						continue;
					LanguageClassifier lc = (LanguageClassifier) r;
					String[] names = lc.getName();
					List<String> engineNames = factory.getNames();
					boolean found = false;
					for (String name : names) {
						// if one of the names of the language classifier is a
						// short name of the engine, then add the language
						// classifier
						for (String en : engineNames) {
							if (en.equals(name)) {
								se.addLanguageClassifier(lc);
								found = true;
								break;
							}
						}
						if (found)
							break;
					}
				}
			}
			retVal.add(se);
		}

		return retVal;
	}

	public static Map<String, LanguageClassifier[]> getFileExtensions() {
		Map<String, LanguageClassifier[]> map = new HashMap<String, LanguageClassifier[]>();
		List<ScriptEngine> engines = getEngines();

		for (ScriptEngine se : engines) {
			String[] ext = se.getFileExtensions();
			for (String ex : ext) {
				map.put(ex, se.getLanguageClassifier());
			}
		}
		return map;
	}
}

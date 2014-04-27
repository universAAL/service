/**
 * 
 *  OCO Source Materials 
 *      Copyright IBM Corp. 2012 
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

import java.util.Enumeration;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.service.asor.impl.ASORServiceMngr;


/**
 * 
 *  @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 *	
 *  Sep 10, 2012
 *
 */
public class ProcessOutputParser {

	public static String extractParameterValue(ProcessOutput po) {
		String serialized = "";
		
		ASORServiceMngr asorServiceMngr = ASORServiceMngr.getInstance();
		
		Enumeration e = po.getPropertyURIs();
		while(e.hasMoreElements()) {
			Object elem = e.nextElement();
			Object propertyValue = po.getProperty((String)elem);
			if (propertyValue instanceof Resource) {
				serialized = asorServiceMngr.getMessageContentSerializer().serialize((Resource) propertyValue);
				System.out.println("Serialized resource:");
				System.out.println(serialized);
				break;
			}
		}
		
		return serialized;
	}
}

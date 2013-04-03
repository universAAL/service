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
package org.universAAL.service.asor.impl;

import java.util.HashMap;
import java.util.Map;

import org.universAAL.middleware.service.ServiceCaller;
import org.universAAL.middleware.serialization.MessageContentSerializer;


/**
 * 
 *  @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 *	
 *  Aug 15, 2012
 *
 */
public class ASORServiceMngr {

	private static ASORServiceMngr instance;
	private final static Object synchObj = new Object();
	
	private Map<String, ServiceCaller> asorServiceCallersMap = new HashMap<String, ServiceCaller>();
	private MessageContentSerializer messageContentSerializer;


	public static ASORServiceMngr getInstance() {
		if (null == instance) {
			synchronized (synchObj) {
				if (null == instance) {
					instance = new ASORServiceMngr();
				}
			}
		}
		
		return instance;
	}
	
	public void addServiceCaller(String serviceCallerKey, ServiceCaller serviceCaller) {
		if (!asorServiceCallersMap.containsKey(serviceCallerKey)) {
			asorServiceCallersMap.put(serviceCallerKey, serviceCaller);
		}
	}
	
	public ServiceCaller getServiceCaller(String serviceCallerKey) {
		return asorServiceCallersMap.get(serviceCallerKey);
	}

	public void setMessageContentSerializer(MessageContentSerializer serializer) {
		messageContentSerializer = serializer;
	}

	public MessageContentSerializer getMessageContentSerializer() {
		return messageContentSerializer;
	}
}

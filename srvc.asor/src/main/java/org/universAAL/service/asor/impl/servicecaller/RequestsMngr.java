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
package org.universAAL.service.asor.impl.servicecaller;

import java.util.HashMap;
import java.util.Map;

import org.universAAL.middleware.service.ServiceCaller;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse;


/**
 * 
 *  @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 *	
 *  Sep 9, 2012
 *
 */
public class RequestsMngr {

	private static RequestsMngr instance = null;
	private static final Object syncToGetInstance = new Object();
	
	private Map<String, ASORServiceResponseWrap> map = new HashMap<String, ASORServiceResponseWrap>();
	
	public static RequestsMngr getInstance() {
		if (null == instance) {
			synchronized (syncToGetInstance) {
				if (null == instance) {
					instance = new RequestsMngr();
				}
			}
		}
		
		return instance;
	}
	
	public ServiceResponse sendRequest(ServiceCaller serviceCaller,
			ServiceRequest sr) {
		
		ASORServiceResponseWrap wrap = new ASORServiceResponseWrap();
		synchronized (map) {
			String requestID = serviceCaller.sendRequest(sr);
			try {
				map.put(requestID, wrap);
				map.wait();
			} 
			catch (InterruptedException e) {
			}
		}

		return wrap.getServiceResponse();
	}
	
	/*
	 
	 
	 */
	
	public void notifyReceivedServiceResponse(String requestID, ServiceResponse serviceResponse) {
		synchronized(map) {
			ASORServiceResponseWrap wrap = map.remove(requestID);
			wrap.setServiceResponse(serviceResponse);
			map.notify();
		}
	}
}

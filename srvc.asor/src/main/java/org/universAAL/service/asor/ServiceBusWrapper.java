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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;

public class ServiceBusWrapper {
	Map<String, ServiceProvider> providers = new HashMap<String, ServiceProvider>();
	ExecutionEngine exec;

	public ServiceBusWrapper(ExecutionEngine exec) {
		this.exec = exec;
	}

	public Map<String, List<Object>> call(Object req) {
		ServiceRequest sreq = null;
		if (req instanceof ServiceRequest) {
			// System.out.println("Performing request -\n");
			sreq = (ServiceRequest) req;
		} else {
			// System.out.println("ERROR: sreq == null!");
			LogUtils.logError(AsorActivator.mc, ServiceBusWrapper.class, "call",
					"Service can not be called, it is not an instance of ServiceRequest.");
			return null;
		}

		ServiceResponse sr = exec.caller.call(sreq);
		if (sr.getCallStatus() == CallStatus.succeeded) {
			// System.out.println("OK: CallStatus succeeded!");
			return sr.getOutputsMap();
		} else {
			// System.out.println("ERROR: CallStatus not succeeded!");
			return null;
		}
	}

	public void register(final String callback, ServiceProfile[] profiles) {
		// System.out.println("Registering profiles: " + callback + "\n");
		ServiceProvider sp = new ServiceProvider(AsorActivator.mc, profiles, exec.engine, callback);
		providers.put(callback, sp);
	}

	public void unregister(final String callback) {
		ServiceProvider sp = providers.remove(callback);
		if (sp != null) {
			sp.close();
		}
	}

	public void unregister() {
		for (ServiceProvider sp : providers.values()) {
			sp.close();
		}
		providers.clear();
	}
}

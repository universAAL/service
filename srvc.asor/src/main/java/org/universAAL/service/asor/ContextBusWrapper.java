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
import java.util.Map;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.rdf.Resource;

public class ContextBusWrapper {
	Map<String, ContextConsumer> consumers = new HashMap<String, ContextConsumer>();
	ExecutionEngine exec;

	public ContextBusWrapper(ExecutionEngine exec) {
		this.exec = exec;
	}

	public void publish(Object event) {
		ContextEvent evt = null;
		if (event instanceof ContextEvent) {
			// System.out.println("Firing event -\n");
			evt = (ContextEvent) event;
		} else {
			// System.out.println("ERROR: event == null!");
			LogUtils.logError(AsorActivator.mc, ContextBusWrapper.class, "publish",
					"Event can not be published, it is not an instance of ContextEvent.");
			return;
		}

		exec.publisher.publish(evt);
	}

	public void publish(Resource subject, String predicate, Object object) {
		subject.setProperty(predicate, object);

		ContextEvent evt = new ContextEvent(subject, predicate);
		exec.publisher.publish(evt);
	}

	public void register(final String callback, Object profile) {
		register(callback, new ContextEventPattern[] { (ContextEventPattern) profile });
	}

	public void register(final String callback, ContextEventPattern[] profiles) {
		// System.out.println("Registering profiles: " + callback + "\n");
		ContextConsumer sp = new ContextConsumer(AsorActivator.mc, profiles, exec.engine, callback);
		consumers.put(callback, sp);
	}

	public void unregister(final String callback) {
		ContextConsumer cc = consumers.remove(callback);
		if (cc != null) {
			cc.close();
		}
	}

	public void unregister() {
		for (ContextConsumer cc : consumers.values()) {
			cc.close();
		}
		consumers.clear();
	}
}

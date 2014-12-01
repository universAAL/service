/*
	Copyright 2008-2010 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute of Computer Graphics Research 
	
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

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.Invocable;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;

@SuppressWarnings("restriction")
public class ServiceProvider extends ServiceCallee {

    private ScriptEngine engine;
    private String callback;

    // this is just to prepare a standard error message for later use
    // private static final ServiceResponse invalidInput = new ServiceResponse(
    // CallStatus.serviceSpecificFailure);
    // static {
    // invalidInput.addOutput(new ProcessOutput(
    // ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR, "Invalid input!"));
    // }

    public ServiceProvider(ModuleContext mc, ServiceProfile[] profiles,
	    ScriptEngine engine, String callback) {
	// TODO: register profiles ~after~ variables are set
	super(mc, profiles);
	this.engine = engine;
	this.callback = callback;
    }

    @Override
    public void communicationChannelBroken() {
    }

    @Override
    public ServiceResponse handleCall(ServiceCall call) {
	if (call == null)
	    // TODO: log
	    return null;

	String operation = call.getProcessURI();
	if (operation == null)
	    return null;

	try {
	    Invocable inv = (Invocable) engine;
	    ServiceResponse sr = (ServiceResponse) inv.invokeFunction(callback,
		    call);

	    return sr;
	} catch (NoSuchMethodException e) {
	    // TODO: log
	    e.printStackTrace();
	} catch (ScriptException e) {
	    // TODO: log
	    e.printStackTrace();
	}

	return null;
    }
}

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

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.Invocable;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextSubscriber;

@SuppressWarnings("restriction")
public class ContextConsumer extends ContextSubscriber {
    private ScriptEngine engine;
    private String callback;

    public ContextConsumer(ModuleContext mc, ContextEventPattern[] profiles,
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
    public void handleContextEvent(ContextEvent event) {
	if (event == null)
	    return;

	try {
	    Invocable inv = (Invocable) engine;
	    inv.invokeFunction(callback, event);
	    return;
	} catch (NoSuchMethodException e) {
	    LogUtils.logError(
		    AsorActivator.mc,
		    ContextConsumer.class,
		    "handleContextEvent",
		    new Object[] { "Context event could not be transferred to script: callback method does not exist. A NoSuchMethodException occurred." },
		    e);
	    // e.printStackTrace();
	} catch (ScriptException e) {
	    LogUtils.logError(
		    AsorActivator.mc,
		    ContextConsumer.class,
		    "handleContextEvent",
		    new Object[] { "Context event could not be transferred to script. A ScriptException occurred." },
		    e);
	    // e.printStackTrace();
	}
    }
}

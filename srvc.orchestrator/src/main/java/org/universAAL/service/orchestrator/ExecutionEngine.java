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
package org.universAAL.service.orchestrator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextPublisher;
import org.universAAL.middleware.context.DefaultContextPublisher;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.context.owl.ContextProviderType;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.DefaultServiceCaller;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceCaller;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owl.Service;
import org.universAAL.middleware.service.owls.process.ProcessOutput;

import javax.script.*;

/**
 *
 * @author Carsten Stockloew
 *
 */
@SuppressWarnings("restriction")
public class ExecutionEngine extends Service {
	private Bindings bindings;
	private ModuleContext mc;
	ServiceCaller caller;
	ContextPublisher publisher;
	ScriptEngine engine = null;
	Date startDate;
	Date endDate;
	boolean isRunning = true;
	ServiceBusWrapper srvcWrapper;
	ContextBusWrapper ctxtWrapper;
	String filename;
	static Class<?>[] stdimports;
	String content;
	Thread thread = null;

	static {
		stdimports = new Class[] { ServiceRequest.class, ServiceCall.class, ServiceResponse.class, CallStatus.class,
				ProcessOutput.class, ContextEvent.class, ContextEventPattern.class, MergedRestriction.class };
	}

	public ExecutionEngine(ModuleContext mc, String filename) {
		this.mc = mc;
		this.filename = filename;
		caller = new DefaultServiceCaller(this.mc);

		ContextProvider info = new ContextProvider(Provider.BASE + "ctxt.provider/" + filename);
		info.setType(ContextProviderType.controller);
		info.setProvidedEvents(new ContextEventPattern[] { new ContextEventPattern() });
		publisher = new DefaultContextPublisher(mc, info);

		srvcWrapper = new ServiceBusWrapper(this);
		ctxtWrapper = new ContextBusWrapper(this);
	}

	public void stop() {
		// stops all existing callees and subscribers
		if (thread != null)
			thread.interrupt();

		ServiceBusWrapper sw = srvcWrapper;
		srvcWrapper = null;
		sw.unregister();

		ContextBusWrapper cw = ctxtWrapper;
		ctxtWrapper = null;
		cw.unregister();

		publisher.close();
		caller.close();
	}

	public void execute(String content, String engineName) {
		this.content = content;
		execute(new StringReader(content), engineName);
	}

	public void execute(File file, String engineName) {
		if (file == null)
			return;
		String content = null;

		try {
			Scanner scanner = new Scanner(file);
			content = scanner.useDelimiter("\\Z").next();
			scanner.close();
		} catch (NoSuchElementException e1) {
			// this just means that the file is empty -> no action/output needed
			// e1.printStackTrace();
		} catch (FileNotFoundException e2) {
			LogUtils.logError(Activator.mc, ExecutionEngine.class, "execute",
					new Object[] { "Script could not be executed.", file.toString() }, e2);
			// e2.printStackTrace();
		}
		if (content == null)
			content = "";
		this.content = content;
		// System.out.println(" ------------- executing script with content:");
		// System.out.println(content);
		// System.out.println(" ------------- ..end content!");
		execute(content, engineName);
	}

	public void execute(final Reader r, String engineName) {
		// System.out.println("hello");
		engine = new ScriptEngineManager().getEngineByName(engineName);

		bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
		bindings.put("service", srvcWrapper);
		bindings.put("ctxt", ctxtWrapper);

		// execute in new thread
		thread = new Thread() {
			@Override
			public void run() {
				LogUtils.logDebug(Activator.mc, ExecutionEngine.class, "execute",
						new Object[] { "Executing script: ", filename }, null);
				// System.out.println("!!!!!!!!!!!!!!!!!!!!!!! script:");

				Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

				Object retval = null;
				try {
					startDate = new Date();
					isRunning = true;
					for (Class<?> c : stdimports) {
						String s = "importClass(" + c.getName() + ");";
						// System.out.println(" - ASOR: use std import: " + s);
						// System.out.println(s);
						// System.out.println(bindings);
						try {
							engine.eval(s, bindings);
						} catch (Exception e) {
						}
					}
					retval = engine.eval(r, bindings);
				} catch (ScriptException e) {
					LogUtils.logError(Activator.mc, ExecutionEngine.class, "execute",
							new Object[] { "An error occurred while executing script: ", filename }, e);
					// e.printStackTrace();
				}
				isRunning = false;
				endDate = new Date();

				String sRetVal = null;
				if (retval == null) {
					sRetVal = "null";
				} else {
					if (retval instanceof Resource) {
						sRetVal = "Resource(" + retval.toString() + ")";
					} else {
						sRetVal = retval.toString();
					}
				}
				LogUtils.logDebug(Activator.mc, ExecutionEngine.class, "execute",
						new Object[] { "Finished executing script: ", filename, " Return value: ", sRetVal }, null);
				// System.out.println("!!!!!!!!!!!!!!!!!!!!!!! finished "
				// + filename);
			}
		};
		thread.start();
	}
}

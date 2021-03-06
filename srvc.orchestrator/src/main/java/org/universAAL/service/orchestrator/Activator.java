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

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.OSGiContainer;

/**
 *
 * @author Carsten Stockloew
 *
 */
public final class Activator implements BundleActivator {
	public static ModuleContext mc;
	public static BundleContext bc;
	private Provider orchestrator;

	public void start(BundleContext context) throws Exception {
		bc = context;
		mc = OSGiContainer.THE_CONTAINER.registerModule(new Object[] { context });
		orchestrator = new Provider(mc.getConfigHome());
	}

	public void stop(BundleContext mc) throws Exception {
		orchestrator.stop();
	}
}

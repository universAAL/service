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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.ontology.orchestration.LanguageClassifier;
import org.universAAL.ontology.orchestration.Script;

public class Provider {
	public static final String BASE = "urn:Orchestrator/";

	class ManagedScript {
		String uri = "";
		ExecutionEngine engine = null;
		File file = null;
		boolean isPersistent = true;
	}

	List<ManagedScript> scripts = new LinkedList<ManagedScript>();
	Integration ai;
	File confHome;
	private Watcher watcher = null;

	public Provider(File confHome) {
		this.confHome = confHome;
		start();
	}

	/**
	 * Determines whether the given file has a valid file extension according to
	 * the given set of extensions.
	 *
	 * @param file
	 *            the file to check.
	 * @param validExt
	 *            the set of valid extensions.
	 * @return boolean
	 */
	public static boolean isValidExt(File file, Set<String> validExt) {
		if (file.isDirectory())
			return false;
		String extension = "";
		int i = file.getName().lastIndexOf('.');
		if (i > 0) {
			extension = file.getName().substring(i + 1);
		}
		if (validExt.contains(extension)) {
			return true;
		}
		return false;
	}

	public void start() {
		// get valid extensions
		Map<String, LanguageClassifier[]> ext = EngineInfo.getFileExtensions();
		Set<String> validExt = ext.keySet();

		// get all files in confHome with vaild extension
		ArrayList<File> files = new ArrayList<File>();
		File[] filesList = confHome.listFiles();
		for (File file : filesList) {
			if (isValidExt(file, validExt)) {
				files.add(file);
			}
		}

		// log
		String msg = "Found the following files for Orchestrator to start:" + System.getProperty("line.separator");
		for (File file : files) {
			// System.out.println("FILE: " + file);
			msg += "   file: " + file.toURI().toString() + System.getProperty("line.separator");
		}
		LogUtils.logDebug(Activator.mc, Provider.class, "start", msg);

		// start all valid files
		for (File file : files) {
			addScript(file);
		}

		// integrate with universAAL buses
		ai = new Integration(this);

		// start watcher
		watcher = new Watcher(this, confHome.getAbsolutePath());
	}

	public void addScript(File file) {
		ManagedScript ms = new ManagedScript();
		ms.file = file;
		ms.uri = file.toURI().toString();
		ms.engine = new ExecutionEngine(Activator.mc, file.getName());
		synchronized (scripts) {
			scripts.add(ms);
			// System.out.println(" ----------------------- \n starting script:
			// "
			// + file.getName() + "\nURI: " + file.toURI().toString());
			LogUtils.logDebug(Activator.mc, Provider.class, "addScript",
					new Object[] { "Starting script: ", ms.uri }, null);

			// TODO: language classifier
			ms.engine.execute(file, "JavaScript");
		}
		while (ms.engine.isRunning) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		}
	}

	public String addScript(Script s) {
		if (s == null)
			return null;
		ManagedScript ms = new ManagedScript();
		String name = s.getName();// TODO: make sure that this fits as URI
		if (name == null)
			return null;

		if (Resource.isAnon(ms.uri)) {
			ms.uri = BASE + name;
		} else {
			ms.uri = s.getURI();
		}
		ms.engine = new ExecutionEngine(Activator.mc, name);
		synchronized (scripts) {
			scripts.add(ms);
			// System.out.println(" ----------------------- \n starting script:
			// "
			// + name);
			LogUtils.logDebug(Activator.mc, Provider.class, "addScript",
					new Object[] { "Starting script: ", ms.uri }, null);
			// TODO: language classifier
			ms.engine.execute(s.getContent(), "JavaScript");
			return ms.uri;
		}
	}

	public boolean removeScript(Script s) {
		if (s == null)
			return false;
		return removeScript(s.getURI());
	}

	public boolean removeScript(String uri) {
		if (uri == null)
			return false;
		synchronized (scripts) {
			for (Iterator<ManagedScript> it = scripts.iterator(); it.hasNext();) {
				ManagedScript ms = it.next();
				if (ms.uri.equals(uri)) {
					it.remove();
					ms.engine.stop();
					LogUtils.logDebug(Activator.mc, Provider.class, "removeScript",
							new Object[] { "Removing script: ", uri }, null);
					return true;
				}
			}
		}
		return false;
	}

	public boolean hasRunning() {
		synchronized (scripts) {
			for (ManagedScript ms : scripts) {
				if (ms.engine.isRunning)
					return true;
			}
		}
		return false;
	}

	public List<Script> getScripts() {
		List<Script> lst = new ArrayList<Script>();
		synchronized (scripts) {
			for (ManagedScript ms : scripts) {
				Script s = new Script(ms.uri);
				s.setRunning(ms.engine.isRunning);
				s.setPersistent(ms.isPersistent);
				s.setContent(ms.engine.content);
				lst.add(s);
			}
		}
		return lst;
	}

	public void stop() {
		if (watcher != null) {
			watcher.stop();
		}

		int i = scripts.size();
		while (i > 0) {
			i--;
			ManagedScript ms = scripts.get(0);
			// System.out.println("---------- removing script: " + ms.uri);
			LogUtils.logDebug(Activator.mc, Provider.class, "stop",
					new Object[] { "Stopping script: ", ms.uri }, null);
			removeScript(ms.uri);
		}
	}
}
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
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.ontology.orchestration.LanguageClassifier;

import static java.nio.file.StandardWatchEventKinds.*;

public class Watcher {
	private Provider provider;
	private Path dir;
	private WatchService watcher;
	private Thread t;
	private Map<String, Long> modTime;

	public Watcher(Provider provider, String fdir) {
		modTime = new HashMap<String, Long>();
		this.provider = provider;
		this.dir = Paths.get(fdir);
		try {
			this.watcher = FileSystems.getDefault().newWatchService();
			dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// process events in separate thread
		t = new Thread() {
			@Override
			public void run() {
				processEvents();
			}
		};
		t.start();
	}

	public void stop() {
		t.interrupt();
	}

	@SuppressWarnings("unchecked")
	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>) event;
	}

	/**
	 * Process all events for keys queued to the watcher
	 */
	void processEvents() {
		for (;;) {

			// wait for key to be signalled
			WatchKey key;
			try {
				key = watcher.take();
			} catch (InterruptedException x) {
				return;
			}

			for (WatchEvent<?> event : key.pollEvents()) {
				Kind<?> kind = event.kind();

				// TBD - provide example of how OVERFLOW event is handled
				if (kind == OVERFLOW) {
					continue;
				}

				// Context for directory entry event is the file name of entry
				WatchEvent<Path> ev = cast(event);
				Path name = ev.context();
				Path child = dir.resolve(name);

				String filename = null;
				File file = null;
				try {
					file = new File(child.toFile().getCanonicalPath());
					filename = file.toURI().toString();
				} catch (IOException e) {
					e.printStackTrace();
				}
				// System.out.format("%s: %s %d %s\n", event.kind().name(),
				// filename, event.count(), child.toUri().toString());
				if (filename == null || file == null) {
					// System.out.println(" -- ERROR: filename: " + filename
					// + " file: " + file);
					continue;
				}

				Map<String, LanguageClassifier[]> ext = EngineInfo.getFileExtensions();
				Set<String> validExt = ext.keySet();
				if (!Provider.isValidExt(file, validExt)) {
					LogUtils.logError(Activator.mc, Watcher.class, "processEvents",
							new Object[] {
									"The watcher detected a change of a file but the file extension could not be recognized: ",
									file.toString() },
							null);
					continue;
				}

				Long mod = file.lastModified();
				if (kind == ENTRY_MODIFY) {
					Long time = modTime.get(filename);
					if (time != null) {
						if (time.equals(mod))
							// time has not changed -> the file was not modified
							// or we get a second notification
							continue;
					}
					// remove the script and re-add it
					if (!provider.removeScript(filename)) {
						// System.out
						// .println(" -- ERROR: removing script failed!");
						LogUtils.logError(Activator.mc, Watcher.class, "processEvents",
								new Object[] {
										"The watcher detected a modification of a file and tried to remove the script, but the script could not be removed: ",
										filename },
								null);
					}
					provider.addScript(file);
					// store mod time
					modTime.put(filename, mod);
				} else if (kind == ENTRY_CREATE) {
					// weird workaround for linux-editors
					provider.removeScript(filename);
					// now add the file
					provider.addScript(file);
					modTime.put(filename, mod);
				} else if (kind == ENTRY_DELETE) {
					if (!provider.removeScript(filename)) {
						// System.out
						// .println(" -- ERROR: removing script failed!");
						LogUtils.logError(Activator.mc, Watcher.class, "processEvents",
								new Object[] {
										"The watcher detected the deletion of a file and tried to remove the script, but the script could not be removed: ",
										filename },
								null);
					}
					// remove mod time
					modTime.remove(filename);
				}
			}

			key.reset();
		}
	}
}

package org.universAAL.service.asor;

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

import static java.nio.file.StandardWatchEventKinds.*;

public class Watcher {
    private AsorProvider provider;
    private Path dir;
    private WatchService watcher;
    private Thread t;
    private Map<String, Long> modTime;

    public Watcher(AsorProvider provider, String fdir) {
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
		System.out.format("%s: %s %d %s\n", event.kind().name(), filename,
			event.count(), child.toUri().toString());
		if (filename == null || file == null)
		    continue;
		if (file.isDirectory())
		    continue;

		if (kind == ENTRY_MODIFY) {
		    Long time = modTime.get(filename);
		    Long mod = file.lastModified();
		    if (time != null) {
			if (time.equals(mod))
			    // time has not changed -> the file was not modified
			    // or we get a second notification
			    continue;
		    }
		    // remove the script and re-add it
		    if (!provider.removeScript(filename))
			System.out.println(" -- ERROR: removing script failed!");
		    provider.addScript(file);
		    // store mod time
		    modTime.put(filename, mod);
		} else if (kind == ENTRY_CREATE) {
		    provider.addScript(file);
		} else if (kind == ENTRY_DELETE) {
		    provider.removeScript(filename);
		    // remove mod time
		    modTime.remove(filename);
		}
	    }

	    key.reset();
	}
    }
}

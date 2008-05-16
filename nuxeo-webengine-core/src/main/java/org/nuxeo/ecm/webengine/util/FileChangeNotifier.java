/*
 * (C) Copyright 2006-2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     bstefanescu
 *
 * $Id$
 */

package org.nuxeo.ecm.webengine.util;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.collections.ListenerList;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class FileChangeNotifier {

    private static final Log log = LogFactory.getLog(FileChangeNotifier.class);

    private final ListenerList listeners = new ListenerList();
    private final Timer timer = new Timer("FileChangeNotifier");
    private final Hashtable<String, FileEntry> files = new Hashtable<String, FileEntry>();

    public void start() {
        start(10000, 2000);
    }

    public void start(int startAfter, int interval) {
        timer.scheduleAtFixedRate(new WatchTask(), startAfter, interval);
    }

    public void stop() {
        timer.cancel();
        timer.purge();
    }

    public String watch(File file) throws IOException {
        FileEntry entry = new FileEntry(file);
        files.put(entry.id, entry);
        return entry.id;
    }

    public String watch(String id, File file) throws IOException {
        files.put(id, new FileEntry(id, file));
        return id;
    }

    public void unwatch(File file) throws IOException {
        files.remove(new FileEntry(file).id);
    }

    public void unwatch(String id, File file) throws IOException {
        files.remove(new FileEntry(id, file).id);
    }

    public void addListener(FileChangeListener listener) {
        listeners.add(listener);
    }

    public void removeListener(FileChangeListener listener) {
        listeners.remove(listener);
    }

    protected void fireNotification(FileEntry entry) {
        long tm = System.currentTimeMillis();
        for (Object listener : listeners.getListeners()) {
            try {
                ((FileChangeListener)listener).fileChanged(entry, tm);
            } catch (Throwable t) {
                log.error("Error while to notifying file change for: "+entry.file, t);
            }
        }
    }

    class WatchTask extends TimerTask {
        @Override
        public void run() {
            try {
                // make a copy to avoid concurrent modifs if a listener is unwatching a file
                FileEntry[] entries = files.values().toArray(new FileEntry[files.size()]);
                for (FileEntry entry : entries) {
                    long lastModified = entry.file.lastModified();
                    if ( entry.lastModified < lastModified) {
                        fireNotification(entry);
                        entry.lastModified = lastModified;
                    }
                }
            } catch (Throwable t) {
                log.error("Error while to notifying file change", t);
            }
        }
    }

    public class FileEntry {
        public String id;
        public File file;
        public long lastModified;

        FileEntry(File file) throws IOException {
            this (null, file);
        }
        FileEntry(String id, File file) throws IOException {
            this.file = file.getCanonicalFile();
            lastModified = file.lastModified();
            this.id = id == null ? file.getAbsolutePath() : id;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj.getClass() == FileEntry.class) {
                return id.equals(((FileEntry) obj).id);
            }
            return false;
        }

        @Override
        public String toString() {
            return id;
        }
    }

}

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

package org.nuxeo.ecm.webengine;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.core.schema.DocumentType;
import org.nuxeo.ecm.core.schema.types.Type;
import org.nuxeo.ecm.core.url.URLFactory;
import org.nuxeo.ecm.platform.rendering.api.RenderingEngine;
import org.nuxeo.ecm.platform.rendering.fm.FreemarkerEngine;
import org.nuxeo.ecm.webengine.config.FileChangeListener;
import org.nuxeo.ecm.webengine.config.FileChangeNotifier;
import org.nuxeo.ecm.webengine.exceptions.WebDeployException;
import org.nuxeo.ecm.webengine.mapping.Mapping;
import org.nuxeo.ecm.webengine.mapping.MappingDescriptor;
import org.nuxeo.ecm.webengine.mapping.PathMapper;
import org.nuxeo.ecm.webengine.resolver.DefaultDocumentResolver;
import org.nuxeo.ecm.webengine.resolver.DocumentResolver;
import org.nuxeo.ecm.webengine.scripting.ScriptFile;
import org.nuxeo.ecm.webengine.util.DirectoryStack;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class DefaultWebApplication implements WebApplication, FileChangeListener {

    public final static Log log = LogFactory.getLog(WebApplication.class);

    protected WebEngine engine;
    protected FreemarkerEngine rendering;
    protected String id;
    protected DirectoryStack dirStack;
    protected String errorPage;
    protected String indexPage;
    protected String defaultPage;
    protected DocumentResolver documentResolver;
    protected Map<String, String> typeBindings;
    protected PathMapper mapper;

    protected WebApplicationDescriptor desc;

    // object binding cache
    protected ConcurrentMap<String, ObjectDescriptor> objects;
    protected ConcurrentMap<String, ScriptFile> fileCache;

    public DefaultWebApplication(WebEngine engine, WebApplicationDescriptor desc) throws WebException {
        this.engine = engine;
        this.id = desc.getId();
        this.defaultPage = desc.getDefaultPage("default.ftl");
        this.indexPage = desc.getIndexPage("index.ftl");
        this.errorPage = desc.getErrorPage("error.ftl");
        this.documentResolver = desc.getDocumentResolver();
        if (this.documentResolver == null) {
            this.documentResolver = new DefaultDocumentResolver();
        }
        List<ObjectBindingDescriptor> list = desc.getBindings();
        if (list != null && !list.isEmpty()) {
            typeBindings = new HashMap<String, String>();
            for (ObjectBindingDescriptor obd : list) {
                typeBindings.put(obd.type, obd.objectId);
            }
        }
        List<MappingDescriptor> mappings = desc.getMappings();
        if (mappings != null && !mappings.isEmpty()) {
            mapper = new PathMapper(mappings);
        }
        try {
            List<RootDescriptor> roots = desc.getRoots();
            this.dirStack = new DirectoryStack();
            if (roots == null) {
                this.dirStack.addDirectory(new File(engine.getRootDirectory(), "default"), 0);
            } else {
                Collections.sort(roots);
                for (RootDescriptor rd : roots) {
                    File file =new File(engine.getRootDirectory(), rd.path);
                    this.dirStack.addDirectory(file, rd.priority);
                }
            }

            this.rendering = new FreemarkerEngine();
            rendering.setResourceLocator(this);
            rendering.setMessageBundle(engine.getMessageBundle());
            rendering.setSharedVariable("env", engine.getEnvironment());
            if (desc.renderingExtensions != null) {
                for (String name : desc.renderingExtensions) {
                    Object tpl = engine.getRenderingExtension(name);
                    if (tpl != null) {
                        rendering.setSharedVariable(name, tpl);
                    } else {
                        log.warn("Unknown rendering extension: "+name);
                    }
                }
            }
            objects = new ConcurrentHashMap<String, ObjectDescriptor>();
            fileCache = new ConcurrentHashMap<String, ScriptFile>();
            this.desc = desc;

            FileChangeNotifier notifier = engine.getFileChangeNotifier();
            if (notifier != null) {
                if (!dirStack.isEmpty()) {
                    notifier.addListener(this);
                    for (DirectoryStack.Entry entry : dirStack.getEntries()) {
                        notifier.watch(entry.file);
                    }
                }
            }

        } catch (Exception e) {
            throw new WebDeployException("Failed to create virtual directory for webapp: "+id, e);
        }
    }



    public RenderingEngine getRendering() {
        return rendering;
    }

    public WebApplicationDescriptor getDescriptor(){
        return desc;
    }

    /**
     * @return the engine.
     */
    public WebEngine getEngine() {
        return engine;
    }

    /**
     * @return the id.
     */
    public String getId() {
        return id;
    }

    public DirectoryStack getDirectoryStack() {
        return dirStack;
    }

    /**
     * @return the documentResolver.
     */
    public DocumentResolver getDocumentResolver() {
        return documentResolver;
    }

    public void setDocumentResolver(DocumentResolver resolver) {
        this.documentResolver = resolver;
    }

    /**
     * @return the defaultPage.
     */
    public String getDefaultPage() {
        return defaultPage;
    }

    /**
     * Used by tests
     * @param defaultPage the defaultPage to set.
     */
    public void setDefaultPage(String defaultPage) {
        this.defaultPage = defaultPage;
    }

    /**
     * @return the errorPage.
     */
    public String getErrorPage() {
        return errorPage;
    }

    /**
     * @return the indexPage.
     */
    public String getIndexPage() {
        return indexPage;
    }

    public Mapping getMapping(String pathInfo) {
        if (mapper == null) return null;
        return mapper.getMapping(pathInfo);
    }


    public String getTypeBinding(String type) {
        if (typeBindings != null) {
            String id = typeBindings.get(type);
            if (id != null) {
                return id;
            }
        }
        return engine.getTypeBinding(type);
    }

    public ObjectDescriptor getDefaultObject() {
        ObjectDescriptor obj = objects.get("Document");
        if (obj == null) {
            String id = getTypeBinding("Document");
            if (id == null) {
                throw new WebRuntimeException("Invalid configuration: The default object (the one mapped to Document type) was not declared");
            }
            obj = engine.getObject(id);
            if (obj == null) {
                throw new WebRuntimeException("Invalid configuration: The default object (the one mapped to Document type) was not declared");
            }
            objects.put("Document", obj);
        }
        return obj;
    }

    public synchronized ObjectDescriptor getObjectDescriptor(Type type) {
        String typeName = type.getName();
        ObjectDescriptor obj = objects.get(typeName);
        if (obj != null) { // in cache
            return obj;
        }
        String id = getTypeBinding(typeName);
        if (id == null) {
            Type stype = type.getSuperType();
            if (stype == null || stype.getName().equals("Document")) {// the default
                obj = getDefaultObject();
            } else {
                obj = getObjectDescriptor(stype);
            }
        } else {
            obj = engine.getObject(id);
            if (obj == null) {
                Type stype = type.getSuperType();
                if (stype == null || stype.getName().equals("Document")) {// the default
                    obj = getDefaultObject();
                } else {
                    obj = getObjectDescriptor(stype);
                }
            }
        }
        objects.put(typeName, obj);
        return obj;
    }

    public void flushCache() {
        objects.clear();
        fileCache.clear();
    }


    public ScriptFile getFile(String path) throws IOException {
        int len = path.length();
        if (path == null || len == 0) return null;
        char c = path.charAt(0);
        if (c == '.') { // avoid getting files outside the web root
            path = new Path(path).makeAbsolute().toString();
        } else if (c != '/') {// avoid doing duplicate entries in document stack cache
            path = new StringBuilder(len+1).append("/").append(path).toString();
        }
        return findFile(path);
    }

    /**
     *
     * @param path a normalized path (absollute path)
     * @return
     */
    private final ScriptFile findFile(String path) throws IOException {
        ScriptFile file = fileCache.get(path);
        if (file == null) {
            File f = dirStack.getFile(path);
            if (f != null) {
                file = new ScriptFile(f);
                fileCache.put(path, file);
            }
        }
        return file;
    }


    public ScriptFile getActionScript(String action, DocumentType docType) throws IOException {
        String type = docType.getName();
        String path = "/" + type + '/' + action + ".ftl";
        // we avoid passing through WebContext#getFile() since this is always an absolute path
        ScriptFile file = findFile(path);
        if (file == null) {
            docType = (DocumentType)docType.getSuperType();
            if (docType != null) {
                file = getActionScript(action, docType);
                if (file != null) {
                    fileCache.put(path, file);
                }
            }
        }
        return file;
    }

    public WebEngine getWebEngine() {
        return engine;
    }

    public void registerRenderingExtension(String id, Object obj) {
        if (desc.getRenderingExtensions() != null && desc.getRenderingExtensions().contains(id)) {
            rendering.setSharedVariable(id, obj);
        }
    }

    public void unregisterRenderingExtension(String id) {
        if (desc.getRenderingExtensions() != null && desc.getRenderingExtensions().contains(id)) {
            rendering.setSharedVariable(id, null);
        }
    }

    public URL getResourceURL(String key) {
        try {
            return URLFactory.getURL(key);
        } catch (Exception e) {
            return null;
        }
    }

    public File getResourceFile(String key) {
        try {
            ScriptFile file = getFile(key);
            if (file != null) {
                return file.getFile();
            }
        } catch (IOException e) {
        }
        return null;
    }

    public void fileChanged(FileChangeNotifier.FileEntry entry, long now) {
        for (DirectoryStack.Entry dir : dirStack.getEntries()) {
            if (dir.file.getPath().equals(entry.file.getPath())) {
                fileCache.clear(); // TODO optimize this do not flush entire cache
            }
        }
    }

}

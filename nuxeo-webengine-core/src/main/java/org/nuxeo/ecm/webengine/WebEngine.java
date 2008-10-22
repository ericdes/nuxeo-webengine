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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.ws.rs.Path;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.xmap.Context;
import org.nuxeo.common.xmap.XMap;
import org.nuxeo.ecm.core.url.URLFactory;
import org.nuxeo.ecm.platform.rendering.api.RenderingEngine;
import org.nuxeo.ecm.platform.rendering.api.ResourceLocator;
import org.nuxeo.ecm.platform.rendering.fm.FreemarkerEngine;
import org.nuxeo.ecm.platform.rendering.fm.i18n.ResourceComposite;
import org.nuxeo.ecm.webengine.loader.WebClassLoader;
import org.nuxeo.ecm.webengine.model.Module;
import org.nuxeo.ecm.webengine.model.Resource;
import org.nuxeo.ecm.webengine.model.WebAdapter;
import org.nuxeo.ecm.webengine.model.WebContext;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.BundleTypeProvider;
import org.nuxeo.ecm.webengine.model.impl.DirectoryTypeProvider;
import org.nuxeo.ecm.webengine.model.impl.ModuleDescriptor;
import org.nuxeo.ecm.webengine.model.impl.ModuleImpl;
import org.nuxeo.ecm.webengine.model.impl.ModuleRegistry;
import org.nuxeo.ecm.webengine.model.io.BlobWriter;
import org.nuxeo.ecm.webengine.model.io.ResourceWriter;
import org.nuxeo.ecm.webengine.model.io.ScriptFileWriter;
import org.nuxeo.ecm.webengine.model.io.TemplateWriter;
import org.nuxeo.ecm.webengine.notifier.FileChangeListener;
import org.nuxeo.ecm.webengine.notifier.FileChangeNotifier;
import org.nuxeo.ecm.webengine.notifier.FileChangeNotifier.FileEntry;
import org.nuxeo.ecm.webengine.scripting.ScriptFile;
import org.nuxeo.ecm.webengine.scripting.Scripting;
import org.nuxeo.runtime.annotations.AnnotationManager;
import org.nuxeo.runtime.annotations.loader.AnnotationLoader;
import org.nuxeo.runtime.annotations.loader.BundleAnnotationsLoader;
import org.nuxeo.runtime.api.Framework;
import org.osgi.framework.Bundle;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class WebEngine implements FileChangeListener, ResourceLocator, AnnotationLoader {

    private static final Log log = LogFactory.getLog(WebEngine.class);

    private static final ThreadLocal<WebContext> CTX = new ThreadLocal<WebContext>();

    protected static Map<Object, Object> mimeTypes = loadMimeTypes();

    static Map<Object, Object> loadMimeTypes() {
        Map<Object,Object> mimeTypes = new HashMap<Object, Object>();
        Properties p = new Properties();
        URL url = WebEngine.class.getClassLoader().getResource("OSGI-INF/mime.properties");
        InputStream in = null;
        try {
            in = url.openStream();
            p.load(in);
            mimeTypes.putAll(p);
        } catch (IOException e) {
            throw new Error("Failed to load mime types");
        } finally {
            if (in != null) {
                try { in.close(); } catch (Exception e) {}
            }
        }
        return mimeTypes;
    }

    public static final WebContext getActiveContext() {
        return CTX.get();
    }

    public static final void setActiveContext(WebContext ctx) {
        CTX.set(ctx);
    }


    protected File root;
    protected ModuleRegistry moduleReg;
    protected FileChangeNotifier notifier;
    protected volatile long lastMessagesUpdate = 0;
    protected Scripting scripting;
    protected RenderingEngine rendering;
    protected Map<String, Object> env;
    protected ResourceBundle messages;
    protected boolean isDebug = false;

    protected BundleTypeProvider bundleTypeProvider;
    protected DirectoryTypeProvider directoryTypeProvider;

    protected AnnotationManager annoMgr;

    protected ResourceRegistry registry;


    public WebEngine(ResourceRegistry registry, File root) throws IOException {
        this.registry = registry;
        isDebug = Boolean.parseBoolean(Framework.getProperty("debug", "false"));
        this.root = root;
        if (isDebug) { // TODO notifier must be intialized by WebEngine
            notifier = new FileChangeNotifier();
            notifier.start();
            notifier.watch(new File(root, "i18n")); // watch i18n files
            notifier.addListener(this);
        }
        scripting = new Scripting(isDebug);
        annoMgr = new AnnotationManager();
        String cp = System.getProperty("groovy.classpath");
        if (cp == null) {
            cp = new File(root, "classes").getAbsolutePath();
        }
        scripting.addClassPath(new File(root,".").getAbsolutePath());
        scripting.addClassPath(cp);

        bundleTypeProvider = new BundleTypeProvider();
        directoryTypeProvider = new DirectoryTypeProvider(this);
        BundleAnnotationsLoader.getInstance().addLoader(WebObject.class.getName(), bundleTypeProvider);
        BundleAnnotationsLoader.getInstance().addLoader(WebAdapter.class.getName(), bundleTypeProvider);

        loadModules();

        env = new HashMap<String, Object>();
        env.put("installDir", root);
        env.put("engine", "Nuxeo Web Engine");
        env.put("version", "1.0.0.b1"); //TODO this should be put in the MANIFEST

        rendering = new FreemarkerEngine();
        rendering.setResourceLocator(this);
        rendering.setSharedVariable("env", getEnvironment());
        loadMessageBundle();
        // register annotation loader
        BundleAnnotationsLoader.getInstance().addLoader(Path.class.getName(), this);

        // register writers
        registry.addMessageBodyWriter(new ResourceWriter());
        registry.addMessageBodyWriter(new TemplateWriter());
        registry.addMessageBodyWriter(new ScriptFileWriter());
        registry.addMessageBodyWriter(new BlobWriter());
    }

    /**
     * @return the registry.
     */
    public ResourceRegistry getRegistry() {
        return registry;
    }

    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return scripting.loadClass(className);
    }

    public BundleTypeProvider getBundleTypeProvider() {
        return bundleTypeProvider;
    }

    public DirectoryTypeProvider getDirectoryTypeProvider() {
        return directoryTypeProvider;
    }

    public String getMimeType(String ext) {
        return (String)mimeTypes.get(ext);
    }

    private void loadMessageBundle() throws IOException {
        log.info("Loading i18n files");
        File file = new File(root, "i18n");
        WebClassLoader cl = new WebClassLoader();
        cl.addFile(file);
        //messages = ResourceBundle.getBundle("messages", Locale.getDefault(), cl);
        messages = new ResourceComposite(cl);
        rendering.setMessageBundle(messages);
    }

    public AnnotationManager getAnnotationManager() {
        return annoMgr;
    }

    protected Context createXMapContext() {
        return new Context() {
            private static final long serialVersionUID = 1L;
            @Override
            public Class<?> loadClass(String className)
                    throws ClassNotFoundException {
                return scripting.loadClass(className);
            }
            @Override
            public URL getResource(String name) {
                return scripting.getGroovyScripting().getGroovyClassLoader().getResource(name);
            }
        };
    }

    protected  void loadModules() {
        moduleReg = new ModuleRegistry(this);
        for (File file : root.listFiles()) {
            try {
                if (file.isDirectory()) {
                    ModuleDescriptor ad = null;
                    File appFile = new File(file, "Main.groovy");
                    if (appFile.isFile()) {
                        ad = loadModuleDescriptor(file.getName()+".Main");
                        appFile = new File(file, "module.xml");
                        if (appFile.isFile()) {
                            ModuleDescriptor ad2 = loadModuleDescriptor(appFile);
                            ad.links = ad2.links;
                            ad2.links = null;
                        }
                    } else {
                        appFile = new File(file, "module.xml");
                        if (appFile.isFile()) {
                            ad = loadModuleDescriptor(appFile);
                        }
                    }
                    if (ad != null) {
                        moduleReg.registerDescriptor(file, ad);
                        if (notifier != null) {
                            notifier.watch(file);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(); // TODO log
            }
        }
    }


    /**
     * Loads a module given its annotated class.
     *
     * @param className
     */
    protected synchronized ModuleDescriptor loadModuleDescriptor(String className) {
        try {
            Class<?> clazz = scripting.loadClass(className);
            ModuleDescriptor ad = ModuleDescriptor.fromAnnotation(clazz);
            if (ad != null) {
                ad.binding = ResourceBinding.fromAnnotation(clazz);
                if (ad.binding != null) {
                    addResourceBinding(ad.binding);
                }
            }
            return ad;
        } catch (ClassNotFoundException e) {
            e.printStackTrace(); //TODO log
            // do nothing
        }
        return null;
    }

    /**
     * Loads an module given its configuration file.
     *
     * @param cfgFile
     */
    protected synchronized ModuleDescriptor loadModuleDescriptor(File cfgFile) {
        try {
            XMap xmap = new XMap();
            xmap.register(ModuleDescriptor.class);
            InputStream in = new BufferedInputStream(new FileInputStream(cfgFile));
            return (ModuleDescriptor)xmap.load(createXMapContext(), in);
        } catch (Exception e) {
            e.printStackTrace(); // TODO log exception
        }
        return null;
    }

    public boolean isDebug() {
        return isDebug;
    }

    public void setDebug(boolean isDebug) {
        this.isDebug = isDebug;
    }

    public void registerRenderingExtension(String id, Object obj) {
        rendering.setSharedVariable(id, obj);
    }

    public void unregisterRenderingExtension(String id) {
        rendering.setSharedVariable(id, null);
    }

    public Map<String, Object> getEnvironment() {
        return env;
    }

    public ResourceBundle getMessageBundle() {
        return messages;
    }

    public Scripting getScripting() {
        return scripting;
    }

    public ModuleRegistry getModuleRegistry() {
        return moduleReg;
    }

    public Module getModule(String name) {
        return moduleReg.getModule(name);
    }

    public File getRootDirectory() {
        return root;
    }

    public FileChangeNotifier getFileChangeNotifier() {
        return notifier;
    }

    public RenderingEngine getRendering() {
        return rendering;
    }

    /** Manage jax-rs root resource bindings */
    public void addResourceBinding(ResourceBinding binding) {
        registry.addBinding(binding);
    }

    public void removeResourceBinding(ResourceBinding binding) {
        registry.removeBinding(binding);
    }

    public ResourceBinding[] getBindings() {
        return registry.getBindings();
    }

    /**
     * Reloads configuration.
     */
    public synchronized void reload() {
        log.info("Reloading WebEngine");
        bundleTypeProvider.flushCache();
        directoryTypeProvider.flushCache();
        reloadModules();
    }

    public synchronized void reloadModules() {
        for (Module module : moduleReg.getModules()) {
            ResourceBinding binding = module.getModuleBinding();
            registry.removeBinding(binding);
        }
        loadModules();
    }

    public void destroy() {
        if (notifier != null) {
            notifier.removeListener(this);
            notifier = null;
        }
        registry.clear();
    }

    protected Module getModuleFromPath(String rootPath, String path) {
        path = path.substring(rootPath.length()+1);
        int p = path.indexOf('/');
        String moduleName = path;
        if (p > -1) {
            moduleName = path.substring(0, p);
        }
        return moduleReg.getModuleByRoot(moduleName);
    }

    public void fileChanged(FileEntry entry, int type, long now) throws Exception {
        if (lastMessagesUpdate == now) {
            return;
        }
        // TODO ignore root since a cache file is generated in root. - may be we should generate the cache elsewhere
        if (entry.file.equals(root)) {
            return;
        }
        String path = entry.file.getAbsolutePath();
        String rootPath = root.getAbsolutePath();
        if (!path.startsWith(rootPath)) {
            return;
        }
        String name = entry.file.getName();
        if (name.endsWith("~") || entry.file.getParent().equals("i18n")) {
            return;
        } else if (name.equals("i18n")) {
            log.info("File changed: "+entry.file);
            loadMessageBundle();
        } else if (type == FileChangeListener.DELETED || type ==FileChangeListener.CREATED) {
            if (entry.file.getParent().equals(root)) {
                log.info("File changed: "+entry.file);
                reload();
            } else {
                Module module = getModuleFromPath(rootPath, path);
                if (module != null) {
                    log.info("File changed: "+entry.file);
                    if (path.indexOf("/skin/", 0) > 0) {
                        ((ModuleImpl)module).flushSkinCache();
                    } else { // not a skin may be a type
                        //module.flushCache();
                        ((ModuleImpl)module).flushTypeCache();
                        directoryTypeProvider.flushCache();
                    }
                }
            }
        } else if (name.equals("module.xml") || name.equals("Main.groovy")) {
            log.info("File changed: "+entry.file);
            reload();
        }
        lastMessagesUpdate = now;
    }

    public void loadAnnotation(Bundle bundle, String annoType,
            String className, String[] args) throws Exception {
        if (Path.class.getName().equals(annoType)) {
            Class<?> clazz = bundle.loadClass(className);
            Path p = clazz.getAnnotation(Path.class);
            ResourceBinding rb = new ResourceBinding();
            rb.path = p.value();
            rb.clazz = clazz;
            addResourceBinding(rb);
        }
    }

    /** ResourceLocator API */

    public URL getResourceURL(String key) {
        try {
            return URLFactory.getURL(key);
        } catch (Exception e) {
            return null;
        }
    }

    public File getResourceFile(String key) {
        WebContext ctx = WebEngine.getActiveContext();
        if (key.startsWith("@")) {
            Resource rs = ctx.getTargetObject();
            if (rs != null) {
                return rs.getView(key.substring(1)).script().getFile();
            }
        } else {
            ScriptFile file = ctx.getFile(key);
            if (file != null) {
                return file.getFile();
            }
        }
        return null;
    }

}

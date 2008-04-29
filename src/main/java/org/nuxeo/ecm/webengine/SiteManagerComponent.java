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
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.common.utils.ZipUtils;
import org.nuxeo.ecm.core.url.URLFactory;
import org.nuxeo.ecm.platform.rendering.api.RenderingEngine;
import org.nuxeo.ecm.platform.rendering.api.ResourceLocator;
import org.nuxeo.ecm.platform.rendering.fm.FreemarkerEngine;
import org.nuxeo.ecm.webengine.rendering.TransformerDescriptor;
import org.nuxeo.ecm.webengine.security.GuardDescriptor;
import org.nuxeo.ecm.webengine.security.PermissionService;
import org.nuxeo.ecm.webengine.util.FileChangeListener;
import org.nuxeo.ecm.webengine.util.FileChangeNotifier;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.ComponentName;
import org.nuxeo.runtime.model.DefaultComponent;
import org.osgi.framework.Bundle;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class SiteManagerComponent extends DefaultComponent implements ResourceLocator, FileChangeListener  {

    public final static ComponentName NAME = new ComponentName(SiteManagerComponent.class.getName());

    public final static String TRANSFORMER_XP = "transformer";
    public final static String SITE_OBJ_XP = "siteObject";
    public final static String GUARD_XP = "guard"; // global guards

    private final static Log log = LogFactory.getLog(SiteManagerComponent.class);

    private SiteManager mgr;
    private FileChangeNotifier notifier;
    private RenderingEngine engine;
    private ComponentContext ctx;


    @Override
    public void activate(ComponentContext context) throws Exception {
        this.ctx = context;
        File root = new File(Framework.getRuntime().getHome(), "web");
        if (!root.exists()) {
            // runtime predeployment is not supporting conditional unziping so we do the predeployment here:
            deployWebDir(context.getRuntimeContext().getBundle(), root);
        }
        String val = (String)context.getPropertyValue("engine", null);
        if (val != null) {
            try {
            engine = (RenderingEngine)context.getRuntimeContext().loadClass(val).newInstance();
            } catch (Exception e) {
                log.error("Failed to load rendering engine from component configuration -> using the default freemarker engine", e);
            }
        }
        if (engine == null) {
            engine = new FreemarkerEngine(); // the default engine
        }
        engine.setResourceLocator(this);
        engine.addResourceDirectories(root);

        mgr = new DefaultSiteManager(root, engine);
        notifier = new FileChangeNotifier();
        notifier.start();



        // load configuration (it ill be put in pending until this component will exit activation code)
        File file = new File(root, "web.xml");
        if (file.isFile()) {
            //XMap xmap = new XMap();
            //WebConfiguration cfg = xmap.load(new XMapContext(context.getRuntimeContext()), new BufferedInputStream(new FileInputStream(file)));
            //new XMapContext(context.getRuntimeContext());
            context.getRuntimeContext().deploy(file.toURI().toURL());
            notifier.watch(file);
            notifier.addListener(this);
        }
    }

    @Override
    public void deactivate(ComponentContext context) throws Exception {
        notifier.stop();
        notifier.removeListener(this);
        notifier = null;
        ctx = null;
    }

    private void deployWebDir(Bundle bundle, File root) throws Exception {
        root.mkdirs(); // create root dir if not already exists
        // copy web dir located in the bundle jar into this dir
        //TODO: getLocation() may not work with some OSGi impl.
        String location = bundle.getLocation();
        if (location.startsWith("file:")) {
            if (location.endsWith(".jar")) {
                ZipUtils.unzip("web", new URL(location), root);
            } else {
                File file = new File(new URL(location).toURI());
                file = new File(file, "web");
                FileUtils.copy(file.listFiles(), root);
            }
        } else {
            if (location.endsWith(".jar")) {
                ZipUtils.unzip("web", new File(location), root);
            } else {
                File file = new File(location);
                file = new File(file, "web");
                FileUtils.copy(file.listFiles(), root);
            }
        }
    }

    @Override
    public void registerContribution(Object contribution,
            String extensionPoint, ComponentInstance contributor)
            throws Exception {
        if (TRANSFORMER_XP.equals(extensionPoint)) {
            TransformerDescriptor td = (TransformerDescriptor)contribution;
            engine.setTransformer(td.getName(), td.newInstance());
        } else if (SITE_OBJ_XP.equals(extensionPoint)) {
            ObjectDescriptor obj = (ObjectDescriptor)contribution;
            mgr.registerObject(obj);
        } else if (GUARD_XP.equals(extensionPoint)) {
            GuardDescriptor gd = (GuardDescriptor)contribution;
            PermissionService.getInstance().registerGuard(gd.getId(), gd.getGuard());
        }
    }

    @Override
    public void unregisterContribution(Object contribution,
            String extensionPoint, ComponentInstance contributor)
            throws Exception {
        if (TRANSFORMER_XP.equals(extensionPoint)) {
//            TransformerDescriptor td = (TransformerDescriptor)contribution;
//            engine.setTransformer(td.getName(), td.newInstance());
        } else if (SITE_OBJ_XP.equals(extensionPoint)) {
            ObjectDescriptor obj = (ObjectDescriptor)contribution;
            mgr.unregisterObject(obj);
        } else if (GUARD_XP.equals(extensionPoint)) {
            GuardDescriptor gd = (GuardDescriptor)contribution;
            PermissionService.getInstance().unregisterGuard(gd.getId());
        }
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (adapter == SiteManager.class) {
            return adapter.cast(mgr);
        } else if (adapter == FileChangeNotifier.class) {
            return adapter.cast(notifier);
        }
        return null;
    }

    public URL getResource(String templateName) {
        try {
            return URLFactory.getURL(templateName);
        } catch (Exception e) {
            return null;
        }
    }

    public void fileChanged(File file, long since) {
        if (ctx == null) return;
//        if (file.getAbsolutePath().startsWith(mgr.getRootDirectory().getAbsolutePath())) {
        if (file.getAbsolutePath().equals(new File(mgr.getRootDirectory(), "web.xml").getAbsolutePath())) {
            try {
                //mgr.reset();
                URL url = file.toURI().toURL();
                ctx.getRuntimeContext().undeploy(url);
                ctx.getRuntimeContext().deploy(url);
            } catch (Exception e) {
                log.error("Failed to redeploy web.xml", e);
            }
        }
    }

}

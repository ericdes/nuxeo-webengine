/*
 * (C) Copyright 2006-2007 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     Nuxeo - initial API and implementation
 *
 * $Id$
 */

package org.nuxeo.ecm.webengine.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.rendering.api.RenderingEngine;
import org.nuxeo.ecm.platform.rendering.api.RenderingException;
import org.nuxeo.ecm.webengine.DefaultDocumentResolver;
import org.nuxeo.ecm.webengine.DefaultWebContext;
import org.nuxeo.ecm.webengine.DocumentResolver;
import org.nuxeo.ecm.webengine.RequestHandler;
import org.nuxeo.ecm.webengine.WebContext;
import org.nuxeo.ecm.webengine.WebEngine;
import org.nuxeo.ecm.webengine.WebException;
import org.nuxeo.ecm.webengine.WebObject;
import org.nuxeo.ecm.webengine.WebRoot;
import org.nuxeo.ecm.webengine.actions.Actions;
import org.nuxeo.ecm.webengine.mapping.Mapping;
import org.nuxeo.ecm.webengine.scripting.ScriptFile;
import org.nuxeo.ecm.webengine.scripting.Scripting;
import org.nuxeo.runtime.api.Framework;

/**
 * Servlet for publishing SiteObjects
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class WebServlet extends HttpServlet {

    private static final long serialVersionUID = 965764764858L;

    private static final Log log = LogFactory.getLog(WebServlet.class);

    protected static final int BUFFER_SIZE = 4096 * 16;

    protected static DocumentResolver resolver = new DefaultDocumentResolver();

    private Scripting scripting;
    private WebEngine engine;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        engine = Framework.getLocalService(WebEngine.class);
        scripting = engine.getScripting();
        Map<String,Object> env = engine.getEnvironment();
        env.put("installDir", engine.getRootDirectory());
        env.put("engine", "Nuxeo Site Engine");
        env.put("version", "1.0.0");
        RenderingEngine renderingEngine = scripting.getRenderingEngine();
        renderingEngine.setSharedVariable("env", env);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        double start = System.currentTimeMillis();

        if (req.getMethod().equals(WebConst.METHOD_HEAD)) {
            resp = new NoBodyResponse(resp);
        }

        WebContext context = null;
        try {
            context = createRequest(req, resp);
            service(context, req, resp);
        } catch (Throwable e) {
            log.error("Site Servlet failed to handle request", e);
            if (context == null) {
                displayError(resp, e, "Failed to create request",
                        WebConst.SC_INTERNAL_SERVER_ERROR);
            } else {
                WebRoot root = context.getRoot();
                ScriptFile page = root.getScript(root.getErrorPage(), null);
                try {
                    context.setProperty("error", e);
                    engine.getScripting().exec(context, page);
                } catch (Throwable ee) {
                    displayError(resp, ee, "Failed to show error page",
                            WebConst.SC_INTERNAL_SERVER_ERROR);
                }
            }
        }
        System.out.println(">>> SITE REQUEST TOOK:  "+((System.currentTimeMillis()-start)/1000));
    }

    protected void service(WebContext context, HttpServletRequest req, HttpServletResponse resp)
            throws Exception {
        if (context.getLastResolvedObject() == null) { // a request outside the root
            showIndex(context);
            return;
        }

        String method = req.getMethod();
        WebObject lastTraversedObject = traverse(context);
        if (lastTraversedObject == null) {
            displayError(resp, null, "Site Root is not a supported object ");
            return;
        }
        // avoid running default action handling mechanism when invocation is redirected to scripts
        if (context.getMapping() == null) {
            RequestHandler handler = lastTraversedObject.getRequestHandler();
            if (handler != null) {
                if (method.equals(WebConst.METHOD_POST)) {
                    handler.doPost(lastTraversedObject);
                } else if (method.equals(WebConst.METHOD_PUT)) {
                    handler.doPut(lastTraversedObject);
                } else if (method.equals(WebConst.METHOD_GET)) {
                    handler.doGet(lastTraversedObject);
                } else if (method.equals(WebConst.METHOD_DELETE)) {
                    handler.doDelete(lastTraversedObject);
                } else if (method.equals(WebConst.METHOD_HEAD)) {
                    handler.doHead(lastTraversedObject);
                }
            }
        }

        // return is handler has done the rendering
        if (context.isCanceled()) {
            return;
        }

        double s = System.currentTimeMillis();
        scripting.exec(context);
        System.out.println(
                ">>>>>>>>>> RENDERING TOOK: " + ((System.currentTimeMillis() - s) / 1000));
    }

    /**
     * @return the last traversed object
     */
    protected WebObject traverse(WebContext context) throws WebException {
        WebObject firstObject = context.getFirstObject();
        WebObject lastResolved = context.getLastResolvedObject();
        if (firstObject == null || lastResolved == null) {
            return null;
        }
        WebObject lastTraversed = firstObject;
        WebObject p = firstObject;
        while (p != lastResolved.next()) {
            if (!p.traverse()) {
                return lastTraversed;
            }
            lastTraversed = p;
            p = p.next();
        }
        return lastTraversed;
    }

    protected static void displayError(HttpServletResponse resp, Throwable t,
            String message, int code) {
        PrintWriter writer;
        try {
            writer = resp.getWriter();
        } catch (IOException e) {
            log.error("Unable to output Error ", e);
            log.error("Application error was " + message, e);
            return;
        }

        writer.write("\nError occured during Site rendering");
        writer.write("\nSite Error message : " + message);
        if (t != null) {
            writer.write("\nException message : " + t.getMessage());
            for (StackTraceElement element : t.getStackTrace()) {
                writer.write("\n" + element.toString() );
            }
        }

        resp.setStatus(code);
    }

    protected void displayError(HttpServletResponse resp, Throwable t,
            String message) {
        if (t instanceof WebException) {
            WebException st = (WebException) t;
            displayError(resp, t, message, st.getReturnCode());
        } else {
            displayError(resp, t, message, WebConst.SC_INTERNAL_SERVER_ERROR);
        }
    }

    public WebContext createRequest(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String pathInfo = req.getPathInfo();
        WebRoot root;
        if (pathInfo == null || "/".equals(pathInfo)) {
            root = engine.getDefaultSiteRoot();
            pathInfo = "/index.ftl"; //TODO use the config to get the name of the index
        } else {
            int p = pathInfo.indexOf('/', 1);
            String siteName = null;
            if (p == -1) {
                siteName = pathInfo.substring(1);
                root = engine.getSiteRoot(siteName);
                if (root != null) {
                } else {
                    root = engine.getDefaultSiteRoot();
                    siteName = null;
                }
            } else {
                siteName = pathInfo.substring(1, p);
                root = engine.getSiteRoot(siteName);
                if (root == null) {
                    root = engine.getDefaultSiteRoot();
                }
            }
        }
        DefaultWebContext context = new DefaultWebContext(root, req, resp);
        // traverse documents if any
        String[] traversal = null;
        Mapping mapping = root.getMapping(pathInfo);
        if (mapping != null) { // get the traversal defined by the mapping if any
            traversal = mapping.getTraversalPath();
        }
        if (traversal == null) { // no traversal defined - compute it from pathInfo
            traversal = new Path(pathInfo).segments();
        }
        buildTraversalPath(context, traversal);
        if (mapping != null) {
            WebObject obj = context.getLastResolvedObject();
            if (obj != null) {
                mapping.addVar("type", obj.getDocument().getType());
            }
            context.setMapping(mapping); //TODO how to propagate vars without storing them in req?
        }
        return context;
    }

    public static void buildTraversalPath(DefaultWebContext context, String[] traversal) throws Exception {
        if (traversal == null || traversal.length == 0) {
            // nothing to traverse
            return;
        }
        CoreSession session = context.getCoreSession();
        String name = traversal[0];
        WebRoot root = context.getRoot();
        DocumentResolver resolver = root.getResolver();
        int p = name.lastIndexOf(WebConst.ACTION_SEPARATOR);
        if (p > -1) {
            context.setActionName(name.substring(p+WebConst.ACTION_SEPARATOR.length()));
            name = name.substring(0, p);
        }
        DocumentModel doc = resolver.getRootDocument(root, name, session);
        context.addWebObject(name, doc);
        if (doc == null) { // abort traversing
            // add the unresolved objects
            for (int i=1; i<traversal.length; i++) {
                context.addWebObject(traversal[i], null);
            }
            return;
        }
        if (traversal.length > 1) {
            for (int i=1; i<traversal.length; i++) {
                name = traversal[i];
                p = name.lastIndexOf(WebConst.ACTION_SEPARATOR);
                if (p > -1) {
                    context.setActionName(name.substring(p+WebConst.ACTION_SEPARATOR.length()));
                    name = name.substring(0, p);
                }
                doc = resolver.getSiteSegment(root, doc, name, session);
                context.addWebObject(name, doc);
                if (doc == null) {
                    for (i=i+1; i<traversal.length; i++) {
                        context.addWebObject(traversal[i], null);
                    }
                    break;
                }
            }
        }
        if (!context.hasUnresolvedObjects() && context.getActionName() == null) {
            context.setActionName(Actions.DEFAULT_ACTION);
        }
    }

    public void showIndex(WebContext context) throws Exception {
        try {
            double s = System.currentTimeMillis();
            scripting.exec(context);
            System.out.println(">>>>>>>>>> STATIC RENDERING TOOK: "+ ((System.currentTimeMillis() - s)/1000));
        } catch (RenderingException e) {
            displayError(context.getResponse(), e, "Error during the rendering process");
        }
    }

}
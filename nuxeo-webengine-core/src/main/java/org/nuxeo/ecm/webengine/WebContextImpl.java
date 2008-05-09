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

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.repository.Repository;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.ecm.platform.rendering.api.RenderingException;
import org.nuxeo.ecm.webengine.actions.ActionDescriptor;
import org.nuxeo.ecm.webengine.mapping.Mapping;
import org.nuxeo.ecm.webengine.rendering.SiteRenderingContext;
import org.nuxeo.ecm.webengine.scripting.ScriptFile;
import org.nuxeo.ecm.webengine.scripting.Scripting;
import org.nuxeo.ecm.webengine.util.FormData;
import org.nuxeo.runtime.api.Framework;
import org.python.core.PyDictionary;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class WebContextImpl implements WebContext {

    public static final String CORESESSION_KEY = "SiteCoreSession";

    protected WebEngine engine;
    protected CoreSession session;
    protected boolean isCanceled = false;

    protected WebObject head; // the site root
    protected WebObject tail;
    protected WebObject lastResolved;

    protected HttpServletRequest request;
    protected HttpServletResponse response;

    protected String pathInfo;

    protected WebRoot root;
    protected Mapping mapping;
    protected String action; // the current object view
    protected FormData form;

    protected Map<String,Object> vars; // global vars to share between scripts


    public WebContextImpl(WebRoot root, HttpServletRequest req, HttpServletResponse resp) {
        this.request = req;
        this.root = root;
        engine = root.getWebEngine();
        this.response = resp;
        vars = new HashMap<String, Object>();
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public void cancel() {
        isCanceled = true;
    }

    public void cancel(int errorCode) {
        isCanceled = true;
        response.setStatus(errorCode);
    }

    public String getActionName() {
        return action;
    }

    public Collection<ActionDescriptor> getActions() {
        WebObject obj = getTargetObject();
        return obj != null ? obj.getActions() : null;
    }

    public Collection<ActionDescriptor> getActions(String category) {
        WebObject obj = getTargetObject();
        return obj != null ? obj.getActions(category) : null;
    }

    public Map<String, Collection<ActionDescriptor>> getActionsByCategory() {
        WebObject obj = getTargetObject();
        return obj != null ? obj.getActionsByCategory() : null;
    }

    public DocumentModel getTargetDocument() {
        WebObject obj = getTargetObject();
        return obj != null ? obj.getDocument() : null;
    }

    public Map<String, Object> getEnvironment() {
        return engine.getEnvironment();
    }

    public FormData getForm() {
        if (form == null) {
            form = new FormData(request);
        }
        return form;
    }

    public Mapping getMapping() {
        return mapping;
    }

    public String getObjectPath(DocumentModel document) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getPathInfo() {
        String path = request.getPathInfo();
        return path == null ? "/" : path;
    }

    public Principal getPrincipal() {
        return request.getUserPrincipal();
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public WebRoot getRoot() {
        return root;
    }

    public WebObject getFirstObject() {
        return head;
    }

    public CoreSession getCoreSession() throws WebException {
        if (session == null) {
            try {
                session = getCoreSession(request);
            } catch (WebException e) {
                throw e;
            } catch (Exception e) {
                throw new WebException("Failed to get core session", e);
            }
        }
        return session;
    }

    public WebObject getTargetObject() {
        return lastResolved;
    }

    public String getTargetObjectPath() {
        WebObject obj = getTargetObject();
        return obj == null ? obj.getAbsolutePath() : null;
    }

    public ScriptFile getTargetScript() throws IOException {
        String type = (lastResolved != null) ? lastResolved.getDocument().getType() : null;
        String path = null;
        if (mapping != null) {
            return root.getScript(mapping.getScript(), type);
        } else if (action != null) {
            if (lastResolved != null) {
                path = lastResolved.getActionScript(action);
            }
        }
        if (path == null) {
            WebObject first = getFirstUnresolvedObject();
            if (first != null) {
                if (first != tail) {
                    path = getPath(first, null);
                } else {
                    path = first.getName();
                }
            } else {
                path = root.getDefaultPage();
            }
        }
        return root.getScript(path, type);
    }

    public String getURI() {
        return request.getRequestURI();
    }

    public String getURL() {
        return request.getRequestURL().toString();
    }

    public String getURLPath() {
        StringBuilder buf = new StringBuilder(request.getRequestURI().length());
        String path = request.getContextPath();
        if (path == null) path = "/nuxeo/site"; // for testing
        buf.append(path).append(request.getServletPath());
        path = request.getPathInfo();
        if (path != null) {
            buf.append(path);
        }
        return buf.toString();
    }

    public String getSitePath() {
        StringBuilder buf = new StringBuilder(request.getRequestURI().length());
        String path = request.getContextPath();
        if (path == null) path = "/nuxeo/site"; // for testing
        buf.append(path).append(request.getServletPath());
        return buf.toString();
    }

    public Object getProperty(String key) {
        return vars.get(key);
    }

    public WebEngine getWebEngine() {
        return engine;
    }

    public String makeAbsolutePath(String relPath) {
        // TODO Auto-generated method stub
        return null;
    }

    public void print(String text) throws IOException {
        response.getWriter().write(text);
    }

    public void redirect(String url) throws IOException {
        response.sendRedirect(url);
    }

    public void render(String template) throws WebException {
        render(template, null);
    }

    @SuppressWarnings("unchecked")
    public void render(String template, Bindings ctx) throws WebException {
        Map map = null;
        if (ctx != null) {
            if (ctx instanceof Map) {
                map = (Map) ctx;
            } else if (ctx instanceof PyDictionary) {
                map = Scripting.convertPythonMap((PyDictionary) ctx);
            }
        }
        try {
            if (lastResolved != null) {
                engine.getScripting().getRenderingEngine().render(template, lastResolved,
                        (Map<String, Object>) map);
            } else {
                engine.getScripting().getRenderingEngine().render(template, new SiteRenderingContext(this),
                        (Map<String, Object>) map);
            }
        } catch (RenderingException e) {
            throw new WebException("Failed to render template: "+template, e);
        }
    }

    public void runScript(String script) throws WebException {
        runScript(script, null);
    }

    public void runScript(String script, Bindings args) throws WebException {
        try {
            engine.getScripting().runScript(this, root.getScript(script, null), args);
        } catch (WebException e) {
            throw e;
        } catch (Exception e) {
            throw new WebException("Failed to run script "+script, e);
        }
    }

    public void setActionName(String name) {
        this.action = name;
    }

    //XXX cleanup the web root implementation
    public void setRoot(String path) throws WebException {
        WebRoot root = engine.getSiteRoot(path);
        if (root != null) {
            this.root = root;
        } else {
            throw new WebException("No such web root: "+path);
        }
    }

    public void setProperty(String key, Object value) {
        vars.put(key, value);
    }


    public WebObject getLastObject() {
        return tail;
    }

    public boolean isRootRequest() {
        return head != null && head.next == null;
    }

    public WebObject getFirstResolvedObject() {
        if (head == null) return null;
        return head.isResolved() ? head : null;
    }

    public WebObject getLastResolvedObject() {
        return lastResolved;
    }

    public WebObject getFirstUnresolvedObject() {
        return lastResolved == null ? head : lastResolved.next;
    }

    public boolean hasUnresolvedObjects() {
        return lastResolved != tail;
    }

    public boolean resolveObject(WebObject object, DocumentModel doc) {
        if (getFirstUnresolvedObject() == object) {
            object.doc = doc;
            lastResolved = object;
            return true;
        }
        return false;
    }

    public List<WebObject> getTraversalPath() {
        ArrayList<WebObject> objects = new ArrayList<WebObject>();
        WebObject p = head;
        while (p != null) {
            objects.add(p);
            p = p.next;
        }
        return objects;
    }

    public List<WebObject> getUnresolvedObjects() {
        ArrayList<WebObject> objects = new ArrayList<WebObject>();
        WebObject p = head;
        while (p != null) {
            objects.add(p);
            p = p.next;
        }
        return objects;
    }

    public List<WebObject> getResolvedObjects() {
        ArrayList<WebObject> objects = new ArrayList<WebObject>();
        WebObject p = head;
        while (p != null) {
            objects.add(p);
            p = p.next;
        }
        return objects;
    }

    //--------------------------------------------------------------------------- TODO internal API

    /**
    *
    * @param start inclusive
    * @param end exclusive
    * @return
    */
   public static String getPath(WebObject start, WebObject end) {
       if (start == null || start == end) {
           return "";
       }
       StringBuilder buf = new StringBuilder(256);
       WebObject p = start;
       while (p != end) {
           buf.append('/').append(p.name);
           p = p.next;
       }
       return buf.toString();
   }

   public String getUnresolvedPath() {
       if (lastResolved == null) {
           return getPath(head, null);
       }
       return getPath(lastResolved.next, null);
   }

   public String getResolvedPath() {
       if (lastResolved == null) {
           return "";
       }
       return getPath(head, lastResolved.next);
   }

   /**
    * XXX should be this made part of the API? or may be createa WebContexFactory ..
    * @param name
    * @param doc
    * @return
    */
   public WebObject addWebObject(String name, DocumentModel doc) {
       WebObject object = new WebObject(this, name, doc);
       if (head == null) {
           head = tail = object;
           object.prev = null;
       } else {
           tail.next = object;
           object.prev = tail;
       }
       object.next = null;
       tail = object;
       if (doc != null) {
           lastResolved = object;
       }
       return object;
   }
   /**
    * XXX remove this method and pass mapping through ctor?
    * @param mapping
    */
   public void setMapping(Mapping mapping) {
       this.mapping = mapping;
   }

   /**
    * XXX this is a shortcut metod we need to remove
    * @return
    */
   public String getFirstUnresolvedSegment() {
       WebObject obj = getFirstUnresolvedObject();
       return obj != null ? obj .getName() : null;
   }


   @Override
   public String toString() {
       return "Resolved Path: " + getResolvedPath() + "; Unresolved Path:" + getUnresolvedPath()
               + "; Action: " + action + "; Mapping: " + (mapping == null ? "none" : mapping.getScript());
   }



    public static CoreSession getCoreSession(HttpServletRequest request)
    throws Exception {

//      for testing
        CoreSession session = (CoreSession) request.getAttribute("TestCoreSession");

        HttpSession httpSession = request.getSession(true);
        if (session == null) {
            session = (CoreSession) httpSession.getAttribute(CORESESSION_KEY);
        }
        if (session == null) {
            String repoName = getTargetRepositoryName(request);
            RepositoryManager rm = Framework.getService(RepositoryManager.class);
            Repository repo = rm.getRepository(repoName);
            if (repo == null) {
                throw new ClientException("Unable to get " + repoName
                        + " repository");
            }
            session = repo.open();
        }
        if (httpSession != null) {
            httpSession.setAttribute(CORESESSION_KEY, session);
        }
        return session;
    }

    public static String getTargetRepositoryName(HttpServletRequest req) {
        return "default";
    }




}

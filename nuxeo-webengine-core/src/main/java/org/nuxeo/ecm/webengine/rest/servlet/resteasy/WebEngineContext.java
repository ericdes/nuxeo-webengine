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

package org.nuxeo.ecm.webengine.rest.servlet.resteasy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.security.Principal;
import java.util.LinkedList;
import java.util.Map;

import javax.script.Bindings;
import javax.script.SimpleBindings;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.webengine.WebException;
import org.nuxeo.ecm.webengine.exceptions.WebResourceNotFoundException;
import org.nuxeo.ecm.webengine.rest.WebContext2;
import org.nuxeo.ecm.webengine.rest.WebEngine2;
import org.nuxeo.ecm.webengine.rest.impl.model.DocumentObject;
import org.nuxeo.ecm.webengine.rest.model.WebApplication;
import org.nuxeo.ecm.webengine.rest.model.WebObject;
import org.nuxeo.ecm.webengine.rest.model.WebView;
import org.nuxeo.ecm.webengine.rest.scripting.ScriptFile;
import org.nuxeo.ecm.webengine.rest.scripting.Scripting;
import org.nuxeo.ecm.webengine.session.UserSession;
import org.python.core.PyDictionary;
import org.resteasy.util.HttpRequestImpl;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class WebEngineContext extends HttpRequestImpl implements WebContext2 {

    protected static final Log log = LogFactory.getLog(WebContext2.class);

    protected WebApplication config;
    protected UserSession us;
    protected final LinkedList<WebObject> stack;
    protected final LinkedList<File> scriptExecutionStack;
    protected HttpServletRequest request;


    public WebEngineContext(HttpServletRequest request, HttpHeaders headers, String httpMethod, UriInfo uri) throws IOException {
        super(request.getInputStream(), headers, httpMethod, uri);
        this.us = UserSession.getCurrentSession(request.getSession(true));
        this.stack = new LinkedList<WebObject>();
        this.scriptExecutionStack = new LinkedList<File>();
        this.request = request;
    }

    public <T> T getAdapter(Class<T> adapter) {
        if (WebObject.class == adapter) {
            return (T)tail();
        } else if (WebEngine2.class == adapter) {
            return (T)this;
        }
        return null;
    }
    
    public HttpServletRequest getHttpServletRequest() {
        return request;
    }

    public void setApplication(WebApplication config) {
        this.config = config;
    }

    /**
     * @return the domain.
     */
    public WebApplication getApplication() {
        return config;
    }

    public WebEngine2 getEngine() {
        return config.getEngine();
    }

    public UserSession getUserSession() {
        return us;
    }

    public CoreSession getCoreSession() {
        return us.getCoreSession();
    }

    public Principal getPrincipal() {
        return us.getPrincipal();
    }

    /** object stack API */

    public void push(WebObject obj) {
        obj.setContext(this);
        stack.add(obj);
    }

    public WebObject pop() {
        if (stack.isEmpty()) {
            return null;
        }
        return stack.removeLast();
    }

    public WebObject tail() {
        return stack.isEmpty() ? null : stack.getLast();
    }

    public WebObject head() {
        return stack.isEmpty() ? null : stack.getFirst();
    }



    /** template and script resolver */

    public ScriptFile getFile(String path) throws IOException {
        if (path == null || path.length() == 0) {
            return null;
        }
        char c = path.charAt(0);
        if (c == '.') { // local path - use the path stack to resolve it
            File file = getCurrentScriptDirectory();
            if (file != null) {
                // get the file local path - TODO this should be done in ScriptFile?
                file = new File(file, path).getCanonicalFile();
                if (file.isFile()) {
                    return new ScriptFile(file);
                }
                // try using stacked roots
                String rootPath = config.getEngine().getRootDirectory().getAbsolutePath();
                String filePath = file.getAbsolutePath();
                path = filePath.substring(rootPath.length());
            } else {
                log.warn("Relative path used but there is any running script");
                path = new Path(path).makeAbsolute().toString();
            }
        }  else if (c == '@' && tail() != null && path.length() > 2 && path.charAt(1) == '@') {
            // workaround to support action references
            // an action shortcut
            ScriptFile script = tail().getActionScript(path.substring(2));
            if (script != null) {
                return script;
            }
        }
        return config.getFile(path);
    }

    public void pushScriptFile(File file) {
        if (scriptExecutionStack.size() > 64) { // stack limit
            throw new IllegalStateException("Script execution stack overflowed. More than 64 calls between scripts");
        }
        if (file == null) {
            throw new IllegalArgumentException("Cannot push a null file");
        }
        scriptExecutionStack.add(file);
    }

    public File popScriptFile() {
        int size = scriptExecutionStack.size();
        if (size == 0) {
            throw new IllegalStateException("Script execution stack underflowed. No script path to pop");
        }
        return scriptExecutionStack.remove(size-1);
    }

    public File getCurrentScriptFile() {
        int size = scriptExecutionStack.size();
        if (size == 0) {
            return null;
        }
        return scriptExecutionStack.get(size-1);
    }

    public File getCurrentScriptDirectory() {
        int size = scriptExecutionStack.size();
        if (size == 0) {
            return null;
        }
        return scriptExecutionStack.get(size-1).getParentFile();
    }



    /** running scripts and rendering templates */

    public void render(String template, Writer writer) throws WebException {
        render(template, null, writer);
    }

    public void render(String template, Object ctx, Writer writer) throws WebException {
        try {
            ScriptFile script = getFile(template);
            if (script != null) {
                render(script, ctx, writer);
            } else {
                throw new WebResourceNotFoundException("Template not found: "+template);
            }
        } catch (IOException e) {
            throw new WebException("Failed to get script file for: "+template);
        }
    }

    @SuppressWarnings("unchecked")
    public void render(ScriptFile script, Object ctx, Writer writer) throws WebException {
        Map map = null;
        if (ctx != null) {
            if (ctx instanceof Map) {
                map = (Map) ctx;
            } else if (ctx instanceof PyDictionary) {
                map = Scripting.convertPythonMap((PyDictionary) ctx);
            }
        }
        try {
            String template = script.getURL();
            Bindings bindings = createBindings(map);
            if (log.isDebugEnabled()) {
                log.debug("## Rendering: "+template);
            }
            pushScriptFile(script.getFile());
            //TODO =========== fix rendering ============
            config.getEngine().getRendering().render(template, bindings, writer);
        } catch (Exception e) {
            e.printStackTrace();
            throw new WebException("Failed to render template: "+script.getAbsolutePath(), e);
        } finally {
            popScriptFile();
        }
    }

    public Object runScript(String script) throws WebException {
        return runScript(script, null);
    }

    public Object runScript(String script, Map<String, Object> args) throws WebException {
        try {
            ScriptFile sf = getFile(script);
            if (sf != null) {
                return runScript(sf, args);
            } else {
                throw new WebResourceNotFoundException("Script not found: "+script);
            }
        } catch (IOException e) {
            throw new WebException("Failed to get script file: "+script, e);
        }
    }

    public Object runScript(ScriptFile script, Map<String, Object> args) throws WebException {
        try {
            pushScriptFile(script.getFile());
            return config.getEngine().getScripting().runScript(script, createBindings(args));
        } catch (WebException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new WebException("Failed to run script "+script, e);
        } finally {
            popScriptFile();
        }
    }

    public WebView getTemplate(ScriptFile script) {
        return new WebView(this, script);
    }

    public WebView getTemplate(String path) throws IOException {
        ScriptFile script = getFile(path);
        if (script == null) {
            throw new FileNotFoundException(path);
        }
        return new WebView(this, script);
    }

    public Bindings createBindings(Map<String, Object> vars) {
        Bindings bindings = new SimpleBindings();
        if (vars != null) {
            bindings.putAll(vars);
        }
        initializeBindings(bindings);
        return bindings;
    }

    protected void initializeBindings(Bindings bindings) {
        WebObject obj = tail();
        bindings.put("Context", this);
        bindings.put("Request", request); //TODO remove this?
        //bindings.put("Response", response);
        if (obj != null) {
            bindings.put("This", obj);
            bindings.put("Root", head());
            if (obj instanceof DocumentObject) {
                bindings.put("Document", ((DocumentObject)obj).getDocument());
            }
        }
        bindings.put("Config", config); // TODO use domain as Root?
        bindings.put("Engine", config.getEngine());
        //TODO
        //bindings.put("basePath", getBasePath());
        //bindings.put("appPath", getApplicationPath());
        try {
            bindings.put("Session", getCoreSession());
        } catch (Exception e) {
            e.printStackTrace(); // TODO
        }
    }


    /** deprecated methods used for compatibility */

//    /**
//     * @deprecated use {@link WebObject#getActions()}
//     */
//    Collection<ActionDescriptor> getActions() throws WebException {
//        if (!stack.isEmpty()) {
//            return stack.getLast().getActions();
//        }
//        return null;
//    }
//
//    /**
//     * @deprecated use {@link WebObject#getActions(String)}
//     * @param category
//     * @return
//     * @throws WebException
//     */
//    Collection<ActionDescriptor> getActions(String category) throws WebException {
//        if (!stack.isEmpty()) {
//            return stack.getLast().getActions(category);
//        }
//        return null;
//    }
//
//    /**
//     * @deprecated use {@link WebObject#getActionsByCategory()}
//     * @return
//     * @throws WebException
//     */
//    Map<String, Collection<ActionDescriptor>> getActionsByCategory() throws WebException {
//        if (!stack.isEmpty()) {
//            return stack.getLast().getActionsByCategory();
//        }
//        return null;
//    }

}

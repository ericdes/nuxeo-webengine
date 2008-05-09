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

import java.io.OutputStream;
import java.io.Writer;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.webengine.actions.ActionDescriptor;
import org.nuxeo.ecm.webengine.mapping.Mapping;
import org.nuxeo.ecm.webengine.scripting.ScriptFile;
import org.nuxeo.ecm.webengine.util.FormData;

/**
 * Represents the web invocation context.
 *<p>
 * This is the main entry for scripts or java modules to access the web invocation context.
 * It provides access to the HTTP request and response, to the Nuxeo core session, to the web engine and
 * all the contextual objects like traversed documents etc.
 *
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public interface WebContext {

    /**
     * Get the web engine instance
     * @return the web engine instance. Cannot return null
     */
    WebEngine getWebEngine();

    /**
     * Get the underlying HTTP servlet request object
     * @return the HTTP Request object. Cannot return null
     */
    HttpServletRequest getRequest();

    /**
     * Get the underlying HTTP servlet response object
     * @return the HTTP Response object. Cannot return null
     */
    HttpServletResponse getResponse();

    /**
     * The target context object. This is the last object that was traversed and that can be seen as the target
     * object for this request.
     * When a request is made, the URL path is mapped on a chain of {@link WebObject} objects.
     * This chain is called the traversal path. The traversal will be empty when the URL path is empty (when requesting the root).
     * In order to choose the right target object, the object chain is traversed
     * (i.e. the {@link WebObject#traverse()} method called) until an object is returning false.
     * Usually this happens when the object cannot be mapped to a Nuxeo document.
     * So by default the target object is the last object that was mapped on a Nuxeo document
     * (or the last <i>resolved</i> object).
     * But anyway this can be changed by registering a custom {@link RequestHandler}.
     *
     * @return the target object (or the target object). May return null in the case of a root request.
     */
    WebObject getTargetObject();

    /**
     * Get the target script for this request if any.
     * The target script is computed as following:
     * <ul>
     * <li>If a mapping was matched for this request the mapping is consulted for the script path to be invoked
     * <li> If the previous step returns nothing the request action (the @@XXX string that may be appended to the request URI)
     * is used to find a suitable script.
     * If no action was specified in the request URI the default @@view action is assumed.
     * <li> If no action script is found try to use the unresolved path portion to locate a script on the file system
     * (in the current web root)
     * <li> If none of these steps return a valid script, the script registered to handle unknown requests is used
     * <li> If no such script was registered null is returned (and the client will get a 404)
     * </ul>
     * XXX can this method return null?
     * @return the target script or null if none.
     */
    ScriptFile getTargetScript();

    /**
     * Get the first object in the traversal path
     * @return the root object. May return null in the case of a root request.
     */
    WebObject getRootObject();

    /**
     * Get the object that precedes the current object bin the traversal path.
     * @return the parent object.
     *      May return null if the current object is the first object in the traversal path.
     */
    WebObject getParentObject();

    /**
     * The Core Session (or Repository Session) corresponding to that request.
     * @return the core session. Cannot return null
     */
    CoreSession getSession();

    /**
     * Get the principal identifying the user that originatd the request
     * @return the current principal. Cannot return null.
     */
    Principal getPrincipal();

    /**
     * Get the Nuxeo document corresponding to the current object.
     * @return the current context document.
     *      May return null if none of the traversal objects wasn't mapped on a document
     */
    DocumentModel getCurrentDocument();

    /**
     * Get the representation of the data form submitted by the user.
     * This is providing access to both POST and GET parameters, or to multipart form data requests.
     *
     * @return the request form data. Cannot return null
     */
    FormData getForm();

    /**
     * Get the mapping that was done on the request path.
     * The mapping is a rule to rewrite the URL or to set some special variables based on a regex pattern on the request path.
     * <p>
     * Mappings are user configurable and can be used to redirect some URLs to custom scripts.
     * @return the mapping if any or null if no mapping was done on this requets
     */
    Mapping getMapping();

    /**
     * Get the request path info.
     * This is the same as the {@link HttpServletRequest#getPathInfo()} with the difference that null is neveer returned.
     * In the case when the underlying {HttpServletRequest#getPathInfo()} method returns null this method will return "/"
     * @return the path info. Cannot return null.
     */
    String getPathInfo();

    /**
     * Get the URL requested by the client. Same as {@link HttpServletRequest#getRequestURL()}
     * @return the request URL. Cannot return null.
     */
    String getURL();

    /**
     * Returns the part of this request's URL from the protocol
     * name up to the query string in the first line of the HTTP request..
     * This is the same as {@link HttpServletRequest#getRequestURI()}
     * @return the request URI. Cannot return null.
     */
    String getURI();

    /**
     * Get the path portion of the request URL
     * @return the path portion of the request URL. Cannot return null.
     */
    String getURLPath();

    /**
     * Get the path  path corresponding to the target object of the request.
     * @return the target object path. Will never return null. If the target object is null returns "/".
     */
    String getTargetObjectPath();

    /**
     * Get a suitable URI path for the given Nuxeo document, that can be used to invoke this document.
     *
     * @param document the nuxeo document
     * @return the path if any or null if no suitable path can be found
     * XXX can this method return null?
     */
    String getObjectPath(DocumentModel document); // try to resolve a nuxeo doc to a web object path

    /**
     * Given a relative path to the current script, create an absolute one (relative to the web directory)
     * If this method is not called from a script context - the base path that will be used will be the current web root.
     * @param relPath the relative path to transform
     * @return the absolute path
     */
    String makeAbsolutePath(String relPath);

    /**
     * Set the action name for this request. The action name is the string following "@@" at the end of the request URI.
     * This method can be used to change the action that will be executed.
     * This will work only if the action was not yet executed.
     * @param name the new action name
     */
    void setActionName(String name);

    /**
     * Get the request action name
     * @see WebContext#setActionName(String)
     * @return the action name or null if none
     */
    String getActionName();

    /**
     * Get the actions that are available on the target object
     * @return the target object actions or null if no target object exists
     */
    ActionDescriptor[] getActions();

    /**
     * Get the actions that are available on the target object and that are part of the given category
     * @param category the category to filter actions
     * @return the target object actions or null if no target object exists
     */
    ActionDescriptor[] getActions(String category);

    /**
     * Get the actions that are available on the target object grouped by categories
     * @return a map of category -> actions or null if no target object exists
     */
    Map<String, List<ActionDescriptor>> getActionsByCategory(); // the available actions grouped by categories ~ same as getCurrentObject().getActionsByCategory()


    /**
     * Get the current web root.
     * @return the web root. Cannot return null.
     */
    WebRoot getRoot();

    /**
     * Set the current web root.
     * <p>
     * The web root is used to resolve paths to scripts. So changing the root will change the scripts used for
     * the current request.
     * This can be used to dynamically change the way request are resolved to scripts
     *
     * @param path the absolute path relative to the web directory that points to the root base directory
     */
    void setRoot(String path);

    /**
     * Get a context variable
     * <p>
     * Context variables can be used to share data between the scripts that are called in that request (and between java code too of course)
     *
     * @param key the variable key
     * @return the variable value or null if none
     */
    Object getVar(String key);

    /**
     * Set a context variable
     * @param key the variable key
     * @param value the variable value
     * @see #getVar(String)
     */
    void setVar(String key, Object value);     // set a context variable (can be shared between scripts)

    /**
     * Get a map with environment variables. These variables are global on the web engine level.
     * @return the environment variable map. Cannot return null.
     */
    Map<String,Object> getEnv();               // get the environment vars (shared at engine level) ~ same as getEngine().getEnv()

    /**
     * Cancel any further processing
     * This can be used to inform the web engine that the next step in the request processing should be canceled and
     * request should end by sending a 200 OK code to the client.
     * <p>
     * This can be used to cancel rendering from request handlers.
     */
    void cancel();

    /**
     * Same as the previous method but the error code returned to the client can be specified by the caller.
     * @param errorCode the error code returned to the client
     * @see #cancel()
     */
    void cancel(int errorCode);

    /**
     * Redirect the client to another URL
     * @param url the URL where to redirect
     */
    void redirect(String url);

    /**
     * Render the given template using the rendering engine registered in that web engine.
     * @param template the template to render. Can be a path absolute to the web directory or relative to the
     *          caller script if any.
     */
    void render(String template);

    /**
     * Run the given script.
     * @param script the script path. Can be a path absolute to the web directory or relative to the
     *          caller script if any.
     */
    void runScript(String script);      // run the given script in the current context (path should be either relative to the current script, either abs to web dir)

    /**
     * Write some text on the HTTP request output stream
     * @param text
     */
    void print(String text);

    /**
     * Get the HTTP response writer.
     * @return the writer. Cannot return null.
     * XXX should this method be removed?
     */
    Writer getWriter();

    /**
     * Get the HTTP response output stream.
     * @return the output stream. Cannot return null.
     * XXX should this method be removed?
     */
    OutputStream getOutputStream();      // shortcut to servlet output stream

}

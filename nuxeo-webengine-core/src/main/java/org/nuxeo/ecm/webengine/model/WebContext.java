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

package org.nuxeo.ecm.webengine.model;

import java.io.IOException;
import java.io.Writer;
import java.security.Principal;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.logging.Log;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.webengine.WebEngine;
import org.nuxeo.ecm.webengine.WebException;
import org.nuxeo.ecm.webengine.forms.FormData;
import org.nuxeo.ecm.webengine.model.impl.DefaultModule;
import org.nuxeo.ecm.webengine.scripting.ScriptFile;
import org.nuxeo.ecm.webengine.session.UserSession;
import org.nuxeo.runtime.model.Adaptable;


/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public interface WebContext extends Adaptable {

    /**
     * Gets the current web application.
     *
     * @return the web root. Cannot return null.
     */
    Module getModule();

    /**
     * Get the root resource if any resource was defined as being the root
     * <p>
     * A root resource can be any resource from the resource chain.
     *
     * @return the root resource or null if no root was defined
     */
    Resource getRoot();

    /**
     * Gets the web engine instance.
     *
     * @return the web engine instance. Cannot return null
     */
    WebEngine getEngine();

    /**
     * Get the current user session.
     * <p>
     * The user session is a WebEngine abstraction for the current user session and can be
     * used to retrieve current login, core session, and to set or get user session variables
     *
     * @return the user session. Never returns null.
     */
    UserSession getUserSession();

    /**
     * The Core Session (or Repository Session) corresponding to that request.
     *
     * @return the core session. Cannot return null
     */
    CoreSession getCoreSession() throws WebException;

    /**
     * Gets the principal identifying the user that originated the request.
     *
     * @return the current principal. Cannot return null.
     */
    Principal getPrincipal();

    /**
     * Get the JAX-RS UriInfo
     * @return the uri info
     */
    UriInfo getUriInfo();

    /**
     * Get HTTP Headers as defined by JAX-RS
     * @return HTTP headers object
     */
    HttpHeaders getHttpHeaders();

    /**
     * Gets the underlying HTTP servlet request object.
     *
     * @return the HTTP Request object. Cannot return null
     */
    HttpServletRequest getRequest();

    /**
     * Get HTTP Method
     * @return the method
     */
    String getMethod();

    /**
     * Gets the representation of the data form submitted by the user. This is
     * providing access to both POST and GET parameters, or to multipart form
     * data requests.
     *
     * @return the request form data. Cannot return null
     */
    FormData getForm();

    /**
     * Gets the URL requested by the client. Same as
     * {@link HttpServletRequest#getRequestURL()}
     *
     * @return the request URL. Cannot return null.
     */
    String getURL();

    /**
     * Returns the part of this request's URL from the protocol name up to the
     * query string in the first line of the HTTP request. This is the same as
     * {@link HttpServletRequest#getRequestURI()}
     *
     * @return the request URI. Cannot return null.
     */
    String getURI();

    /**
     * Gets the path portion of the request URL.
     *
     * @return the path portion of the request URL. Cannot return null.
     */
    String getUrlPath();

    /**
     * Get the path prefix that identify the current web application.
     * The application path will include the base path (context + servlet path)
     * @return the application path. Cannot be null
     */
    String getModulePath();

    /**
     * Gets the path of the servlet. Same as servlet context path + servlet path
     *
     * @return the site path
     */
    String getBasePath();

    /**
     * Gets the URL of the base path. This is the same as {@link #getURL()}
     * after removing the path segments over the base path.
     *
     * @return the base URL
     */
    String getBaseURL();

    /**
     * TODO: should we remove this method from the context and create a specialized service to resolve document models to paths?
     * Get a suitable URI path for the given Nuxeo document, that can be used to invoke this document.
     *
     * @param document the nuxeo document
     * @return the path if any or null if no suitable path can be found
     * XXX can this method return null?
     */
    String getUrlPath(DocumentModel document); // try to resolve a nuxeo doc to a web object path

    /**
     * Sets a context variable.
     *
     * @param key the variable key
     * @param value the variable value
     * @see #getProperty(String)
     */
    void setProperty(String key, Object value);

    /**
     * Gets a context variable.
     * <p>
     * Context variables can be used to share data between the scripts that are
     * called in that request (and between Java code too of course).
     *
     * @param key
     *            the variable key
     * @return the variable value or null if none
     */
    Object getProperty(String key);

    /**
     * Gets a context variable.
     * <p>
     * Context variables can be used to share data between the scripts that are
     * called in that request (and between java code too of course).
     *
     * @param key
     *            the variable key
     * @param defaultValue
     *            the default value to use if the property doesn't exists
     * @return the variable value or the given default value if none
     */
    Object getProperty(String key, Object defaultValue);

    /**
     * Convenience method to get a cookie value
     * @param name the cookie name
     * @return the cookie value if any null otherwise
     */
    String getCookie(String name);

    /**
     * Convenience method to get a cookie value using a default value
     * @param name the cookie name
     * @param defaultValue the value to return when cookie is not set
     * @return the cookie value if any or the default if none
     */
    String getCookie(String name, String defaultValue);

    /**
     * Get a logger to be used by scripts for logging
     * @return a logger
     */
    Log getLog();

    Resource newObject(String typeName, Object ...  args) throws WebException;

    Resource newObject(ResourceType type, Object ...  args) throws WebException;

    ServiceResource newService(Resource ctx, String serviceName, Object ...  args) throws WebException;

    /** object stack API */

    DefaultModule getModuleInstance();

    Resource push(Resource obj);

    Resource pop();

    Resource tail();

    Resource head();

    Resource getTargetObject();

    ServiceResource getTargetService();


    /** template and script resolver */

    /**
     * Resolves the given path into a file.
     * <p>
     * The path is resolved as following:
     * <ol>
     * <li> if the path begin with a dot '.' then a local path is assumed and
     * the path will be resolved relative to the current executed script if any.
     * Note that the directory stack will be consulted as well. If there is no
     * current executed script then the path will be transformed into an
     * absolute path and next step is entered.
     * <li> the resolving is delegated to the current
     * {@link Module#getFile(String)} that will try to resolve the path
     * relative to each directory in the directory stack
     * </ol>
     *
     * @param path
     *            the path to resolve into a file
     * @return the file or null if the path couldn't be resolved
     * @throws IOException
     */
    ScriptFile getFile(String path) throws WebException;


    /** running scripts and rendering templates */

    /**
     * Renders the given template using the rendering engine registered in that
     * web engine.
     * <p>
     * This is similar to the {@link #render(String, Map)} method with a null
     * value for the <i>args</i> argument.
     *
     * @param template
     *            the template to render. Can be a path absolute to the web
     *            directory or relative to the caller script if any.
     *  @param writer
     *            the writer to use
     * @see #render(String, Map)
     */
    void render(String template, Writer writer) throws WebException;

    /**
     * Renders the given template using the rendering engine registered in that
     * web engine. The given arguments are passed to the rendering process as
     * context variables
     *
     * @param template
     *            the template to render
     * @param args
     *            the arguments to pass
     *  @param writer
     *            the writer to use
     *
     * @throws WebException
     */
    void render(String template, Object args, Writer writer) throws WebException;

    /**
     * Renders the given template using the rendering engine registered in that
     * web engine. The given arguments are passed to the rendering process as
     * context variables
     *
     * @param script
     *            the template to render
     * @param args
     *            the arguments to pass
     *  @param writer
     *            the writer to use
     *
     * @throws WebException
     */
    void render(ScriptFile script, Object args, Writer writer) throws WebException;


    /**
     * Runs the given script.
     *
     * @param script
     *            the script path. Can be a path absolute to the web directory
     *            or relative to the caller script if any.
     * @param args
     *            the arguments to pass
     */
    Object runScript(String script, Map<String, Object> args) throws WebException;

    /**
     * Runs the given script.
     * <p>
     * This is similar to {@link #runScript(String, Map)} with a null value for
     * the <i>args</i> argument
     *
     * @param script
     *            the script path. Can be a path absolute to the web directory
     *            or relative to the caller script if any.
     * @see #runScript(String, Map)
     */
    Object runScript(String script) throws WebException;

    /**
     * Runs the script using given arguments
     * <p>
     * This is similar to {@link #runScript(String, Map)} with a null value for
     * the <i>args</i> argument
     *
     * @param script
     *            the script path. Can be a path absolute to the web directory
     *            or relative to the caller script if any.
     * @param args
     *            a map of arguments
     * @see #runScript(String, Map)
     */
    Object runScript(ScriptFile script, Map<String, Object> args) throws WebException;
}

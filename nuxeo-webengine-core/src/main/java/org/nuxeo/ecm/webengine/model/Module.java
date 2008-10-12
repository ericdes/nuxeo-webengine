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
import java.util.List;

import org.nuxeo.ecm.webengine.ResourceBinding;
import org.nuxeo.ecm.webengine.WebEngine;
import org.nuxeo.ecm.webengine.WebException;
import org.nuxeo.ecm.webengine.exceptions.WebSecurityException;
import org.nuxeo.ecm.webengine.scripting.ScriptFile;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public interface Module {
    
    String getName();
    
    boolean isFragment();

    WebEngine getEngine();

    void flushCache();

    Module getSuperModule();
    
    /**
     * The root resource  type
     * @return
     */
    ModuleType getModuleType(); 
    
    /**
     * Get the root binding of this module
     * @return
     */
    ResourceBinding getModuleBinding();
    
    /**
     * Get a file using the configured directory stack. Each directory in the stack is asked for the file
     * until a file is found. If no file is found return null.
     * <p>
     * Note that the implementation may cache the results. 
     * To clear any cached data you may call the {@link #flushCache()} method
     *  
     * @param path the file path
     * @return null if no file found otherwise the file
     * @throws IOException if any error occurs
     */
    ScriptFile getFile(String path) throws WebException;    
    

    /**
     * Load a class given it's name. The scripting class loader will be used to load the class. 
     * @param className the class name
     * @return the class instance
     * @throws ClassNotFoundException
     */
    Class<?> loadClass(String className) throws ClassNotFoundException;

    /**
     * Get a {@link ResourceType} instance given it's name.
     * The web type lookup is performed in the following order:
     * <ol>
     * <li> First the annotated Groovy classes are checked. (web/ directory)  
     * <li> Then the configuration type registry corresponding 
     * </ol> 
     * @param typeName the type name
     * @return the web type instance
     * @throws TypeNotFoundException if no such web type was defined
     */
    ResourceType getType(String typeName) throws TypeNotFoundException;
    
    /**
     * Get the types registered within this module
     * @return the types. Cannot be null.
     */
    ResourceType[] getTypes();
    
    /**
     * Get the services registered within this module
     * @return the services. Cannot be null. 
     */
    ServiceType[] getServices();
    
    /**
     * Get the named service definition for the given resource.   
     * @param ctx the target resource 
     * @param name the service name
     * @return the service if any service with that name applies for that resource otherwise throws an exception
     * 
     * @throws WebSecurityException if the service exists but cannot be accessed in the context of that resource
     * @throws ServiceNotFoundException if no such service exists for that resource
     */
    ServiceType getService(Resource ctx, String name) throws ServiceNotFoundException;
    
    /**
     * Get the list of services that applies to the given resource  
     * @param ctx the context resource
     * @return the list of service.s Cannot be null.
     */
    List<ServiceType> getServices(Resource ctx);
    
    /**
     * Get the list of service names that applies to the given resource.  
     * @param ctx the context resource
     * @return the list of service.s Cannot be null.
     */
    List<String> getServiceNames(Resource ctx);

    /**
     * Get the list of services that are enabled for the given context.
     * Enabled services are those services which can be accessed in the current security context
     * @param ctx the context resource
     * @return the list of service.s Cannot be null.
     */
    List<ServiceType> getEnabledServices(Resource ctx);
    
    /**
     * Get the list of service names that are enabled for the given context.
     * Enabled services are those services which can be accessed in the current security context  
     * @param ctx the context resource
     * @return the list of service.s Cannot be null.
     */
    List<String> getEnabledServiceNames(Resource ctx);

    public List<LinkDescriptor> getLinks(String category);
    
    public List<LinkDescriptor> getActiveLinks(Resource context, String category);

}
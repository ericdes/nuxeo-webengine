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
 */
package org.nuxeo.ecm.webengine.loader.store;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import org.apache.commons.jci.utils.ConversionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class ResourceStoreClassLoader extends ClassLoader {

    private final Log log = LogFactory.getLog(ResourceStoreClassLoader.class);

    private final ResourceStore[] stores;

    public ResourceStoreClassLoader( final ClassLoader pParent, final ResourceStore[] stores ) {
        super(pParent);
        this.stores = stores;
    }

    protected Class<?> fastFindClass(final String name) {
        if (stores != null) {
            for (int i = 0; i < stores.length; i++) {
                final ResourceStore store = stores[i];
                final byte[] clazzBytes = store.getBytes(convertClassToResourcePath(name));
                if (clazzBytes != null) {
                    if (log.isTraceEnabled()) {
                        log.trace(getId() + " found class: " + name  + " (" + clazzBytes.length + " bytes)");
                    }
                    return defineClass(name, clazzBytes, 0, clazzBytes.length);
                }            
            }
        }
        return null;            
    }
    
    @Override
    protected URL findResource(String name) {
        if (stores != null) {
            for (int i = 0; i < stores.length; i++) {
                final ResourceStore store = stores[i];
                final URL url = store.getURL(name);
                if (url != null) {
                    if (log.isTraceEnabled()) {
                        log.trace(getId() + " found resource: " + name);    
                    }
                    return url;
                }            
            }
        }
        return null;         
    }
    
    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        if (stores != null) {
            ArrayList<URL> result = new ArrayList<URL>(); 
            for (int i = 0; i < stores.length; i++) {
                final ResourceStore store = stores[i];
                final URL url = store.getURL(name);
                if (url != null) {
                    if (log.isTraceEnabled()) {
                        log.trace(getId() + " found resource: " + name);    
                    }
                    result.add(url);
                }            
            }
            return Collections.enumeration(result);  
        }
        return null;
    }
    
    
    public synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // log.debug(getId() + " looking for: " + name);
        Class<?> clazz = findLoadedClass(name);

        if (clazz == null) {
            clazz = fastFindClass(name);
            
            if (clazz == null) {

                final ClassLoader parent = getParent();
                if (parent != null) {
                    clazz = parent.loadClass(name);
                    // log.debug(getId() + " delegating loading to parent: " + name);
                } else {
                    throw new ClassNotFoundException(name);
                }
                
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(getId() + " loaded from store: " + name);
                }
            }
        }

        if (resolve) {
            resolveClass(clazz);
        }

        return clazz;
    }

    protected Class<?> findClass( final String name ) throws ClassNotFoundException {
        final Class<?> clazz = fastFindClass(name);
        if (clazz == null) {
            throw new ClassNotFoundException(name);
        }
        return clazz;
    }
    

    
    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> urls = findResources(name);
        if (urls == null) {
            final ClassLoader parent = getParent();
            if (parent != null) {
                urls = parent.getResources(name);
            }
        }
        return urls;
        }
    
    @Override
    public URL getResource(String name) {
        URL url = findResource(name);
        if (url == null) {
            final ClassLoader parent = getParent();
            if (parent != null) {
                url = parent.getResource(name);
            }
        }
        return url;
    }
    

    protected String getId() {
        return "" + this + "[" + this.getClass().getClassLoader() + "]";
    }


    /**
     * org/my/Class.xxx -> org.my.Class
     */
    public static String convertResourceToClassName( final String pResourceName ) {
        return ConversionUtils.stripExtension(pResourceName).replace('/', '.');
    }

    /**
     * org.my.Class -> org/my/Class.class
     */
    public static String convertClassToResourcePath( final String pName ) {
        return pName.replace('.', '/') + ".class";
    }
    
}

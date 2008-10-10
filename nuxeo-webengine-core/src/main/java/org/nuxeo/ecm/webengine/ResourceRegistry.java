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

import org.nuxeo.ecm.webengine.loader.ClassProxy;

/**
 * To be implemented by backends to allow WebEngine dynamically register/unregister resources
 * 
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public interface ResourceRegistry {

    void registerSingletonResource(String path, Object resource);
    
    void registerSingletonResource(Object resource);
    
    void registerPerRequestResource(String path, Object resource);
    
    void registerPerRequestResource(Object resource);
    
    void unregisterResource(String path);
    
    void unregisterResource(Class<?> clazz);
    
    void reload();
}

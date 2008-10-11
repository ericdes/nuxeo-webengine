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

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;


/**
 * To be implemented by backends to allow WebEngine dynamically register/unregister resources
 * 
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public interface ResourceRegistry {

    void addBinding(ResourceBinding binding) throws WebException;
        
    void removeBinding(ResourceBinding binding) throws WebException;

    void clear();
    
    void reload();
    
    ResourceBinding[] getBindings();
   
    void addMessageBodyWriter(MessageBodyWriter<?> writer);
    void addMessageBodyReader(MessageBodyReader<?> reader);
    
}

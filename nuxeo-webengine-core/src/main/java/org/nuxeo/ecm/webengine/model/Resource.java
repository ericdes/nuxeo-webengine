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

import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.nuxeo.ecm.webengine.WebException;
import org.nuxeo.runtime.model.Adaptable;


/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public interface Resource extends Adaptable {

    Resource initialize(WebContext ctx, ResourceType type, Object ...  args) throws WebException;

    void dispose();

    WebContext getContext();

    Module getModule();

    ResourceType getType();

    boolean isInstanceOf(String type);

    String getPath();
    
    String getTrailingPath();
    
    String getNextSegment();
    
    String getURL();

    Resource getPrevious();

    Resource getNext();

    boolean isAdapter();

    boolean isModule();

    boolean isRoot();
    void setRoot(boolean isRoot);

    Set<String> getFacets();
    boolean hasFacet(String facet);

    List<LinkDescriptor> getLinks(String category);


    Resource newObject(String type, Object ... args);
    AdapterResource newAdapter(String type, Object ... args);
    Template getView(String fileName);
    Template getTemplate(String fileName);

    Response redirect(String uri);

    /**
     * Return the active Adapter on this object if any in the current request
     * @return the service instance or null
     */
    AdapterResource getActiveAdapter();

}

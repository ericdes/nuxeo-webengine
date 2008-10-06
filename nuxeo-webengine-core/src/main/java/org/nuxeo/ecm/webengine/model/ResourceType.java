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

import org.nuxeo.ecm.webengine.WebException;
import org.nuxeo.ecm.webengine.security.Guard;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public interface ResourceType {
    
    public final static String ROOT_TYPE_NAME = "*";

    Guard getGuard();
    
    String getName();
    
    Class<? extends Resource> getResourceClass();
    
    <T extends Resource> T newInstance() throws WebException;

    public ResourceType getSuperType();    
    
    Set<String> getFacets();
    boolean hasFacet(String facet);
    
    public ViewDescriptor getView(String name);
    public List<ViewDescriptor> getViews();
    public List<ViewDescriptor> getViews(String category);
    public List<ViewDescriptor> getEnabledViews(Resource obj);
    public List<ViewDescriptor> getEnabledViews(Resource obj, String category);
    public List<String> getViewNames();
    public List<String> getViewNames(String category);
    public List<String> getEnabledViewNames(Resource obj);
    public List<String> getEnabledViewNames(Resource obj, String category);

    boolean isEnabled(Resource ctx);
}

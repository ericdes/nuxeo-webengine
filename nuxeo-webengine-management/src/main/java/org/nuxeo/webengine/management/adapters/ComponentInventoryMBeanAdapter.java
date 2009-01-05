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
 *     matic
 */
package org.nuxeo.webengine.management.adapters;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.nuxeo.ecm.platform.relations.api.impl.AbstractResourceAdapter;
import org.nuxeo.runtime.model.ExtensionPoint;
import org.nuxeo.runtime.model.Property;
import org.nuxeo.runtime.model.RegistrationInfo;

/**
 * @author matic
 * 
 */
public class ComponentInventoryMBeanAdapter extends AbstractResourceAdapter
        implements ComponentInventoryMBean {

    public ComponentInventoryMBeanAdapter(RegistrationInfo info) {  
        this.info = info;
    }
    
    protected RegistrationInfo info;
    
   
    public String getDescription() {
        return info.getDocumentation();
    }

    public Integer getExtensionPointsCount() {
        return info.getExtensionPoints().length;
    }

    public Set<String> getExtensionPointsName() {
        Set<String> names = new HashSet<String>();
        for(ExtensionPoint extensionPoint:info.getExtensionPoints()) {
            names.add(extensionPoint.getName());
        }
        return names;
    }

    public String getName() {
        return info.getName().getRawName();
    }

    public Map<String, Property> getProperties() {
        return info.getProperties();
    }

    public Set<String> getProvidedServices() {
        Set<String> names = new HashSet<String>();
        for(String name:info.getProvidedServiceNames()) {
            names.add(name);
        }
        return names;
    }

    public Integer getProvidedServicesCount() {
        return info.getProvidedServiceNames().length;
    }

    public String getVersion() {
        return info.getVersion().toString();
    }

}

package org.nuxeo.webengine.management.adapters;

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

import java.util.HashSet;
import java.util.Set;

import javax.management.ObjectName;

import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.repository.RepositoryManager;
import org.nuxeo.ecm.core.repository.RepositoryService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.management.AbstractResourceFactory;
import org.nuxeo.runtime.management.ObjectNameFactory;
import org.nuxeo.runtime.management.ResourceDescriptor;
import org.nuxeo.runtime.management.ResourceFactory;
import org.nuxeo.runtime.management.ResourceFactoryDescriptor;

/**
 * @author matic
 * 
 */
public class RepositoryMBeanAdapterFactory extends AbstractResourceFactory
        implements ResourceFactory {

    public RepositoryMBeanAdapterFactory(ResourceFactoryDescriptor descriptor) {
        super(descriptor);
    }

    protected RepositoryManager guardedRepositoryManager() {
        RepositoryService service = null;
        try {
            service = (RepositoryService) Framework.getRuntime().getComponent(
                    RepositoryService.NAME);
        } catch (Exception cause) {
            throw new ClientRuntimeException("Cannot get repository service",
                    cause);
        }
        if (service == null) {
            throw new ClientRuntimeException("Cannot get repository service");
        }
        return service.getRepositoryManager();
    }

    public Set<ResourceDescriptor> getDescriptors() {
        RepositoryManager manager = guardedRepositoryManager();
        Set<ResourceDescriptor> descriptors = new HashSet<ResourceDescriptor>();
        ObjectName objectName = ObjectNameFactory.getObjectName(descriptor.getName());
        descriptors.add(new ResourceDescriptor(objectName,
                RepositoryManagerMBeanAdapter.class, RepositoryMBean.class,
                true));
        for (String repositoryName : manager.getRepositoryNames()) {
            ObjectName repositoryObjectName = ObjectNameFactory.getObjectName(
                    objectName, "repository", repositoryName);
            ResourceDescriptor repositoryDescriptor = new ResourceDescriptor(
                    repositoryObjectName, RepositoryMBeanAdapter.class,
                    RepositoryMBean.class, true);
            descriptors.add(repositoryDescriptor);
        }
        return descriptors;
    }
}

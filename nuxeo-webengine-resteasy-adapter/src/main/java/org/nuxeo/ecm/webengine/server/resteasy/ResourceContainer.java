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

package org.nuxeo.ecm.webengine.server.resteasy;

import javax.ws.rs.ext.RuntimeDelegate;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.nuxeo.ecm.webengine.ResourceRegistry;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentName;
import org.nuxeo.runtime.model.DefaultComponent;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class ResourceContainer extends DefaultComponent {

    public static final ComponentName NAME = new ComponentName("org.nuxeo.ecm.webengine.server");

    protected WebEngineDispatcher dispatcher;

    @Override
    public void activate(ComponentContext context) throws Exception {
        RuntimeDelegate.setInstance(new ResteasyProviderFactory());
        ResteasyProviderFactory providerFactory = new ResteasyProviderFactory();
        dispatcher = new WebEngineDispatcher(providerFactory);
    }

    @Override
    public void deactivate(ComponentContext context) throws Exception {
        dispatcher = null;
    }

    public WebEngineDispatcher getDispatcher() {
        return dispatcher;
    }

    public ResourceRegistry getRegistry() {
        return dispatcher.getResourceRegistry();
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (ResourceRegistry.class.isAssignableFrom(adapter)) {
            return adapter.cast(dispatcher.getResourceRegistry());
        }
        // TODO Auto-generated method stub
        return super.getAdapter(adapter);
    }

}

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

package org.nuxeo.ecm.webengine.rendering;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import org.nuxeo.ecm.core.schema.DocumentType;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.Component;
import org.nuxeo.runtime.model.ComponentName;
import org.nuxeo.runtime.model.RegistrationInfo;
import org.osgi.framework.Bundle;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class APIHelper implements RenderingExtensionFactory {

    public final static APIHelper INSTANCE = new APIHelper();

    public static final Comparator<DocumentType> DOCTYPE_COMPARATOR = new Comparator<DocumentType>() {
        public int compare(DocumentType o1, DocumentType o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    public Object createTemplate() {
        return this;
    }

    public final DocumentType[] getSortedDocumentTypes() {
        DocumentType[] doctypes = Framework.getLocalService(SchemaManager.class).getDocumentTypes();
        Arrays.sort(doctypes, DOCTYPE_COMPARATOR);
        return doctypes;
    }

    public final Bundle[] getBundles() {
        return Framework.getRuntime().getContext().getBundle().getBundleContext().getBundles();
    }

    public final Collection<RegistrationInfo> getComponents() {
        return Framework.getRuntime().getComponentManager().getRegistrations();
    }

    public final Collection<ComponentName> getPendingComponents() {
        return Framework.getRuntime().getComponentManager().getPendingRegistrations().keySet();
    }

}

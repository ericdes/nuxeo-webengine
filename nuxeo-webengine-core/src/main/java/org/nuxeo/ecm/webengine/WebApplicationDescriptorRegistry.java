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

import java.util.Hashtable;
import java.util.Map;


/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class WebApplicationDescriptorRegistry {

    Map<String, WebApplicationDescriptor> registry = new Hashtable<String, WebApplicationDescriptor>();

    public WebApplicationDescriptor add(WebApplicationDescriptor desc) {
        WebApplicationDescriptor base = registry.get(desc.id);
        WebApplicationDescriptor prev = null;
        while (base != null) {
            if (base.fragment == desc.fragment || (base.fragment != null && base.fragment.equals(desc.fragment))) {
                break;
            }
            prev = base;
            base = base.next();
        }
        desc.setRemoved(false);// make sure the desc is marked as active
        if (prev != null) {
            WebApplicationDescriptor next = prev.next();
            if (next != null) {
                desc.setNext(next);
            }
            prev.setNext(desc);
        } else {
            registry.put(desc.id, desc);
        }
        return get(desc.id);
    }

    public WebApplicationDescriptor remove(WebApplicationDescriptor desc) {
        WebApplicationDescriptor base = registry.get(desc.id);
        while (base != null) {
            if (base.fragment == desc.fragment || (base.fragment != null && base.fragment.equals(desc.fragment))) {
                base.setRemoved(true);
                break;
            }
            base = base.next();
        }
        return get(desc.id);
    }

    public WebApplicationDescriptor get(String id) {
        WebApplicationDescriptor desc = registry.get(id);
        if (desc == null) {
            return null;
        }
        boolean isEmpty = true;
        WebApplicationDescriptor result = new WebApplicationDescriptor();
        result.setId(id);
        do {
            if (!desc.isRemoved()) {
                isEmpty = false;
                desc.copyTo(result);
            }
            desc = desc.next();
        } while (desc != null);
        return isEmpty ? null : result;
    }

}

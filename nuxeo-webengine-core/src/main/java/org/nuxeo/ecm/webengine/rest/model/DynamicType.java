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

package org.nuxeo.ecm.webengine.rest.model;

import org.nuxeo.ecm.webengine.WebException;


/**
 *  A type that is not registered through extension points
 *  
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class DynamicType extends AbstractWebType {

    protected String className;
    protected String name;
    protected Class<? extends WebObject> klass;
    protected WebTypeManager mgr;

    public DynamicType(WebTypeManager mgr, String type) {
        this.mgr = mgr;
        int p = type.lastIndexOf('.');
        if (p > -1) {
            name = type.substring(p+1);
            className = type.substring(0, p);
        } else {
            name = type;
        }
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    public String getName() {
        return name;
    }

    public WebType getSuperType() {
        return WebType.ROOT;
    }

    public Class<? extends WebObject> getObjectClass() throws WebException {
        if (klass == null) {
            klass = resolveObjectClass(mgr, className);
        }
        return klass;
    }


}

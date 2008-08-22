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

package org.nuxeo.ecm.webengine.rest;

import java.util.HashMap;
import java.util.Map;

import org.nuxeo.ecm.webengine.rest.domains.WebDomain;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class WebEngine2 {

    protected Map<String, WebDomain> domains = new HashMap<String, WebDomain>();
    protected Map<String, WebDomain> mappings = new HashMap<String, WebDomain>();

    public WebEngine2() {
        domains.put("repository", new WebDomain());
        domains.put("root", new WebDomain());
        domains.put("admin", new WebDomain());
        domains.put("wikis", new WebDomain());
    }

    public void registerDomain(WebDomain domain) {
        //domains.put(domain.getId(), value);
    }

    public WebDomain[] getDomains() {
        return domains.values().toArray(new WebDomain[domains.size()]);
    }

    public WebDomain getDomain(String id) {
        return domains.get(id);
    }

    public WebDomain getDomainByPath(String path) {
        return mappings.get(path);
    }

    /**
     * Reload configuration
     */
    public void reload() {

    }

}

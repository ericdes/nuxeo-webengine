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

package org.nuxeo.ecm.webengine.rest.domains;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.Context;

import org.nuxeo.ecm.webengine.rest.WebEngine2;
import org.nuxeo.ecm.webengine.rest.WebObjectManager;
import org.nuxeo.ecm.webengine.rest.adapters.WebObject;

import com.sun.jersey.api.core.HttpContext;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
@ProduceMime({"text/html", "*/*"})
public class WebDomain<T extends DomainDescriptor> {

    public String id;
    public WebEngine2 engine;
    public T descriptor;

    public WebDomain(WebEngine2 engine, T desc) {
        descriptor = desc;
        this.engine = engine;
    }


    protected WebObject resolve(HttpContext ctx, HttpServletRequest req, HttpServletResponse resp, String path) throws Exception {
        return null;
    }

    @Path(value="{path}", limited=false)
    public WebObject dispatch(@PathParam("path") String path, @Context HttpContext ctx, @Context HttpServletRequest req, @Context HttpServletResponse resp) throws Exception {
        return resolve(ctx, req, resp, path);
    }

    protected WebObject getObject(String type) {
        return WebObjectManager.getCurrent().newInstance(type);
    }

}

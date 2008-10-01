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

package org.nuxeo.ecm.webengine.server.jersey;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.webengine.rest.WebContext2;
import org.nuxeo.ecm.webengine.rest.impl.AbstractWebContext;
import org.nuxeo.ecm.webengine.session.UserSession;

import com.sun.jersey.api.core.HttpContext;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class WebContextImpl extends AbstractWebContext {

    protected static final Log log = LogFactory.getLog(WebContext2.class);
    
    protected HttpContext ctx;
    protected UriInfo uri;

    public WebContextImpl(HttpContext ctx, HttpServletRequest request) {
        super (request);
        this.ctx = ctx;
    }


    public UriInfo getUriInfo() {
        if (uri == null) {
            uri = ctx.getUriInfo();
        }
        return uri;
    }
    
}

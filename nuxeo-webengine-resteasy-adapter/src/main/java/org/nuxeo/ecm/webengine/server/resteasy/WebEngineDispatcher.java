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

import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.nuxeo.ecm.webengine.ResourceRegistry;

/**
 * We need this wrapper to be able to know when resteasy is sending a 404...
 * This way we can optimize lazy module loading - because we can check for a lazy module
 * after dispatching the jax-rs request
 *
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
public class WebEngineDispatcher extends SynchronousDispatcher {

    protected final ResourceRegistryImpl resourceReg;

    public WebEngineDispatcher(ResteasyProviderFactory providerFactory) {
        super(providerFactory);
        resourceReg = new ResourceRegistryImpl(this);
        addInterceptors();
    }

    public ResourceRegistry getResourceRegistry() {
        return resourceReg;
    }


//    public void service(String httpMethod, HttpServletRequest request,
//            HttpServletResponse response) throws ServletException {
//
//        // bs: is this needed anymore?
//        // String path = request.getPathInfo();
//        // if (path == null) path = "/";
//
//        HttpHeaders headers = ServletUtil.extractHttpHeaders(request);
//        // UriInfoImpl uriInfo = ServletUtil.extractUriInfo(request,
//        // servletMappingPrefix);
//        // bs: using real servlet path
//        //UriInfoImpl uriInfo = ServletUtil.extractUriInfo(request, request.getServletPath());
//        UriInfoImpl uriInfo = UriInfoImpl.create(request);
//
//        HttpRequest in = new HttpServletInputMessage(headers,
//                new HttpRequestLazyInputStream(request), uriInfo, httpMethod.toUpperCase());
//        HttpResponse theResponse = new HttpServletResponseWrapper(response,
//                super.getProviderFactory());
////        double d = System.currentTimeMillis();
//        WebContext ctx = null;
//        try {
//            // bs: initialize webengine context
//            ctx = new WebEngineContext(in, request);
//            WebEngine.setActiveContext(ctx);
//
//            ResteasyProviderFactory.pushContext(HttpServletRequest.class, request);
//            ResteasyProviderFactory.pushContext(HttpServletResponse.class, response);
//            ResteasyProviderFactory.pushContext(SecurityContext.class, new ServletSecurityContext(
//                    request));
//            super.invoke(in, theResponse);
//        } finally {
//            if (ctx != null) {
//                UserSession us = ctx.getUserSession();
//                if (us != null) {
//                    us.terminateRequest(request);
//                }
//            }
//            ResteasyProviderFactory.clearContextData();
//            // bs: cleanup webengine context
//            WebEngine.setActiveContext(null);
////            System.out.println(">>>>>>>>>>>>"+((System.currentTimeMillis()-d)/1000));
//        }
//    }

    public void addInterceptors() {
        super.getProviderFactory().getInterceptorRegistry().registerResourceMethodInterceptor(
                new SecurityInterceptor());
    }

}

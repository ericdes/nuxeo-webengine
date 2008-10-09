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

package org.nuxeo.ecm.webengine.model.impl;

import java.security.Principal;
import java.util.List;
import java.util.Set;

import org.nuxeo.ecm.webengine.WebException;
import org.nuxeo.ecm.webengine.exceptions.WebSecurityException;
import org.nuxeo.ecm.webengine.model.LinkDescriptor;
import org.nuxeo.ecm.webengine.model.Module;
import org.nuxeo.ecm.webengine.model.Resource;
import org.nuxeo.ecm.webengine.model.ResourceType;
import org.nuxeo.ecm.webengine.model.Template;
import org.nuxeo.ecm.webengine.model.WebContext;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
// DO NOT MODIFY class declaration! Cannot use WebResourceType<?> since groovy doesn't supports wildcards for now
@SuppressWarnings("unchecked")
public abstract class AbstractResource<T extends ResourceType> implements Resource {

    protected WebContext ctx;
    protected AbstractResource<?> next;
    protected AbstractResource<?> prev;
    protected String path;
    protected T type;
    protected Template template;
    
    public Resource initialize(WebContext ctx, ResourceType type, Object ...  args) throws WebException {
        this.ctx = ctx;
        this.type = (T)type;
        this.path = ctx.getUriInfo().getMatchedURIs().get(0);
        if (!this.type.getGuard().check(this)) {
            throw new WebSecurityException("Failed to initialize object: "+getPath()+". Object is not accessible in the current context", getPath());
        }
        return this;
    }
    
    public boolean isRoot() {
        return this == ctx.getRoot();
    }    
    
    public void setRoot(boolean isRoot) {
        AbstractWebContext ctx = (AbstractWebContext)this.ctx;
        if (isRoot) {
            ctx.root = this;
        } else {
            if (ctx.root == this) {
                ctx.root = null;
            }
        }
    }
    
    public boolean isInstanceOf(String type) {
        return this.type.isDerivedFrom(type);
    }
    
    public void dispose() {
        this.ctx = null;
        this.type = null;
        this.path = null;
    }

    public Set<String> getFacets() {
        return type.getFacets();
    }
    
    public boolean hasFacet(String facet) {
        return type.hasFacet(facet);
    }

    public T getType() {
        return type;
    }
    
    public WebContext getContext() {
        return ctx;
    }
    
    public Module getModule() {
        return ctx.getModule();
    }
    

    public Resource getNext() {
        return next;
    }

    public Resource getPrevious() {
        return prev;
    }

    public String getPath() {
        return path;
    }

    public List<LinkDescriptor> getLinks(String category) {
        return ctx.getModule().getActiveLinks(this, category);
    }

    public void setTemplate(Template template) {
        this.template = template;
    }
    
    public Template getTemplate() throws WebException {
        if (template == null) {
            template = new Template(this).resolve();
        }
        return template;
    }

    public <A> A getAdapter(Class<A> adapter) {
        if (adapter == Principal.class) {
            return adapter.cast(ctx.getPrincipal());   
        }
        if (adapter == WebContext.class) {
            return adapter.cast(ctx);
        }
        if (Resource.class.isAssignableFrom(adapter)) {
            return adapter.cast(this);
        }
        return null;
    }

}

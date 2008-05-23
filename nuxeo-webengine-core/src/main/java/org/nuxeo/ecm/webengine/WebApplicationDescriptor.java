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

import java.util.ArrayList;
import java.util.List;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XNodeList;
import org.nuxeo.common.xmap.annotation.XObject;
import org.nuxeo.ecm.webengine.exceptions.WebDeployException;
import org.nuxeo.ecm.webengine.mapping.MappingDescriptor;
import org.nuxeo.ecm.webengine.resolver.DefaultDocumentResolver;
import org.nuxeo.ecm.webengine.resolver.DocumentResolver;
import org.nuxeo.runtime.deploy.CompositeContribution;
import org.nuxeo.runtime.deploy.Contribution;
import org.nuxeo.runtime.deploy.ExtensibleContribution;
import org.nuxeo.runtime.deploy.ManagedComponent;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
@XObject("webapp")
public class WebApplicationDescriptor extends CompositeContribution {

    @XNode("@id")
    @Override
    public void setContributionId(String id) { this.contributionId = id; }

    @XNode("@extends")
    @Override
    public void setBaseContributionId(String id) { this.baseContributionId = id; }

    @XNodeList(value="roots/root", type=ArrayList.class, componentType=RootDescriptor.class, nullByDefault=true)
    protected List<RootDescriptor> roots;

    @XNode("errorPage")
    protected String errorPage;

    @XNode("indexPage")
    protected String indexPage;

    @XNode("defaultPage")
    protected String defaultPage;

    @XNodeList(value="mappings/mapping", type=ArrayList.class, componentType=MappingDescriptor.class, nullByDefault=true)
    protected List<MappingDescriptor> mappings;

    @XNodeList(value="bindings/binding", type=ArrayList.class, componentType=WebObjectBindingDescriptor.class, nullByDefault=true)
    protected List<WebObjectBindingDescriptor> bindings;

    @XNodeList(value="rendering-extensions/rendering-extension", type=ArrayList.class, componentType=String.class, nullByDefault=true)
    protected List<String> renderingExtensions;

    @XNode("resolver")
    protected DefaultDocumentResolver resolver;


    public String getId() {
        return contributionId;
    }

    public String getIndexPage() {
        return indexPage;
    }

    public String getIndexPage(String defaultValue) {
        return indexPage == null ? defaultValue : indexPage;
    }

    public void setIndexPage(String indexPage) {
        this.indexPage = indexPage;
    }

    public String getDefaultPage() {
        return defaultPage;
    }

    public String getDefaultPage(String defaultValue) {
        return defaultPage == null ? "default.ftl" : defaultPage;
    }

    public void setDefaultPage(String defaultPage) {
        this.defaultPage = defaultPage;
    }

    public String getErrorPage() {
        return errorPage;
    }

    public String getErrorPage(String defaultValue) {
        return errorPage == null ? defaultValue : errorPage;
    }

    public void setErrorPage(String errorPage) {
        this.errorPage = errorPage;
    }

    public List<RootDescriptor> getRoots() {
        return roots;
    }

    public void setRoots(ArrayList<RootDescriptor> descriptors) {
        roots = descriptors;
    }

    public List<MappingDescriptor> getMappings() {
        return mappings;
    }

    public void setMappings(List<MappingDescriptor> mappings) {
        this.mappings = mappings;
    }

    public List<WebObjectBindingDescriptor> getBindings() {
        return bindings;
    }

    public void setBindings(List<WebObjectBindingDescriptor> bindings) {
        this.bindings = bindings;
    }

    public List<String> getRenderingExtensions() {
        return renderingExtensions;
    }

    public void setRenderingExtensions(List<String> templates) {
        this.renderingExtensions = templates;
    }

    public DocumentResolver getDocumentResolver() throws WebDeployException {
        if (resolver == null) {
            resolver = DefaultDocumentResolver.DEFAULT;
        }
        return resolver;
    }

    public void copyOver(ExtensibleContribution contrib) {
        WebApplicationDescriptor desc = (WebApplicationDescriptor)contrib;
        if (defaultPage != null) {
            desc.defaultPage = defaultPage;
        }
        if (indexPage != null) {
            desc.indexPage = indexPage;
        }
        if (errorPage != null) {
            desc.errorPage = errorPage;
        }
        if (resolver != null && resolver != DefaultDocumentResolver.DEFAULT) {
            desc.resolver = resolver;
        }
        if (roots != null && !roots.isEmpty()) {
            if (desc.roots == null) {
                desc.roots = new ArrayList<RootDescriptor>();
            }
            desc.roots.addAll(roots);
        }
        if (bindings != null && !bindings.isEmpty()) {
            if (desc.bindings == null) {
                desc.bindings = new ArrayList<WebObjectBindingDescriptor>();
            }
            desc.bindings.addAll(bindings);
        }
        if (mappings != null && !mappings.isEmpty()) {
            if (desc.mappings == null) {
                desc.mappings = new ArrayList<MappingDescriptor>();
            }
            desc.mappings.addAll(mappings);
        }
        if (renderingExtensions != null && !renderingExtensions.isEmpty()) {
            if (desc.renderingExtensions == null) {
                desc.renderingExtensions = new ArrayList<String>();
            }
            desc.renderingExtensions.addAll(renderingExtensions);
        }
    }

    @Override
    public void install(ManagedComponent comp, Contribution contrib) throws Exception {
        WebEngine engine = ((WebEngineComponent)comp).getEngine();
        engine.registerApplication((WebApplicationDescriptor)contrib);
    }

    @Override
    public void uninstall(ManagedComponent comp, Contribution contrib) throws Exception {
        WebEngine engine = ((WebEngineComponent)comp).getEngine();
        engine.unregisterApplication(contrib.getContributionId());
    }

}

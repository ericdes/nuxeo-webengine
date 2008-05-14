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

package org.nuxeo.ecm.platform.rendering.fm;

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.nuxeo.ecm.platform.rendering.api.RenderingEngine;
import org.nuxeo.ecm.platform.rendering.api.RenderingException;
import org.nuxeo.ecm.platform.rendering.api.RenderingTransformer;
import org.nuxeo.ecm.platform.rendering.api.ResourceLocator;
import org.nuxeo.ecm.platform.rendering.fm.adapters.DocumentObjectWrapper;
import org.nuxeo.ecm.platform.rendering.fm.extensions.BlockDirective;
import org.nuxeo.ecm.platform.rendering.fm.extensions.BlockWriter;
import org.nuxeo.ecm.platform.rendering.fm.extensions.BlockWriterRegistry;
import org.nuxeo.ecm.platform.rendering.fm.extensions.DocRefMethod;
import org.nuxeo.ecm.platform.rendering.fm.extensions.ExtendsDirective;
import org.nuxeo.ecm.platform.rendering.fm.extensions.MessagesMethod;
import org.nuxeo.ecm.platform.rendering.fm.extensions.NewMethod;
import org.nuxeo.ecm.platform.rendering.fm.extensions.SuperBlockDirective;
import org.nuxeo.ecm.platform.rendering.fm.extensions.TransformDirective;

import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class FreemarkerEngine implements RenderingEngine {

    public static final String RENDERING_ENGINE_KEY = "NX_RENDERING_ENGINE";

    protected final Configuration cfg;

    // the wrapper is not a singleton since it contains some info about the engine instance
    // so we will have one wrapper per engine instance
    protected final DocumentObjectWrapper wrapper;

    protected final Map<String, RenderingTransformer> transformers = new HashMap<String, RenderingTransformer>();

    protected final MessagesMethod messages = new MessagesMethod(null);

    protected ResourceTemplateLoader loader;

    public FreemarkerEngine() {
        this(null, null);
    }

    public FreemarkerEngine(Configuration cfg, ResourceLocator locator) {
        this.wrapper = new DocumentObjectWrapper(this);
        this.cfg = cfg == null ? new Configuration() : cfg;
        this.cfg.setWhitespaceStripping(true);
        this.cfg.setLocalizedLookup(false);
        this.cfg.setClassicCompatible(true);
        this.cfg.setObjectWrapper(wrapper);

        // custom directives goes here
        this.cfg.setSharedVariable("block", new BlockDirective());
        this.cfg.setSharedVariable("superBlock", new SuperBlockDirective());
        this.cfg.setSharedVariable("extends", new ExtendsDirective());
        this.cfg.setSharedVariable("transform", new TransformDirective());
        this.cfg.setSharedVariable("docRef", new DocRefMethod());
        this.cfg.setSharedVariable("new", new NewMethod());
        this.cfg.setSharedVariable("message", messages);

        this.cfg.setCustomAttribute(RENDERING_ENGINE_KEY, this);
        setResourceLocator(locator);
    }

    public void setMessageBundle(ResourceBundle messages) {
        this.messages.setBundle(messages);
    }

    public ResourceBundle getMessageBundle() {
        return messages.getBundle();
    }

    public void setResourceLocator(ResourceLocator locator) {
        this.loader = new ResourceTemplateLoader(locator);
        this.cfg.setTemplateLoader(loader);
    }

    public ResourceLocator getResourceLocator() {
        return this.loader.getLocator();
    }

    /**
     * @return the loader.
     */
    public ResourceTemplateLoader getLoader() {
        return loader;
    }

    public void setSharedVariable(String key, Object value) {
        try {
            cfg.setSharedVariable(key, value);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DocumentObjectWrapper getObjectWrapper() {
        return wrapper;
    }

    public Configuration getConfiguration() {
        return cfg;
    }

    public void setTransformer(String name, RenderingTransformer transformer) {
        transformers.put(name, transformer);
    }

    public RenderingTransformer getTransformer(String name) {
        return transformers.get(name);
    }


    public void render(String template, Object input, Writer writer)
            throws RenderingException {
        try {
            Template temp = cfg.getTemplate(template);
            BlockWriter bw = new BlockWriter(temp.getName(), "", new BlockWriterRegistry());
            Environment env = temp.createProcessingEnvironment(input,
                    bw, wrapper);
            env.process();
            bw.copyTo(writer);
        } catch (Exception e) {
            throw new RenderingException(e);
        }
    }

}

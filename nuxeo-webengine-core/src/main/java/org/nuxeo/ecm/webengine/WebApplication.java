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

import java.io.File;
import java.io.IOException;

import org.nuxeo.ecm.core.schema.types.Type;
import org.nuxeo.ecm.platform.rendering.api.RenderingTransformer;
import org.nuxeo.ecm.platform.rendering.api.ResourceLocator;
import org.nuxeo.ecm.webengine.mapping.Mapping;
import org.nuxeo.ecm.webengine.resolver.DocumentResolver;
import org.nuxeo.ecm.webengine.scripting.ScriptFile;
import org.nuxeo.ecm.webengine.scripting.Scripting;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public interface WebApplication extends ResourceLocator {

    Scripting getScripting();

    Mapping getMapping(String pathInfo);

    String getTypeBinding(String type);

    ObjectDescriptor getObjectDescriptor(Type type);

    void flushCache();

    String getErrorPage();

    String getIndexPage();

    String getDefaultPage();

    void setDefaultPage(String page);

    ScriptFile getScript(String path)  throws IOException;

    ScriptFile getScriptByDocumentType(String path, Type type) throws IOException;

    File getFile(String path)  throws IOException;

    WebEngine getWebEngine();

    DocumentResolver getDocumentResolver();

    void setDocumentResolver(DocumentResolver resolver);

    void registerTemplate(String id, Object obj);

    void unregisterTemplate(String id);

    void registerTransformer(String id, RenderingTransformer obj);

    void unregisterTransformer(String id);
}

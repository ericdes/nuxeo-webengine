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

package org.nuxeo.ecm.platform.rendering.wiki.filters;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.nuxeo.ecm.platform.rendering.wiki.WikiMacro;
import org.nuxeo.ecm.platform.rendering.wiki.WikiSerializerHandler;
import org.wikimodel.wem.WikiParameters;

import freemarker.core.Environment;
import freemarker.template.Template;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class FreemarkerMacro implements WikiMacro {

    public String getName() {
        return "freemarker";
    }

    public void eval(WikiParameters params, String content, WikiSerializerHandler serializer) throws Exception {
        Environment env = serializer.getEnvironment();
        if (env != null) {
            Template tpl = new Template("inline", new StringReader(content),
                    env.getConfiguration(), env.getTemplate().getEncoding());
            Writer oldw = env.getOut();
            Writer neww = new StringWriter();
            try {
            env.setOut(neww);
            env.include(tpl);
            } finally {
                env.setOut(oldw);
            }
            serializer.print(neww.toString());
        }
    }

    public void evalInline(WikiParameters params, String content,
            WikiSerializerHandler serializer) throws Exception {
        eval(params, content, serializer);
    }

}

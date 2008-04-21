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

package org.nuxeo.ecm.platform.site.tests.rendering;

import java.io.StringWriter;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.impl.DataModelImpl;
import org.nuxeo.ecm.core.api.impl.DocumentModelImpl;
import org.nuxeo.ecm.core.api.impl.blob.StreamingBlob;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.core.schema.SchemaManagerImpl;
import org.nuxeo.ecm.core.schema.XSDLoader;
import org.nuxeo.ecm.platform.rendering.fm.FreemarkerEngine;
import org.nuxeo.ecm.platform.rendering.wiki.WikiTransformer;
import org.nuxeo.ecm.platform.rendering.wiki.filters.DocumentExpression;
import org.nuxeo.ecm.platform.rendering.wiki.filters.FreemarkerMacro;
import org.nuxeo.ecm.platform.rendering.wiki.filters.PatternFilter;
import org.nuxeo.runtime.api.DefaultServiceProvider;
import org.nuxeo.runtime.services.streaming.URLSource;
import org.nuxeo.runtime.test.NXRuntimeTestCase;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class TestFreemarkerRendering extends NXRuntimeTestCase {


    FreemarkerEngine engine;

    public static void initSchemaManager() throws Exception {
        SchemaManagerImpl mgr = new SchemaManagerImpl();
        XSDLoader loader = new XSDLoader(mgr);
        loader.loadSchema("dublincore", "dc", SimpleContext.class.getClassLoader().getResource("mySchema.xsd"));
        // set a custom service provider to be able to lookup services without loading the framework
        DefaultServiceProvider provider = new DefaultServiceProvider();
        provider.registerService(SchemaManager.class, mgr);
        DefaultServiceProvider.setProvider(provider);
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        initSchemaManager();

        engine = new FreemarkerEngine();
        engine.setResourceLocator(new MyResourceLocator());

        WikiTransformer tr = new WikiTransformer();
        tr.getSerializer().addFilter(new PatternFilter("[A-Z]+[a-z]+[A-Z][A-Za-z]*", "<link>$0</link>"));
        tr.getSerializer().addFilter(new PatternFilter("NXP-[0-9]+", "<a href=\"http://jira.nuxeo.org/browse/$0\">$0</a>"));
        tr.getSerializer().registerMacro(new FreemarkerMacro());
        tr.getSerializer().registerExpression(new DocumentExpression());
        engine.setTransformer("wiki", tr);

    }

    public void testRendering() throws Exception {
        final DocumentModelImpl doc1 = new DocumentModelImpl("/root/folder/wiki1", "Test Doc 1", "File");
        doc1.addDataModel(new DataModelImpl("dublincore"));
        doc1.getPart("dublincore").get("title").setValue("The dublincore title for doc1");
        doc1.getPart("dublincore").get("description").setValue("A descripton *with* wiki code and a WikiName");
        Blob blob = new StreamingBlob(new URLSource(SimpleContext.class.getClassLoader().getResource("blob.wiki")));
        doc1.getPart("dublincore").get("content").setValue(blob);

        DocumentModelImpl doc2 = new DocumentModelImpl("/root/folder/wiki2", "Test Doc 2", "File");
        doc2.addDataModel(new DataModelImpl("dublincore"));
        doc2.getPart("dublincore").get("title").setValue("The dublincore title for doc1");
        engine.setSharedVariable("doc", doc2);

        StringWriter writer = new StringWriter();
        SimpleContext ctx = new SimpleContext(doc1, writer);

        System.err.flush();
        double s = System.currentTimeMillis();
        engine.render("c.ftl", ctx);
        double e = System.currentTimeMillis();
        System.out.println("###############################");
        System.out.println(writer.getBuffer());
        System.out.println("###############################");
        System.out.println(">>>>>>>>>> RENDERING TOOK: "+((e-s)/1000));
        System.out.println("###############################");
        System.out.flush();
        for (int i=0; i<1; i++) {
            ctx.writer = new StringWriter();
            s = System.currentTimeMillis();
            engine.render("c.ftl", ctx);
            e = System.currentTimeMillis();
            System.out.println("###############################");
            System.out.println(writer.getBuffer());
            System.out.println("###############################");
            System.out.println(">>>>>>>>>> "+(i+2)+" RENDERING TOOK: "+((e-s)/1000));
            System.out.println("###############################");
        }

    }

}

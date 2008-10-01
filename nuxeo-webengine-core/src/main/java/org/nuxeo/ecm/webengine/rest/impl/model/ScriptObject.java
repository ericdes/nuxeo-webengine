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

package org.nuxeo.ecm.webengine.rest.impl.model;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.nuxeo.ecm.webengine.WebException;
import org.nuxeo.ecm.webengine.rest.WebContext2;
import org.nuxeo.ecm.webengine.rest.annotations.WebObject;
import org.nuxeo.ecm.webengine.rest.impl.DefaultObject;
import org.nuxeo.ecm.webengine.rest.model.Resource;
import org.nuxeo.ecm.webengine.rest.model.ResourceType;
import org.nuxeo.ecm.webengine.rest.model.WebView;
import org.nuxeo.ecm.webengine.rest.scripting.ScriptFile;
import org.nuxeo.ecm.webengine.rest.scripting.Scripting;



/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
@WebObject("Script")
public class ScriptObject extends DefaultObject {

    protected ScriptFile file;

    public ScriptObject() {
    }

    @Override
    public Resource initialize(WebContext2 ctx, ResourceType<?> type) throws WebException {
        super.initialize(ctx, type);
        try {
            file = ctx.getApplication().getFile(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    @GET
    //TODO: scripts should be run by the writer?
    public Object get(@Context ServletContext servletCtx) throws WebException {
        if (file == null) {
            return null;
        } else {
            String ext = file.getExtension();
            Scripting scripting = ctx.getEngine().getScripting();
            if (file.isTemplate()) {
                return file;
            } else if (scripting.isScript(ext)) { // script
                return ctx.runScript(file, null);
            } else { // regular file
                File f = file.getFile();
                String ctype = servletCtx.getMimeType(f.getName());
                return Response.ok(f, ctype).build();
            }
        }
    }

}

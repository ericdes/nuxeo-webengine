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

package org.nuxeo.ecm.webengine.util;

import net.sf.json.JSONObject;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.DocumentModelImpl;
import org.nuxeo.ecm.core.api.model.DocumentPart;
import org.nuxeo.ecm.webengine.WebException;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class JSonHelper {

    public static JSONObject doc2JSon(DocumentModel doc) throws WebException {
        return doc2JSon(doc, (String[])null);
    }

    public static JSONObject doc2JSon(DocumentModel doc, String ... schemas) throws WebException {
        try {
        JSONObject obj = new JSONObject();
        obj.put("id", doc.getId());
        obj.put("name", doc.getName());
        obj.put("path", doc.getPathAsString());
        obj.put("isLocked", doc.isLocked());
        obj.put("lifeCycleState", doc.getCurrentLifeCycleState());
        obj.put("lifeCyclePolicy", doc.getLifeCyclePolicy());
        obj.put("type", doc.getType());
        obj.put("isVersion", doc.isVersion());
        obj.put("isProxy", doc.isProxy());
        obj.put("sourceId", doc.getSourceId());
        obj.put("facets", doc.getDeclaredFacets());
        obj.put("schemas", doc.getDeclaredSchemas());
        JSonDocumentExporter jde = new JSonDocumentExporter();
        if (schemas != null) {
            for (String schema : schemas) {
                obj.put(schema, jde.run(doc.getPart(schema)));
            }
        } else {
            for (DocumentPart part : doc.getParts()) {
                obj.put(part.getName(), jde.run(part));
            }
        }
        return obj;
        } catch (Exception e) {
            throw new WebException("Failed to export documnt as json: "+doc.getPath(), e);
        }
    }

}

/*
 * (C) Copyright 2006-2007 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     Nuxeo - initial API and implementation
 *
 * $Id$
 */

package org.nuxeo.ecm.platform.site.adapters;

import java.io.IOException;
import java.io.Writer;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.site.SiteRequest;
import org.nuxeo.ecm.platform.site.api.SiteException;
import org.nuxeo.ecm.platform.site.servlet.SiteConst;

public class NoteSiteObjectHandler extends AbstractSiteObjectHandler {

    public NoteSiteObjectHandler() {
    }

    public NoteSiteObjectHandler(DocumentModel doc) {
        super(doc);
    }

    @Override
    public void doPost(SiteRequest request) throws SiteException {
        String newContent = request.getParameter("note");
        if (newContent != null) {
            sourceDocument.setProperty("note", "note", newContent);
            try {
                sourceDocument = request.getCoreSession().saveDocument(sourceDocument);
                request.getCoreSession().save();
            } catch (SiteException e) {
                throw e;
            } catch (Exception e) {
                throw new SiteException("Error during update process", e);
            }
            doGet(request);
        } else {
            try {
                Writer writer = request.getResponse().getWriter();
                writer.write("Unable to update");
                request.cancelRendering();
                request.getResponse().setStatus(SiteConst.SC_METHOD_FAILURE);
            } catch (IOException e) {
                throw new SiteException("Error during update process", e);
            }
        }
    }

}

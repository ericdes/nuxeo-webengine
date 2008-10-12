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

package org.nuxeo.ecm.core.rest;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.webengine.WebException;
import org.nuxeo.ecm.webengine.model.WebService;
import org.nuxeo.ecm.webengine.model.impl.DefaultService;

/**
 * Lock Service - manage locks on documents
 * <p>
 * Accepts the following methods:
 * <ul>
 * <li> GET - get the Lock Owner if any 
 * <li> POST - Lock the document using current login information as the lock owner
 * <li> DELETE - Delete the lock
 * </ul>
 *  
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
@WebService(name="lock", targetType="Document")
public class LockService extends DefaultService {

    @GET
    public Object getView() {
        try {
            DocumentModel  doc =getTarget().getAdapter(DocumentModel.class);
            return ctx.getCoreSession().getLock(doc.getRef());
        } catch (Exception e) {
            throw WebException.wrap("Failed to get lock on document", e);
        }
    }
    
    
    @DELETE
    public Object removeLock() {        
        try {
            DocumentModel  doc =getTarget().getAdapter(DocumentModel.class);
            ctx.getCoreSession().unlock(doc.getRef());
            doc.refresh();
            return null; //TODO
        } catch (Exception e) {
            throw WebException.wrap("Failed to unlock document", e);
        }
    }
    
    @POST
    public Object doPost() {
        try {
            DocumentModel  doc =getTarget().getAdapter(DocumentModel.class);
            ctx.getCoreSession().setLock(doc.getRef(), ctx.getPrincipal().getName());
            doc.refresh();
            return null; //TODO
        } catch (Exception e) {
            throw WebException.wrap("Failed to lock document", e);
        }
    }
    
}

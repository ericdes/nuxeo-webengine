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

package org.nuxeo.ecm.core.rest.security;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoGroup;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.impl.ACLImpl;
import org.nuxeo.ecm.core.api.security.impl.ACPImpl;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.webengine.WebException;
import org.nuxeo.ecm.webengine.model.Resource;
import org.nuxeo.ecm.webengine.model.View;
import org.nuxeo.ecm.webengine.model.WebAdapter;
import org.nuxeo.ecm.webengine.model.impl.DefaultAdapter;
import org.nuxeo.ecm.webengine.util.ACLUtils;
import org.nuxeo.runtime.api.Framework;

/**
 * Version Service - manage document versions
 * TODO not yet implemented
 * <p>
 * Accepts the following methods:
 * <ul>
 * <li> GET - get the last document version
 * <li> DELETE - delete a version
 * <li> POST - create a new version
 * </ul>
 *
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
@WebAdapter(name="permissions", type="PermissionService", targetType="Document", targetFacets={"Folderish"})
public class PermissionService extends DefaultAdapter {

    @GET
    public Object doGet() {
        return new View(getTarget(), "permissions").resolve();
    }

    @POST
    @Path("add")
    public Response postPermission() {
        try {
            HttpServletRequest req = ctx.getRequest();
            String action = req.getParameter("action");
            String permission = req.getParameter("permission");
            String username = req.getParameter("user");

            UserManager userManager = Framework.getService(UserManager.class);
            NuxeoPrincipal user = userManager.getPrincipal(username);
            if (user == null) {
                NuxeoGroup group = userManager.getGroup(username);
                if (group == null) {
                    return Response.status(500).build();
                }
            }
            ACPImpl acp = new ACPImpl();
            ACLImpl acl = new ACLImpl(ACL.LOCAL_ACL);
            acp.addACL(acl);
            boolean granted = "grant".equals(action);
            ACE ace = new ACE(username, permission, granted);
            acl.add(ace);
            CoreSession session = ctx.getCoreSession();
            Resource target = getTarget();
            session.setACP(target.getAdapter(DocumentModel.class).getRef(), acp, false);
            session.save();
            return Response.seeOther(new URI(target.getPath())).build();
        } catch (Exception e) {
            throw WebException.wrap(e);
        }
    }

    @GET @POST
    @Path("delete")
    public Response deletePermission() {
        try {
            HttpServletRequest req = ctx.getRequest();
            String permission = req.getParameter("permission");
            String username = req.getParameter("user");
            CoreSession session = ctx.getCoreSession();
            Resource target = getTarget();
            ACLUtils.removePermission(session, target.getAdapter(DocumentModel.class).getRef(),
                    username, permission);
            session.save();
            return Response.seeOther(new URI(target.getPath())).build();
        } catch (Exception e) {
            throw WebException.wrap(e);
        }
    }

    public List<Permission> getPermissions() {
        try {
            ACP acp = ctx.getCoreSession().getACP(getTarget().getAdapter(DocumentModel.class).getRef());
            List<Permission> permissions = new ArrayList<Permission>();
            for (ACL acl : acp.getACLs()) {
                for (ACE ace : acl.getACEs()) {
                    permissions.add(new Permission(ace.getUsername(), ace.getPermission(), ace.isGranted()));
                }
            }
            return permissions;
        } catch (Exception e) {
            throw WebException.wrap("Faield to get ACLs", e);
        }
    }


}

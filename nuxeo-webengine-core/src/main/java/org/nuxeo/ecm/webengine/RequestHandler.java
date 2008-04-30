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


/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public interface RequestHandler {

    public final static RequestHandler DEFAULT = new DefaultRequestHandler();

    public void doGet(SiteObject object) throws SiteException;

    public void doHead(SiteObject object) throws SiteException;

    public void doPost(SiteObject object) throws SiteException;

    public void doPut(SiteObject object) throws SiteException;

    public void doDelete(SiteObject object) throws SiteException;

    public boolean traverse(SiteObject object)  throws SiteException;

}

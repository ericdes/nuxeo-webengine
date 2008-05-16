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

package org.nuxeo.ecm.webengine.exceptions;

import org.nuxeo.ecm.webengine.WebException;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class WebSecurityException extends WebException {

    private static final long serialVersionUID = 1L;

    public final static String ID = "security";

    protected String action;


    public WebSecurityException(String action) {
        super ("Permission on "+action+" is denied");
        this.action = action;
    }

    public WebSecurityException(String action, Throwable cause) {
        super ("Permission on "+action+" is denied", cause);
        this.action =action;
    }


    /**
     * @return the action.
     */
    public String getAction() {
        return action;
    }

    @Override
    public String getId() {
        return ID;
    }
}

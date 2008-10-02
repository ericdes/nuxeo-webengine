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

package org.nuxeo.ecm.webengine.rest.model;

import java.util.Collection;
import java.util.Map;

import org.nuxeo.ecm.webengine.WebException;
import org.nuxeo.ecm.webengine.rest.impl.ActionTypeImpl;
import org.nuxeo.ecm.webengine.rest.scripting.ScriptFile;


/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public interface ObjectResource extends Resource {

    ObjectType getType();

    ActionTypeImpl getAction(String action);

    ActionTypeImpl[] getActions();

    ActionTypeImpl[] getActions(String category);

    Map<String, Collection<ActionTypeImpl>> getActionsByCategory() throws WebException;

    ScriptFile getTemplate() throws WebException;
    
    ScriptFile getTemplate(String name) throws WebException;
    
}

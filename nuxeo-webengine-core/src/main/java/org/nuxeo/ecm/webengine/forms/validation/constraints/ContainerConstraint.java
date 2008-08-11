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

package org.nuxeo.ecm.webengine.forms.validation.constraints;

import java.util.ArrayList;
import java.util.List;

import org.nuxeo.ecm.webengine.forms.validation.Constraint;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public abstract class ContainerConstraint extends AbstractConstraint {

    protected List<Constraint> children = new ArrayList<Constraint>();

    @Override
    public void add(Constraint constraint) {
        children.add(constraint);
    }

    @Override
    public boolean isContainer() {
        return true;
    }


    /**
     * @return the children.
     */
    public List<Constraint> getChildren() {
        return children;
    }


}

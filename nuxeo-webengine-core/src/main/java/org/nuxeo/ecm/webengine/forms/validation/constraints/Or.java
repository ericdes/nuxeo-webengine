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

import org.nuxeo.ecm.webengine.forms.FormInstance;
import org.nuxeo.ecm.webengine.forms.validation.Constraint;
import org.nuxeo.ecm.webengine.forms.validation.ErrorStatus;
import org.nuxeo.ecm.webengine.forms.validation.Field;
import org.nuxeo.ecm.webengine.forms.validation.Status;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class Or extends ContainerConstraint {

    @Override
    public Status validate(FormInstance form, Field field, String rawValue, Object value) {
        if (children.isEmpty()) {
            throw new IllegalStateException("Or constraint have no content");
        }
        // FIXME: this loop doesn't loop
        for (Constraint child : children) {
            Status status = child.validate(form, field, rawValue, value);
            if (status.isOk()) {
                return Status.OK;
            } else {
                return error(status);
            }
        }
        return new ErrorStatus(field.getId()); // should never reach this
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        int end = children.size()-1;
        for (int i=0; i<end; i++) {
            Constraint c = children.get(i);
            if (c.isContainer()) {
                buf.append("(").append(c).append(") OR ");
            } else {
                buf.append(c).append(" OR ");
            }
        }
        Constraint c = children.get(end);
        if (c.isContainer()) {
            buf.append("(").append(c).append(")");
        } else {
            buf.append(c);
        }
        return buf.toString();
    }

}

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

package org.nuxeo.ecm.platform.site.rendering;

import java.io.InputStream;
import java.io.StringBufferInputStream;

/**
 * Rending Engine for Unit Tests
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 *
 */
public class DebugRenderingEngine implements SiteRenderingEngine {

    public InputStream render(InputStream template,
            OldRenderingContext renderContext) {
        StringBuffer sb = new StringBuffer();

        sb.append("====================");
        sb.append("  Template");
        sb.append(template.toString());
        sb.append("  Context");
        sb.append(renderContext.toString());
        return new StringBufferInputStream(sb.toString());
    }
}

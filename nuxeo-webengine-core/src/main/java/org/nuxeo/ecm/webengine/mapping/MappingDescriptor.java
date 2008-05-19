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

package org.nuxeo.ecm.webengine.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
@XObject("mapping")
public class MappingDescriptor {

    public static final Pattern DOLAR_PATTERN = Pattern.compile("\\$([0-9]+|[A-Za-z_][A-Za-z_0-9]*)");

    PathPattern pattern;
    ReplacementSegment[] script;
    ReplacementSegment[] traversal;

    @XNode("@pattern")
    public void setPattern(String pattern) {
        this.pattern = new PathPattern(pattern);
    }

    @XNode("script")
    public void setScript(String script) {
        if (script != null) {
            this.script = parseReplacement(script);
        }
    }

    @XNode("traversalPath")
    public void setTraversal(String traversal) {
        if (traversal != null) {
            this.traversal = parseReplacement(traversal);
        }
    }

    public static ReplacementSegment[] parseReplacement(String replacement) {
        Matcher m = DOLAR_PATTERN.matcher(replacement);
        if (!m.find()) {
            return new ReplacementSegment[] {new StringSegment(replacement)};
        }

        List<ReplacementSegment> ar = new ArrayList<ReplacementSegment>();
        int s = 0;
        do {
            int e = m.start();
            // add segment
            if (e > s) {
                ar.add(new StringSegment(replacement.substring(s, e)));
            }
            // add dolar
            String var = m.group(1);
            if (Character.isDigit(var.charAt(0))) {
                ar.add(new IndexedSegment(Integer.parseInt(var)));
            } else {
                ar.add(new NamedSegment(var));
            }
            s = m.end();
        } while (m.find());
        if (s < replacement.length()) {
            // add segment
            ar.add(new StringSegment(replacement.substring(s)));
        }
        return ar.toArray(new ReplacementSegment[ar.size()]);
    }

    public final Mapping match(String input) {
        Mapping mapping = pattern.match(input);
        if (mapping == null) {
            return null;
        }
        if (mapping.size() != 0) {
            for (int i=0; i<pattern.vars.length ; i++) {
                String name = pattern.vars[i];
                if (name != null) {
                    mapping.setName(i, name);
                }
            }
        }
        mapping.mdef = this;
        // it's matching - do the rewrite
        if (traversal != null) {
            mapping.traversalPath = mapping.resolveSegments(traversal);
        } else {
            mapping.traversalPath = new Path(input).segments; // TODO get parent segments
        }
        return mapping;
    }

}

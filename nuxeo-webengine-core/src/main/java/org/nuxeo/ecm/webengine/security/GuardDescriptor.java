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

package org.nuxeo.ecm.webengine.security;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.nuxeo.common.xmap.annotation.XContent;
import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;
import org.nuxeo.ecm.webengine.security.guards.And;
import org.nuxeo.ecm.webengine.security.guards.FacetGuard;
import org.nuxeo.ecm.webengine.security.guards.GroupGuard;
import org.nuxeo.ecm.webengine.security.guards.PermissionGuard;
import org.nuxeo.ecm.webengine.security.guards.SchemaGuard;
import org.nuxeo.ecm.webengine.security.guards.ScriptGuard;
import org.nuxeo.ecm.webengine.security.guards.TypeGuard;
import org.nuxeo.ecm.webengine.security.guards.UserGuard;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
@XObject("permission")
public class GuardDescriptor {

    @XNode("@id")
    protected String id;

    @XNode("@expression")
    protected String expression;

    protected Map<String, Guard> guards;


    public GuardDescriptor() {
        this(null);
    }

    public GuardDescriptor(String name) {
        id = name;
        guards = new HashMap<String, Guard>();
    }

    public Map<String, Guard> getGuards() {
        return guards;
    }

    /**
     * @param expression the expression to set.
     */
    public void setExpression(String expression) {
        this.expression = expression;
    }

    /**
     * @return the expression.
     */
    public String getExpression() {
        return expression;
    }

    @XContent
    protected void setGuards(DocumentFragment content) {
        Node node = content.getFirstChild();
        while (node != null) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                String name = node.getNodeName();
                if ("guard".equals(name)) {
                    NamedNodeMap map = node.getAttributes();
                    Node aId = map.getNamedItem("id");
                    Node aType = map.getNamedItem("type");
                    if (aId == null) {
                        throw new IllegalArgumentException("id is required");
                    }
                    String id = aId.getNodeValue();
                    if (aType == null) {
                        throw new IllegalArgumentException("type is required");
                    } else {
                        //String value = node.getTextContent().trim();
                        //guards.put(id, new ScriptGuard(value));
                        //TODO: compound guard
                    }
                    String type = aType.getNodeValue();
                    if ("permission".equals(type)) {
                        String value = node.getTextContent().trim();
                        guards.put(id, new PermissionGuard(value));
                    } else if ("facet".equals(type)) {
                        String value = node.getTextContent().trim();
                        guards.put(id, new FacetGuard(value));
                    } else if ("type".equals(type)) {
                        String value = node.getTextContent().trim();
                        guards.put(id, new TypeGuard(value));
                    } else if ("schema".equals(type)) {
                        String value = node.getTextContent().trim();
                        guards.put(id, new SchemaGuard(value));
                    } else if ("user".equals(type)) {
                        String value = node.getTextContent().trim();
                        guards.put(id, new UserGuard(value));
                    } else if ("group".equals(type)) {
                        String value = node.getTextContent().trim();
                        guards.put(id, new GroupGuard(value));
                    } else if ("script".equals(type)) {
                        Node engineNode = map.getNamedItem("engine");
                        if (engineNode == null) {
                            throw new IllegalArgumentException("Must specify an engine attribute on script guards");
                        }
                        String value = node.getTextContent().trim();
                        guards.put(id, new ScriptGuard(engineNode.getNodeValue(), value));
                    } else if ("expression".equals(type)) {
                        String value = node.getTextContent().trim();
                        try {
                            guards.put(id, PermissionService.getInstance().parse(value, guards));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } else { // the type should be a guard factory
                        String value = node.getTextContent().trim();
                        try {
                            Class<?> factory = Class.forName(type);
                            Guard guard = ((GuardFactory)factory.newInstance()).newGuard(value);
                            guards.put(id, guard);
                        } catch (Exception e) {
                            e.printStackTrace(); //TODO should throw a DeployException
                        }
                    }
                }
            }
            node = node.getNextSibling();
        }
    }

    public Guard getGuard() throws ParseException {
        if (expression == null || expression.length() == 0) {
            return new And(guards.values());
        }
        return PermissionService.getInstance().parse(expression, guards);
    }

    public String getId() {
        return id;
    }

    public Permission getPermission() throws ParseException {
        return new DefaultPermission(id, getGuard());
    }


}

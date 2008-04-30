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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.nuxeo.ecm.webengine.security.PostfixExpression.Token;
import org.nuxeo.ecm.webengine.security.guards.And;
import org.nuxeo.ecm.webengine.security.guards.FacetGuard;
import org.nuxeo.ecm.webengine.security.guards.GroupGuard;
import org.nuxeo.ecm.webengine.security.guards.Not;
import org.nuxeo.ecm.webengine.security.guards.Or;
import org.nuxeo.ecm.webengine.security.guards.PermissionGuard;
import org.nuxeo.ecm.webengine.security.guards.SchemaGuard;
import org.nuxeo.ecm.webengine.security.guards.TypeGuard;
import org.nuxeo.ecm.webengine.security.guards.UserGuard;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class PermissionService implements PostfixExpression.Visitor {

    private static final PermissionService instance = new PermissionService();

    protected final ConcurrentMap<String, Guard> guards; // global guards

    public static PermissionService getInstance() {
        return instance;
    }

    protected PermissionService() {
        guards = new ConcurrentHashMap<String, Guard>();
    }

    public void registerGuard(String name, Guard guard) {
        guards.put(name, guard);
    }

    public Guard unregisterGuard(String name) {
        return guards.remove(name);
    }

    public Guard getGuard(String name) {
        return guards.get(name);
    }

    public static Guard parse(String expr) throws ParseException {
        return (Guard) new PostfixExpression(expr).visit(instance);
    }

    public Guard parse(String expr, final Map<String,Guard> localGuards) throws ParseException {
        PostfixExpression.Visitor visitor = new PostfixExpression.Visitor() {
            public Object createOperation(Token token, Object lparam,
                    Object rparam) {
                return PermissionService.this.createOperation(token, lparam, rparam);
            }
            public Object createParameter(Token token) {
                Guard guard = localGuards.get(token.name);
                if (guard == null) { // assume a built-in permission name
                    return PermissionService.this.createParameter(token);
                }
                return guard;
            }
        };
        return (Guard)new PostfixExpression(expr).visit(visitor);
    }

    public Object createOperation(Token token, Object lparam, Object rparam) {
        switch (token.type) {
        case PostfixExpression.AND:
            return new And((Guard)lparam, (Guard)rparam);
        case PostfixExpression.OR:
            return new Or((Guard)lparam, (Guard)rparam);
        case PostfixExpression.NOT:
            return new Not((Guard)lparam);
        }
        throw new IllegalStateException("Supported ops are: AND, OR and NOT");
    }

    public Object createParameter(Token token) {
        String name = token.name;
        int p = name.indexOf('=');
        if (p > -1) {
            String key = name.substring(0, p).trim();
            String value = name.substring(p+1).trim();
            if ("user".equals(key)) {
                return new UserGuard(value);
            } else if ("group".equals(key)) {
                return new GroupGuard(value);
            } else if ("type".equals(key)) {
                return new TypeGuard(value);
            } else if ("facet".equals(key)) {
                return new FacetGuard(value);
            } else if ("schema".equals(key)) {
                return new SchemaGuard(value);
            } else if ("permission".equals(key)) {
                return new PermissionGuard(value);
            }
            throw new IllegalArgumentException("Invalid argument: "+name);
        } else {
            Guard guard = guards.get(token.name);
            if (guard == null) { // assume a built-in permission name
                guard = new PermissionGuard(token.name);
            }
            return guard;
        }
    }

}

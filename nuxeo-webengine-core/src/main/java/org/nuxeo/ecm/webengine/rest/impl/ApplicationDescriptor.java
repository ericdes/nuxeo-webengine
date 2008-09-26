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

package org.nuxeo.ecm.webengine.rest.impl;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.tools.ant.taskdefs.Typedef;
import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XNodeList;
import org.nuxeo.common.xmap.annotation.XNodeMap;
import org.nuxeo.common.xmap.annotation.XObject;
import org.nuxeo.ecm.webengine.RootDescriptor;
import org.nuxeo.ecm.webengine.exceptions.WebSecurityException;
import org.nuxeo.ecm.webengine.rest.PathDescriptor;
import org.nuxeo.ecm.webengine.security.Guard;
import org.nuxeo.ecm.webengine.security.GuardDescriptor;
import org.nuxeo.runtime.model.Adaptable;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
@XObject("application")
public class ApplicationDescriptor implements Cloneable {

    /**
     * The application directory
     * Must be set by the client before registering the descriptor.
     */
    public File directory;
    
    @XNode("@name")
    public String name;

    /**
     * A fragment id to be used only if this contribution is patching another one
     */
    @XNode("@fragment")
    public String fragment;

    @XNode("@extends")
    public String base;
    
    /**
     * The main class to use. The main class is the root JAX-RS resource that will be registered 
     * for this application
     */
    @XNode("main")
    public String main;

    /**
     * The path this application is bound. 
     * If this application extends another one and the path is not specified then the base application path will be used.
     * Thus the base application will be replaced by this one.  
     */
    @XNode("path")
    public PathDescriptor path;

    /** 
     * the class of the resource to serve
     * @deprecated check if this is still needed 
     * */
    @XNode("type")
    public String type;

    /**
     * @deprecated you should use properties instead - this field is too specialized 
     */
    @XNode("contentRoot")
    public String contentRoot;

    @XNodeMap(value="property", key="property@name", componentType=String.class, type=HashMap.class, nullByDefault=false)
    public HashMap<String, Object> properties;

    @XNodeList(value="types/type", componentType=TypeDescriptor.class, type=ArrayList.class, nullByDefault=false)
    public ArrayList<TypeDescriptor> types;

    @XNodeList(value="actions/action", componentType=ActionDescriptor.class, type=ArrayList.class, nullByDefault=false)
    public ArrayList<ActionDescriptor> actions;

    @XNode("error-page")
    public String errorPage = "error.ftl";

    @XNode("index-page")
    public String indexPage = "index.ftl";

    @XNode("default-page")
    public String defaultPage = "default.ftl";

    @XNode("script-extension")
    public String scriptExtension = "groovy";

    @XNode("template-extension")
    public String templateExtension = "ftl";

    @XNodeList(value="roots/root", type=ArrayList.class, componentType=RootDescriptor.class, nullByDefault=true)
    public List<RootDescriptor> roots;

    @XNode("permission")
    public  GuardDescriptor guardDescriptor;

    private Guard guard;

    public void checkPermission(Adaptable adaptable) throws WebSecurityException {
        if (!getGuard().check(adaptable)) {
            throw new WebSecurityException("Access Restricted");
        }
    }

    public Guard getGuard() {
        if (guard == null) {
            try {
                guard = guardDescriptor != null? guardDescriptor.getGuard() : Guard.DEFAULT;
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        }
        return guard;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() == ApplicationDescriptor.class) {
            ApplicationDescriptor dd = (ApplicationDescriptor)obj;
            return dd.name.equals(name) && TypeDescriptor.streq(dd.fragment, fragment);
        }
        return false;
    }
    
    @Override
    public ApplicationDescriptor clone() {
        try {
            ApplicationDescriptor cfg = (ApplicationDescriptor)super.clone();
            cfg.actions = (ArrayList)actions.clone();
            cfg.types = (ArrayList)types.clone();
            return cfg; 
        } catch (CloneNotSupportedException e) {
            throw new Error("Should never happen");
        }
    }
    
}

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
 *     matic
 */
package org.nuxeo.ecm.client.commands;

import java.net.URL;

import org.nuxeo.ecm.client.Command;

/**
 * @author matic
 * 
 */
public abstract class AbstractCommand<T> implements Command<T> {

    protected final String serviceName;
    protected final String methodName;

    protected AbstractCommand(String serviceName, String methodName) {
        this.serviceName = serviceName;
        this.methodName = methodName;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public String getMethodName() {
        return methodName;
    }

    public abstract String formatURL(URL baseURL);
}

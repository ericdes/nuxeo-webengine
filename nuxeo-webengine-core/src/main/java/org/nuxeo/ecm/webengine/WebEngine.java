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

package org.nuxeo.ecm.webengine;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.ResourceBundle;


/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public interface WebEngine {

    ResourceBundle getMessageBundle();

    File getRootDirectory();

    void registerObject(ObjectDescriptor obj);

    void unregisterObject(ObjectDescriptor obj);

    String getTypeBinding(String type);

    void registerBinding(String type, String objectId);

    void unregisterBinding(String type);

    Collection<ObjectDescriptor> getObjects();

    ObjectDescriptor getObject(String id);

    void reset();

    Map<String, Object> getEnvironment();

    WebApplication getApplication(String name);

    void registerApplication(WebApplicationDescriptor desc) throws WebException;

    void unregisterApplication(String id);

    WebApplication[]  getApplications();

    void addConfigurationChangedListener(ConfigurationChangedListener listener);

    void removeConfigurationChangedListener(ConfigurationChangedListener listener);

    void fireConfigurationChanged() throws WebException;

    void registerRenderingExtension(String id, Object obj);

    void unregisterRenderingExtension(String id);

    Object getRenderingExtension(String id);

    void destroy();

}

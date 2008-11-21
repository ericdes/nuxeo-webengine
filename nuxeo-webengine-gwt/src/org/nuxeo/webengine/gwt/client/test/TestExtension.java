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

package org.nuxeo.webengine.gwt.client.test;


import org.nuxeo.webengine.gwt.client.Application;
import org.nuxeo.webengine.gwt.client.ui.Item;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class TestExtension extends Item {

    public TestExtension() {
        super("testExt", new Label("Test Extension!"));
        setTitle("Test Extension");
        setPreferredIndex(30);
    }
    
    @Override
    public Image getIcon() {
        return Application.getImages(Images.class).contactsgroup().createImage();
    }

}

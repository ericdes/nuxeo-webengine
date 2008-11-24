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

package org.nuxeo.ecm.webengine.gwt.client.ui;

import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.HTML;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class ObjectViewManager extends ItemDeck {

    public ObjectViewManager() {
        super("editorContainer");
    }
    
    public void open(Object input) {
        DeckPanel panel = getDeckPanel();
        int cnt = panel.getWidgetCount();
        for (int i=0; i<cnt; i++) {
            View view = (View)panel.getWidget(i); 
            if (view.acceptInput(input)) {
                view.setInput(input);
                return;
            }
        }
        // no suitable view was found. create a new view to catch all contexts
        DefaultView v = new DefaultView();
        panel.add(v);
        v.setInput(input);
        panel.showWidget(panel.getWidgetCount()-1);
    }
    
    @Override
    public void add(Item item) {
        // we need to make sure the last item is always the default one 
        DeckPanel panel = getDeckPanel();
        if (panel.getWidget(panel.getWidgetCount()-1) instanceof DefaultView) {
            panel.insert(item, panel.getWidgetCount()-1);
        } else {
            panel.add(item);
        }
    }

    @Override
    public void insert(Item item, int beforeIndex) {
        DeckPanel panel = getDeckPanel();
        if (beforeIndex >= panel.getWidgetCount()) {
            if (panel.getWidget(panel.getWidgetCount()-1) instanceof DefaultView) {
                beforeIndex = panel.getWidgetCount()-1;
            }
        }
        panel.insert(item, beforeIndex);
    }
    
    static class DefaultView extends View {
        public DefaultView() {
            super("_default_", new HTML());
        }
        @Override
        public boolean acceptInput(Object input) {
            return true;
        }
        public void setInput(Object input) {
            ((HTML)getWidget()).setText("No view was registered for the object: "+input.toString());
        }
    }
}

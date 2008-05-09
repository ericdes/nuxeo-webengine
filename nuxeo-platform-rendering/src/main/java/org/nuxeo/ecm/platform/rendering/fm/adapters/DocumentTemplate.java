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
 *     bstefanescu
 *
 * $Id$
 */

package org.nuxeo.ecm.platform.rendering.fm.adapters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.rendering.api.DocumentContextView;
import org.nuxeo.ecm.platform.rendering.api.DocumentField;

import freemarker.template.AdapterTemplateModel;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * TODO document template should not be aware of rendering context ?
 *
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class DocumentTemplate implements TemplateHashModelEx, AdapterTemplateModel {

    protected final ObjectWrapper wrapper;
    protected final DocumentModel doc;


    public DocumentTemplate(ObjectWrapper wrapper, DocumentModel doc) {
        this.doc = doc;
        this.wrapper = wrapper;
    }

    @SuppressWarnings("unchecked")
    public Object getAdaptedObject(Class hint) {
        return doc;
    }

    public DocumentModel getDocument() {
        return doc;
    }

    public TemplateModel get(String key) throws TemplateModelException {
        try {
            Object value = DocumentContextView.DEFAULT.get(doc, key);
            if (value != DocumentContextView.UNKNOWN) {
                return wrapper.wrap(value);
            }
        } catch(Exception e) {
            throw new TemplateModelException("Failed to get document field: "+key, e);
        }
        return null;
    }

    public CoreSession getSession() {
        return CoreInstance.getInstance().getSession(doc.getSessionId());
    }

    /**
     * A doc model is never empty.
     */
    public boolean isEmpty() throws TemplateModelException {
        return false;
    }

    public Collection<String> getRawKeys() {
        return DocumentContextView.DEFAULT.getFields().keySet();
    }

    public TemplateCollectionModel keys() throws TemplateModelException {
        return (TemplateCollectionModel)wrapper.wrap(getRawKeys());
    }

    public Collection<Object> getRawValues() throws TemplateModelException {
        List<Object> values = new ArrayList<Object>();
        try {
            Collection<DocumentField> fields = DocumentContextView.DEFAULT.getFields().values();
            for (DocumentField field : fields) {
                values.add(field.getValue(doc));
            }
        } catch (Exception e) {
            throw new TemplateModelException("failed to fetch field values", e);
        }
        return values;
    }


    public TemplateCollectionModel values() throws TemplateModelException {
        return (TemplateCollectionModel)wrapper.wrap(getRawValues());
    }

    public int size() throws TemplateModelException {
        return DocumentContextView.DEFAULT.size(null);
    }


}

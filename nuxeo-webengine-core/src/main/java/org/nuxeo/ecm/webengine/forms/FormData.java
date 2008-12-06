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

package org.nuxeo.ecm.webengine.forms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.impl.blob.StreamingBlob;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.core.api.model.impl.primitives.BlobProperty;
import org.nuxeo.ecm.core.schema.types.ListType;
import org.nuxeo.ecm.core.schema.types.Type;
import org.nuxeo.ecm.platform.versioning.api.VersioningActions;
import org.nuxeo.ecm.webengine.WebException;
import org.nuxeo.ecm.webengine.servlet.WebConst;
import org.nuxeo.runtime.services.streaming.ByteArraySource;
import org.nuxeo.runtime.services.streaming.InputStreamSource;
import org.nuxeo.runtime.services.streaming.StreamSource;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class FormData implements FormInstance {

    public static final String PROPERTY = "property";
    public static final String TITLE = "dc:title";
    public static final String DOCTYPE = "doctype";
    public static final String VERSIONING = "versioning";
    public static final String MAJOR = "major";
    public static final String MINOR = "minor";

    protected static ServletFileUpload fu = new ServletFileUpload(new DiskFileItemFactory());

    protected final HttpServletRequest request;
    protected boolean isMultipart = false;
    protected RequestContext ctx;

    // Multipart items cache
    protected Map<String, List<FileItem>> items;
    // parameter map cache - used in Multipart forms to convert to ServletRequest#getParameterMap
    // format
    //protected Map<String, String[]> parameterMap;

    public FormData(HttpServletRequest request) {
        this.request = request;
        isMultipart = getIsMultipartContent();
        if (isMultipart) {
            ctx = new ServletRequestContext(request);
        }
    }

    private boolean getIsMultipartContent() {
        if (!"post".equals(request.getMethod().toLowerCase())) {
            return false;
        }
        String contentType = request.getContentType();
        if (contentType == null) {
            return false;
        }
        return contentType.toLowerCase().startsWith(WebConst.MULTIPART);
    }

    public boolean isMultipartContent() {
        return isMultipart;
    }


    @SuppressWarnings("unchecked")
    public Map<String, String[]> getFormFields() {
        if (isMultipart) {
            return getMultiPartFormFields();
        } else {
            return request.getParameterMap();
        }
    }

    public Map<String, String[]> getMultiPartFormFields() {
        Map<String, List<FileItem>> items = getMultiPartItems();
        Map<String, String[]> result = new HashMap<String, String[]>();
        for (Map.Entry<String,List<FileItem>> entry : items.entrySet()) {
            List<FileItem> list = entry.getValue();
            String[] ar = new String[list.size()];
            for (int i=0; i<ar.length; i++) {
                ar[i++] = list.get(i).getString();
            }
            result.put(entry.getKey(), ar);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public Map<String, List<FileItem>> getMultiPartItems() {
        if (items == null) {
            if (!isMultipart) {
                throw new IllegalStateException("Not in a multi part form request");
            }
            try {
                items = new HashMap<String, List<FileItem>>();
                ServletRequestContext ctx = new ServletRequestContext(request);
                List<FileItem> fileItems = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(ctx);
                for (FileItem item : fileItems) {
                    String key = item.getFieldName();
                    List<FileItem> list = items.get(key);
                    if (list == null) {
                        list = new ArrayList<FileItem>();
                        items.put(key, list);
                    }
                    list.add(item);
                }
            } catch (FileUploadException e) {
                throw new WebException("Failed to get uploaded files", e);
            }
        }
        return items;
    }

    @SuppressWarnings("unchecked")
    public Collection<String> getKeys() {
        if (isMultipart) {
            return getMultiPartItems().keySet();
        } else {
            return ((Map<String, String[]>) request.getParameterMap()).keySet();
        }
    }

    public Blob getBlob(String key) {
        FileItem item = getFileItem(key);
        return item == null ? null : getBlob(item);
    }

    public Blob[] getBlobs(String key) {
        List<FileItem> list = getFileItems(key);
        Blob[] ar = null;
        if (list != null) {
            ar = new Blob[list.size()];
            for (int i=0,len=list.size(); i<len; i++) {
                ar[i] = getBlob(list.get(i));
            }
        }
        return ar;
    }

    /**
     * XXX TODO implement it
     */
    public Map<String, Blob[]> getBlobFields() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public Blob getFirstBlob() {
        Map<String, List<FileItem>> items = getMultiPartItems();
        for (List<FileItem> list : items.values()) {
            for (FileItem item : list) {
                if (!item.isFormField()) {
                    return getBlob(item);
                }
            }
        }
        return null;
    }

    protected Blob getBlob(FileItem item) {
        StreamSource src;
        if (item.isInMemory()) {
            src = new ByteArraySource(item.get());
        } else {
            try {
                src = new InputStreamSource(item.getInputStream());
            } catch (IOException e) {
                throw new WebException("Failed to get blob data", e);
            }
        }
        String ctype  = item.getContentType();
        StreamingBlob blob = new StreamingBlob(src, ctype == null ? "application/octet-stream" : ctype);
        blob.setFilename(item.getName());
        return blob;
    }

    public final FileItem getFileItem(String key) {
        Map<String, List<FileItem>> items = getMultiPartItems();
        List<FileItem> list = items.get(key);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public final List<FileItem> getFileItems(String key) {
        return getMultiPartItems().get(key);
    }

    public String getMultiPartFormProperty(String key) {
        FileItem item = getFileItem(key);
        return item == null ? null : item.getString();
    }

    public String[] getMultiPartFormListProperty(String key) {
        List<FileItem> list = getFileItems(key);
        String[] ar = null;
        if (list != null) {
            ar = new String[list.size()];
            for (int i=0,len=list.size(); i<len; i++) {
                ar[i] = list.get(i).getString();
            }
        }
        return ar;
    }

    /**
     *
     * @param key
     * @return an array of strings or an array of blobs
     */
    public Object[] getMultiPartFormItems(String key) {
        return getMultiPartFormItems(getFileItems(key));
    }

    public Object[] getMultiPartFormItems(List<FileItem> list) {
        Object[] ar = null;
        if (list != null) {
            if (list.isEmpty()) {
                return null;
            }
            FileItem item0 = list.get(0);
            if (item0.isFormField()) {
                ar = new String[list.size()];
                ar[0] = item0.getString();
                for (int i=1,len=list.size(); i<len; i++) {
                    ar[i] = list.get(i).getString();
                }
            } else {
                ar = new Blob[list.size()];
                ar[0] = getBlob(item0);
                for (int i=1,len=list.size(); i<len; i++) {
                    ar[i] = getBlob(list.get(i));
                }
            }
        }
        return ar;
    }

    public final Object getFileItemValue(FileItem item) {
        if (item.isFormField()) {
            return item.getString();
        } else {
            return getBlob(item);
        }
    }

    public String getFormProperty(String key) {
        String[] value = request.getParameterValues(key);
        if (value != null && value.length > 0) {
            return value[0];
        }
        return null;
    }

    public String[] getFormListProperty(String key) {
        return request.getParameterValues(key);
    }

    public String getString(String key) {
        if (isMultipart) {
            return getMultiPartFormProperty(key);
        } else {
            return getFormProperty(key);
        }
    }

    public String[] getList(String key) {
        if (isMultipart) {
            return getMultiPartFormListProperty(key);
        } else {
            return getFormListProperty(key);
        }
    }

    public Object[] get(String key) {
        if (isMultipart) {
            return getMultiPartFormItems(key);
        } else {
            return getFormListProperty(key);
        }
    }

    public void fillDocument(DocumentModel doc) {
        try {
            if (isMultipart) {
                fillDocumentFromMultiPartForm(doc);
            } else {
                fillDocumentFromForm(doc);
            }
        } catch (PropertyException e) {
            throw new WebException("Failed to fill document properties from request properties", e);
        }
    }

    @SuppressWarnings("unchecked")
    public void fillDocumentFromForm(DocumentModel doc) throws PropertyException {
        Map<String, String[]> map = request.getParameterMap();
        for (Map.Entry<String,String[]> entry : map.entrySet()) {
            String key = entry.getKey();
            if (key.indexOf(':') > -1) { // an XPATH property
                Property p;
                try {
                    p = doc.getProperty(key);
                } catch (PropertyException e) {
                    continue; // not a valid property
                } catch (ClientException e) {
                    throw WebException.wrap(e);
                }
                String[] ar = entry.getValue();
                fillDocumentProperty(p, key, ar);
            }
        }
    }

    public void fillDocumentFromMultiPartForm(DocumentModel doc) throws PropertyException {
        Map<String,List<FileItem>> map = getMultiPartItems();
        for (Map.Entry<String,List<FileItem>> entry : map.entrySet()) {
            String key = entry.getKey();
            if (key.indexOf(':') > -1) { // an XPATH property
                Property p;
                try {
                    p = doc.getProperty(key);
                } catch (PropertyException e) {
                    continue; // not a valid property
                } catch (ClientException e) {
                    throw WebException.wrap(e);
                }
                List<FileItem> list = entry.getValue();
                if (list.isEmpty()) {
                    fillDocumentProperty(p, key, null);
                } else {
                    Object[] ar = getMultiPartFormItems(list);
                    fillDocumentProperty(p, key, ar);
                }
            }
        }
    }

    static void fillDocumentProperty(Property p, String key, Object[] ar) throws PropertyException {
        if (ar == null || ar.length == 0) {
            p.remove();
        } else if (p.isScalar()) {
            p.setValue(ar[0]);
        } else if (p.isList()) {
            if (!p.isContainer()) { // an array
                p.setValue(ar);
            } else {
                Type elType = ((ListType)p.getType()).getFieldType();
                if (elType.isSimpleType()) {
                    p.setValue(ar);
                } else if ("content".equals(elType.getName())) {
                    // list of blobs
                    List<Blob> blobs = new ArrayList<Blob>();
                    if (ar.getClass().getComponentType() == String.class) { // transform strings to blobs
                        for (Object obj : ar) {
                            blobs.add(new StringBlob(obj.toString()));
                        }
                    } else {
                        for (Object obj : ar) {
                            blobs.add((Blob) obj);
                        }
                    }
                    p.setValue(blobs);
                } else {
                    // complex properties will be ignored
                    //throw new WebException("Cannot create complex lists properties from HTML forms");
                }
            }
        } else if (p.isComplex()) {
            if (p.getClass() == BlobProperty.class) {
                // should be a file upload
                Blob blob = null;
                if (ar[0].getClass() == String.class) {
                    blob = new StringBlob(ar[0].toString());
                } else {
                    blob = (Blob)ar[0];
                }
                p.setValue(blob);
            } else {
                // complex properties will be ignored
//                throw new WebException(
//                        "Cannot set complex properties from HTML forms. You need to set each sub-scalar property explicitely");
            }
        }
    }

    public VersioningActions getVersioningOption() {
        String val = getString(VERSIONING);
        if (val != null) {
            return val.equals(MAJOR) ? VersioningActions.ACTION_INCREMENT_MAJOR
                    : val.equals(MINOR) ? VersioningActions.ACTION_INCREMENT_MINOR : null;
        }
        return null;
    }

    public String getDocumentType() {
        return getString(DOCTYPE);
    }

    public String getDocumentTitle() {
        return getString(TITLE);
    }

}

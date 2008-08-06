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

package org.nuxeo.ecm.webengine.validation;

import java.net.URL;

import org.nuxeo.common.xmap.XMap;
import org.nuxeo.common.xmap.annotation.XContent;
import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;
import org.nuxeo.ecm.webengine.validation.constraints.And;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.java_cup.internal.sym;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
@XObject(value = "field", order = {"@type"})
public class Field {

    @XNode("@id") protected String id;
    @XNode("@required") protected boolean required = false;
    @XNode("@min-length") protected int minLength = -1;
    @XNode("@max-length") protected int maxLength = -1;
    @XNode("@length") protected int maxCount = -1;
    @XNode("@length") protected int minCount = -1;

    protected Form form;

    @XNode("@type")
    void setType(String type) {
        handler = TypeHandler.getHandler(type);
        if (handler == null) {
            throw new IllegalArgumentException("Unknown type handler:  "+type);
        }
    }
    protected TypeHandler handler = TypeHandler.STRING;

    @XContent("constraints")
    void setConstraints(DocumentFragment body) {
        root = new And();
        try {
            loadChildren(this, body, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected Constraint root;

    public Field() {

    }

    public Field(TypeHandler handler, Constraint root) {
        this.root = root;
        this.handler = handler;
    }

    /**
     * @return the handler.
     */
    public TypeHandler getHandler() {
        return handler;
    }

    /**
     * @return the id.
     */
    public String getId() {
        return id;
    }

    /**
     * @return the maxCount.
     */
    public int getMaxCount() {
        return maxCount;
    }

    /**
     * @return the maxLength.
     */
    public int getMaxLength() {
        return maxLength;
    }

    /**
     * @return the minLength.
     */
    public int getMinLength() {
        return minLength;
    }

    /**
     * @return the minCount.
     */
    public int getMinCount() {
        return minCount;
    }

    /**
     * @param maxCount the maxCount to set.
     */
    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    /*
    /**
     * @param maxLength the maxLength to set.
     */
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * @param handler the handler to set.
     */
    public void setHandler(TypeHandler handler) {
        this.handler = handler;
    }

    /**
     * @param minCount the minCount to set.
     */
    public void setMinCount(int minCount) {
        this.minCount = minCount;
    }

    /**
     * @param minLength the minLength to set.
     */
    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    /**
     * @param required the required to set.
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * @return the required.
     */
    public boolean isRequired() {
        return required;
    }

    public void setForm(Form form) {
        this.form = form;
    }

    public Object decode(String value) {
        return handler.decode(value);
    }

    public ValidationStatus validate(String value) {
        //TODO check common constraints
        return root.validate(this, value, handler.decode(value));
    }

    @Override
    public String toString() {
        return id;
    }

    protected void loadChildren(Field field, Node body, Constraint constraint) throws Exception {
        NodeList nodes = body.getChildNodes();
        for (int i=0, len=nodes.getLength(); i<len; i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                loadElement(field, (Element)node, constraint);
            }
        }
    }

    protected void loadElement(Field field, Node body, Constraint root) throws Exception {
        String name = body.getNodeName();
        Constraint constraint = Constraints.newConstraint(name);
        if (constraint.isContainer()) {
            loadChildren(field, body, constraint);
        } else {
            String value = body.getTextContent();
            constraint.init(field, value);
        }
        root.add(constraint);
    }

    public Constraint getConstraints() {
        return root;
    }

    public static void main(String[] args) throws Exception {

        XMap xmap = new XMap();
        URL url = Field.class.getResource("test.xml");
        xmap.register(Field.class);
        Object[] objects = xmap.loadAll(url);

        for (Object obj : objects) {
            Field f = (Field)obj;
            System.out.println(f.getConstraints());
        }

        Field f = (Field)objects[0];
        System.out.println("--------------");
        System.out.println(f.validate("12"));
        System.out.println(f.validate("35"));
        System.out.println(f.validate("40"));
        System.out.println(f.validate("41"));

        f = (Field)objects[1];
        System.out.println("--------------");
        System.out.println(f.validate("AC@dc"));
        System.out.println(f.validate("AC@Dc"));

        f = (Field)objects[2];
        System.out.println("--------------");
        System.out.println(f.validate("abc"));
        System.out.println(f.validate("def"));
        System.out.println(f.validate("abcdef"));
        System.out.println(f.validate("abc def"));
        System.out.println(f.validate("ab"));

        f = (Field)objects[3];
        System.out.println("--------------");
        System.out.println(f.validate("abc"));
        System.out.println(f.validate("def"));
        System.out.println(f.validate("abcdef"));
        System.out.println(f.validate("abc def"));
        System.out.println(f.validate("ab"));

    }

}

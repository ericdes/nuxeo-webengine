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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class PostfixExpression implements Iterable<PostfixExpression.Token> {

    public static final int ARG = 0;
    public static final int NOT = 1;
    public static final int AND = 2;
    public static final int OR = 3;
    public static final int PARA = 4;
    public static final int LPARA = 5;
    public static final int RPARA = 6;

    protected Token[] expr;


    public PostfixExpression(String expr) throws ParseException {
        parse(expr);
    }


    public Token[] getExpression() {
        return expr;
    }

    @SuppressWarnings("unchecked")
    public Iterator<Token> iterator() {
        return Arrays.asList(expr).iterator();
    }

    private static void pushOp(Token tok, OpStack stack, List<Token> result) {
        if (!stack.isEmpty() && stack.top().type <= tok.type) {
            result.add(stack.pop());
        }
        stack.push(tok);
    }

    public Object visit(Visitor visitor) {
        LinkedList<Object> stack = new LinkedList<Object>();
        for (Token token : expr) {
            if (token.type == ARG) {
                stack.add(visitor.createParameter(token));
            } else {
                Object lparam;
                Object rparam = null;
                int arity = token.type > NOT ? 2 : 1;
                if (arity == 1) {
                    lparam = stack.removeLast();
                } else {// arity == 2
                    rparam = stack.removeLast();
                    lparam = stack.removeLast();
                }
                stack.add(visitor.createOperation(token, lparam, rparam));
            }
        }
        return stack.getLast();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Token token : expr) {
            sb.append(token.name).append(" ");
        }
        return sb.toString();
    }

    public interface Visitor {
        Object createParameter(Token token);
        Object createOperation(Token token, Object lparam, Object rparam);
    }

    public static class Token {
        public final int type;
        public final String name;

        public Token(int type, String name) {
            this.type = type;
            this.name = name;
        }
        @Override
        public String toString() {
            return name;
        }
    }

    public static class OpStack extends LinkedList<Token> {
        private static final long serialVersionUID = 1L;
        public final void push(Token token) {
            add(token);
        }
        public final Token pop() {
            return removeLast();
        }
        public final Token top() {
            return getLast();
        }
    }

    protected void parse(String expr) throws ParseException {
        OpStack stack = new OpStack();
        List<Token> result = new ArrayList<Token>();
        String arg = null;
        StringTokenizer tok = new StringTokenizer(expr, " \t\n\r\f()", true);
        while (tok.hasMoreTokens()) {
            String token = tok.nextToken();
            char c = token.charAt(0);
            switch (c) {
            case ' ':
            case '\t':
            case '\r':
            case '\n':
            case '\f':
                arg = null;
                break;
            case '(':
                stack.push(new Token(LPARA, "("));
                break;
            case ')':
                if (arg != null) {
                    result.add(new Token(ARG, arg));
                }
                while (!stack.isEmpty() && stack.top().type != LPARA) {
                    result.add(stack.pop());
                }
                if (stack.isEmpty()) {
                    throw new ParseException("Not matching LPARA '(' found ", -1);
                }
                stack.pop(); // remove LPARA from stack
                break;
            default:
                if ("OR".equals(token)) {
                    pushOp(new Token(OR, "OR"), stack, result);
                } else if ("AND".equals(token)){
                    pushOp(new Token(AND, "AND"), stack, result);
                } else if ("NOT".equals(token)) {
                    pushOp(new Token(NOT, "NOT"), stack, result);
                } else {
                    result.add(new Token(ARG, token));
                }
            }
        }
        while (!stack.isEmpty()) {
            result.add(stack.pop());
        }
        this.expr = result.toArray(new Token[result.size()]);
    }

}

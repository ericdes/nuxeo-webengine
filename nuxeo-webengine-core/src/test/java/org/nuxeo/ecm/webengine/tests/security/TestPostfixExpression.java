package org.nuxeo.ecm.webengine.tests.security;

import java.text.ParseException;

import junit.framework.TestCase;
import org.nuxeo.ecm.webengine.security.PostfixExpression;

public class TestPostfixExpression extends TestCase {

    public void test1() throws ParseException {
        String expr = "a AND b OR d";
        assertEquals("a  b AND  d OR", new PostfixExpression(expr).toString().trim());
    }

    public void test2() throws ParseException {
        String expr = "a OR b AND d";
        assertEquals("a  b  d AND OR", new PostfixExpression(expr).toString().trim());
    }

    public void test3() throws ParseException {
        String expr = "a OR b OR d";
        assertEquals("a  b OR  d OR", new PostfixExpression(expr).toString().trim());
    }

    public void test4() throws ParseException {
        String expr = "(a OR b) AND d";
        assertEquals("a  b OR  d AND", new PostfixExpression(expr).toString().trim());
    }

    public void test5() throws ParseException {
        String expr = "(a OR b) AND (c OR d) OR e";
        assertEquals("a  b OR c  d OR AND  e OR", new PostfixExpression(expr).toString().trim());
    }

    public void test6() throws ParseException {
        String expr = "(a AND b OR c) AND ((d OR e) AND f)";
        assertEquals("a  b AND  c OR d  e OR  f AND AND", new PostfixExpression(expr).toString().trim());
    }

}

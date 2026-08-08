package org.apache.cxf.endpoint.jaxws.definition;

import static org.junit.Assert.*;

import org.junit.Test;

public class SoapMethodTest {

    @Test
    public void shouldCreateMethodWithDefaults() {
        SoapMethod method = new SoapMethod();
        assertEquals("", method.getOperationName());
        assertEquals("", method.getAction());
        assertFalse(method.isExclude());
    }

    @Test
    public void shouldCreateMethodWithOperationName() {
        SoapMethod method = new SoapMethod("sayHello");
        assertEquals("sayHello", method.getOperationName());
        assertEquals("", method.getAction());
        assertFalse(method.isExclude());
    }

    @Test
    public void shouldCreateFullySpecifiedMethod() {
        SoapMethod method = new SoapMethod("sayHello", "http://example.com/sayHello", true);
        assertEquals("sayHello", method.getOperationName());
        assertEquals("http://example.com/sayHello", method.getAction());
        assertTrue(method.isExclude());
    }

    @Test
    public void shouldAllowOperationNameOverride() {
        SoapMethod method = new SoapMethod("old");
        method.setOperationName("new");
        assertEquals("new", method.getOperationName());
    }

    @Test
    public void shouldAllowActionOverride() {
        SoapMethod method = new SoapMethod();
        method.setAction("http://example.com/action");
        assertEquals("http://example.com/action", method.getAction());
    }

    @Test
    public void shouldAllowExcludeOverride() {
        SoapMethod method = new SoapMethod();
        method.setExclude(true);
        assertTrue(method.isExclude());
    }
}

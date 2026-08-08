package org.apache.cxf.endpoint.jaxws.definition;

import static org.junit.Assert.*;

import org.junit.Test;

public class SoapResultTest {

    @Test
    public void shouldCreateResultWithTypeAndName() {
        SoapResult<String> result = new SoapResult<>(String.class, "result");
        assertEquals(String.class, result.getRtClass());
        assertEquals("result", result.getName());
        assertEquals("", result.getTargetNamespace());
        assertFalse(result.isHeader());
        assertEquals("", result.getPartName());
    }

    @Test
    public void shouldCreateFullySpecifiedResult() {
        SoapResult<String> result = new SoapResult<>(String.class, "ret",
                "http://example.com", true, "part1");
        assertEquals(String.class, result.getRtClass());
        assertEquals("ret", result.getName());
        assertEquals("http://example.com", result.getTargetNamespace());
        assertTrue(result.isHeader());
        assertEquals("part1", result.getPartName());
    }

    @Test
    public void shouldAllowRtClassOverride() {
        SoapResult<String> result = new SoapResult<>(String.class, "ret");
        result.setRtClass(String.class);
        assertEquals(String.class, result.getRtClass());
    }

    @Test
    public void shouldAllowNameOverride() {
        SoapResult<String> result = new SoapResult<>(String.class, "old");
        result.setName("new");
        assertEquals("new", result.getName());
    }

    @Test
    public void shouldAllowTargetNamespaceOverride() {
        SoapResult<String> result = new SoapResult<>(String.class, "ret");
        result.setTargetNamespace("http://ns");
        assertEquals("http://ns", result.getTargetNamespace());
    }

    @Test
    public void shouldAllowHeaderOverride() {
        SoapResult<String> result = new SoapResult<>(String.class, "ret");
        result.setHeader(true);
        assertTrue(result.isHeader());
    }

    @Test
    public void shouldAllowPartNameOverride() {
        SoapResult<String> result = new SoapResult<>(String.class, "ret");
        result.setPartName("part");
        assertEquals("part", result.getPartName());
    }
}

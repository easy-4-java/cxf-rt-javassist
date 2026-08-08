package org.apache.cxf.endpoint.jaxws.definition;

import static org.junit.Assert.*;

import jakarta.jws.WebParam;

import org.junit.Test;

public class SoapParamTest {

    @Test
    public void shouldCreateParamWithTypeAndName() {
        SoapParam<String> param = new SoapParam<>(String.class, "userName");
        assertEquals(String.class, param.getType());
        assertEquals("userName", param.getName());
        assertEquals(WebParam.Mode.IN, param.getMode());
        assertFalse(param.isHeader());
        assertEquals("", param.getPartName());
        assertEquals("", param.getTargetNamespace());
    }

    @Test
    public void shouldCreateParamWithHeaderFlag() {
        SoapParam<String> param = new SoapParam<>(String.class, "token", true);
        assertTrue(param.isHeader());
    }

    @Test
    public void shouldCreateParamWithMode() {
        SoapParam<String> param = new SoapParam<>(String.class, "data", WebParam.Mode.OUT);
        assertEquals(WebParam.Mode.OUT, param.getMode());
    }

    @Test
    public void shouldCreateParamWithModeAndHeader() {
        SoapParam<String> param = new SoapParam<>(String.class, "data", WebParam.Mode.INOUT, true);
        assertEquals(WebParam.Mode.INOUT, param.getMode());
        assertTrue(param.isHeader());
    }

    @Test
    public void shouldCreateFullySpecifiedParam() {
        SoapParam<String> param = new SoapParam<>(String.class, "data", "part1",
                "http://example.com", WebParam.Mode.OUT, true);
        assertEquals(String.class, param.getType());
        assertEquals("data", param.getName());
        assertEquals("part1", param.getPartName());
        assertEquals("http://example.com", param.getTargetNamespace());
        assertEquals(WebParam.Mode.OUT, param.getMode());
        assertTrue(param.isHeader());
    }

    @Test
    public void shouldAllowTypeOverride() {
        SoapParam<String> param = new SoapParam<>(String.class, "val");
        param.setType(String.class);
        assertEquals(String.class, param.getType());
    }

    @Test
    public void shouldAllowNameOverride() {
        SoapParam<String> param = new SoapParam<>(String.class, "old");
        param.setName("new");
        assertEquals("new", param.getName());
    }

    @Test
    public void shouldAllowPartNameOverride() {
        SoapParam<String> param = new SoapParam<>(String.class, "data");
        param.setPartName("part");
        assertEquals("part", param.getPartName());
    }

    @Test
    public void shouldAllowTargetNamespaceOverride() {
        SoapParam<String> param = new SoapParam<>(String.class, "data");
        param.setTargetNamespace("http://ns");
        assertEquals("http://ns", param.getTargetNamespace());
    }

    @Test
    public void shouldAllowModeOverride() {
        SoapParam<String> param = new SoapParam<>(String.class, "data");
        param.setMode(WebParam.Mode.OUT);
        assertEquals(WebParam.Mode.OUT, param.getMode());
    }

    @Test
    public void shouldAllowHeaderOverride() {
        SoapParam<String> param = new SoapParam<>(String.class, "data");
        param.setHeader(true);
        assertTrue(param.isHeader());
    }
}

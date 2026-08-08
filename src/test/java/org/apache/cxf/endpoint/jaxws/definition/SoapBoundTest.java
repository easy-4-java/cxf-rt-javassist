package org.apache.cxf.endpoint.jaxws.definition;

import static org.junit.Assert.*;

import org.junit.Test;

public class SoapBoundTest {

    @Test
    public void shouldCreateBoundWithUidOnly() {
        SoapBound bound = new SoapBound("uid-123");
        assertEquals("uid-123", bound.getUid());
        assertEquals("", bound.getJson());
    }

    @Test
    public void shouldCreateBoundWithUidAndJson() {
        SoapBound bound = new SoapBound("uid-456", "{\"key\":\"value\"}");
        assertEquals("uid-456", bound.getUid());
        assertEquals("{\"key\":\"value\"}", bound.getJson());
    }

    @Test
    public void shouldAllowUidOverride() {
        SoapBound bound = new SoapBound("old");
        bound.setUid("new");
        assertEquals("new", bound.getUid());
    }

    @Test
    public void shouldAllowJsonOverride() {
        SoapBound bound = new SoapBound("uid");
        bound.setJson("{\"updated\":true}");
        assertEquals("{\"updated\":true}", bound.getJson());
    }

    @Test
    public void shouldAllowNullJson() {
        SoapBound bound = new SoapBound("uid");
        bound.setJson(null);
        assertNull(bound.getJson());
    }
}

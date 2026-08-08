package org.apache.cxf.endpoint.jaxrs.definition;

import static org.junit.Assert.*;

import org.junit.Test;

public class RestBoundTest {

    @Test
    public void shouldCreateBoundWithUidOnly() {
        RestBound bound = new RestBound("uid-123");
        assertEquals("uid-123", bound.getUid());
        assertEquals("", bound.getJson());
    }

    @Test
    public void shouldCreateBoundWithUidAndJson() {
        RestBound bound = new RestBound("uid-456", "{\"key\":\"value\"}");
        assertEquals("uid-456", bound.getUid());
        assertEquals("{\"key\":\"value\"}", bound.getJson());
    }

    @Test
    public void shouldAllowUidOverride() {
        RestBound bound = new RestBound("old");
        bound.setUid("new");
        assertEquals("new", bound.getUid());
    }

    @Test
    public void shouldAllowJsonOverride() {
        RestBound bound = new RestBound("uid");
        bound.setJson("{\"updated\":true}");
        assertEquals("{\"updated\":true}", bound.getJson());
    }

    @Test
    public void shouldAllowNullJson() {
        RestBound bound = new RestBound("uid");
        bound.setJson(null);
        assertNull(bound.getJson());
    }
}

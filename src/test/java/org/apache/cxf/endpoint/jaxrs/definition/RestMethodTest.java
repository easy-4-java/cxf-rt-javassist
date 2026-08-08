package org.apache.cxf.endpoint.jaxrs.definition;

import static org.junit.Assert.*;

import org.junit.Test;

public class RestMethodTest {

    @Test
    public void shouldCreateMethodWithBasicConstructor() {
        RestMethod method = new RestMethod(HttpMethodEnum.GET, "findById", "/{id}");
        assertEquals(HttpMethodEnum.GET, method.getMethod());
        assertEquals("findById", method.getName());
        assertEquals("/{id}", method.getPath());
        assertNull(method.getConsumes());
        assertNotNull(method.getMediaTypes());
        assertArrayEquals(new String[]{"*/*"}, method.getMediaTypes());
    }

    @Test
    public void shouldCreateMethodWithConsumes() {
        RestMethod method = new RestMethod(HttpMethodEnum.POST, "create", "/", "application/json");
        assertEquals(HttpMethodEnum.POST, method.getMethod());
        assertEquals("create", method.getName());
        assertEquals("/", method.getPath());
        assertNotNull(method.getConsumes());
        assertArrayEquals(new String[]{"application/json"}, method.getConsumes());
    }

    @Test
    public void shouldAllowMediaTypesOverride() {
        RestMethod method = new RestMethod(HttpMethodEnum.GET, "list", "/");
        method.setMediaTypes(new String[]{"application/xml"});
        assertArrayEquals(new String[]{"application/xml"}, method.getMediaTypes());
    }

    @Test
    public void shouldAllowConsumesOverride() {
        RestMethod method = new RestMethod(HttpMethodEnum.POST, "save", "/");
        method.setConsumes(new String[]{"text/plain"});
        assertArrayEquals(new String[]{"text/plain"}, method.getConsumes());
    }
}

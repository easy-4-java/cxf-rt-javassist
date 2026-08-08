package org.apache.cxf.endpoint.jaxrs.definition;

import static org.junit.Assert.*;

import java.util.NoSuchElementException;

import org.junit.Test;

public class HttpMethodEnumTest {

    @Test
    public void shouldReturnCorrectKeyForEachVerb() {
        assertEquals("GET", HttpMethodEnum.GET.getKey());
        assertEquals("POST", HttpMethodEnum.POST.getKey());
        assertEquals("PUT", HttpMethodEnum.PUT.getKey());
        assertEquals("DELETE", HttpMethodEnum.DELETE.getKey());
        assertEquals("PATCH", HttpMethodEnum.PATCH.getKey());
        assertEquals("HEAD", HttpMethodEnum.HEAD.getKey());
        assertEquals("OPTIONS", HttpMethodEnum.OPTIONS.getKey());
    }

    @Test
    public void shouldResolveByCaseInsensitiveKey() {
        assertSame(HttpMethodEnum.GET, HttpMethodEnum.valueOfIgnoreCase("get"));
        assertSame(HttpMethodEnum.POST, HttpMethodEnum.valueOfIgnoreCase("POST"));
        assertSame(HttpMethodEnum.PUT, HttpMethodEnum.valueOfIgnoreCase("Put"));
        assertSame(HttpMethodEnum.DELETE, HttpMethodEnum.valueOfIgnoreCase("delete"));
        assertSame(HttpMethodEnum.PATCH, HttpMethodEnum.valueOfIgnoreCase("Patch"));
        assertSame(HttpMethodEnum.HEAD, HttpMethodEnum.valueOfIgnoreCase("HEAD"));
        assertSame(HttpMethodEnum.OPTIONS, HttpMethodEnum.valueOfIgnoreCase("options"));
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowWhenKeyNotFound() {
        HttpMethodEnum.valueOfIgnoreCase("UNKNOWN");
    }

    @Test
    public void shouldHaveSevenConstants() {
        assertEquals(7, HttpMethodEnum.values().length);
    }
}

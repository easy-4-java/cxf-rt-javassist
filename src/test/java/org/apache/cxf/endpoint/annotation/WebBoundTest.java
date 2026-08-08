package org.apache.cxf.endpoint.annotation;

import static org.junit.Assert.*;

import org.junit.Test;

public class WebBoundTest {

    @Test
    public void shouldHaveCorrectDefaults() throws Exception {
        WebBound bound = WebBoundTestHelper.class.getAnnotation(WebBound.class);
        assertNotNull(bound);
        assertEquals("", bound.uid());
        assertEquals("{}", bound.json());
    }

    @Test
    public void shouldHaveCustomValues() throws Exception {
        WebBound bound = WebBoundCustomHelper.class.getAnnotation(WebBound.class);
        assertNotNull(bound);
        assertEquals("uid-123", bound.uid());
        assertEquals("{\"key\":\"val\"}", bound.json());
    }

    @WebBound
    static class WebBoundTestHelper {
    }

    @WebBound(uid = "uid-123", json = "{\"key\":\"val\"}")
    static class WebBoundCustomHelper {
    }
}

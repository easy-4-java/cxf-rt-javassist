package org.apache.cxf.endpoint.annotation;

import static org.junit.Assert.*;

import org.junit.Test;

public class WebEndpointTest {

    @Test
    public void shouldHaveCorrectDefaults() throws Exception {
        WebEndpoint ep = WebEndpointHelper.class.getAnnotation(WebEndpoint.class);
        assertNotNull(ep);
        assertEquals("http://localhost:8080", ep.addr());
        assertArrayEquals(new String[]{""}, ep.inInterceptors());
        assertArrayEquals(new String[]{""}, ep.outInterceptors());
        assertArrayEquals(new String[]{""}, ep.inFaults());
        assertArrayEquals(new String[]{""}, ep.outFaults());
        assertArrayEquals(new String[]{""}, ep.features());
        assertArrayEquals(new String[]{""}, ep.handlers());
    }

    @WebEndpoint(addr = "http://localhost:8080")
    static class WebEndpointHelper {
    }
}

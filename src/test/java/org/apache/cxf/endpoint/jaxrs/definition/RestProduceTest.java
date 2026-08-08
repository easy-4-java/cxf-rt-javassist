package org.apache.cxf.endpoint.jaxrs.definition;

import static org.junit.Assert.*;

import org.junit.Test;

public class RestProduceTest {

    @Test
    public void shouldCreateProduceWithPathAndMediaTypes() {
        RestProduce produce = new RestProduce("/api", "application/json", "application/xml");
        assertEquals("/api", produce.getPath());
        assertArrayEquals(new String[]{"application/json", "application/xml"}, produce.getMediaTypes());
    }

    @Test
    public void shouldCreateProduceWithDefaultMediaTypes() {
        RestProduce produce = new RestProduce("/api");
        assertEquals("/api", produce.getPath());
        assertNotNull(produce.getMediaTypes());
    }

    @Test
    public void shouldAllowMediaTypesOverride() {
        RestProduce produce = new RestProduce("/api");
        produce.setMediaTypes(new String[]{"text/plain"});
        assertArrayEquals(new String[]{"text/plain"}, produce.getMediaTypes());
    }
}

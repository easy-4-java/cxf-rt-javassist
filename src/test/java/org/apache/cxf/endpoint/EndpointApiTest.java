package org.apache.cxf.endpoint;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.junit.Test;

public class EndpointApiTest {

    @Test
    public void shouldCreateInstanceWithDefaultConstructor() {
        EndpointApi api = new EndpointApi() {};
        assertNull(api.getHandler());
    }

    @Test
    public void shouldStoreHandlerViaConstructor() {
        InvocationHandler handler = (proxy, method, args) -> null;
        EndpointApi api = new EndpointApi(handler) {};
        assertSame(handler, api.getHandler());
    }

    @Test
    public void shouldReturnNullHandlerWhenDefaultConstructed() {
        EndpointApi api = new EndpointApi() {};
        assertNull(api.getHandler());
    }

    @Test
    public void shouldAcceptNullHandler() {
        EndpointApi api = new EndpointApi(null) {};
        assertNull(api.getHandler());
    }
}

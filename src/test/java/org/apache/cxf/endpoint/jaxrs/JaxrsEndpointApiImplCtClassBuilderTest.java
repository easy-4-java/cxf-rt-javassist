package org.apache.cxf.endpoint.jaxrs;

import static org.junit.Assert.*;

import org.apache.cxf.endpoint.jaxrs.definition.HttpMethodEnum;
import org.apache.cxf.endpoint.jaxrs.definition.HttpParamEnum;
import org.apache.cxf.endpoint.jaxrs.definition.RestBound;
import org.apache.cxf.endpoint.jaxrs.definition.RestParam;
import org.junit.Test;

import javassist.ClassPool;
import javassist.CtClass;

public class JaxrsEndpointApiImplCtClassBuilderTest {

    @Test
    public void shouldBuildImplClassWithDefaultPool() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiImplCtClassBuilder("org.test.JaxrsImpl1")
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldBuildImplClassWithCustomPool() throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = new JaxrsEndpointApiImplCtClassBuilder(pool, "org.test.JaxrsImpl2")
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldForwardPathToInterface() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiImplCtClassBuilder("org.test.JaxrsImpl3")
                .path("/api")
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldForwardProducesToInterface() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiImplCtClassBuilder("org.test.JaxrsImpl4")
                .produces("application/json")
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldForwardDefaultProducesToInterface() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiImplCtClassBuilder("org.test.JaxrsImpl5")
                .produces()
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldForwardBindUidJson() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiImplCtClassBuilder("org.test.JaxrsImpl6")
                .bind("uid", "{}")
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldForwardBindRestBound() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiImplCtClassBuilder("org.test.JaxrsImpl7")
                .bind(new RestBound("uid", "{}"))
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldAddMethodToBothInterfaceAndImpl() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiImplCtClassBuilder("org.test.JaxrsImpl8")
                .path("/api")
                .newMethod(String.class, HttpMethodEnum.GET, "hello", "/hello",
                        new RestBound("b1"), new RestParam(String.class, "name"))
                .build();
        assertNotNull(ctClass);
        assertNotNull(ctClass.getDeclaredMethod("hello"));
        ctClass.detach();
    }

    @Test
    public void shouldUseImplSuffix() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiImplCtClassBuilder("org.test.JaxrsImplSuffix1")
                .build();
        assertTrue(ctClass.getName().endsWith("$Impl"));
        ctClass.detach();
    }

    @Test
    public void shouldSupportFluentChaining() throws Exception {
        JaxrsEndpointApiImplCtClassBuilder builder = new JaxrsEndpointApiImplCtClassBuilder("org.test.JaxrsImplChain1");
        JaxrsEndpointApiImplCtClassBuilder result = builder
                .path("/api")
                .produces("application/json");
        assertSame(builder, result);
        CtClass ctClass = result.build();
        assertNotNull(ctClass);
        ctClass.detach();
    }
}

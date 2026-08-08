package org.apache.cxf.endpoint.jaxrs;

import static org.junit.Assert.*;

import org.apache.cxf.endpoint.jaxrs.definition.HttpMethodEnum;
import org.apache.cxf.endpoint.jaxrs.definition.HttpParamEnum;
import org.apache.cxf.endpoint.jaxrs.definition.RestBound;
import org.apache.cxf.endpoint.jaxrs.definition.RestMethod;
import org.apache.cxf.endpoint.jaxrs.definition.RestParam;
import org.junit.Test;

import javassist.ClassPool;
import javassist.CtClass;

public class JaxrsEndpointApiInterfaceCtClassBuilderTest {

    @Test
    public void shouldBuildInterfaceWithDefaultPool() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiInterfaceCtClassBuilder("org.test.JaxrsIface1")
                .build();
        assertNotNull(ctClass);
        assertTrue(ctClass.isInterface());
        ctClass.detach();
    }

    @Test
    public void shouldBuildInterfaceWithCustomPool() throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = new JaxrsEndpointApiInterfaceCtClassBuilder(pool, "org.test.JaxrsIface2")
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldAddPathAnnotation() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiInterfaceCtClassBuilder("org.test.JaxrsIface3")
                .path("/api")
                .build();
        assertNotNull(ctClass.getAnnotation(jakarta.ws.rs.Path.class));
        ctClass.detach();
    }

    @Test
    public void shouldAddProducesAnnotation() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiInterfaceCtClassBuilder("org.test.JaxrsIface4")
                .produces("application/xml")
                .build();
        assertNotNull(ctClass.getAnnotation(jakarta.ws.rs.Produces.class));
        ctClass.detach();
    }

    @Test
    public void shouldAddDefaultProduces() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiInterfaceCtClassBuilder("org.test.JaxrsIface5")
                .produces()
                .build();
        assertNotNull(ctClass.getAnnotation(jakarta.ws.rs.Produces.class));
        ctClass.detach();
    }

    @Test
    public void shouldBindUidJson() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiInterfaceCtClassBuilder("org.test.JaxrsIface6")
                .bind("uid", "{}")
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldBindRestBound() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiInterfaceCtClassBuilder("org.test.JaxrsIface7")
                .bind(new RestBound("uid", "{}"))
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldMakeFieldFromSource() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiInterfaceCtClassBuilder("org.test.JaxrsIface8")
                .makeField("public int k = 3;")
                .build();
        assertNotNull(ctClass.getDeclaredField("k"));
        ctClass.detach();
    }

    @Test
    public void shouldAddTypedField() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiInterfaceCtClassBuilder("org.test.JaxrsIface9")
                .newField(String.class, "uid", "test")
                .build();
        assertNotNull(ctClass.getDeclaredField("uid"));
        ctClass.detach();
    }

    @Test
    public void shouldNoopWhenFieldAlreadyExists() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiInterfaceCtClassBuilder("org.test.JaxrsIface10")
                .newField(String.class, "uid", "test")
                .newField(String.class, "uid", "test2")
                .build();
        assertNotNull(ctClass.getDeclaredField("uid"));
        ctClass.detach();
    }

    @Test
    public void shouldRemoveExistingField() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiInterfaceCtClassBuilder("org.test.JaxrsIface11")
                .makeField("public int k = 3;")
                .removeField("k")
                .build();
        try {
            ctClass.getDeclaredField("k");
            fail("Field should have been removed");
        } catch (javassist.NotFoundException e) {
            // expected
        }
        ctClass.detach();
    }

    @Test
    public void shouldNoopWhenRemovingNonexistentField() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiInterfaceCtClassBuilder("org.test.JaxrsIface12")
                .removeField("nonexistent")
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldAddAbstractMethodWithAllParams() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiInterfaceCtClassBuilder("org.test.JaxrsIface13")
                .abstractMethod(String.class, HttpMethodEnum.GET, "find", "/{id}",
                        new RestBound("b1"), new RestParam(String.class, "id", HttpParamEnum.PATH))
                .build();
        assertNotNull(ctClass.getDeclaredMethod("find"));
        ctClass.detach();
    }

    @Test
    public void shouldAddAbstractMethodWithoutBound() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiInterfaceCtClassBuilder("org.test.JaxrsIface14")
                .abstractMethod(String.class, HttpMethodEnum.GET, "find", "/{id}",
                        new RestParam(String.class, "id"))
                .build();
        assertNotNull(ctClass.getDeclaredMethod("find"));
        ctClass.detach();
    }

    @Test
    public void shouldAddAbstractMethodWithRestMethodAndBound() throws Exception {
        RestMethod rm = new RestMethod(HttpMethodEnum.POST, "create", "/");
        CtClass ctClass = new JaxrsEndpointApiInterfaceCtClassBuilder("org.test.JaxrsIface15")
                .abstractMethod(String.class, rm, new RestBound("b1"),
                        new RestParam(String.class, "data"))
                .build();
        assertNotNull(ctClass.getDeclaredMethod("create"));
        ctClass.detach();
    }

    @Test
    public void shouldAddAbstractMethodWithRestMethodNoBound() throws Exception {
        RestMethod rm = new RestMethod(HttpMethodEnum.GET, "list", "/");
        CtClass ctClass = new JaxrsEndpointApiInterfaceCtClassBuilder("org.test.JaxrsIface16")
                .abstractMethod(String.class, rm, new RestParam(String.class, "q"))
                .build();
        assertNotNull(ctClass.getDeclaredMethod("list"));
        ctClass.detach();
    }

    @Test
    public void shouldAddAbstractMethodNoReturnNoBoundHttpEnum() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiInterfaceCtClassBuilder("org.test.JaxrsIface17")
                .abstractMethod(HttpMethodEnum.GET, "health", "/health")
                .build();
        assertNotNull(ctClass.getDeclaredMethod("health"));
        ctClass.detach();
    }

    @Test
    public void shouldAddAbstractMethodNoReturnWithBoundHttpEnum() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiInterfaceCtClassBuilder("org.test.JaxrsIface18")
                .abstractMethod(HttpMethodEnum.POST, "save", "/", new RestBound("b1"))
                .build();
        assertNotNull(ctClass.getDeclaredMethod("save"));
        ctClass.detach();
    }

    @Test
    public void shouldAddAbstractMethodNoReturnWithBoundRestMethod() throws Exception {
        RestMethod rm = new RestMethod(HttpMethodEnum.DELETE, "delete", "/{id}");
        CtClass ctClass = new JaxrsEndpointApiInterfaceCtClassBuilder("org.test.JaxrsIface19")
                .abstractMethod(rm, new RestBound("b1"), new RestParam(String.class, "id"))
                .build();
        assertNotNull(ctClass.getDeclaredMethod("delete"));
        ctClass.detach();
    }

    @Test
    public void shouldAddAbstractMethodNoReturnNoBoundRestMethod() throws Exception {
        RestMethod rm = new RestMethod(HttpMethodEnum.GET, "ping", "/ping");
        CtClass ctClass = new JaxrsEndpointApiInterfaceCtClassBuilder("org.test.JaxrsIface20")
                .abstractMethod(rm)
                .build();
        assertNotNull(ctClass.getDeclaredMethod("ping"));
        ctClass.detach();
    }

    @Test
    public void shouldRemoveExistingMethod() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiInterfaceCtClassBuilder("org.test.JaxrsIface21")
                .abstractMethod(HttpMethodEnum.GET, "temp", "/temp")
                .removeMethod("temp")
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldNoopWhenRemovingNonexistentMethod() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiInterfaceCtClassBuilder("org.test.JaxrsIface22")
                .removeMethod("nonexistent")
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldRemoveMethodWithParams() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiInterfaceCtClassBuilder("org.test.JaxrsIface23")
                .abstractMethod(String.class, HttpMethodEnum.GET, "withParam", "/p",
                        new RestParam(String.class, "x"))
                .removeMethod("withParam", new RestParam(String.class, "x"))
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldNoopWhenRemovingNonexistentMethodWithParams() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiInterfaceCtClassBuilder("org.test.JaxrsIface24")
                .removeMethod("nope", new RestParam(String.class, "x"))
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldConvertToClass() throws Exception {
        Class<?> clazz = new JaxrsEndpointApiInterfaceCtClassBuilder("org.test.JaxrsIface25")
                .path("/api")
                .toClass();
        assertNotNull(clazz);
    }

    @Test
    public void shouldAddVoidAbstractMethodWithNoParams() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiInterfaceCtClassBuilder("org.test.JaxrsIface26")
                .abstractMethod(HttpMethodEnum.GET, "noop", "/noop")
                .build();
        assertNotNull(ctClass.getDeclaredMethod("noop"));
        ctClass.detach();
    }
}

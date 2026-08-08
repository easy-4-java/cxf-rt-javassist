package org.apache.cxf.endpoint.jaxrs;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationHandler;

import org.apache.cxf.endpoint.jaxrs.definition.HttpMethodEnum;
import org.apache.cxf.endpoint.jaxrs.definition.HttpParamEnum;
import org.apache.cxf.endpoint.jaxrs.definition.RestBound;
import org.apache.cxf.endpoint.jaxrs.definition.RestMethod;
import org.apache.cxf.endpoint.jaxrs.definition.RestParam;
import org.junit.Test;

import javassist.ClassPool;
import javassist.CtClass;

public class JaxrsEndpointApiCtClassBuilderTest {

    @Test
    public void shouldBuildClassWithDefaultPool() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiCtClassBuilder("org.test.JaxrsDefault1")
                .build();
        assertNotNull(ctClass);
        assertEquals("org.test.JaxrsDefault1", ctClass.getName());
        ctClass.detach();
    }

    @Test
    public void shouldBuildClassWithCustomPool() throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = new JaxrsEndpointApiCtClassBuilder(pool, "org.test.JaxrsCustom1")
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldAddPathAnnotation() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiCtClassBuilder("org.test.JaxrsPath1")
                .path("/api")
                .build();
        assertNotNull(ctClass.getAnnotation(jakarta.ws.rs.Path.class));
        ctClass.detach();
    }

    @Test
    public void shouldAddProducesAnnotation() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiCtClassBuilder("org.test.JaxrsProd1")
                .produces("application/json")
                .build();
        assertNotNull(ctClass.getAnnotation(jakarta.ws.rs.Produces.class));
        ctClass.detach();
    }

    @Test
    public void shouldAddDefaultProducesWhenEmpty() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiCtClassBuilder("org.test.JaxrsProd2")
                .produces()
                .build();
        assertNotNull(ctClass.getAnnotation(jakarta.ws.rs.Produces.class));
        ctClass.detach();
    }

    @Test
    public void shouldBindWithUidAndJson() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiCtClassBuilder("org.test.JaxrsBind1")
                .bind("uid-1", "{}")
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldBindWithRestBound() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiCtClassBuilder("org.test.JaxrsBind2")
                .bind(new RestBound("uid-2", "{\"x\":1}"))
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldMakeFieldFromSource() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiCtClassBuilder("org.test.JaxrsField1")
                .makeField("public int k = 3;")
                .build();
        assertNotNull(ctClass.getDeclaredField("k"));
        ctClass.detach();
    }

    @Test
    public void shouldAddTypedField() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiCtClassBuilder("org.test.JaxrsField2")
                .newField(String.class, "uid", "test-value")
                .build();
        assertNotNull(ctClass.getDeclaredField("uid"));
        ctClass.detach();
    }

    @Test
    public void shouldRemoveExistingField() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiCtClassBuilder("org.test.JaxrsField3")
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
        CtClass ctClass = new JaxrsEndpointApiCtClassBuilder("org.test.JaxrsField4")
                .removeField("nonexistent")
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldAddMethodWithReturnTypeAndBound() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiCtClassBuilder("org.test.JaxrsMethod1")
                .newMethod(String.class, HttpMethodEnum.GET, "sayHello", "/{id}",
                        new RestBound("b1"), new RestParam(String.class, "id", HttpParamEnum.PATH))
                .build();
        assertNotNull(ctClass.getDeclaredMethod("sayHello"));
        ctClass.detach();
    }

    @Test
    public void shouldAddMethodWithoutReturnType() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiCtClassBuilder("org.test.JaxrsMethod2")
                .newMethod(HttpMethodEnum.POST, "create", "/",
                        new RestParam(String.class, "name"))
                .build();
        assertNotNull(ctClass.getDeclaredMethod("create"));
        ctClass.detach();
    }

    @Test
    public void shouldAddMethodWithRestMethodAndBound() throws Exception {
        RestMethod rm = new RestMethod(HttpMethodEnum.GET, "findById", "/{id}");
        CtClass ctClass = new JaxrsEndpointApiCtClassBuilder("org.test.JaxrsMethod3")
                .newMethod(String.class, rm, new RestBound("b1"),
                        new RestParam(String.class, "id", HttpParamEnum.PATH))
                .build();
        assertNotNull(ctClass.getDeclaredMethod("findById"));
        ctClass.detach();
    }

    @Test
    public void shouldAddMethodWithRestMethodNoBound() throws Exception {
        RestMethod rm = new RestMethod(HttpMethodEnum.GET, "list", "/");
        CtClass ctClass = new JaxrsEndpointApiCtClassBuilder("org.test.JaxrsMethod4")
                .newMethod(String.class, rm)
                .build();
        assertNotNull(ctClass.getDeclaredMethod("list"));
        ctClass.detach();
    }

    @Test
    public void shouldAddMethodWithHttpMethodEnumNamePath() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiCtClassBuilder("org.test.JaxrsMethod5")
                .newMethod(HttpMethodEnum.PUT, "update", "/{id}",
                        new RestBound("b1"), new RestParam(String.class, "id"))
                .build();
        assertNotNull(ctClass.getDeclaredMethod("update"));
        ctClass.detach();
    }

    @Test
    public void shouldAddMethodWithRestMethodBoundNoReturn() throws Exception {
        RestMethod rm = new RestMethod(HttpMethodEnum.DELETE, "remove", "/{id}");
        CtClass ctClass = new JaxrsEndpointApiCtClassBuilder("org.test.JaxrsMethod6")
                .newMethod(rm, new RestBound("b1"),
                        new RestParam(String.class, "id"))
                .build();
        assertNotNull(ctClass.getDeclaredMethod("remove"));
        ctClass.detach();
    }

    @Test
    public void shouldAddMethodWithRestMethodNoBoundNoReturn() throws Exception {
        RestMethod rm = new RestMethod(HttpMethodEnum.GET, "health", "/health");
        CtClass ctClass = new JaxrsEndpointApiCtClassBuilder("org.test.JaxrsMethod7")
                .newMethod(rm)
                .build();
        assertNotNull(ctClass.getDeclaredMethod("health"));
        ctClass.detach();
    }

    @Test
    public void shouldRemoveExistingMethod() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiCtClassBuilder("org.test.JaxrsMethod8")
                .newMethod(HttpMethodEnum.GET, "temp", "/temp")
                .removeMethod("temp")
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldNoopWhenRemovingNonexistentMethod() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiCtClassBuilder("org.test.JaxrsMethod9")
                .removeMethod("nonexistent")
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldRemoveMethodWithParams() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiCtClassBuilder("org.test.JaxrsMethod10")
                .newMethod(String.class, HttpMethodEnum.GET, "withParam", "/p",
                        new RestParam(String.class, "x"))
                .removeMethod("withParam", new RestParam(String.class, "x"))
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldNoopWhenRemovingNonexistentMethodWithParams() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiCtClassBuilder("org.test.JaxrsMethod11")
                .removeMethod("nope", new RestParam(String.class, "x"))
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldAddVoidMethodWithNoParams() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiCtClassBuilder("org.test.JaxrsMethod12")
                .newMethod(HttpMethodEnum.GET, "noop", "/noop")
                .build();
        assertNotNull(ctClass.getDeclaredMethod("noop"));
        ctClass.detach();
    }

    @Test
    public void shouldSetSuperclassToEndpointApi() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiCtClassBuilder("org.test.JaxrsParent1")
                .build();
        assertNotNull(ctClass.getSuperclass());
        assertEquals("org.apache.cxf.endpoint.EndpointApi", ctClass.getSuperclass().getName());
        ctClass.detach();
    }

    @Test
    public void shouldHaveDefaultConstructor() throws Exception {
        CtClass ctClass = new JaxrsEndpointApiCtClassBuilder("org.test.JaxrsCtor1")
                .build();
        assertTrue(ctClass.getConstructors().length > 0);
        ctClass.detach();
    }

    @Test
    public void shouldSupportFluentChaining() throws Exception {
        JaxrsEndpointApiCtClassBuilder builder = new JaxrsEndpointApiCtClassBuilder("org.test.JaxrsChain1");
        JaxrsEndpointApiCtClassBuilder result = builder
                .path("/api")
                .produces("application/json")
                .bind("uid", "{}")
                .makeField("public int k = 3;")
                .newField(String.class, "name", "test")
                .newMethod(HttpMethodEnum.GET, "get", "/get");
        assertSame(builder, result);
        CtClass ctClass = result.build();
        assertNotNull(ctClass);
        ctClass.detach();
    }
}

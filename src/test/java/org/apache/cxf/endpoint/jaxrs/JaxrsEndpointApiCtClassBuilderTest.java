package org.apache.cxf.endpoint.jaxrs;

import static org.junit.Assert.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.apache.cxf.endpoint.jaxrs.definition.HttpMethodEnum;
import org.apache.cxf.endpoint.jaxrs.definition.HttpParamEnum;
import org.apache.cxf.endpoint.jaxrs.definition.RestBound;
import org.apache.cxf.endpoint.jaxrs.definition.RestMethod;
import org.apache.cxf.endpoint.jaxrs.definition.RestParam;
import org.junit.Test;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;

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

    // ==================== 新增强化测试：注解验证 & @DefaultValue bug 修复验证 ====================

    @Test
    public void shouldVerifyClassLevelPathAndProducesAnnotations() throws Exception {
        Class<?> clazz = new JaxrsEndpointApiCtClassBuilder("org.test.JaxrsAnn1")
                .path("/users")
                .produces("application/json", "application/xml")
                .toClass();

        Path pathAnno = clazz.getAnnotation(Path.class);
        assertNotNull(pathAnno);
        assertEquals("/users", pathAnno.value());

        Produces producesAnno = clazz.getAnnotation(Produces.class);
        assertNotNull(producesAnno);
        assertArrayEquals(new String[]{"application/json", "application/xml"}, producesAnno.value());
    }

    @Test
    public void shouldVerifyHttpMethodAnnotationsOnMethods() throws Exception {
        RestMethod[] methods = new RestMethod[]{
                new RestMethod(HttpMethodEnum.GET, "mGet", "/a"),
                new RestMethod(HttpMethodEnum.POST, "mPost", "/b"),
                new RestMethod(HttpMethodEnum.PUT, "mPut", "/c"),
                new RestMethod(HttpMethodEnum.DELETE, "mDel", "/d"),
                new RestMethod(HttpMethodEnum.PATCH, "mPatch", "/e"),
                new RestMethod(HttpMethodEnum.HEAD, "mHead", "/f"),
                new RestMethod(HttpMethodEnum.OPTIONS, "mOpt", "/g")
        };
        JaxrsEndpointApiCtClassBuilder b = new JaxrsEndpointApiCtClassBuilder("org.test.JaxrsAnn2");
        for (RestMethod m : methods) {
            b.newMethod(String.class, m);
        }
        Class<?> clazz = b.toClass();

        assertNotNull(clazz.getMethod("mGet").getAnnotation(GET.class));
        assertNotNull(clazz.getMethod("mPost").getAnnotation(POST.class));
        assertNotNull(clazz.getMethod("mPut").getAnnotation(PUT.class));
        assertNotNull(clazz.getMethod("mDel").getAnnotation(DELETE.class));
        assertNotNull(clazz.getMethod("mPatch").getAnnotation(PATCH.class));
        assertNotNull(clazz.getMethod("mHead").getAnnotation(HEAD.class));
        assertNotNull(clazz.getMethod("mOpt").getAnnotation(OPTIONS.class));

        assertEquals("/a", clazz.getMethod("mGet").getAnnotation(Path.class).value());
        assertEquals("/b", clazz.getMethod("mPost").getAnnotation(Path.class).value());
    }

    @Test
    public void shouldVerifyMethodLevelConsumes() throws Exception {
        RestMethod rm = new RestMethod(HttpMethodEnum.POST, "add", "/data", "application/json");
        Class<?> clazz = new JaxrsEndpointApiCtClassBuilder("org.test.JaxrsAnn3")
                .newMethod(rm)
                .toClass();
        Method m = clazz.getMethod("add", new Class<?>[0]);
        Consumes c = m.getAnnotation(Consumes.class);
        assertNotNull(c);
        assertArrayEquals(new String[]{"application/json"}, c.value());
    }

    @Test
    public void shouldVerifyPathParamAnnotationOnParameter() throws Exception {
        RestParam<String> idParam = new RestParam<>(String.class, "userId", HttpParamEnum.PATH);
        Class<?> clazz = new JaxrsEndpointApiCtClassBuilder("org.test.JaxrsAnn4")
                .newMethod(String.class, HttpMethodEnum.GET, "findById", "/{userId}", idParam)
                .toClass();
        Method m = clazz.getMethod("findById", String.class);
        Annotation[][] anns = m.getParameterAnnotations();
        assertEquals(1, anns.length);
        assertTrue(hasAnnotation(anns[0], PathParam.class));
        assertEquals("userId", findAnnotation(anns[0], PathParam.class).value());
    }

    @Test
    public void shouldVerifyQueryParamAnnotationOnParameter() throws Exception {
        RestParam<String> nameParam = new RestParam<>(String.class, "name", HttpParamEnum.QUERY);
        Class<?> clazz = new JaxrsEndpointApiCtClassBuilder("org.test.JaxrsAnn5")
                .newMethod(String.class, HttpMethodEnum.GET, "search", "/s", nameParam)
                .toClass();
        Method m = clazz.getMethod("search", String.class);
        Annotation[][] anns = m.getParameterAnnotations();
        assertTrue(hasAnnotation(anns[0], QueryParam.class));
        assertEquals("name", findAnnotation(anns[0], QueryParam.class).value());
    }

    @Test
    public void shouldVerifyHeaderCookieFormMatrixParamAnnotations() throws Exception {
        RestParam<String> h = new RestParam<>(String.class, "Auth", HttpParamEnum.HEADER);
        RestParam<String> c = new RestParam<>(String.class, "sid", HttpParamEnum.COOKIE);
        RestParam<String> f = new RestParam<>(String.class, "body", HttpParamEnum.FORM);
        RestParam<String> mx = new RestParam<>(String.class, "m", HttpParamEnum.MATRIX);

        Class<?> clazz = new JaxrsEndpointApiCtClassBuilder("org.test.JaxrsAnn6")
                .newMethod(String.class, HttpMethodEnum.POST, "mix", "/m", h, c, f, mx)
                .toClass();
        Method m = clazz.getMethod("mix", String.class, String.class, String.class, String.class);
        Annotation[][] anns = m.getParameterAnnotations();
        assertEquals(4, anns.length);

        assertTrue(hasAnnotation(anns[0], HeaderParam.class));
        assertEquals("Auth", findAnnotation(anns[0], HeaderParam.class).value());

        assertTrue(hasAnnotation(anns[1], CookieParam.class));
        assertEquals("sid", findAnnotation(anns[1], CookieParam.class).value());

        assertTrue(hasAnnotation(anns[2], FormParam.class));
        assertEquals("body", findAnnotation(anns[2], FormParam.class).value());

        assertTrue(hasAnnotation(anns[3], MatrixParam.class));
        assertEquals("m", findAnnotation(anns[3], MatrixParam.class).value());
    }

    @Test
    public void shouldVerifyBeanParamAnnotationWithoutValue() throws Exception {
        RestParam<Object> bean = new RestParam<>(Object.class, "ignored", HttpParamEnum.BEAN);
        Class<?> clazz = new JaxrsEndpointApiCtClassBuilder("org.test.JaxrsAnn7")
                .newMethod(String.class, HttpMethodEnum.GET, "bean", "/b", bean)
                .toClass();
        Method m = clazz.getMethod("bean", Object.class);
        Annotation[][] anns = m.getParameterAnnotations();
        assertTrue(hasAnnotation(anns[0], BeanParam.class));
    }

    @Test
    public void shouldVerifyDefaultValueAnnotationOnParam() throws Exception {
        RestParam<String> p = new RestParam<>(String.class, "page", HttpParamEnum.QUERY, "1");
        Class<?> clazz = new JaxrsEndpointApiCtClassBuilder("org.test.JaxrsAnn8")
                .newMethod(String.class, HttpMethodEnum.GET, "list", "/list", p)
                .toClass();
        Method m = clazz.getMethod("list", String.class);
        Annotation[][] anns = m.getParameterAnnotations();
        Annotation[] paramAnns = anns[0];

        assertTrue("@QueryParam missing", hasAnnotation(paramAnns, QueryParam.class));
        assertEquals("page", findAnnotation(paramAnns, QueryParam.class).value());

        assertTrue("@DefaultValue missing — bug #462 fix verification", hasAnnotation(paramAnns, DefaultValue.class));
        assertEquals("1", findAnnotation(paramAnns, DefaultValue.class).value());
    }

    @Test
    public void shouldVerifyMultipleParamsMixedWithAndWithoutDefaultValue() throws Exception {
        RestParam<String> q = new RestParam<>(String.class, "kw", HttpParamEnum.QUERY, "");
        RestParam<Integer> page = new RestParam<>(Integer.class, "page", HttpParamEnum.QUERY, "1");
        RestParam<Integer> size = new RestParam<>(Integer.class, "size", HttpParamEnum.QUERY, "20");

        Class<?> clazz = new JaxrsEndpointApiCtClassBuilder("org.test.JaxrsAnn9")
                .newMethod(String.class, HttpMethodEnum.GET, "search2", "/q", q, page, size)
                .toClass();
        Method m = clazz.getMethod("search2", String.class, Integer.class, Integer.class);
        Annotation[][] anns = m.getParameterAnnotations();

        assertTrue("page should have @DefaultValue", hasAnnotation(anns[1], DefaultValue.class));
        assertEquals("1", findAnnotation(anns[1], DefaultValue.class).value());

        assertTrue("size should have @DefaultValue", hasAnnotation(anns[2], DefaultValue.class));
        assertEquals("20", findAnnotation(anns[2], DefaultValue.class).value());

        assertFalse("q has def='' (blank), should NOT get @DefaultValue", hasAnnotation(anns[0], DefaultValue.class));
    }

    @Test
    public void shouldVerifyRestParamFromIsCorrectlyApplied() throws Exception {
        RestParam<String> p1 = new RestParam<>(String.class, "x", HttpParamEnum.PATH);
        assertEquals(HttpParamEnum.PATH, p1.getFrom());

        RestParam<String> p2 = new RestParam<>(String.class, "x", HttpParamEnum.HEADER, "abc");
        assertEquals(HttpParamEnum.HEADER, p2.getFrom());
        assertEquals("abc", p2.getDef());

        RestParam<String> p3 = new RestParam<>(String.class, "q");
        assertEquals(HttpParamEnum.QUERY, p3.getFrom());

        RestParam<String> p4 = new RestParam<>(String.class, "q", "dv");
        assertEquals(HttpParamEnum.QUERY, p4.getFrom());
        assertEquals("dv", p4.getDef());
    }

    private static boolean hasAnnotation(Annotation[] anns, Class<? extends Annotation> type) {
        for (Annotation a : anns) {
            if (type.isInstance(a)) return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Annotation> T findAnnotation(Annotation[] anns, Class<T> type) {
        for (Annotation a : anns) {
            if (type.isInstance(a)) return (T) a;
        }
        return null;
    }
}

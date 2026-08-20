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

package org.apache.cxf.endpoint.jaxws;

import static org.junit.Assert.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.UUID;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.xml.ws.Service;

import org.apache.cxf.endpoint.jaxws.definition.SoapBound;
import org.apache.cxf.endpoint.jaxws.definition.SoapMethod;
import org.apache.cxf.endpoint.jaxws.definition.SoapParam;
import org.apache.cxf.endpoint.jaxws.definition.SoapResult;
import org.apache.cxf.endpoint.jaxws.definition.SoapService;
import org.junit.Test;

import javassist.ClassPool;
import javassist.CtClass;

public class JaxwsEndpointApiCtClassBuilderTest {

    @Test
    public void shouldBuildClassWithDefaultPool() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsDefault1")
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldBuildClassWithCustomPool() throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = new JaxwsEndpointApiCtClassBuilder(pool, "org.test.JaxwsCustom1")
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldAddWebServiceAnnotation() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsWs1")
                .webService("MyService", "http://example.com")
                .build();
        assertNotNull(ctClass.getAnnotation(jakarta.jws.WebService.class));
        ctClass.detach();
    }

    @Test
    public void shouldAddWebServiceWithServiceName() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsWs2")
                .webService("MyService", "http://example.com", "MyWSDLService")
                .build();
        assertNotNull(ctClass.getAnnotation(jakarta.jws.WebService.class));
        ctClass.detach();
    }

    @Test
    public void shouldAddFullWebServiceAnnotation() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsWs3")
                .webService("MyService", "http://example.com", "svc", "port", "/wsdl", "com.example.Sei")
                .build();
        assertNotNull(ctClass.getAnnotation(jakarta.jws.WebService.class));
        ctClass.detach();
    }

    @Test
    public void shouldAddWebServiceFromDescriptor() throws Exception {
        SoapService svc = new SoapService("MyService", "http://example.com", "svc");
        CtClass ctClass = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsWs4")
                .webService(svc)
                .build();
        assertNotNull(ctClass.getAnnotation(jakarta.jws.WebService.class));
        ctClass.detach();
    }

    @Test
    public void shouldAddWebServiceProviderAnnotation() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsWsp1")
                .webServiceProvider("/wsdl", "svc", "http://ns", "port")
                .build();
        assertNotNull(ctClass.getAnnotation(jakarta.xml.ws.WebServiceProvider.class));
        ctClass.detach();
    }

    @Test
    public void shouldAddAddressingAnnotation() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsAddr1")
                .addressing(true, true, jakarta.xml.ws.soap.AddressingFeature.Responses.ALL)
                .build();
        assertNotNull(ctClass.getAnnotation(jakarta.xml.ws.soap.Addressing.class));
        ctClass.detach();
    }

    @Test
    public void shouldAddServiceModeAnnotation() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsSm1")
                .serviceMode(Service.Mode.PAYLOAD)
                .build();
        assertNotNull(ctClass.getAnnotation(jakarta.xml.ws.ServiceMode.class));
        ctClass.detach();
    }

    @Test
    public void shouldBindWithUidAndJson() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsBind1")
                .bind("uid", "{}")
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldBindWithSoapBound() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsBind2")
                .bind(new SoapBound("uid", "{}"))
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldMakeFieldFromSource() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsField1")
                .makeField("public int k = 3;")
                .build();
        assertNotNull(ctClass.getDeclaredField("k"));
        ctClass.detach();
    }

    @Test
    public void shouldAddTypedField() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsField2")
                .newField(String.class, "uid", UUID.randomUUID().toString())
                .build();
        assertNotNull(ctClass.getDeclaredField("uid"));
        ctClass.detach();
    }

    @Test
    public void shouldRemoveExistingField() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsField3")
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
        CtClass ctClass = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsField4")
                .removeField("nonexistent")
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldMakeMethodFromSource() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsMakeMethod1")
                .makeMethod("public String hello() { return \"hi\"; }")
                .build();
        assertNotNull(ctClass.getDeclaredMethod("hello"));
        ctClass.detach();
    }

    @Test
    public void shouldAddMethodByNameAndParams() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsMethod1")
                .newMethod("sayHello", new SoapParam(String.class, "text"))
                .build();
        assertNotNull(ctClass.getDeclaredMethod("sayHello"));
        ctClass.detach();
    }

    @Test
    public void shouldAddMethodWithNameAndBound() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsMethod2")
                .newMethod("sayHello", new SoapBound("b1"),
                        new SoapParam(String.class, "text"))
                .build();
        assertNotNull(ctClass.getDeclaredMethod("sayHello"));
        ctClass.detach();
    }

    @Test
    public void shouldAddMethodWithResultAndBound() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsMethod3")
                .newMethod(new SoapResult<>(String.class, "result"),
                        new SoapMethod("greet"),
                        new SoapBound("b1"),
                        new SoapParam(String.class, "name"))
                .build();
        assertNotNull(ctClass.getDeclaredMethod("greet"));
        ctClass.detach();
    }

    @Test
    public void shouldAddMethodWithNullResult() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsMethod4")
                .newMethod(null, new SoapMethod("doSomething"), null)
                .build();
        assertNotNull(ctClass.getDeclaredMethod("doSomething"));
        ctClass.detach();
    }

    @Test
    public void shouldRemoveExistingMethod() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsMethod5")
                .newMethod("temp")
                .removeMethod("temp")
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldNoopWhenRemovingNonexistentMethod() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsMethod6")
                .removeMethod("nonexistent")
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldRemoveMethodWithParams() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsMethod7")
                .newMethod("withParam", new SoapParam(String.class, "x"))
                .removeMethod("withParam", new SoapParam(String.class, "x"))
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldNoopWhenRemovingNonexistentMethodWithParams() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsMethod8")
                .removeMethod("nope", new SoapParam(String.class, "x"))
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldSetSuperclassToEndpointApi() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsParent1")
                .build();
        assertNotNull(ctClass.getSuperclass());
        assertEquals("org.apache.cxf.endpoint.EndpointApi", ctClass.getSuperclass().getName());
        ctClass.detach();
    }

    @Test
    public void shouldHaveDefaultConstructor() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsCtor1")
                .build();
        assertTrue(ctClass.getConstructors().length > 0);
        ctClass.detach();
    }

    @Test
    public void shouldSupportFluentChaining() throws Exception {
        JaxwsEndpointApiCtClassBuilder builder = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsChain1");
        JaxwsEndpointApiCtClassBuilder result = builder
                .webService("svc", "http://ns")
                .bind("uid", "{}")
                .makeField("public int k = 3;")
                .newField(String.class, "name", "test")
                .newMethod("hello");
        assertSame(builder, result);
        CtClass ctClass = result.build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    // ==================== 新增强化测试：注解属性深度验证 ====================

    @Test
    public void shouldVerifyWebServiceAnnotationAttributes() throws Exception {
        Class<?> clazz = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsAnn1")
                .webService("UserService", "http://example.com/ns", "UserSvc", "UserPort", "/wsdl/user.wsdl", "com.example.UserSEI")
                .toClass();
        WebService ws = clazz.getAnnotation(WebService.class);
        assertNotNull(ws);
        assertEquals("UserService", ws.name());
        assertEquals("http://example.com/ns", ws.targetNamespace());
        assertEquals("UserSvc", ws.serviceName());
        assertEquals("UserPort", ws.portName());
        assertEquals("/wsdl/user.wsdl", ws.wsdlLocation());
        assertEquals("com.example.UserSEI", ws.endpointInterface());
    }

    @Test
    public void shouldVerifyWebServiceAnnotationFromDescriptor() throws Exception {
        SoapService svc = new SoapService("OrderService", "http://o.example.com", "OrderSvc", "OrderPort");
        svc.setWsdlLocation("/wsdl/order.wsdl");
        svc.setEndpointInterface("com.example.OrderSEI");

        Class<?> clazz = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsAnn2")
                .webService(svc)
                .toClass();
        WebService ws = clazz.getAnnotation(WebService.class);
        assertNotNull(ws);
        assertEquals("OrderService", ws.name());
        assertEquals("http://o.example.com", ws.targetNamespace());
        assertEquals("OrderSvc", ws.serviceName());
        assertEquals("OrderPort", ws.portName());
        assertEquals("/wsdl/order.wsdl", ws.wsdlLocation());
        assertEquals("com.example.OrderSEI", ws.endpointInterface());
    }

    @Test
    public void shouldVerifyServiceModeAnnotationAttribute() throws Exception {
        Class<?> clazz = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsAnn3")
                .serviceMode(Service.Mode.MESSAGE)
                .toClass();
        jakarta.xml.ws.ServiceMode sm = clazz.getAnnotation(jakarta.xml.ws.ServiceMode.class);
        assertNotNull(sm);
        assertEquals(Service.Mode.MESSAGE, sm.value());
    }

    @Test
    public void shouldVerifyAddressingAnnotationAttributes() throws Exception {
        Class<?> clazz = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsAnn4")
                .addressing(true, false, jakarta.xml.ws.soap.AddressingFeature.Responses.ANONYMOUS)
                .toClass();
        jakarta.xml.ws.soap.Addressing addr = clazz.getAnnotation(jakarta.xml.ws.soap.Addressing.class);
        assertNotNull(addr);
        assertTrue(addr.enabled());
        assertFalse(addr.required());
        assertEquals(jakarta.xml.ws.soap.AddressingFeature.Responses.ANONYMOUS, addr.responses());
    }

    @Test
    public void shouldVerifyWebMethodAnnotationAttributes() throws Exception {
        SoapMethod sm = new SoapMethod("SayHelloOp", "urn:sayHelloAction", false);
        Class<?> clazz = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsAnn5")
                .webService("S", "http://n")
                .newMethod(null, sm, null)
                .toClass();
        Method m = clazz.getMethod("SayHelloOp", new Class<?>[0]);
        WebMethod wm = m.getAnnotation(WebMethod.class);
        assertNotNull(wm);
        assertEquals("SayHelloOp", wm.operationName());
        assertEquals("urn:sayHelloAction", wm.action());
        assertFalse(wm.exclude());
    }

    @Test
    public void shouldVerifyWebMethodExcludeFlag() throws Exception {
        SoapMethod sm = new SoapMethod("internalDoWork", "", true);
        Class<?> clazz = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsAnn6")
                .webService("S", "http://n")
                .newMethod(null, sm, null)
                .toClass();
        Method m = clazz.getMethod("internalDoWork", new Class<?>[0]);
        WebMethod wm = m.getAnnotation(WebMethod.class);
        assertNotNull(wm);
        assertTrue(wm.exclude());
    }

    @Test
    public void shouldVerifyWebParamAnnotationOnParameter() throws Exception {
        Class<?> clazz = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsAnn7")
                .webService("S", "http://n")
                .newMethod(new SoapResult<>(String.class, "greetResult"),
                        new SoapMethod("greet"),
                        new SoapBound("b1"),
                        new SoapParam(String.class, "userName"))
                .toClass();
        Method m = clazz.getMethod("greet", String.class);
        Annotation[][] anns = m.getParameterAnnotations();
        assertEquals(1, anns.length);
        assertTrue(hasAnnotation(anns[0], WebParam.class));
        WebParam wp = findAnnotation(anns[0], WebParam.class);
        assertEquals("userName", wp.name());
        assertEquals(WebParam.Mode.IN, wp.mode());
        assertFalse(wp.header());
    }

    @Test
    public void shouldVerifyWebParamHeaderAndMode() throws Exception {
        SoapParam<String> authParam = new SoapParam<>(String.class, "AuthToken", WebParam.Mode.IN, true);
        SoapParam<Integer> outParam = new SoapParam<>(Integer.class, "count", WebParam.Mode.INOUT, false);

        Class<?> clazz = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsAnn8")
                .webService("S", "http://n")
                .newMethod("op", authParam, outParam)
                .toClass();
        Method m = clazz.getMethod("op", String.class, Integer.class);
        Annotation[][] anns = m.getParameterAnnotations();

        WebParam wp0 = findAnnotation(anns[0], WebParam.class);
        assertNotNull(wp0);
        assertEquals("AuthToken", wp0.name());
        assertEquals(WebParam.Mode.IN, wp0.mode());
        assertTrue(wp0.header());

        WebParam wp1 = findAnnotation(anns[1], WebParam.class);
        assertNotNull(wp1);
        assertEquals("count", wp1.name());
        assertEquals(WebParam.Mode.INOUT, wp1.mode());
        assertFalse(wp1.header());
    }

    @Test
    public void shouldVerifyWebParamPartNameAndTargetNamespace() throws Exception {
        SoapParam<String> p = new SoapParam<>(String.class, "x", "partX", "http://ns.p", WebParam.Mode.IN, false);
        Class<?> clazz = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsAnn9")
                .webService("S", "http://n")
                .newMethod("op", p)
                .toClass();
        Method m = clazz.getMethod("op", String.class);
        WebParam wp = findAnnotation(m.getParameterAnnotations()[0], WebParam.class);
        assertNotNull(wp);
        assertEquals("x", wp.name());
        assertEquals("partX", wp.partName());
        assertEquals("http://ns.p", wp.targetNamespace());
    }

    @Test
    public void shouldVerifyWebResultAnnotation() throws Exception {
        SoapResult<String> result = new SoapResult<>(String.class, "sumResult", "http://r.ns", false, "sumPart");
        Class<?> clazz = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsAnn10")
                .webService("S", "http://n")
                .newMethod(result, new SoapMethod("add"), null,
                        new SoapParam(Integer.class, "a"), new SoapParam(Integer.class, "b"))
                .toClass();
        Method m = clazz.getMethod("add", Integer.class, Integer.class);
        WebResult wr = m.getAnnotation(WebResult.class);
        assertNotNull(wr);
        assertEquals("sumResult", wr.name());
        assertEquals("sumPart", wr.partName());
        assertEquals("http://r.ns", wr.targetNamespace());
        assertFalse(wr.header());
    }

    @Test
    public void shouldVerifyWebResultHeaderFlag() throws Exception {
        SoapResult<String> result = new SoapResult<>(String.class, "token");
        result.setHeader(true);
        Class<?> clazz = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsAnn11")
                .webService("S", "http://n")
                .newMethod(result, new SoapMethod("login"), null,
                        new SoapParam(String.class, "u"), new SoapParam(String.class, "pw"))
                .toClass();
        Method m = clazz.getMethod("login", String.class, String.class);
        WebResult wr = m.getAnnotation(WebResult.class);
        assertNotNull(wr);
        assertEquals("token", wr.name());
        assertTrue(wr.header());
    }

    @Test
    public void shouldVerifyMultipleParamsAndResultCombined() throws Exception {
        SoapResult<String> result = new SoapResult<>(String.class, "resp");
        SoapMethod sm = new SoapMethod("doWork", "urn:doWork", false);
        SoapBound bound = new SoapBound("tx-123", "{\"ctx\":1}");
        SoapParam<String> p1 = new SoapParam<>(String.class, "reqId", WebParam.Mode.IN, true);
        SoapParam<String> p2 = new SoapParam<>(String.class, "body", "bodyPart", "http://b", WebParam.Mode.IN, false);

        Class<?> clazz = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsAnn12")
                .webService("WorkSvc", "http://work")
                .newMethod(result, sm, bound, p1, p2)
                .toClass();

        Method m = clazz.getMethod("doWork", String.class, String.class);

        WebMethod wm = m.getAnnotation(WebMethod.class);
        assertNotNull(wm);
        assertEquals("doWork", wm.operationName());
        assertEquals("urn:doWork", wm.action());

        WebResult wr = m.getAnnotation(WebResult.class);
        assertNotNull(wr);
        assertEquals("resp", wr.name());

        Annotation[][] anns = m.getParameterAnnotations();
        assertEquals(2, anns.length);

        WebParam wp0 = findAnnotation(anns[0], WebParam.class);
        assertEquals("reqId", wp0.name());
        assertTrue(wp0.header());
        assertEquals(WebParam.Mode.IN, wp0.mode());

        WebParam wp1 = findAnnotation(anns[1], WebParam.class);
        assertEquals("body", wp1.name());
        assertEquals("bodyPart", wp1.partName());
        assertEquals("http://b", wp1.targetNamespace());
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

package org.apache.cxf.endpoint.jaxws;

import static org.junit.Assert.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

import jakarta.jws.WebParam;
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

    private final InvocationHandler handler = (proxy, method, args) -> "invoked";

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
        Class<?> clazz = ctClass.toClass();
        assertNotNull(clazz.getMethod("sayHello", String.class));
        ctClass.detach();
    }

    @Test
    public void shouldAddMethodWithNameAndBound() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsMethod2")
                .newMethod("sayHello", new SoapBound("b1"),
                        new SoapParam(String.class, "text"))
                .build();
        Class<?> clazz = ctClass.toClass();
        assertNotNull(clazz.getMethod("sayHello", String.class));
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
        Class<?> clazz = ctClass.toClass();
        assertNotNull(clazz.getMethod("greet", String.class));
        ctClass.detach();
    }

    @Test
    public void shouldAddMethodWithNullResult() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsMethod4")
                .newMethod(null, new SoapMethod("doSomething"), null)
                .build();
        Class<?> clazz = ctClass.toClass();
        assertNotNull(clazz.getMethod("doSomething"));
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
    public void shouldConvertToClass() throws Exception {
        Class<?> clazz = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsToClass1")
                .webService("svc", "http://ns")
                .toClass();
        assertNotNull(clazz);
    }

    @Test
    public void shouldConvertToInstance() throws Exception {
        Object instance = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsToInst1")
                .webService("svc", "http://ns")
                .newMethod("hello", new SoapParam(String.class, "name"))
                .toInstance(handler);
        assertNotNull(instance);
        assertTrue(instance instanceof org.apache.cxf.endpoint.EndpointApi);
        Method hello = instance.getClass().getMethod("hello", String.class);
        Object result = hello.invoke(instance, "World");
        assertEquals("invoked", result);
    }

    @Test
    public void shouldDispatchMethodCallThroughHandler() throws Exception {
        Object instance = new JaxwsEndpointApiCtClassBuilder("org.test.JaxwsDispatch1")
                .webService("svc", "http://ns")
                .newMethod(new SoapResult<>(String.class, "result"),
                        new SoapMethod("greet"),
                        new SoapBound("b1"),
                        new SoapParam(String.class, "name"))
                .toInstance(handler);
        Method greet = instance.getClass().getMethod("greet", String.class);
        Object result = greet.invoke(instance, "World");
        assertEquals("invoked", result);
    }
}

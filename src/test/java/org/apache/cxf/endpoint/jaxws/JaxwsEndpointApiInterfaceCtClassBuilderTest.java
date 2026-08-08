package org.apache.cxf.endpoint.jaxws;

import static org.junit.Assert.*;

import jakarta.jws.WebParam;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.soap.AddressingFeature;

import org.apache.cxf.endpoint.jaxws.definition.SoapBound;
import org.apache.cxf.endpoint.jaxws.definition.SoapMethod;
import org.apache.cxf.endpoint.jaxws.definition.SoapParam;
import org.apache.cxf.endpoint.jaxws.definition.SoapResult;
import org.apache.cxf.endpoint.jaxws.definition.SoapService;
import org.junit.Test;

import javassist.ClassPool;
import javassist.CtClass;

public class JaxwsEndpointApiInterfaceCtClassBuilderTest {

    @Test
    public void shouldBuildInterfaceWithDefaultPool() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiInterfaceCtClassBuilder("org.test.JaxwsIface1")
                .build();
        assertNotNull(ctClass);
        assertTrue(ctClass.isInterface());
        ctClass.detach();
    }

    @Test
    public void shouldBuildInterfaceWithCustomPool() throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = new JaxwsEndpointApiInterfaceCtClassBuilder(pool, "org.test.JaxwsIface2")
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldAddWebServiceAnnotation() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiInterfaceCtClassBuilder("org.test.JaxwsIface3")
                .webService("svc", "http://ns")
                .build();
        assertNotNull(ctClass.getAnnotation(jakarta.jws.WebService.class));
        ctClass.detach();
    }

    @Test
    public void shouldAddWebServiceWithServiceName() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiInterfaceCtClassBuilder("org.test.JaxwsIface4")
                .webService("svc", "http://ns", "wsdlSvc")
                .build();
        assertNotNull(ctClass.getAnnotation(jakarta.jws.WebService.class));
        ctClass.detach();
    }

    @Test
    public void shouldAddFullWebService() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiInterfaceCtClassBuilder("org.test.JaxwsIface5")
                .webService("svc", "http://ns", "wsdlSvc", "port", "/wsdl", "com.example.Sei")
                .build();
        assertNotNull(ctClass.getAnnotation(jakarta.jws.WebService.class));
        ctClass.detach();
    }

    @Test
    public void shouldAddWebServiceFromDescriptor() throws Exception {
        SoapService svc = new SoapService("svc", "http://ns", "wsdlSvc");
        CtClass ctClass = new JaxwsEndpointApiInterfaceCtClassBuilder("org.test.JaxwsIface6")
                .webService(svc)
                .build();
        assertNotNull(ctClass.getAnnotation(jakarta.jws.WebService.class));
        ctClass.detach();
    }

    @Test
    public void shouldAddServiceModeAnnotation() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiInterfaceCtClassBuilder("org.test.JaxwsIface7")
                .serviceMode(Service.Mode.PAYLOAD)
                .build();
        assertNotNull(ctClass.getAnnotation(jakarta.xml.ws.ServiceMode.class));
        ctClass.detach();
    }

    @Test
    public void shouldAddWebServiceProviderAnnotation() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiInterfaceCtClassBuilder("org.test.JaxwsIface8")
                .webServiceProvider("/wsdl", "svc", "http://ns", "port")
                .build();
        assertNotNull(ctClass.getAnnotation(jakarta.xml.ws.WebServiceProvider.class));
        ctClass.detach();
    }

    @Test
    public void shouldAddAddressingAnnotation() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiInterfaceCtClassBuilder("org.test.JaxwsIface9")
                .addressing(true, true, AddressingFeature.Responses.ALL)
                .build();
        assertNotNull(ctClass.getAnnotation(jakarta.xml.ws.soap.Addressing.class));
        ctClass.detach();
    }

    @Test
    public void shouldBindUidJson() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiInterfaceCtClassBuilder("org.test.JaxwsIface10")
                .bind("uid", "{}")
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldBindSoapBound() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiInterfaceCtClassBuilder("org.test.JaxwsIface11")
                .bind(new SoapBound("uid", "{}"))
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldMakeFieldFromSource() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiInterfaceCtClassBuilder("org.test.JaxwsIface12")
                .makeField("public int k = 3;")
                .build();
        assertNotNull(ctClass.getDeclaredField("k"));
        ctClass.detach();
    }

    @Test
    public void shouldAddTypedField() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiInterfaceCtClassBuilder("org.test.JaxwsIface13")
                .newField(String.class, "uid", "test")
                .build();
        assertNotNull(ctClass.getDeclaredField("uid"));
        ctClass.detach();
    }

    @Test
    public void shouldNoopWhenFieldAlreadyExists() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiInterfaceCtClassBuilder("org.test.JaxwsIface14")
                .newField(String.class, "uid", "test")
                .newField(String.class, "uid", "test2")
                .build();
        assertNotNull(ctClass.getDeclaredField("uid"));
        ctClass.detach();
    }

    @Test
    public void shouldRemoveExistingField() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiInterfaceCtClassBuilder("org.test.JaxwsIface15")
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
        CtClass ctClass = new JaxwsEndpointApiInterfaceCtClassBuilder("org.test.JaxwsIface16")
                .removeField("nonexistent")
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldAddAbstractMethodByNameAndParams() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiInterfaceCtClassBuilder("org.test.JaxwsIface17")
                .abstractMethod("sayHello", new SoapParam(String.class, "text"))
                .build();
        assertNotNull(ctClass.getDeclaredMethod("sayHello"));
        ctClass.detach();
    }

    @Test
    public void shouldAddAbstractMethodWithNameAndBound() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiInterfaceCtClassBuilder("org.test.JaxwsIface18")
                .abstractMethod("sayHello", new SoapBound("b1"), new SoapParam(String.class, "text"))
                .build();
        assertNotNull(ctClass.getDeclaredMethod("sayHello"));
        ctClass.detach();
    }

    @Test
    public void shouldAddAbstractMethodWithResultAndBound() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiInterfaceCtClassBuilder("org.test.JaxwsIface19")
                .abstractMethod(new SoapResult<>(String.class, "result"),
                        new SoapMethod("greet"),
                        new SoapBound("b1"),
                        new SoapParam(String.class, "name"))
                .build();
        assertNotNull(ctClass.getDeclaredMethod("greet"));
        ctClass.detach();
    }

    @Test
    public void shouldRemoveExistingMethod() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiInterfaceCtClassBuilder("org.test.JaxwsIface20")
                .abstractMethod("temp")
                .removeMethod("temp")
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldNoopWhenRemovingNonexistentMethod() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiInterfaceCtClassBuilder("org.test.JaxwsIface21")
                .removeMethod("nonexistent")
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldRemoveMethodWithParams() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiInterfaceCtClassBuilder("org.test.JaxwsIface22")
                .abstractMethod("withParam", new SoapParam(String.class, "x"))
                .removeMethod("withParam", new SoapParam(String.class, "x"))
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldNoopWhenRemovingNonexistentMethodWithParams() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiInterfaceCtClassBuilder("org.test.JaxwsIface23")
                .removeMethod("nope", new SoapParam(String.class, "x"))
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldConvertToClass() throws Exception {
        Class<?> clazz = new JaxwsEndpointApiInterfaceCtClassBuilder("org.test.JaxwsIface24")
                .webService("svc", "http://ns")
                .toClass();
        assertNotNull(clazz);
    }

    @Test
    public void shouldAddAbstractMethodWithNullResult() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiInterfaceCtClassBuilder("org.test.JaxwsIface25")
                .abstractMethod(null, new SoapMethod("doSomething"), null)
                .build();
        assertNotNull(ctClass.getDeclaredMethod("doSomething"));
        ctClass.detach();
    }
}

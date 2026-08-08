package org.apache.cxf.endpoint.jaxws;

import static org.junit.Assert.*;

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

public class JaxwsEndpointApiImplCtClassBuilderTest {

    @Test
    public void shouldBuildImplClassWithDefaultPool() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiImplCtClassBuilder("org.test.JaxwsImpl1")
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldBuildImplClassWithCustomPool() throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = new JaxwsEndpointApiImplCtClassBuilder(pool, "org.test.JaxwsImpl2")
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldForwardWebServiceWithNameAndNamespace() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiImplCtClassBuilder("org.test.JaxwsImpl3")
                .webService("svc", "http://ns")
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldForwardWebServiceWithServiceName() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiImplCtClassBuilder("org.test.JaxwsImpl4")
                .webService("svc", "http://ns", "wsdlSvc")
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldForwardFullWebService() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiImplCtClassBuilder("org.test.JaxwsImpl5")
                .webService("svc", "http://ns", "wsdlSvc", "port", "/wsdl", "com.example.Sei")
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldForwardWebServiceFromDescriptor() throws Exception {
        SoapService svc = new SoapService("svc", "http://ns", "wsdlSvc");
        CtClass ctClass = new JaxwsEndpointApiImplCtClassBuilder("org.test.JaxwsImpl6")
                .webService(svc)
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldForwardServiceMode() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiImplCtClassBuilder("org.test.JaxwsImpl7")
                .serviceMode(Service.Mode.PAYLOAD)
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldForwardWebServiceProvider() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiImplCtClassBuilder("org.test.JaxwsImpl8")
                .webServiceProvider("/wsdl", "svc", "http://ns", "port")
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldForwardAddressing() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiImplCtClassBuilder("org.test.JaxwsImpl9")
                .annotAddressing(true, true, AddressingFeature.Responses.ALL)
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldForwardBind() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiImplCtClassBuilder("org.test.JaxwsImpl10")
                .bind(new SoapBound("uid", "{}"))
                .build();
        assertNotNull(ctClass);
        ctClass.detach();
    }

    @Test
    public void shouldAddMethodToBothInterfaceAndImpl() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiImplCtClassBuilder("org.test.JaxwsImpl11")
                .webService("svc", "http://ns")
                .newMethod(new SoapResult<>(String.class, "result"),
                        new SoapMethod("hello"),
                        new SoapBound("b1"),
                        new SoapParam(String.class, "name"))
                .build();
        assertNotNull(ctClass);
        assertNotNull(ctClass.getDeclaredMethod("hello"));
        ctClass.detach();
    }

    @Test
    public void shouldUseImplSuffix() throws Exception {
        CtClass ctClass = new JaxwsEndpointApiImplCtClassBuilder("org.test.JaxwsImplSuffix1")
                .build();
        assertTrue(ctClass.getName().endsWith("$Impl"));
        ctClass.detach();
    }

    @Test
    public void shouldSupportFluentChaining() throws Exception {
        JaxwsEndpointApiImplCtClassBuilder builder = new JaxwsEndpointApiImplCtClassBuilder("org.test.JaxwsImplChain1");
        JaxwsEndpointApiImplCtClassBuilder result = builder
                .webService("svc", "http://ns")
                .bind(new SoapBound("uid", "{}"));
        assertSame(builder, result);
        CtClass ctClass = result.build();
        assertNotNull(ctClass);
        ctClass.detach();
    }
}

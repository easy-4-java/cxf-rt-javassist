package org.apache.cxf.endpoint.jaxws.definition;

import static org.junit.Assert.*;

import org.junit.Test;

public class SoapServiceTest {

    @Test
    public void shouldCreateServiceWithNameAndTargetNamespace() {
        SoapService service = new SoapService("MyService", "http://example.com");
        assertEquals("MyService", service.getName());
        assertEquals("http://example.com", service.getTargetNamespace());
        assertNull(service.getServiceName());
        assertNull(service.getPortName());
        assertNull(service.getWsdlLocation());
        assertNull(service.getEndpointInterface());
    }

    @Test
    public void shouldCreateServiceWithServiceName() {
        SoapService service = new SoapService("MyService", "http://example.com", "MyWSDLService");
        assertEquals("MyWSDLService", service.getServiceName());
    }

    @Test
    public void shouldCreateServiceWithServiceNameAndPortName() {
        SoapService service = new SoapService("MyService", "http://example.com", "svc", "port");
        assertEquals("svc", service.getServiceName());
        assertEquals("port", service.getPortName());
    }

    @Test
    public void shouldCreateServiceWithWsdlLocation() {
        SoapService service = new SoapService("MyService", "http://example.com", "svc", "port", "/wsdl");
        assertEquals("/wsdl", service.getWsdlLocation());
    }

    @Test
    public void shouldCreateFullySpecifiedService() {
        SoapService service = new SoapService("MyService", "http://example.com",
                "svc", "port", "/wsdl", "com.example.Sei");
        assertEquals("MyService", service.getName());
        assertEquals("http://example.com", service.getTargetNamespace());
        assertEquals("svc", service.getServiceName());
        assertEquals("port", service.getPortName());
        assertEquals("/wsdl", service.getWsdlLocation());
        assertEquals("com.example.Sei", service.getEndpointInterface());
    }

    @Test
    public void shouldAllowServiceNameOverride() {
        SoapService service = new SoapService("name", "ns");
        service.setServiceName("newSvc");
        assertEquals("newSvc", service.getServiceName());
    }

    @Test
    public void shouldAllowPortNameOverride() {
        SoapService service = new SoapService("name", "ns");
        service.setPortName("newPort");
        assertEquals("newPort", service.getPortName());
    }

    @Test
    public void shouldAllowWsdlLocationOverride() {
        SoapService service = new SoapService("name", "ns");
        service.setWsdlLocation("/newWsdl");
        assertEquals("/newWsdl", service.getWsdlLocation());
    }

    @Test
    public void shouldAllowEndpointInterfaceOverride() {
        SoapService service = new SoapService("name", "ns");
        service.setEndpointInterface("com.example.NewSei");
        assertEquals("com.example.NewSei", service.getEndpointInterface());
    }
}

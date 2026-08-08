package org.apache.cxf.endpoint.jaxws;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.Assert.*;

import jakarta.jws.WebParam;

import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.endpoint.jaxws.definition.SoapBound;
import org.apache.cxf.endpoint.jaxws.definition.SoapMethod;
import org.apache.cxf.endpoint.jaxws.definition.SoapParam;
import org.apache.cxf.endpoint.jaxws.definition.SoapResult;
import org.junit.Test;

import javassist.CtClass;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class JaxwsApiCtClassBuilder_Test {

	@Test
	public void testClass() throws Exception {

		CtClass ctClass = new JaxwsEndpointApiCtClassBuilder("org.apache.cxf.spring.boot.FirstCaseV1")
				.webService("get", "http://ws.cxf.com", "getxx").makeField("public int k = 3;")
				.newField(String.class, "uid", UUID.randomUUID().toString())
				.newMethod("sayHello", new SoapParam(String.class, "text"))
				.newMethod(new SoapResult<String>(String.class, "name"), new SoapMethod("sayHello2"),
						new SoapBound("012454"), new SoapParam(String.class, "text", WebParam.Mode.OUT))
				.build();

		assertNotNull(ctClass);
		assertNotNull(ctClass.getDeclaredMethod("sayHello"));
		assertNotNull(ctClass.getDeclaredMethod("sayHello2"));
		assertNotNull(ctClass.getDeclaredField("k"));
		assertNotNull(ctClass.getDeclaredField("uid"));
		ctClass.detach();
	}

	@Test
	public void testInstance() throws Exception{

		InvocationHandler handler = new EndpointApiInvocationHandler();

		CtClass ctClass = new JaxwsEndpointApiCtClassBuilder("org.apache.cxf.spring.boot.FirstCaseV2")
				.webService("get", "http://ws.cxf.com", "getxx").makeField("public int k = 3;")
				.newField(String.class, "uid", UUID.randomUUID().toString())
				.newMethod("sayHello", new SoapParam(String.class, "text"))
				.newMethod(new SoapResult<String>(String.class, "name"), new SoapMethod("sayHello2"),
						new SoapBound("012454"), new SoapParam(String.class, "text", WebParam.Mode.OUT))
				.build();

		assertNotNull(ctClass);
		assertNotNull(ctClass.getDeclaredMethod("sayHello"));
		assertNotNull(ctClass.getDeclaredMethod("sayHello2"));
		ctClass.detach();
	}

}

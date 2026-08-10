/*
 * Copyright (c) 2018, Loong Wan (https://github.com/loong10k).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.cxf.endpoint.utils;

import java.lang.reflect.InvocationHandler;

import jakarta.jws.HandlerChain;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.ServiceMode;
import jakarta.xml.ws.WebServiceProvider;
import jakarta.xml.ws.soap.Addressing;
import jakarta.xml.ws.soap.AddressingFeature.Responses;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.endpoint.annotation.WebBound;
import org.apache.cxf.endpoint.jaxws.definition.SoapBound;
import org.apache.cxf.endpoint.jaxws.definition.SoapMethod;
import org.apache.cxf.endpoint.jaxws.definition.SoapParam;
import org.apache.cxf.endpoint.jaxws.definition.SoapResult;
import org.apache.cxf.endpoint.jaxws.definition.SoapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.easy4j.javassist.bytecode.CtAnnotationBuilder;
import io.github.easy4j.javassist.utils.JavassistUtils;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.ParameterAnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;

/**
 * Utility methods used by the JAX-WS endpoint builders to create
 * Javassist classes, interfaces, constructors, methods, and
 * annotations.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see org.apache.cxf.endpoint.jaxws.JaxwsEndpointApiCtClassBuilder
 */
public class JaxwsEndpointApiUtils {

	protected static final Logger LOG = LoggerFactory.getLogger(JaxwsEndpointApiUtils.class);

    /**
     * Creates or retrieves a concrete class in the supplied pool. If
     * the class already exists the existing instance is returned.
     *
     * @param pool      the class pool.
     * @param classname fully qualified name of the class.
     * @return the created or retrieved {@link CtClass}.
     * @throws NotFoundException      if a referenced type cannot be
     *                                resolved.
     * @throws CannotCompileException if the class cannot be compiled.
     */
	public static CtClass makeClass(final ClassPool pool, final String classname)
			throws NotFoundException, CannotCompileException {

		CtClass declaring = pool.getOrNull(classname);
		if (null == declaring) {
			declaring = pool.makeClass(classname);
		}
		
		// 当 ClassPool.doPruning=true的时候，Javassist 在CtClass
		// object被冻结时，会释放存储在ClassPool对应的数据。这样做可以减少javassist的内存消耗。默认情况ClassPool.doPruning=false。
		declaring.stopPruning(true);

		return declaring;
	}
	
    /**
     * Creates a default no-argument constructor for the supplied class.
     *
     * @param declaring the class to add the constructor to.
     * @return the created {@link CtConstructor}.
     * @throws CannotCompileException if the constructor cannot be
     *                                compiled.
     */
	public static CtConstructor defaultConstructor(final CtClass declaring) throws CannotCompileException   {
		CtConstructor cons = new CtConstructor(null, declaring);
		cons.setBody("{}");
    	return cons;
	}

    /**
     * Creates a constructor that accepts an {@link InvocationHandler}
     * and delegates to {@code super(handler)}.
     *
     * @param pool      the class pool.
     * @param declaring the class to add the constructor to.
     * @return the created {@link CtConstructor}.
     * @throws NotFoundException      if {@code InvocationHandler}
     *                                cannot be resolved.
     * @throws CannotCompileException if the constructor cannot be
     *                                compiled.
     */
	public static CtConstructor makeConstructor(final ClassPool pool, final CtClass declaring) throws NotFoundException, CannotCompileException  {
    	CtClass[] parameters = new CtClass[] {pool.get(InvocationHandler.class.getName())};
    	CtClass[] exceptions = new CtClass[] { pool.get("java.lang.Exception") };
    	return CtNewConstructor.make(parameters, exceptions, "{super($1);}", declaring);
	}

    /**
     * Creates or retrieves an interface in the supplied pool. If the
     * interface already exists the existing instance is returned.
     *
     * @param pool      the class pool.
     * @param classname fully qualified name of the interface.
     * @return the created or retrieved {@link CtClass}.
     * @throws NotFoundException      if a referenced type cannot be
     *                                resolved.
     * @throws CannotCompileException if the interface cannot be
     *                                compiled.
     */
	public static CtClass makeInterface(final ClassPool pool, final String classname)
			throws NotFoundException, CannotCompileException {

		CtClass declaring = pool.getOrNull(classname);
		if (null == declaring) {
			declaring = pool.makeInterface(classname);
		}

		// 当 ClassPool.doPruning=true的时候，Javassist 在CtClass
		// object被冻结时，会释放存储在ClassPool对应的数据。这样做可以减少javassist的内存消耗。默认情况ClassPool.doPruning=false。
		declaring.stopPruning(true);

		return declaring;
	}
	

    /**
     * Sets the superclass of the supplied class.
     *
     * @param pool      the class pool.
     * @param declaring the class whose superclass to set.
     * @param clazz     the Java class to use as superclass.
     * @param <T>       the type of the superclass.
     * @throws Exception if the superclass cannot be resolved or set.
     */
	public static <T> void setSuperclass(final ClassPool pool, final CtClass declaring, final Class<T> clazz)
			throws Exception {
		CtClass superclass = pool.get(clazz.getName());
		declaring.setSuperclass(superclass);
	}

    /**
     * Converts an array of {@link SoapParam} descriptors into an array
     * of {@link CtClass} parameter types.
     *
     * @param pool   the class pool.
     * @param params the parameter descriptors; may be {@code null} or
     *               empty.
     * @return the resolved parameter types, or {@code null} when no
     *         parameters are supplied.
     * @throws NotFoundException if a parameter type cannot be resolved.
     */
	public static CtClass[] makeParams(final ClassPool pool, SoapParam<?>... params) throws NotFoundException {
		if(params == null || params.length == 0) {
			return null;
		}
		CtClass[] parameters = new CtClass[params.length];
		for(int i = 0;i < params.length; i++) {
			parameters[i] = pool.get(params[i].getType().getName());
		}
		return parameters;
	}
	

    /**
     * Builds a {@code @WebServiceProvider} annotation.
     *
     * @param constPool       the constant pool.
     * @param wsdlLocation    URL of the WSDL document.
     * @param serviceName     the WSDL service name.
     * @param targetNamespace the XML namespace for the service.
     * @param portName        the WSDL port name.
     * @return the constructed annotation.
     */
	public static Annotation annotWebServiceProvider(final ConstPool constPool, String wsdlLocation,
			String serviceName, String targetNamespace, String portName) {

		wsdlLocation = StringUtils.isNotBlank(wsdlLocation) ? wsdlLocation : "";
		serviceName = StringUtils.isNotBlank(serviceName) ? serviceName : "";
		targetNamespace = StringUtils.isNotBlank(targetNamespace) ? targetNamespace : "";
		portName = StringUtils.isNotBlank(portName) ? portName : "";

		return CtAnnotationBuilder.create(WebServiceProvider.class, constPool)
				.addStringMember("wsdlLocation", wsdlLocation).addStringMember("serviceName", serviceName)
				.addStringMember("targetNamespace", targetNamespace).addStringMember("portName", portName).build();

	}
	
    /**
     * Builds a {@code @WebService} annotation from a SOAP service
     * descriptor.
     *
     * @param constPool the constant pool.
     * @param service   the service descriptor.
     * @return the constructed annotation.
     */
	public static Annotation annotWebService(final ConstPool constPool, final SoapService service) {

		CtAnnotationBuilder builder = CtAnnotationBuilder.create(WebService.class, constPool)
				.addStringMember("name", service.getName())
				.addStringMember("targetNamespace", service.getTargetNamespace());

		if (StringUtils.isNotBlank(service.getServiceName())) {
			builder.addStringMember("serviceName", service.getServiceName());
		}
		if (StringUtils.isNotBlank(service.getPortName())) {
			builder.addStringMember("portName", service.getPortName());
		}
		if (StringUtils.isNotBlank(service.getWsdlLocation())) {
			builder.addStringMember("wsdlLocation", service.getWsdlLocation());
		}
		if (StringUtils.isNotBlank(service.getEndpointInterface())) {
			builder.addStringMember("endpointInterface", service.getEndpointInterface());
		}
		
		return builder.build();

	}
	
    /**
     * Builds an {@code @Addressing} annotation.
     *
     * @param constPool the constant pool.
     * @param enabled   whether WS-Addressing is enabled.
     * @param required  whether WS-Addressing is required.
     * @param responses the addressing responses policy.
     * @return the constructed annotation.
     */
	public static Annotation annotAddressing(final ConstPool constPool, final boolean enabled, final boolean required,
			final Responses responses) {
		
		return CtAnnotationBuilder.create(Addressing.class, constPool)
				.addBooleanMember("enabled", enabled)
				.addBooleanMember("required", required)
				.addEnumMember("responses", responses).build();

	}

    /**
     * Builds a {@code @ServiceMode} annotation.
     *
     * @param constPool the constant pool.
     * @param mode      the service mode ({@code PAYLOAD} or
     *                  {@code MESSAGE}).
     * @return the constructed annotation.
     */
	public static Annotation annotServiceMode(final ConstPool constPool, final Service.Mode mode) {
		return CtAnnotationBuilder.create(ServiceMode.class, constPool).addEnumMember("value", mode).build();
	}
	
    /**
     * Builds a {@code @HandlerChain} annotation.
     *
     * @param constPool the constant pool.
     * @param name      the handler chain name; may be {@code null}.
     * @param file      the handler chain file path; may be
     *                  {@code null}.
     * @return the constructed annotation.
     */
	public static Annotation annotHandlerChain(final ConstPool constPool, String name, String file) {

		CtAnnotationBuilder builder = CtAnnotationBuilder.create(HandlerChain.class, constPool);
		if (StringUtils.isNotBlank(name)) {
			builder.addStringMember("name", name );
		}
		if (StringUtils.isNotBlank(file)) {
			builder.addStringMember("file", file);
		}		
		return builder.build();
		
	}
	
	
    /**
     * Attaches all JAX-WS method-level and parameter-level annotations
     * to the supplied method: {@code @WebMethod}, {@code @WebResult},
     * {@code @WebBound}, and {@code @WebParam}.
     *
     * @param ctMethod   the target method.
     * @param constPool  the constant pool.
     * @param result     the result descriptor, may be {@code null}.
     * @param method     the SOAP method descriptor.
     * @param bound      the binding descriptor, may be {@code null}.
     * @param params     the parameter descriptors; may be
     *                   {@code null} or empty.
     * @param <T>        the return type parameter.
     */
	public static <T> void methodAnnotations(final CtMethod ctMethod, final ConstPool constPool, final SoapResult<T> result, final SoapMethod method, final SoapBound bound, SoapParam<?>... params) {
		
		// 添加方法注解
		AnnotationsAttribute methodAttr = JavassistUtils.getAnnotationsAttribute(ctMethod);
		
        // 添加 @WebBound 注解
        if (bound != null) {
	        methodAttr.addAnnotation(JaxwsEndpointApiUtils.annotWebBound(constPool, bound));
        }
        
        // 添加 @WebMethod 注解	        
        methodAttr.addAnnotation(JaxwsEndpointApiUtils.annotWebMethod(constPool, method));
        
        // 添加 @WebResult 注解
        if (null != result ) {
	        methodAttr.addAnnotation(JaxwsEndpointApiUtils.annotWebResult(constPool, result));
        }
        
        ctMethod.getMethodInfo().addAttribute(methodAttr);
        
        // 添加 @WebParam 参数注解
        if(params != null && params.length > 0) {
        	
        	ParameterAnnotationsAttribute parameterAtrribute = JavassistUtils.getParameterAnnotationsAttribute(ctMethod);
            Annotation[][] paramArrays = JaxwsEndpointApiUtils.annotParams(constPool, params);
            parameterAtrribute.setAnnotations(paramArrays);
            ctMethod.getMethodInfo().addAttribute(parameterAtrribute);
            
        }
        
	}
	
    /**
     * Generates and sets the method body that dispatches calls through
     * the configured {@link InvocationHandler}.
     *
     * @param ctMethod the target method.
     * @param method   the SOAP method descriptor.
     * @throws CannotCompileException if the body cannot be compiled.
     */
	public static void methodBody(final CtMethod ctMethod, final SoapMethod method) throws CannotCompileException {
		
		// 构造方法体
		StringBuilder body = new StringBuilder(); 
        body.append("{\n");
        	body.append("if(getHandler() != null){\n");
        		body.append("Method method = this.getClass().getDeclaredMethod(\"" + method.getOperationName() + "\", $sig);");
        		body.append("return ($r)getHandler().invoke($0, method, $args);");
        	body.append("}\n"); 
	        body.append("return null;\n");
        body.append("}"); 
        // 将方法的内容设置为要写入的代码，当方法被 abstract修饰时，该修饰符被移除。
        ctMethod.setBody(body.toString());
        
	}
	
    /**
     * Adds a catch block that prints and re-throws any
     * {@code Exception} thrown by the method body.
     *
     * @param pool     the class pool.
     * @param ctMethod the target method.
     * @throws NotFoundException      if {@code Exception} cannot be
     *                                resolved.
     * @throws CannotCompileException if the catch block cannot be
     *                                compiled.
     */
	public static void methodCatch(final ClassPool pool, final CtMethod ctMethod) throws NotFoundException, CannotCompileException {
		
		// 构造异常处理逻辑
        CtClass etype = pool.get("java.lang.Exception");
        ctMethod.addCatch("{ System.out.println($e); throw $e; }", etype);
        
	}
	
    /**
     * Builds a {@code @WebBound} annotation from a JAX-WS bound
     * descriptor.
     *
     * @param constPool the constant pool.
     * @param bound     the bound descriptor.
     * @return the constructed annotation.
     */
	public static Annotation annotWebBound(final ConstPool constPool, final SoapBound bound) {

		CtAnnotationBuilder builder = CtAnnotationBuilder.create(WebBound.class, constPool).
			addStringMember("uid", bound.getUid());
		if (StringUtils.isNotBlank(bound.getJson())) {
			builder.addStringMember("json", bound.getJson());
        }
		return builder.build();
		
	}
	
    /**
     * Builds a {@code @WebMethod} annotation from a SOAP method
     * descriptor.
     *
     * @param constPool the constant pool.
     * @param method    the SOAP method descriptor.
     * @return the constructed annotation.
     */
	public static Annotation annotWebMethod(final ConstPool constPool, final SoapMethod method) {
		
		CtAnnotationBuilder builder = CtAnnotationBuilder.create(WebMethod.class, constPool)
				.addStringMember("operationName", method.getOperationName());
		if (StringUtils.isNotBlank(method.getAction())) {
			builder.addStringMember("action", method.getAction());
		}
		builder.addBooleanMember("exclude", method.isExclude());
		return builder.build();
		
	}
	
    /**
     * Builds {@code @WebParam} parameter-level annotations for each
     * {@link SoapParam} descriptor.
     *
     * @param constPool the constant pool.
     * @param params    the parameter descriptors; may be {@code null}
     *                  or empty.
     * @return a two-dimensional annotation array suitable for
     *         {@link ParameterAnnotationsAttribute#setAnnotations(Annotation[][])},
     *         or {@code null} when no parameters are supplied.
     */
	public static Annotation[][] annotParams(final ConstPool constPool, SoapParam<?>... params) {

		// 添加 @WebParam 参数注解
		if (params != null && params.length > 0) {

			// 参数模式定义
			// Map<String, EnumMemberValue> modeMap = modeMap(constPool, params);
			
			Annotation[][] paramArrays = new Annotation[params.length][1];
			
			for (int i = 0; i < params.length; i++) {
				
				CtAnnotationBuilder builder = CtAnnotationBuilder.create(WebParam.class, constPool)
						.addStringMember("name", params[i].getName())
						.addStringMember("targetNamespace", params[i].getTargetNamespace())
						.addEnumMember("mode", params[i].getMode())
						.addBooleanMember("header", params[i].isHeader());
				if (StringUtils.isNotBlank(params[i].getPartName())) {
					builder.addStringMember("partName", params[i].getPartName());
				}
				paramArrays[i][0] = builder.build();
				
				/*
				
				Annotation paramAnnot = new Annotation(WebParam.class.getName(), constPool);
				paramAnnot.addMemberValue("name", new StringMemberValue(params[i].getName(), constPool));
				if (StringUtils.isNotBlank(params[i].getPartName())) {
					paramAnnot.addMemberValue("partName", new StringMemberValue(params[i].getPartName(), constPool));
				}
				paramAnnot.addMemberValue("targetNamespace",
						new StringMemberValue(params[i].getTargetNamespace(), constPool));
				paramAnnot.addMemberValue("mode", modeMap.get(params[i].getMode().name()));
				if (params[i].isHeader()) {
					paramAnnot.addMemberValue("header", new BooleanMemberValue(true, constPool));
				}
				paramArrays[i][0] = paramAnnot;*/

			}

			return paramArrays;

		}
		return null;
	}
	
    /**
     * Builds a {@code @WebResult} annotation from a SOAP result
     * descriptor.
     *
     * @param constPool the constant pool.
     * @param result    the result descriptor.
     * @param <T>       the return type parameter.
     * @return the constructed annotation.
     */
	public static <T> Annotation annotWebResult(final ConstPool constPool, final SoapResult<T> result) {
		
		CtAnnotationBuilder builder = CtAnnotationBuilder.create(WebResult.class, constPool)
				.addStringMember("name", StringUtils.isNotBlank(result.getName()) ? result.getName() : "")
				.addBooleanMember("header", result.isHeader());
		if (StringUtils.isNotBlank(result.getPartName())) {
			builder.addStringMember("partName", result.getPartName());
		}
		 if (StringUtils.isNotBlank(result.getTargetNamespace())) {
			 builder.addStringMember("targetNamespace", result.getTargetNamespace());
        }
		return builder.build();
		
	}
	
	
    /**
     * Placeholder for future cleanup logic.
     *
     * @param declaring the class to clean up.
     */
	public static void rm(CtClass declaring) {

	}

}

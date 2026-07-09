/*
 * Copyright (c) 2018, hiwepy (https://github.com/hiwepy).
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

import io.github.hiwepy.javassist.bytecode.CtAnnotationBuilder;
import io.github.hiwepy.javassist.utils.JavassistUtils;

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

public class JaxwsEndpointApiUtils {
	
	protected static final Logger LOG = LoggerFactory.getLogger(JaxwsEndpointApiUtils.class);

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
	
	public static CtConstructor defaultConstructor(final CtClass declaring) throws CannotCompileException   {
		// 默认添加无参构造器  
		CtConstructor cons = new CtConstructor(null, declaring);  
		cons.setBody("{}");  
    	return cons;
	}
	
	public static CtConstructor makeConstructor(final ClassPool pool, final CtClass declaring) throws NotFoundException, CannotCompileException  {

		// 添加有参构造器，注入回调接口
    	CtClass[] parameters = new CtClass[] {pool.get(InvocationHandler.class.getName())};
    	CtClass[] exceptions = new CtClass[] { pool.get("java.lang.Exception") };
    	return CtNewConstructor.make(parameters, exceptions, "{super($1);}", declaring);
    	
	}

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
	

	public static <T> void setSuperclass(final ClassPool pool, final CtClass declaring, final Class<T> clazz)
			throws Exception {

		/* 获得 JaxwsHandler 类作为动态类的父类 */
		CtClass superclass = pool.get(clazz.getName());
		declaring.setSuperclass(superclass);

	}

	public static CtClass[] makeParams(final ClassPool pool, SoapParam<?>... params) throws NotFoundException {
		// 无参
		if(params == null || params.length == 0) {
			return null;
		}
		// 方法参数
		CtClass[] parameters = new CtClass[params.length];
		for(int i = 0;i < params.length; i++) {
			parameters[i] = pool.get(params[i].getType().getName());
		}

		return parameters;
	}
	

	/**
	 * 构造 @WebServiceProvider 注解
	 * @param constPool			: {@link ConstPool} instance
	 * @param wsdlLocation		：Location of the WSDL description for the service.
	 * @param serviceName		：Service name.
	 * @param targetNamespace	：Target namespace for the service
	 * @param portName			：Port name.
	 * @return {@link Annotation} instance
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
	 * 构造 @WebService 注解
	 * @param constPool			: {@link ConstPool} instance
	 * @param service			: {@link SoapService} instance
	 * @return {@link Annotation} instance
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
	 * 构造 @Addressing 注解
	 * @param constPool			: {@link ConstPool} instance
	 * @param enabled			: the value of enabled
	 * @param required			: the value of required
	 * @param responses			: {@link Responses} instance
	 * @return {@link Annotation} instance
	 */
	public static Annotation annotAddressing(final ConstPool constPool, final boolean enabled, final boolean required,
			final Responses responses) {
		
		return CtAnnotationBuilder.create(Addressing.class, constPool)
				.addBooleanMember("enabled", enabled)
				.addBooleanMember("required", required)
				.addEnumMember("responses", responses).build();

	}

	/**
	 * 构造 @ServiceMode 注解
	 * @param constPool			: {@link ConstPool} instance
	 * @param mode				: the mode of {@link Service}
	 * @return {@link Annotation} instance
	 */
	public static Annotation annotServiceMode(final ConstPool constPool, final Service.Mode mode) {
		return CtAnnotationBuilder.create(ServiceMode.class, constPool).addEnumMember("value", mode).build();
	}
	
	/**
	 * 构造 @HandlerChain 注解
	 * @param constPool			: {@link ConstPool} instance
	 * @param name				: the value of name
	 * @param file				: the value of file
	 * @return {@link Annotation} instance
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
	 * 
	 * 为方法添加 @WebMethod、 @WebResult、@WebBound、@WebParam 注解
	 * @author 		： <a href="https://github.com/hiwepy">hiwepy</a>
	 * @param ctMethod			: {@link CtMethod} instance
	 * @param constPool			: {@link ConstPool} instance
	 * @param result			: {@link SoapResult} instance
	 * @param method			: {@link SoapMethod} instance
	 * @param bound				: {@link SoapBound} instance
	 * @param <T>				: 泛型参数
	 * @param params			: The {@link SoapParam} params
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
	 * 设置方法体
	 * @param ctMethod			: {@link CtMethod} instance
	 * @param method			: {@link SoapMethod} instance
	 * @throws CannotCompileException if can't compile 
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
	 * 设置方法异常捕获逻辑
	 * @param pool				: {@link ClassPool} instance
	 * @param ctMethod			: {@link CtMethod} instance
	 * @throws NotFoundException  if not found
	 * @throws CannotCompileException if can't compile
	 */
	public static void methodCatch(final ClassPool pool, final CtMethod ctMethod) throws NotFoundException, CannotCompileException {
		
		// 构造异常处理逻辑
        CtClass etype = pool.get("java.lang.Exception");
        ctMethod.addCatch("{ System.out.println($e); throw $e; }", etype);
        
	}
	
	/**
	 * 构造 @WebBound 注解
	 * @param constPool			: {@link ConstPool} instance
	 * @param bound				: {@link SoapBound} instance
	 * @return {@link Annotation} instance
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
	 * 构造 @WebMethod 注解
	 * @param constPool			: {@link ConstPool} instance
	 * @param method			: {@link SoapMethod} instance
	 * @return {@link Annotation} instance
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
	 * 构造 @WebParam 参数注解
	 * @param constPool			: {@link ConstPool} instance
	 * @param params			: The {@link SoapParam} params
	 * @return {@link Annotation} instance
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
	 * 构造 @WebResult 注解
	 * @param constPool			: {@link ConstPool} instance
	 * @param result			: {@link SoapResult} instance
	 * @param <T>				: 泛型参数
	 * @return {@link Annotation} instance
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
	
	
	public static void rm(CtClass declaring) {

	}

}

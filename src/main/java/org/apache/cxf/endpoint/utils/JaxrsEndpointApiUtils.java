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

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.endpoint.annotation.WebBound;
import org.apache.cxf.endpoint.jaxrs.definition.HttpParamEnum;
import org.apache.cxf.endpoint.jaxrs.definition.RestBound;
import org.apache.cxf.endpoint.jaxrs.definition.RestMethod;
import org.apache.cxf.endpoint.jaxrs.definition.RestParam;

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
import javassist.bytecode.annotation.StringMemberValue;

public class JaxrsEndpointApiUtils {

	public static CtClass makeClass(final ClassPool pool, final String classname)
			throws NotFoundException, CannotCompileException {

		CtClass declaring = pool.getOrNull(classname);
		if (null == declaring) {
			declaring = pool.makeClass(classname);
		}
		
		/*
		 * 当 ClassPool.doPruning=true的时候，Javassist 在CtClass object被冻结时，会释放存储在ClassPool对应的数据。
		 * 这样做可以减少javassist的内存消耗。默认情况ClassPool.doPruning=false。
		 */
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
	
	public static CtClass[] makeParams(final ClassPool pool, RestParam<?>... params) throws NotFoundException {
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
	 * 构造  @Path 注解
	 * @param constPool {@link ConstPool} instance
	 * @param path the path
	 * @return {@link Annotation} instance
	 */
	public static Annotation annotPath(final ConstPool constPool, String path) {
		return CtAnnotationBuilder.create(Path.class, constPool).addStringMember("value", path).build();
	}
	
	/**
	 * 构造  @Produces 注解
	 * @param constPool {@link ConstPool} instance
	 * @param mediaTypes the media types
	 * @return {@link Annotation} instance
	 */
	public static Annotation annotProduces(final ConstPool constPool, String... mediaTypes) {
		
		// 参数预处理
		mediaTypes = ArrayUtils.isEmpty(mediaTypes) ? new String[] {"*/*"} : mediaTypes;
		CtAnnotationBuilder builder = CtAnnotationBuilder.create(Produces.class, constPool).
				addStringMember("value", mediaTypes);
		return builder.build();
		 
	}
	
	/**
	 * 为方法添加 @HttpMethod、 @GET、 @POST、 @PUT、 @DELETE、 @PATCH、 @HEAD、 @OPTIONS、@Path、、@Consumes、@Produces、@RestBound、@RestParam 注解
	 * @author 		： <a href="https://github.com/hiwepy">hiwepy</a>
	 * @param ctMethod {@link CtMethod} instance
	 * @param constPool {@link ConstPool} instance 
	 * @param method {@link RestMethod} instance 
	 * @param bound {@link RestBound} instance
	 * @param params the params
	 * @see HttpMethod
	 * @see GET
	 * @see POST
	 * @see PUT
	 * @see DELETE
	 * @see PATCH
	 * @see HEAD
	 * @see OPTIONS
	 */
	public static void methodAnnotations(final CtMethod ctMethod, final ConstPool constPool, final RestMethod method, final RestBound bound, RestParam<?>... params) {
		
		// 添加方法注解
		AnnotationsAttribute methodAttr = JavassistUtils.getAnnotationsAttribute(ctMethod);
       
        // 添加 @WebBound 注解
        if (bound != null) {
        	methodAttr.addAnnotation(JaxrsEndpointApiUtils.annotWebBound(constPool, bound));
        }
        
        // 添加 @GET、 @POST、 @PUT、 @DELETE、 @PATCH、 @HEAD、 @OPTIONS  注解
        methodAttr.addAnnotation(JaxrsEndpointApiUtils.annotHttpMethod(constPool, method));
        
        // 添加 @Path 注解	        
        methodAttr.addAnnotation(JaxrsEndpointApiUtils.annotPath(constPool, method.getPath()));
        
        // 添加 @Consumes 注解	
        if (ArrayUtils.isNotEmpty(method.getConsumes())) {
	        methodAttr.addAnnotation(JaxrsEndpointApiUtils.annotConsumes(constPool, method.getConsumes()));
        }
        
        // 添加 @Produces 注解
        if (ArrayUtils.isNotEmpty(method.getMediaTypes())) {
        	methodAttr.addAnnotation(JaxrsEndpointApiUtils.annotProduces(constPool, method.getMediaTypes()));
 		}
     		
        ctMethod.getMethodInfo().addAttribute(methodAttr);
        
        // 添加 @WebParam 参数注解
        if(params != null && params.length > 0) {
        	
        	ParameterAnnotationsAttribute parameterAtrribute = JavassistUtils.getParameterAnnotationsAttribute(ctMethod);
            Annotation[][] paramArrays = JaxrsEndpointApiUtils.annotParams(constPool, params);
            parameterAtrribute.setAnnotations(paramArrays);
            ctMethod.getMethodInfo().addAttribute(parameterAtrribute);
            
        }
        
	}
	
	/**
	 * 设置方法体
	 * @param ctMethod {@link CtMethod} instance
	 * @param method {@link RestMethod} instance 
	 * @throws CannotCompileException  if can't compile
	 */
	public static void methodBody(final CtMethod ctMethod, final RestMethod method) throws CannotCompileException {
		
		// 构造方法体
		StringBuilder body = new StringBuilder(); 
        body.append("{\n");
        	body.append("if(getHandler() != null){\n");
        		body.append("Method method = this.getClass().getDeclaredMethod(\"" + method.getName() + "\", $sig);");
        		body.append("return ($r)getHandler().invoke($0, method, $args);");
        	body.append("}\n"); 
	        body.append("return null;\n");
        body.append("}"); 
        // 将方法的内容设置为要写入的代码，当方法被 abstract修饰时，该修饰符被移除。
        ctMethod.setBody(body.toString());
        
	}
	
	/**
	 * 设置方法异常捕获逻辑
	 * @param pool {@link ClassPool} instance 
	 * @param ctMethod {@link CtMethod} instance
	 * @throws NotFoundException  if not found
	 * @throws CannotCompileException   if can't compile
	 */
	public static void methodCatch(final ClassPool pool, final CtMethod ctMethod) throws NotFoundException, CannotCompileException {
		
		// 构造异常处理逻辑
        CtClass etype = pool.get("java.lang.Exception");
        ctMethod.addCatch("{ System.out.println($e); throw $e; }", etype);
        
	}
	
	/**
	 * 构造 @WebBound 注解
	 * @param constPool {@link ConstPool} instance 
	 * @param bound {@link RestBound} instance
	 * @return {@link Annotation} instance
	 */
	public static Annotation annotWebBound(final ConstPool constPool, final RestBound bound) {
		
		CtAnnotationBuilder builder = CtAnnotationBuilder.create(WebBound.class, constPool).
			addStringMember("uid", bound.getUid());
		if (StringUtils.isNotBlank(bound.getJson())) {
			builder.addStringMember("json", bound.getJson());
        }
		return builder.build();
		
	}
	
	/**
	 * 根据参数 构造   @GET、 @POST、 @PUT、 @DELETE、 @PATCH、 @HEAD、 @OPTIONS 注解
	 * @param constPool {@link ConstPool} instance 
	 * @param method {@link RestMethod} instance
	 * @return {@link Annotation} instance
	 */
	public static Annotation annotHttpMethod(final ConstPool constPool, final RestMethod method) {
		
		Annotation annot = null;
		switch (method.getMethod()) {
			case GET:{
				annot = new Annotation(GET.class.getName(), constPool);
			};break;
			case POST:{
				annot = new Annotation(POST.class.getName(), constPool);
			};break;
			case PUT:{
				annot = new Annotation(PUT.class.getName(), constPool);
			};break;
			case DELETE:{
				annot = new Annotation(DELETE.class.getName(), constPool);
			};break;
			case PATCH:{
				annot = new Annotation(PATCH.class.getName(), constPool);
			};break;
			case HEAD:{
				annot = new Annotation(HEAD.class.getName(), constPool);
			};break;
			case OPTIONS:{
				annot = new Annotation(OPTIONS.class.getName(), constPool);
			};break;
			default:{
				annot = new Annotation(GET.class.getName(), constPool);
			};break;
		}
		
		return annot;
	}
	
	/**
	 * 构造 @Consumes 注解
	 * @param constPool {@link ConstPool} instance 
	 * @param consumes the consumes
	 * @return {@link Annotation} instance
	 */
	public static Annotation annotConsumes(final ConstPool constPool, String... consumes) {
		// 参数预处理
		consumes = ArrayUtils.isEmpty(consumes) ? new String[] {"*/*"} : consumes;
		CtAnnotationBuilder builder = CtAnnotationBuilder.create(Consumes.class, constPool).
				addStringMember("value", consumes);
		return builder.build();
	}
	
	/**
	 * 构造 @BeanParam 、@CookieParam、@FormParam、@HeaderParam、@MatrixParam、@PathParam、@QueryParam 参数注解
	 * @param constPool {@link ConstPool} instance 
	 * @param params the params
	 * @return {@link Annotation} Array
	 */
	public static Annotation[][] annotParams(final ConstPool constPool, RestParam<?>... params) {

		// 添加 @WebParam 参数注解
		if (params != null && params.length > 0) {

			Annotation[][] paramArrays = new Annotation[params.length][1];
			
			Annotation paramAnnot = null;
			for (int i = 0; i < params.length; i++) {
				
				switch (params[i].getFrom()) {
					case BEAN:{
						paramAnnot = new Annotation(BeanParam.class.getName(), constPool);
					};break;
					case COOKIE:{
						paramAnnot = new Annotation(CookieParam.class.getName(), constPool);
					};break;
					case FORM:{
						paramAnnot = new Annotation(FormParam.class.getName(), constPool);
					};break;
					case HEADER:{
						paramAnnot = new Annotation(HeaderParam.class.getName(), constPool);
					};break;
					case MATRIX:{
						paramAnnot = new Annotation(MatrixParam.class.getName(), constPool);
					};break;
					case PATH:{
						paramAnnot = new Annotation(PathParam.class.getName(), constPool);
					};break;
					case QUERY:{
						paramAnnot = new Annotation(QueryParam.class.getName(), constPool);
					};break;
					default:{
						paramAnnot = new Annotation(QueryParam.class.getName(), constPool);
					};break;
				}
				if(HttpParamEnum.BEAN.compareTo(params[i].getFrom()) != 0){
					paramAnnot.addMemberValue("value", new StringMemberValue(params[i].getName(), constPool));
				}
				
				// 有默认值
				if(StringUtils.isNotBlank(params[i].getDef())) {
					
					paramArrays[i] = new Annotation[2];
					paramArrays[i][0] = paramAnnot;
					
					Annotation defAnnot = new Annotation(DefaultValue.class.getName(), constPool);
					defAnnot.addMemberValue("value", new StringMemberValue(params[i].getDef(), constPool));
					paramArrays[i][1] = paramAnnot;
					
				} else {
					paramArrays[i][0] = paramAnnot;
				}
				
			}
			
			return paramArrays;

		}
		return null;
	}
	
}

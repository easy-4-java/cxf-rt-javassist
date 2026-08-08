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
import javassist.bytecode.annotation.StringMemberValue;

/**
 * Utility methods used by the JAX-RS endpoint builders to create
 * Javassist classes, interfaces, constructors, methods, and
 * annotations.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see org.apache.cxf.endpoint.jaxrs.JaxrsEndpointApiCtClassBuilder
 */
public class JaxrsEndpointApiUtils {

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
		
		/*
		 * 当 ClassPool.doPruning=true的时候，Javassist 在CtClass object被冻结时，会释放存储在ClassPool对应的数据。
		 * 这样做可以减少javassist的内存消耗。默认情况ClassPool.doPruning=false。
		 */
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
     * Converts an array of {@link RestParam} descriptors into an array
     * of {@link CtClass} parameter types.
     *
     * @param pool   the class pool.
     * @param params the parameter descriptors; may be {@code null} or
     *               empty.
     * @return the resolved parameter types, or {@code null} when no
     *         parameters are supplied.
     * @throws NotFoundException if a parameter type cannot be resolved.
     */
	public static CtClass[] makeParams(final ClassPool pool, RestParam<?>... params) throws NotFoundException {
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
     * Builds a {@code @Path} annotation.
     *
     * @param constPool the constant pool.
     * @param path      the URI template value.
     * @return the constructed annotation.
     */
	public static Annotation annotPath(final ConstPool constPool, String path) {
		return CtAnnotationBuilder.create(Path.class, constPool).addStringMember("value", path).build();
	}

    /**
     * Builds a {@code @Produces} annotation. When no media types are
     * supplied the default {@code *&#47;*} value is used.
     *
     * @param constPool  the constant pool.
     * @param mediaTypes the produced media types.
     * @return the constructed annotation.
     */
	public static Annotation annotProduces(final ConstPool constPool, String... mediaTypes) {
		
		// 参数预处理
		mediaTypes = ArrayUtils.isEmpty(mediaTypes) ? new String[] {"*/*"} : mediaTypes;
		CtAnnotationBuilder builder = CtAnnotationBuilder.create(Produces.class, constPool).
				addStringMember("value", mediaTypes);
		return builder.build();
		 
	}
	
    /**
     * Attaches all JAX-RS method-level and parameter-level annotations
     * to the supplied method: {@code @HttpMethod}, {@code @Path},
     * {@code @Consumes}, {@code @Produces}, {@code @WebBound}, and
     * parameter annotations ({@code @QueryParam}, {@code @PathParam},
     * etc.).
     *
     * @param ctMethod   the target method.
     * @param constPool  the constant pool.
     * @param method     the REST method descriptor.
     * @param bound      the binding descriptor, may be {@code null}.
     * @param params     the parameter descriptors; may be
     *                   {@code null} or empty.
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
     * Generates and sets the method body that dispatches calls through
     * the configured {@link InvocationHandler}.
     *
     * @param ctMethod the target method.
     * @param method   the REST method descriptor.
     * @throws CannotCompileException if the body cannot be compiled.
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
     * Adds a catch block that prints and re-throws any
     * {@code Exception} thrown by the method body.
     *
     * @param pool    the class pool.
     * @param ctMethod the target method.
     * @throws NotFoundException      if {@code Exception} cannot be
     *                                resolved.
     * @throws CannotCompileException if the catch block cannot be
     *                                compiled.
     */
	public static void methodCatch(final ClassPool pool, final CtMethod ctMethod) throws NotFoundException, CannotCompileException {
        CtClass etype = pool.get("java.lang.Exception");
        ctMethod.addCatch("{ System.out.println($e); throw $e; }", etype);
	}

    /**
     * Builds a {@code @WebBound} annotation from a JAX-RS bound
     * descriptor.
     *
     * @param constPool the constant pool.
     * @param bound     the bound descriptor.
     * @return the constructed annotation.
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
     * Builds the appropriate HTTP method annotation ({@code @GET},
     * {@code @POST}, {@code @PUT}, {@code @DELETE}, {@code @PATCH},
     * {@code @HEAD}, {@code @OPTIONS}) based on the verb carried by
     * the descriptor.
     *
     * @param constPool the constant pool.
     * @param method    the REST method descriptor.
     * @return the constructed annotation.
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
     * Builds a {@code @Consumes} annotation. When no media types are
     * supplied the default {@code *&#47;*} value is used.
     *
     * @param constPool the constant pool.
     * @param consumes  the consumed media types.
     * @return the constructed annotation.
     */
	public static Annotation annotConsumes(final ConstPool constPool, String... consumes) {
		// 参数预处理
		consumes = ArrayUtils.isEmpty(consumes) ? new String[] {"*/*"} : consumes;
		CtAnnotationBuilder builder = CtAnnotationBuilder.create(Consumes.class, constPool).
				addStringMember("value", consumes);
		return builder.build();
	}
	
    /**
     * Builds parameter-level annotations for each {@link RestParam}
     * descriptor. The annotation type is determined by the parameter's
     * {@link HttpParamEnum} binding source. When a default value is
     * configured, a {@code @DefaultValue} annotation is appended.
     *
     * @param constPool the constant pool.
     * @param params    the parameter descriptors; may be {@code null}
     *                  or empty.
     * @return a two-dimensional annotation array suitable for
     *         {@link ParameterAnnotationsAttribute#setAnnotations(Annotation[][])},
     *         or {@code null} when no parameters are supplied.
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

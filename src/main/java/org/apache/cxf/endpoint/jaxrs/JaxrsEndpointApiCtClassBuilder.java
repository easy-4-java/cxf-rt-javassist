package org.apache.cxf.endpoint.jaxrs;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.Builder;
import org.apache.cxf.endpoint.EndpointApi;
import org.apache.cxf.endpoint.jaxrs.definition.HttpMethodEnum;
import org.apache.cxf.endpoint.jaxrs.definition.RestBound;
import org.apache.cxf.endpoint.jaxrs.definition.RestMethod;
import org.apache.cxf.endpoint.jaxrs.definition.RestParam;
import org.apache.cxf.endpoint.utils.JaxrsEndpointApiUtils;

import io.github.easy4j.javassist.bytecode.CtFieldBuilder;
import io.github.easy4j.javassist.utils.ClassPoolFactory;
import io.github.easy4j.javassist.utils.JavassistUtils;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.NotFoundException;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;

/**
 * Builder that creates a concrete JAX-RS endpoint class extending
 * {@link EndpointApi} on top of a {@link ClassPool}.
 *
 * <p>The builder wires the standard JAX-RS metadata ({@code @Path},
 * {@code @Produces}, {@code @WebBound}) onto the generated class and
 * exposes a fluent API to add annotated methods, fields, and
 * constructors. Each {@code new*} / {@code add*} method mutates the
 * underlying {@link CtClass} in-place and returns {@code this}, so
 * calls can be chained. The final class can be obtained as a
 * {@link CtClass} through {@link #build()}, as a {@link Class} through
 * {@link #toClass()}, or as an already-instantiated proxy through
 * {@link #toInstance(InvocationHandler)}.</p>
 *
 * <p>This is the JAX-RS counterpart of
 * {@link org.apache.cxf.endpoint.jaxws.JaxwsEndpointApiCtClassBuilder}.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see JaxrsEndpointApiUtils
 * @see JaxrsEndpointApiInterfaceCtClassBuilder
 * @see JaxrsEndpointApiImplCtClassBuilder
 */
public class JaxrsEndpointApiCtClassBuilder implements Builder<CtClass> {
	
	/**
	 * Class pool used to resolve types and define the generated
	 * endpoint class. Configured by the constructors.
	 */
	protected ClassPool pool = null;
	/**
	 * {@link CtClass} representing the generated endpoint. Mutated in
	 * place by every fluent setter on this builder.
	 */
	protected CtClass declaring  = null;
	/**
	 * {@link ClassFile} view of {@link #declaring}; cached so annotation
	 * writes do not have to query the {@link ClassPool} every time.
	 */
	protected ClassFile ccFile = null;
	//private Loader loader = new Loader(pool);

    /**
     * Creates a new builder using the shared default {@link ClassPool}
     * provided by {@link ClassPoolFactory#getDefaultPool()}.
     *
     * @param classname fully qualified name of the class to generate.
     * @throws CannotCompileException if the generated class cannot be
     *                                compiled by Javassist.
     * @throws NotFoundException      if a referenced type cannot be
     *                                resolved in the pool.
     */
	public JaxrsEndpointApiCtClassBuilder(final String classname) throws CannotCompileException, NotFoundException  {
		this(ClassPoolFactory.getDefaultPool(), classname);
	}

    /**
     * Creates a new builder bound to the supplied {@link ClassPool}.
     *
     * @param pool      pool used to resolve types and create the class.
     * @param classname fully qualified name of the class to generate.
     * @throws CannotCompileException if the generated class cannot be
     *                                compiled by Javassist.
     * @throws NotFoundException      if a referenced type cannot be
     *                                resolved in the pool.
     */
	public JaxrsEndpointApiCtClassBuilder(final ClassPool pool, final String classname) throws CannotCompileException, NotFoundException {

		this.pool = pool;
		this.declaring = JaxrsEndpointApiUtils.makeClass(pool, classname);

		/* Resolve EndpointApi as the generated class' parent. */
		CtClass superclass = pool.get(EndpointApi.class.getName());
		declaring.setSuperclass(superclass);

		// add a default no-argument constructor
		declaring.addConstructor(CtNewConstructor.defaultConstructor(declaring));

		this.ccFile = this.declaring.getClassFile();

	}

    /**
     * Attaches a {@code @Path} annotation to the generated class.
     *
     * @param path URI template that defines the resource base path; must
     *             not contain matrix parameters.
     * @return this builder for chaining.
     */
	public JaxrsEndpointApiCtClassBuilder path(final String path) {

		ConstPool constPool = this.ccFile.getConstPool();
		JavassistUtils.addClassAnnotation(declaring, JaxrsEndpointApiUtils.annotPath(constPool, path));

		return this;
	}

    /**
     * Attaches a {@code @Produces} annotation to the generated class.
     * When no media types are supplied the default {@code *&#47;*}
     * value is used.
     *
     * @param mediaTypes produced media types.
     * @return this builder for chaining.
     */
	public JaxrsEndpointApiCtClassBuilder produces(final String... mediaTypes) {

		String[] noyNullMediaTypes = ArrayUtils.isNotEmpty(mediaTypes) ? mediaTypes : new String[] { "*/*" };
		ConstPool constPool = this.ccFile.getConstPool();
		JavassistUtils.addClassAnnotation(declaring, JaxrsEndpointApiUtils.annotProduces(constPool, noyNullMediaTypes));

		return this;
	}

    /**
     * Attaches a {@code @WebBound} annotation with the supplied primary
     * key and JSON payload.
     *
     * @param uid  primary key for the bound target.
     * @param json JSON payload that backs the bound target.
     * @return this builder for chaining.
     */
	public JaxrsEndpointApiCtClassBuilder bind(final String uid, final String json) {
		return bind(new RestBound(uid, json));
	}

    /**
     * Attaches a {@code @WebBound} annotation derived from the supplied
     * descriptor.
     *
     * @param bound descriptor carrying the bound values.
     * @return this builder for chaining.
     */
	public JaxrsEndpointApiCtClassBuilder bind(final RestBound bound) {

		ConstPool constPool = this.ccFile.getConstPool();
		JavassistUtils.addClassAnnotation(declaring, JaxrsEndpointApiUtils.annotWebBound(constPool, bound));

		return this;
	}

	/**
     * Compiles the given source code and adds a new field to the
     * generated class. The source must include the trailing
     * semicolon &mdash; see {@link CtField#make(String, CtClass)}.
     *
     * @param src the source text, e.g. {@code "public int k = 3;"}.
     * @return this builder for chaining.
     * @throws CannotCompileException if Javassist cannot compile the
     *                                provided snippet.
     */
	public JaxrsEndpointApiCtClassBuilder makeField(final String src) throws CannotCompileException {
		//创建属性
        declaring.addField(CtField.make(src, declaring));
		return this;
	}

    /**
     * Adds a strongly typed field initialised with the supplied value
     * via the {@link CtFieldBuilder} helper.
     *
     * @param fieldClass runtime type of the new field.
     * @param fieldName  simple name of the new field.
     * @param fieldValue initial value expressed as a Java expression
     *                   evaluated inside the generated class.
     * @param <T>        type of the new field.
     * @return this builder for chaining.
     * @throws CannotCompileException if the initialiser cannot be
     *                                compiled.
     * @throws NotFoundException      if the field type cannot be
     *                                resolved.
     */
	public <T> JaxrsEndpointApiCtClassBuilder newField(final Class<T> fieldClass, final String fieldName, final String fieldValue) throws CannotCompileException, NotFoundException {
		CtFieldBuilder.create(declaring, this.pool.get(fieldClass.getName()), fieldName, fieldValue);
		return this;
	}

    /**
     * Removes a previously declared field. If the field does not exist
     * the call is a no-op.
     *
     * @param fieldName simple name of the field to remove.
     * @return this builder for chaining.
     * @throws NotFoundException if the field lookup fails unexpectedly.
     */
	public JaxrsEndpointApiCtClassBuilder removeField(final String fieldName) throws NotFoundException {

		// 检查字段是否已经定义
		if(!JavassistUtils.hasField(declaring, fieldName)) {
			return this;
		}

		declaring.removeField(declaring.getDeclaredField(fieldName));

		return this;
	}

    /**
     * Convenience overload that wraps the supplied arguments in a
     * {@link RestMethod} and forwards to
     * {@link #newMethod(Class, RestMethod, RestBound, RestParam[])}.
     *
     * @param rtClass return type of the generated method, may be
     *                {@code null} for {@code void}.
     * @param method  HTTP verb.
     * @param name    Java method name.
     * @param path    URI template appended to the resource path.
     * @param bound   method-level binding or {@code null}.
     * @param params  method-level parameters.
     * @param <T>     return type parameter.
     * @return this builder for chaining.
     * @throws CannotCompileException if the generated body cannot be
     *                                compiled.
     * @throws NotFoundException      if a referenced type cannot be
     *                                resolved.
     */
	public <T> JaxrsEndpointApiCtClassBuilder newMethod(final Class<T> rtClass, final HttpMethodEnum method, final String name,final String path, final RestBound bound, RestParam<?>... params) throws CannotCompileException, NotFoundException {
		return this.newMethod(rtClass , new RestMethod(method, name, path), bound, params);
	}

    /**
     * Convenience overload without a method-level binding.
     *
     * @param rtClass return type of the generated method, may be
     *                {@code null} for {@code void}.
     * @param method  HTTP verb.
     * @param name    Java method name.
     * @param path    URI template appended to the resource path.
     * @param params  method-level parameters.
     * @param <T>     return type parameter.
     * @return this builder for chaining.
     * @throws CannotCompileException if the generated body cannot be
     *                                compiled.
     * @throws NotFoundException      if a referenced type cannot be
     *                                resolved.
     */
	public <T> JaxrsEndpointApiCtClassBuilder newMethod(final Class<T> rtClass, final HttpMethodEnum method, final String name,final String path, RestParam<?>... params) throws CannotCompileException, NotFoundException {
		return this.newMethod(rtClass , new RestMethod(method, name, path), params);
	}

    /**
     * Adds a fully-described REST method (verb, path, binding, and
     * parameters) to the generated class. The generated body
     * dispatches every invocation through the configured
     * {@link InvocationHandler}.
     *
     * @param rtClass return type of the generated method, may be
     *                {@code null} for {@code void}.
     * @param method  descriptor carrying the verb, name and path.
     * @param bound   method-level binding, may be {@code null}.
     * @param params  method-level parameters.
     * @param <T>     return type parameter.
     * @return this builder for chaining.
     * @throws CannotCompileException if the generated body cannot be
     *                                compiled.
     * @throws NotFoundException      if a referenced type cannot be
     *                                resolved.
     */
	public <T> JaxrsEndpointApiCtClassBuilder newMethod(final Class<T> rtClass, final RestMethod method, final RestBound bound, RestParam<?>... params) throws CannotCompileException, NotFoundException {

		ConstPool constPool = this.ccFile.getConstPool();

		// 创建抽象方法
		CtClass returnType = rtClass != null ? pool.get(rtClass.getName()) : CtClass.voidType;
		CtMethod ctMethod = null;
		// 方法参数
		CtClass[] parameters = JaxrsEndpointApiUtils.makeParams(pool, params);
		// 有参方法
		if(parameters != null && parameters.length > 0) {
			ctMethod = new CtMethod(returnType, method.getName(), parameters, declaring);
		}
		// 无参方法
		else {
			ctMethod = new CtMethod(returnType, method.getName() , null, declaring);
		}
        // 设置方法体
        JaxrsEndpointApiUtils.methodBody(ctMethod, method);
        // 设置方法异常捕获逻辑
        JaxrsEndpointApiUtils.methodCatch(pool, ctMethod);
        // 为方法添加 @HttpMethod、 @GET、 @POST、 @PUT、 @DELETE、 @PATCH、 @HEAD、 @OPTIONS、@Path、、@Consumes、@Produces、@RestBound、@RestParam 注解
        JaxrsEndpointApiUtils.methodAnnotations(ctMethod, constPool, method, bound, params);

        //新增方法
        declaring.addMethod(ctMethod);

        return this;
	}

    /**
     * Convenience overload without a method-level binding.
     *
     * @param rtClass return type of the generated method.
     * @param method  descriptor carrying the verb, name and path.
     * @param params  method-level parameters.
     * @param <T>     return type parameter.
     * @return this builder for chaining.
     * @throws CannotCompileException if the generated body cannot be
     *                                compiled.
     * @throws NotFoundException      if a referenced type cannot be
     *                                resolved.
     */
	public <T> JaxrsEndpointApiCtClassBuilder newMethod(final Class<T> rtClass, final RestMethod method, RestParam<?>... params) throws CannotCompileException, NotFoundException {
		return this.newMethod(rtClass, method, null, params);
	}

    /**
     * Convenience overload that omits the return type and the
     * method-level binding.
     *
     * @param method HTTP verb.
     * @param name   Java method name.
     * @param path   URI template appended to the resource path.
     * @param params method-level parameters.
     * @param <T>    return type parameter.
     * @return this builder for chaining.
     * @throws CannotCompileException if the generated body cannot be
     *                                compiled.
     * @throws NotFoundException      if a referenced type cannot be
     *                                resolved.
     */
	public <T> JaxrsEndpointApiCtClassBuilder newMethod(final HttpMethodEnum method, final String name, final String path, RestParam<?>... params) throws CannotCompileException, NotFoundException {
		return this.newMethod(null , new RestMethod(method, name, path), null, params);
	}

    /**
     * Convenience overload that omits the return type but keeps the
     * method-level binding.
     *
     * @param method HTTP verb.
     * @param name   Java method name.
     * @param path   URI template appended to the resource path.
     * @param bound  method-level binding.
     * @param params method-level parameters.
     * @param <T>    return type parameter.
     * @return this builder for chaining.
     * @throws CannotCompileException if the generated body cannot be
     *                                compiled.
     * @throws NotFoundException      if a referenced type cannot be
     *                                resolved.
     */
	public <T> JaxrsEndpointApiCtClassBuilder newMethod(final HttpMethodEnum method, final String name, final String path, final RestBound bound, RestParam<?>... params) throws CannotCompileException, NotFoundException {
		return this.newMethod(null , new RestMethod(method, name, path), bound, params);
	}

    /**
     * Convenience overload that omits the return type.
     *
     * @param method descriptor carrying the verb, name and path.
     * @param bound  method-level binding.
     * @param params method-level parameters.
     * @param <T>    return type parameter.
     * @return this builder for chaining.
     * @throws CannotCompileException if the generated body cannot be
     *                                compiled.
     * @throws NotFoundException      if a referenced type cannot be
     *                                resolved.
     */
	public <T> JaxrsEndpointApiCtClassBuilder newMethod(final RestMethod method, final RestBound bound, RestParam<?>... params) throws CannotCompileException, NotFoundException {
		return this.newMethod(null, method, bound, params);
	}

    /**
     * Convenience overload that omits both the return type and the
     * method-level binding.
     *
     * @param method descriptor carrying the verb, name and path.
     * @param params method-level parameters.
     * @param <T>    return type parameter.
     * @return this builder for chaining.
     * @throws CannotCompileException if the generated body cannot be
     *                                compiled.
     * @throws NotFoundException      if a referenced type cannot be
     *                                resolved.
     */
	public <T> JaxrsEndpointApiCtClassBuilder newMethod(final RestMethod method, RestParam<?>... params) throws CannotCompileException, NotFoundException {
		return this.newMethod(null, method, null, params);
	}

    /**
     * Removes a previously declared method. If the method does not
     * exist the call is a no-op.
     *
     * @param methodName simple name of the method to remove.
     * @param params     parameter descriptors used to disambiguate
     *                   overloaded methods; may be empty for non
     *                   overloaded methods.
     * @param <T>        unused generic parameter kept for symmetry with
     *                   the other {@code newMethod} overloads.
     * @return this builder for chaining.
     * @throws NotFoundException if the method lookup fails
     *                           unexpectedly.
     */
	public <T> JaxrsEndpointApiCtClassBuilder removeMethod(final String methodName, RestParam<?>... params) throws NotFoundException {

		// 有参方法
		if(params != null && params.length > 0) {

			// 方法参数
			CtClass[] parameters = JaxrsEndpointApiUtils.makeParams(pool, params);

			// 检查方法是否已经定义
			if(!JavassistUtils.hasMethod(declaring, methodName, parameters)) {
				return this;
			}

			declaring.removeMethod(declaring.getDeclaredMethod(methodName, parameters));

		}
		else {

			// 检查方法是否已经定义
			if(!JavassistUtils.hasMethod(declaring, methodName)) {
				return this;
			}

			declaring.removeMethod(declaring.getDeclaredMethod(methodName));

		}

		return this;
	}

    /**
     * Returns the underlying {@link CtClass} so the caller can perform
     * additional Javassist-level manipulations or feed it to
     * {@link #toClass()} / {@link #toInstance(InvocationHandler)}.
     *
     * @return the live {@link CtClass} handled by this builder.
     */
	@Override
	public CtClass build() {
        return declaring;
	}

    /**
     * Resolves the generated class through the current class loader and
     * detaches the {@link CtClass} from the pool so the in-memory cache
     * does not grow unbounded.
     *
     * @return the generated {@link Class}.
     * @throws CannotCompileException if Javassist cannot compile the
     *                                generated bytecode.
     */
	public Class<?> toClass() throws CannotCompileException {
        try {
        	// 通过类加载器加载该CtClass
			return declaring.toClass();
		} finally {
			// 将该class从ClassPool中删除
			declaring.detach();
		}
	}

    /**
     * Adds an {@link InvocationHandler}-accepting constructor, loads the
     * generated class, instantiates it through the new constructor and
     * detaches the {@link CtClass}.
     *
     * @param handler handler that will receive every dispatched
     *                invocation.
     * @return the freshly instantiated proxy.
     * @throws CannotCompileException     if the constructor body cannot
     *                                    be compiled.
     * @throws NotFoundException          if a referenced type cannot be
     *                                    resolved.
     * @throws InstantiationException     if the generated class cannot
     *                                    be instantiated.
     * @throws IllegalAccessException     if the constructor is not
     *                                    accessible.
     * @throws IllegalArgumentException   if the supplied arguments do
     *                                    not match the constructor.
     * @throws InvocationTargetException  if the constructor throws.
     * @throws NoSuchMethodException      if the generated constructor
     *                                    is missing.
     * @throws SecurityException          if a security manager refuses
     *                                    reflective access.
     */
	public Object toInstance(final InvocationHandler handler) throws CannotCompileException, NotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        try {
        	// 设置InvocationHandler参数构造器
			declaring.addConstructor(JaxrsEndpointApiUtils.makeConstructor(pool, declaring));
			// 通过类加载器加载该CtClass，并通过构造器初始化对象
			return declaring.toClass().getConstructor(InvocationHandler.class).newInstance(handler);
		} finally {
			// 将该class从ClassPool中删除
			declaring.detach();
		}
	}

}
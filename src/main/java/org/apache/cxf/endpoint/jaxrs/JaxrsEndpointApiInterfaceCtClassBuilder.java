package org.apache.cxf.endpoint.jaxrs;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.Builder;
import org.apache.cxf.endpoint.jaxrs.definition.HttpMethodEnum;
import org.apache.cxf.endpoint.jaxrs.definition.RestBound;
import org.apache.cxf.endpoint.jaxrs.definition.RestMethod;
import org.apache.cxf.endpoint.jaxrs.definition.RestParam;
import org.apache.cxf.endpoint.utils.JaxrsEndpointApiUtils;

import io.github.easy4j.javassist.utils.ClassPoolFactory;
import io.github.easy4j.javassist.utils.JavassistUtils;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;

/**
 * Builder that creates a JAX-RS resource interface as a Javassist
 * {@link CtClass}.
 *
 * <p>The generated interface extends {@link Cloneable} and exposes
 * abstract methods annotated with the standard JAX-RS annotations
 * ({@code @GET}, {@code @POST}, {@code @Path}, {@code @QueryParam},
 * etc.). This builder is typically used together with
 * {@link JaxrsEndpointApiImplCtClassBuilder} which generates the
 * paired implementation class.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see JaxrsEndpointApiCtClassBuilder
 * @see JaxrsEndpointApiImplCtClassBuilder
 */
public class JaxrsEndpointApiInterfaceCtClassBuilder implements Builder<CtClass> {

    /**
     * Class pool used to resolve types and define the generated
     * interface. Configured by the constructors.
     */
	private ClassPool pool = null;
    /**
     * {@link CtClass} representing the generated interface. Mutated in
     * place by every fluent setter on this builder.
     */
	private CtClass declaring  = null;
    /**
     * {@link ClassFile} view of {@link #declaring}; cached so
     * annotation writes do not have to query the {@link ClassPool}
     * every time.
     */
	private ClassFile ccFile = null;

	//private Loader loader = new Loader(pool);

    /**
     * Creates a new builder using the shared default {@link ClassPool}
     * provided by {@link ClassPoolFactory#getDefaultPool()}.
     *
     * @param classname fully qualified name of the interface to
     *                  generate.
     * @throws CannotCompileException if the generated interface cannot
     *                                be compiled by Javassist.
     * @throws NotFoundException      if a referenced type cannot be
     *                                resolved in the pool.
     */
	public JaxrsEndpointApiInterfaceCtClassBuilder(final String classname) throws CannotCompileException, NotFoundException  {
		this(ClassPoolFactory.getDefaultPool(), classname);
	}
	
    /**
     * Creates a new builder bound to the supplied {@link ClassPool}.
     *
     * @param pool      pool used to resolve types and create the
     *                  interface.
     * @param classname fully qualified name of the interface to
     *                  generate.
     * @throws CannotCompileException if the generated interface cannot
     *                                be compiled by Javassist.
     * @throws NotFoundException      if a referenced type cannot be
     *                                resolved in the pool.
     */
	public JaxrsEndpointApiInterfaceCtClassBuilder(final ClassPool pool, final String classname) throws CannotCompileException, NotFoundException {

		this.pool = pool;
		this.declaring = JaxrsEndpointApiUtils.makeInterface(pool, classname);

		/* Set Cloneable as the generated interface's parent. */
		CtClass superclass = pool.get(Cloneable.class.getName());
		declaring.setSuperclass(superclass);

		this.ccFile = this.declaring.getClassFile();
	}

    /**
     * Attaches a {@code @Path} annotation to the generated interface.
     *
     * @param path URI template that defines the resource base path.
     * @return this builder for chaining.
     */
	public JaxrsEndpointApiInterfaceCtClassBuilder path(final String path) {

		ConstPool constPool = this.ccFile.getConstPool();
		JavassistUtils.addClassAnnotation(declaring, JaxrsEndpointApiUtils.annotPath(constPool, path));
		
		return this;
	}
	
    /**
     * Attaches a {@code @Produces} annotation to the generated
     * interface. When no media types are supplied the default
     * {@code *&#47;*} value is used.
     *
     * @param mediaTypes produced media types.
     * @return this builder for chaining.
     */
	public JaxrsEndpointApiInterfaceCtClassBuilder produces(final String... mediaTypes) {

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
	public JaxrsEndpointApiInterfaceCtClassBuilder bind(final String uid, final String json) {
		return bind(new RestBound(uid, json));
	}

    /**
     * Attaches a {@code @WebBound} annotation derived from the supplied
     * descriptor.
     *
     * @param bound descriptor carrying the bound values.
     * @return this builder for chaining.
     */
	public JaxrsEndpointApiInterfaceCtClassBuilder bind(final RestBound bound) {

		ConstPool constPool = this.ccFile.getConstPool();
		JavassistUtils.addClassAnnotation(declaring, JaxrsEndpointApiUtils.annotWebBound(constPool, bound));
		
		return this;
	}
	
	/**
     * Compiles the given source code and creates a field.
     * Examples of the source code are:
     * 
     * <pre>
     * "public String name;"
     * "public int k = 3;"</pre>
     *
     * <p>Note that the source code ends with <code>';'</code>
     * (semicolon).
     *
     * @param src               the source text.
     * @return {@link JaxrsEndpointApiInterfaceCtClassBuilder} instance
     * @throws CannotCompileException if can't compile
     */
	public JaxrsEndpointApiInterfaceCtClassBuilder makeField(final String src) throws CannotCompileException {
		//创建属性
        declaring.addField(CtField.make(src, declaring));
		return this;
	}
	
    /**
     * Adds a strongly typed field to the generated interface. If the
     * field already exists, the call is a no-op.
     *
     * @param fieldClass runtime type of the new field.
     * @param fieldName  simple name of the new field.
     * @param fieldValue initial value expressed as a string literal.
     * @param <T>        type of the new field.
     * @return this builder for chaining.
     * @throws CannotCompileException if the field cannot be compiled.
     * @throws NotFoundException      if the field type cannot be
     *                                resolved.
     */
	public <T> JaxrsEndpointApiInterfaceCtClassBuilder newField(final Class<T> fieldClass, final String fieldName, final String fieldValue) throws CannotCompileException, NotFoundException {
		
		// 检查字段是否已经定义
		if(JavassistUtils.hasField(declaring, fieldName)) {
			return this;
		}
		
		/** 添加属性字段 */
		CtField field = new CtField(this.pool.get(fieldClass.getName()), fieldName, declaring);
        field.setModifiers(Modifier.PUBLIC);

        //新增Field
        declaring.addField(field, "\"" + fieldValue + "\"");
        
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
	public <T> JaxrsEndpointApiInterfaceCtClassBuilder removeField(final String fieldName) throws NotFoundException {
		
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
     * {@link #abstractMethod(Class, RestMethod, RestBound, RestParam[])}.
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
     * @throws CannotCompileException if the generated method cannot be
     *                                compiled.
     * @throws NotFoundException      if a referenced type cannot be
     *                                resolved.
     */
	public <T> JaxrsEndpointApiInterfaceCtClassBuilder abstractMethod(final Class<T> rtClass, final HttpMethodEnum method, final String name,final String path, final RestBound bound, RestParam<?>... params) throws CannotCompileException, NotFoundException {
		return this.abstractMethod(rtClass , new RestMethod(method, name, path), bound, params);
	}

    /**
     * Convenience overload without a method-level binding.
     *
     * @param rtClass return type of the generated method.
     * @param method  HTTP verb.
     * @param name    Java method name.
     * @param path    URI template appended to the resource path.
     * @param params  method-level parameters.
     * @param <T>     return type parameter.
     * @return this builder for chaining.
     * @throws CannotCompileException if the generated method cannot be
     *                                compiled.
     * @throws NotFoundException      if a referenced type cannot be
     *                                resolved.
     */
	public <T> JaxrsEndpointApiInterfaceCtClassBuilder abstractMethod(final Class<T> rtClass, final HttpMethodEnum method, final String name,final String path, RestParam<?>... params) throws CannotCompileException, NotFoundException {
		return this.abstractMethod(rtClass , new RestMethod(method, name, path), params);
	}

    /**
     * Adds a fully-described abstract REST method (verb, path, binding,
     * and parameters) to the generated interface. The method will be
     * annotated with the appropriate JAX-RS annotations.
     *
     * @param rtClass return type of the generated method, may be
     *                {@code null} for {@code void}.
     * @param method  descriptor carrying the verb, name and path.
     * @param bound   method-level binding, may be {@code null}.
     * @param params  method-level parameters.
     * @param <T>     return type parameter.
     * @return this builder for chaining.
     * @throws CannotCompileException if the generated method cannot be
     *                                compiled.
     * @throws NotFoundException      if a referenced type cannot be
     *                                resolved.
     */
	public <T> JaxrsEndpointApiInterfaceCtClassBuilder abstractMethod(final Class<T> rtClass, final RestMethod method, final RestBound bound, RestParam<?>... params) throws CannotCompileException, NotFoundException {
			      
		ConstPool constPool = this.ccFile.getConstPool();
		
		// 创建抽象方法
		CtClass returnType = rtClass != null ? pool.get(rtClass.getName()) : CtClass.voidType;
		CtClass[] exceptions = new CtClass[] { pool.get("java.lang.Exception") };
		// 方法参数
		CtClass[] parameters = JaxrsEndpointApiUtils.makeParams(pool, params);
		CtMethod ctMethod = null;
		// 有参方法
		if(parameters != null && parameters.length > 0) {
			ctMethod = CtNewMethod.abstractMethod(returnType, method.getName(), parameters , exceptions, declaring);
		} 
		// 无参方法 
		else {
			ctMethod = CtNewMethod.abstractMethod(returnType, method.getName(), null , exceptions, declaring);
		}
		
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
     * @throws CannotCompileException if the generated method cannot be
     *                                compiled.
     * @throws NotFoundException      if a referenced type cannot be
     *                                resolved.
     */
	public <T> JaxrsEndpointApiInterfaceCtClassBuilder abstractMethod(final Class<T> rtClass, final RestMethod method, RestParam<?>... params) throws CannotCompileException, NotFoundException {
		return this.abstractMethod(rtClass, method, null, params);
	}

    /**
     * Convenience overload that omits the return type and the
     * method-level binding.
     *
     * @param method HTTP verb.
     * @param name   Java method name.
     * @param path   URI template appended to the resource path.
     * @param params method-level parameters.
     * @return this builder for chaining.
     * @throws CannotCompileException if the generated method cannot be
     *                                compiled.
     * @throws NotFoundException      if a referenced type cannot be
     *                                resolved.
     */
	public JaxrsEndpointApiInterfaceCtClassBuilder abstractMethod(final HttpMethodEnum method, final String name,final String path, RestParam<?>... params) throws CannotCompileException, NotFoundException {
		return this.abstractMethod(null , new RestMethod(method, name, path), null, params);
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
     * @return this builder for chaining.
     * @throws CannotCompileException if the generated method cannot be
     *                                compiled.
     * @throws NotFoundException      if a referenced type cannot be
     *                                resolved.
     */
	public JaxrsEndpointApiInterfaceCtClassBuilder abstractMethod(final HttpMethodEnum method, final String name, final String path, final RestBound bound, RestParam<?>... params) throws CannotCompileException, NotFoundException {
		return this.abstractMethod(null, new RestMethod(method, name, path), bound, params);
	}

    /**
     * Convenience overload that omits the return type.
     *
     * @param method descriptor carrying the verb, name and path.
     * @param bound  method-level binding.
     * @param params method-level parameters.
     * @return this builder for chaining.
     * @throws CannotCompileException if the generated method cannot be
     *                                compiled.
     * @throws NotFoundException      if a referenced type cannot be
     *                                resolved.
     */
	public JaxrsEndpointApiInterfaceCtClassBuilder abstractMethod(final RestMethod method, final RestBound bound, RestParam<?>... params) throws CannotCompileException, NotFoundException {
		return this.abstractMethod(null, method, bound, params);
	}

    /**
     * Convenience overload that omits both the return type and the
     * method-level binding.
     *
     * @param method descriptor carrying the verb, name and path.
     * @param params method-level parameters.
     * @return this builder for chaining.
     * @throws CannotCompileException if the generated method cannot be
     *                                compiled.
     * @throws NotFoundException      if a referenced type cannot be
     *                                resolved.
     */
	public JaxrsEndpointApiInterfaceCtClassBuilder abstractMethod(final RestMethod method, RestParam<?>... params) throws CannotCompileException, NotFoundException {
		return this.abstractMethod(null, method, null, params);
	}

    /**
     * Removes a previously declared method. If the method does not
     * exist the call is a no-op.
     *
     * @param methodName simple name of the method to remove.
     * @param params     parameter descriptors used to disambiguate
     *                   overloaded methods; may be empty.
     * @return this builder for chaining.
     * @throws NotFoundException if the method lookup fails
     *                           unexpectedly.
     */
	public JaxrsEndpointApiInterfaceCtClassBuilder removeMethod(final String methodName, RestParam<?>... params) throws NotFoundException {
		
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
     * {@link #toClass()}.
     *
     * @return the live {@link CtClass} handled by this builder.
     */
	@Override
	public CtClass build() {
        return declaring;
	}

    /**
     * Resolves the generated interface through the current class loader
     * and detaches the {@link CtClass} from the pool so the in-memory
     * cache does not grow unbounded.
     *
     * @return the generated {@link Class}.
     * @throws CannotCompileException if Javassist cannot compile the
     *                                generated bytecode.
     */
	public Class<?> toClass() throws CannotCompileException {
        try {
			return declaring.toClass();
		} finally {
			declaring.detach();
		}
	}

}
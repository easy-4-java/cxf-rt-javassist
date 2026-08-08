package org.apache.cxf.endpoint.jaxws;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;

import jakarta.xml.ws.Service;
import jakarta.xml.ws.soap.AddressingFeature.Responses;

import org.apache.commons.lang3.builder.Builder;
import org.apache.cxf.endpoint.EndpointApi;
import org.apache.cxf.endpoint.jaxws.definition.SoapBound;
import org.apache.cxf.endpoint.jaxws.definition.SoapMethod;
import org.apache.cxf.endpoint.jaxws.definition.SoapParam;
import org.apache.cxf.endpoint.jaxws.definition.SoapResult;
import org.apache.cxf.endpoint.jaxws.definition.SoapService;
import org.apache.cxf.endpoint.utils.JaxwsEndpointApiUtils;

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
import javassist.bytecode.annotation.Annotation;

/**
 * Builder that creates a concrete JAX-WS endpoint class extending
 * {@link EndpointApi} on top of a {@link ClassPool}.
 *
 * <p>The builder wires the standard JAX-WS metadata ({@code @WebService},
 * {@code @WebBound}) onto the generated class and exposes a fluent API
 * to add annotated methods, fields, and constructors. Each
 * {@code new*} / {@code add*} method mutates the underlying
 * {@link CtClass} in-place and returns {@code this}, so calls can be
 * chained. The final class can be obtained as a {@link CtClass} through
 * {@link #build()}, as a {@link Class} through {@link #toClass()}, or
 * as an already-instantiated proxy through
 * {@link #toInstance(InvocationHandler)}.</p>
 *
 * <p>This is the JAX-WS counterpart of
 * {@link org.apache.cxf.endpoint.jaxrs.JaxrsEndpointApiCtClassBuilder}.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see JaxwsEndpointApiUtils
 * @see JaxwsEndpointApiInterfaceCtClassBuilder
 * @see JaxwsEndpointApiImplCtClassBuilder
 */
public class JaxwsEndpointApiCtClassBuilder implements Builder<CtClass> {

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
	protected ClassFile classFile = null;
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
	public JaxwsEndpointApiCtClassBuilder(final String classname) throws CannotCompileException, NotFoundException  {
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
	public JaxwsEndpointApiCtClassBuilder(final ClassPool pool, final String classname) throws CannotCompileException, NotFoundException {

		this.pool = pool;
		this.declaring = JaxwsEndpointApiUtils.makeClass(pool, classname);
		this.declaring.defrost();

		/* Resolve EndpointApi as the generated class' parent. */
		CtClass superclass = pool.get(EndpointApi.class.getName());
		declaring.setSuperclass(superclass);

		// add a default no-argument constructor
		declaring.addConstructor(CtNewConstructor.defaultConstructor(declaring));

		this.classFile = this.declaring.getClassFile();
	}

    /**
     * Attaches a {@code @WebService} annotation with the supplied name
     * and target namespace.
     *
     * @param name            the WSDL port type name.
     * @param targetNamespace the XML namespace for the service.
     * @return this builder for chaining.
     */
	public JaxwsEndpointApiCtClassBuilder webService(final String name, final String targetNamespace) {
		return this.webService(name, targetNamespace, null, null, null, null);
	}

    /**
     * Attaches a {@code @WebService} annotation with name, target
     * namespace and service name.
     *
     * @param name            the WSDL port type name.
     * @param targetNamespace the XML namespace for the service.
     * @param serviceName     the WSDL service name.
     * @return this builder for chaining.
     */
	public JaxwsEndpointApiCtClassBuilder webService(final String name, final String targetNamespace, String serviceName) {
		return this.webService(name, targetNamespace, serviceName, null, null, null);
	}

    /**
     * Attaches a fully-specified {@code @WebService} annotation to the
     * generated class.
     *
     * @param name               the WSDL port type name.
     * @param targetNamespace    the XML namespace for the service.
     * @param serviceName        the WSDL service name; defaults to the
     *                           simple class name + {@code "Service"}.
     * @param portName           the WSDL port name; defaults to
     *                           {@code name + "Port"}.
     * @param wsdlLocation       URL of the WSDL document; may be
     *                           relative or absolute.
     * @param endpointInterface  fully qualified name of the SEI.
     * @return this builder for chaining.
     */
	public JaxwsEndpointApiCtClassBuilder webService(final String name, final String targetNamespace, String serviceName,
			String portName, String wsdlLocation, String endpointInterface) {
		return webService(new SoapService(name, targetNamespace, serviceName, portName, wsdlLocation, endpointInterface));
	}

    /**
     * Attaches a {@code @WebService} annotation derived from the
     * supplied descriptor.
     *
     * @param service descriptor carrying the Web Service attributes.
     * @return this builder for chaining.
     */
	public JaxwsEndpointApiCtClassBuilder webService(final SoapService service) {

		ConstPool constPool = this.classFile.getConstPool();
		Annotation annot = JaxwsEndpointApiUtils.annotWebService(constPool, service);
		JavassistUtils.addClassAnnotation(declaring, annot);
		
		return this;
	}

    /**
     * Attaches a {@code @WebServiceProvider} annotation to the
     * generated class.
     *
     * @param wsdlLocation    URL of the WSDL document.
     * @param serviceName     the WSDL service name.
     * @param targetNamespace the XML namespace for the service.
     * @param portName        the WSDL port name.
     * @return this builder for chaining.
     */
	public JaxwsEndpointApiCtClassBuilder webServiceProvider(String wsdlLocation, String serviceName,
			String targetNamespace, String portName) {

		ConstPool constPool = this.classFile.getConstPool();
		Annotation annot = JaxwsEndpointApiUtils.annotWebServiceProvider(constPool, wsdlLocation, serviceName,
				targetNamespace, portName);
		JavassistUtils.addClassAnnotation(declaring, annot);

		return this;
	}

    /**
     * Attaches an {@code @Addressing} annotation to the generated class.
     *
     * @param enabled   whether WS-Addressing is enabled.
     * @param required  whether WS-Addressing is required.
     * @param responses the addressing responses policy.
     * @return this builder for chaining.
     */
	public JaxwsEndpointApiCtClassBuilder addressing(final boolean enabled, final boolean required,
			final Responses responses) {
		
		ConstPool constPool = this.classFile.getConstPool();
		Annotation annot = JaxwsEndpointApiUtils.annotAddressing(constPool, enabled, required, responses);
		JavassistUtils.addClassAnnotation(declaring, annot);
        
		return this;
	}
	
    /**
     * Attaches a {@code @ServiceMode} annotation to the generated class.
     *
     * @param mode the service mode ({@code PAYLOAD} or {@code MESSAGE}).
     * @return this builder for chaining.
     */
	public JaxwsEndpointApiCtClassBuilder serviceMode(final Service.Mode mode) {
		
		ConstPool constPool = this.classFile.getConstPool();
		Annotation annot = JaxwsEndpointApiUtils.annotServiceMode(constPool, mode);
		JavassistUtils.addClassAnnotation(declaring, annot);
        
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
	public JaxwsEndpointApiCtClassBuilder bind(final String uid, final String json) {
		return bind(new SoapBound(uid, json));
	}

    /**
     * Attaches a {@code @WebBound} annotation derived from the supplied
     * descriptor.
     *
     * @param bound descriptor carrying the bound values.
     * @return this builder for chaining.
     */
	public JaxwsEndpointApiCtClassBuilder bind(final SoapBound bound) {

		ConstPool constPool = this.classFile.getConstPool();
		Annotation annot = JaxwsEndpointApiUtils.annotWebBound(constPool, bound);
		JavassistUtils.addClassAnnotation(declaring, annot);
		
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
     * @return {@link JaxwsEndpointApiCtClassBuilder} instance
     * @throws CannotCompileException if can't compile
     */
	public JaxwsEndpointApiCtClassBuilder makeField(final String src) throws CannotCompileException {
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
	public <T> JaxwsEndpointApiCtClassBuilder newField(final Class<T> fieldClass, final String fieldName, final String fieldValue) throws CannotCompileException, NotFoundException {
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
	public <T> JaxwsEndpointApiCtClassBuilder removeField(final String fieldName) throws NotFoundException {
		
		// 检查字段是否已经定义
		if(!JavassistUtils.hasField(declaring, fieldName)) {
			return this;
		}
		
		declaring.removeField(declaring.getDeclaredField(fieldName));
		
		return this;
	}
	
	
	/**
     * Compiles the given source code and creates a method.
     * The source code must include not only the method body
     * but the whole declaration, for example,
     *
     * <pre>"public Object id(Object obj) { return obj; }"</pre>
     *
     * @param src               the source text. 
     * @return {@link JaxwsEndpointApiCtClassBuilder} instance
     * @throws CannotCompileException if can't compile
     */
	public JaxwsEndpointApiCtClassBuilder makeMethod(final String src) throws CannotCompileException {
		//创建方法 
		declaring.addMethod(CtMethod.make(src, declaring));
		return this;
	}
	
    /**
     * Convenience overload that creates a method with no return type
     * or binding, identified only by its operation name.
     *
     * @param methodName the WSDL operation name.
     * @param params     method-level parameters.
     * @return this builder for chaining.
     * @throws CannotCompileException if the generated body cannot be
     *                                compiled.
     * @throws NotFoundException      if a referenced type cannot be
     *                                resolved.
     */
	public JaxwsEndpointApiCtClassBuilder newMethod(final String methodName, SoapParam<?>... params) throws CannotCompileException, NotFoundException {
		return this.newMethod(null, new SoapMethod(methodName), null, params);
	}

    /**
     * Convenience overload with a method-level binding but no return
     * type.
     *
     * @param methodName the WSDL operation name.
     * @param bound      method-level binding.
     * @param params     method-level parameters.
     * @return this builder for chaining.
     * @throws CannotCompileException if the generated body cannot be
     *                                compiled.
     * @throws NotFoundException      if a referenced type cannot be
     *                                resolved.
     */
	public JaxwsEndpointApiCtClassBuilder newMethod( final String methodName, final SoapBound bound, SoapParam<?>... params) throws CannotCompileException, NotFoundException {
		return this.newMethod(null, new SoapMethod(methodName), bound, params);
	}

    /**
     * Adds a fully-described JAX-WS method (operation, result, binding,
     * and parameters) to the generated class. The generated body
     * dispatches every invocation through the configured
     * {@link InvocationHandler}.
     *
     * @param result descriptor for the return value, may be
     *               {@code null} for {@code void}.
     * @param method descriptor carrying the operation name.
     * @param bound  method-level binding, may be {@code null}.
     * @param params method-level parameters.
     * @param <T>    return type parameter.
     * @return this builder for chaining.
     * @throws CannotCompileException if the generated body cannot be
     *                                compiled.
     * @throws NotFoundException      if a referenced type cannot be
     *                                resolved.
     */
	public <T> JaxwsEndpointApiCtClassBuilder newMethod(final SoapResult<T> result, final SoapMethod method, final SoapBound bound, SoapParam<?>... params) throws CannotCompileException, NotFoundException {
	       
		ConstPool constPool = this.classFile.getConstPool();
		
		CtClass returnType = result != null ? pool.get(result.getRtClass().getName()) : CtClass.voidType;
		CtMethod ctMethod = null;
		// 方法参数
		CtClass[] parameters = JaxwsEndpointApiUtils.makeParams(pool, params);
		// 有参方法
		if(parameters != null && parameters.length > 0) {
			ctMethod = new CtMethod(returnType, method.getOperationName(), parameters, declaring);
		} 
		// 无参方法 
		else {
			ctMethod = new CtMethod(returnType, method.getOperationName() , null, declaring);
		}
        // 设置方法体
        JaxwsEndpointApiUtils.methodBody(ctMethod, method);
        // 设置方法异常捕获逻辑
        JaxwsEndpointApiUtils.methodCatch(pool, ctMethod);
        // 为方法添加 @WebMethod、 @WebResult、@WebBound、@WebParam 注解
        JaxwsEndpointApiUtils.methodAnnotations(ctMethod, constPool, result, method, bound, params);
        
        //新增方法
        declaring.addMethod(ctMethod);
        
        return this;
	}
	
    /**
     * Removes a previously declared method. If the method does not
     * exist the call is a no-op.
     *
     * @param methodName simple name of the method to remove.
     * @param params     parameter descriptors used to disambiguate
     *                   overloaded methods; may be empty.
     * @param <T>        unused generic parameter kept for symmetry.
     * @return this builder for chaining.
     * @throws NotFoundException if the method lookup fails
     *                           unexpectedly.
     */
	public <T> JaxwsEndpointApiCtClassBuilder removeMethod(final String methodName, SoapParam<?>... params) throws NotFoundException {
		
		// 有参方法
		if(params != null && params.length > 0) {
			
			// 方法参数
			CtClass[] parameters = JaxwsEndpointApiUtils.makeParams(pool, params);
			
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
			return declaring.toClass();
		} finally {
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
			declaring.addConstructor(JaxwsEndpointApiUtils.makeConstructor(pool, declaring));
			// 通过类加载器加载该CtClass，并通过构造器初始化对象
			return declaring.toClass().getConstructor(InvocationHandler.class).newInstance(handler);
		} finally {
			// 将该class从ClassPool中删除
			declaring.detach();
		} 
	}

}
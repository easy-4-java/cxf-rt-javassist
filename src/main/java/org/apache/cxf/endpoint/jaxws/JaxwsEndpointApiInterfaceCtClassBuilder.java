package org.apache.cxf.endpoint.jaxws;

import jakarta.xml.ws.Service;
import jakarta.xml.ws.soap.AddressingFeature.Responses;

import org.apache.commons.lang3.builder.Builder;
import org.apache.cxf.endpoint.jaxws.definition.SoapBound;
import org.apache.cxf.endpoint.jaxws.definition.SoapMethod;
import org.apache.cxf.endpoint.jaxws.definition.SoapParam;
import org.apache.cxf.endpoint.jaxws.definition.SoapResult;
import org.apache.cxf.endpoint.jaxws.definition.SoapService;
import org.apache.cxf.endpoint.utils.JaxwsEndpointApiUtils;

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
import javassist.bytecode.annotation.Annotation;

/**
 * Builder that creates a JAX-WS service endpoint interface (SEI) as a
 * Javassist {@link CtClass}.
 *
 * <p>The generated interface extends {@link Cloneable} and exposes
 * abstract methods annotated with the standard JAX-WS annotations
 * ({@code @WebMethod}, {@code @WebParam}, {@code @WebResult},
 * {@code @WebBound}). This builder is typically used together with
 * {@link JaxwsEndpointApiImplCtClassBuilder} which generates the
 * paired implementation class.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see JaxwsEndpointApiCtClassBuilder
 * @see JaxwsEndpointApiImplCtClassBuilder
 */
public class JaxwsEndpointApiInterfaceCtClassBuilder implements Builder<CtClass> {

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
	private ClassFile classFile = null;

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
	public JaxwsEndpointApiInterfaceCtClassBuilder(final String classname) throws CannotCompileException, NotFoundException  {
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
	public JaxwsEndpointApiInterfaceCtClassBuilder(final ClassPool pool, final String classname) throws CannotCompileException, NotFoundException {

		this.pool = pool;
		this.declaring = JaxwsEndpointApiUtils.makeInterface(pool, classname);

		/* Set Cloneable as the generated interface's parent. */
		CtClass superclass = pool.get(Cloneable.class.getName());
		declaring.setSuperclass(superclass);

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
	public JaxwsEndpointApiInterfaceCtClassBuilder webService(final String name, final String targetNamespace) {
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
	public JaxwsEndpointApiInterfaceCtClassBuilder webService(final String name, final String targetNamespace, String serviceName) {
		return this.webService(name, targetNamespace, serviceName, null, null, null);
	}

    /**
     * Attaches a fully-specified {@code @WebService} annotation to the
     * generated interface.
     *
     * @param name               the WSDL port type name.
     * @param targetNamespace    the XML namespace for the service.
     * @param serviceName        the WSDL service name.
     * @param portName           the WSDL port name.
     * @param wsdlLocation       URL of the WSDL document.
     * @param endpointInterface  fully qualified name of the SEI.
     * @return this builder for chaining.
     */
	public JaxwsEndpointApiInterfaceCtClassBuilder webService(final String name, final String targetNamespace, String serviceName,
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
	public JaxwsEndpointApiInterfaceCtClassBuilder webService(final SoapService service) {

		ConstPool constPool = this.classFile.getConstPool();
		Annotation annot = JaxwsEndpointApiUtils.annotWebService(constPool, service);
		JavassistUtils.addClassAnnotation(declaring, annot);
		
		return this;
	}
	
    /**
     * Attaches a {@code @ServiceMode} annotation to the generated
     * interface.
     *
     * @param mode the service mode ({@code PAYLOAD} or {@code MESSAGE}).
     * @return this builder for chaining.
     */
	public JaxwsEndpointApiInterfaceCtClassBuilder serviceMode(final Service.Mode mode) {

		ConstPool constPool = this.classFile.getConstPool();
		Annotation annot = JaxwsEndpointApiUtils.annotServiceMode(constPool, mode);
		JavassistUtils.addClassAnnotation(declaring, annot);

		return this;
	}

    /**
     * Attaches a {@code @WebServiceProvider} annotation to the
     * generated interface.
     *
     * @param wsdlLocation    URL of the WSDL document.
     * @param serviceName     the WSDL service name.
     * @param targetNamespace the XML namespace for the service.
     * @param portName        the WSDL port name.
     * @return this builder for chaining.
     */
	public JaxwsEndpointApiInterfaceCtClassBuilder webServiceProvider(String wsdlLocation, String serviceName,
			String targetNamespace, String portName) {

		ConstPool constPool = this.classFile.getConstPool();
		Annotation annot = JaxwsEndpointApiUtils.annotWebServiceProvider(constPool, wsdlLocation, serviceName,
				targetNamespace, portName);
		JavassistUtils.addClassAnnotation(declaring, annot);

		return this;
	}

    /**
     * Attaches an {@code @Addressing} annotation to the generated
     * interface.
     *
     * @param enabled   whether WS-Addressing is enabled.
     * @param required  whether WS-Addressing is required.
     * @param responses the addressing responses policy.
     * @return this builder for chaining.
     */
	public JaxwsEndpointApiInterfaceCtClassBuilder addressing(final boolean enabled, final boolean required,
			final Responses responses) {

		ConstPool constPool = this.classFile.getConstPool();
		Annotation annot = JaxwsEndpointApiUtils.annotAddressing(constPool, enabled, required, responses);
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
	public JaxwsEndpointApiInterfaceCtClassBuilder bind(final String uid, final String json) {
		return bind(new SoapBound(uid, json));
	}

    /**
     * Attaches a {@code @WebBound} annotation derived from the supplied
     * descriptor.
     *
     * @param bound descriptor carrying the bound values.
     * @return this builder for chaining.
     */
	public JaxwsEndpointApiInterfaceCtClassBuilder bind(final SoapBound bound) {

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
	public JaxwsEndpointApiInterfaceCtClassBuilder makeField(final String src) throws CannotCompileException {
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
	public <T> JaxwsEndpointApiInterfaceCtClassBuilder newField(final Class<T> fieldClass, final String fieldName, final String fieldValue) throws CannotCompileException, NotFoundException {
		
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
	public JaxwsEndpointApiInterfaceCtClassBuilder removeField(final String fieldName) throws NotFoundException {
		
		// 检查字段是否已经定义
		if(!JavassistUtils.hasField(declaring, fieldName)) {
			return this;
		}
		
		declaring.removeField(declaring.getDeclaredField(fieldName));
		
		return this;
	}
	
    /**
     * Convenience overload that creates an abstract method with no
     * return type or binding, identified only by its operation name.
     *
     * @param methodName the WSDL operation name.
     * @param params     method-level parameters.
     * @return this builder for chaining.
     * @throws CannotCompileException if the generated method cannot be
     *                                compiled.
     * @throws NotFoundException      if a referenced type cannot be
     *                                resolved.
     */
	public JaxwsEndpointApiInterfaceCtClassBuilder abstractMethod(final String methodName, SoapParam<?>... params) throws CannotCompileException, NotFoundException {
		return this.abstractMethod(null, new SoapMethod(methodName), null, params);
	}

    /**
     * Convenience overload with a method-level binding but no return
     * type.
     *
     * @param methodName the WSDL operation name.
     * @param bound      method-level binding.
     * @param params     method-level parameters.
     * @return this builder for chaining.
     * @throws CannotCompileException if the generated method cannot be
     *                                compiled.
     * @throws NotFoundException      if a referenced type cannot be
     *                                resolved.
     */
	public JaxwsEndpointApiInterfaceCtClassBuilder abstractMethod( final String methodName, final SoapBound bound, SoapParam<?>... params) throws CannotCompileException, NotFoundException {
		return this.abstractMethod(null, new SoapMethod(methodName), bound, params);
	}

    /**
     * Adds a fully-described abstract JAX-WS method (operation, result,
     * binding, and parameters) to the generated interface. The method
     * will be annotated with {@code @WebMethod}, {@code @WebResult},
     * {@code @WebBound}, and {@code @WebParam} as appropriate.
     *
     * @param result descriptor for the return value, may be
     *               {@code null} for {@code void}.
     * @param method descriptor carrying the operation name.
     * @param bound  method-level binding, may be {@code null}.
     * @param params method-level parameters.
     * @param <T>    return type parameter.
     * @return this builder for chaining.
     * @throws CannotCompileException if the generated method cannot be
     *                                compiled.
     * @throws NotFoundException      if a referenced type cannot be
     *                                resolved.
     */
	public <T> JaxwsEndpointApiInterfaceCtClassBuilder abstractMethod(final SoapResult<T> result, final SoapMethod method, final SoapBound bound, SoapParam<?>... params) throws CannotCompileException, NotFoundException {
	       
		ConstPool constPool = this.classFile.getConstPool();
		
		// 创建抽象方法
		CtClass returnType = result != null ? pool.get(result.getRtClass().getName()) : CtClass.voidType;
		CtClass[] exceptions = new CtClass[] { pool.get("java.lang.Exception") };
		// 方法参数
		CtClass[] parameters = JaxwsEndpointApiUtils.makeParams(pool, params);
		CtMethod ctMethod = null;
		// 有参方法
		if(parameters != null && parameters.length > 0) {
			ctMethod = CtNewMethod.abstractMethod(returnType, method.getOperationName(), parameters , exceptions, declaring);
		} 
		// 无参方法 
		else {
			ctMethod = CtNewMethod.abstractMethod(returnType, method.getOperationName(), null , exceptions, declaring);
		}
		
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
     * @return this builder for chaining.
     * @throws NotFoundException if the method lookup fails
     *                           unexpectedly.
     */
	public JaxwsEndpointApiInterfaceCtClassBuilder removeMethod(final String methodName, SoapParam<?>... params) throws NotFoundException {
		
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
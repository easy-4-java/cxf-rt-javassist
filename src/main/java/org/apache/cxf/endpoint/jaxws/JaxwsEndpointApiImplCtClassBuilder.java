package org.apache.cxf.endpoint.jaxws;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;

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

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * Builder that produces a paired JAX-WS interface and implementation
 * class on top of {@link JaxwsEndpointApiCtClassBuilder}.
 *
 * <p>The implementation class is generated under the {@code $Impl}
 * suffix ({@link #IMPL_CLASSNAME_PREFIX}) and implements the
 * interface produced by the inner
 * {@link JaxwsEndpointApiInterfaceCtClassBuilder}. Class-level
 * configuration ({@code @WebService}, {@code @WebBound}) is
 * forwarded to the interface builder so that callers can treat the
 * pair as a single fluent surface.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see JaxwsEndpointApiCtClassBuilder
 * @see JaxwsEndpointApiInterfaceCtClassBuilder
 */
public class JaxwsEndpointApiImplCtClassBuilder extends JaxwsEndpointApiCtClassBuilder implements Builder<CtClass> {

    /**
     * Suffix appended to the supplied class name to derive the
     * implementation class name.
     */
    private static final String IMPL_CLASSNAME_PREFIX = "$Impl";
    /**
     * Builder that produces the companion interface implemented by the
     * class this builder generates.
     */
	private JaxwsEndpointApiInterfaceCtClassBuilder classBuilder;

    /**
     * Creates a new builder using the shared default {@link ClassPool}.
     *
     * @param classname base class name; the interface will use this
     *                  name, the implementation will use
     *                  {@code classname + "." + IMPL_CLASSNAME_PREFIX}.
     * @throws CannotCompileException if the implementation class
     *                                cannot be compiled.
     * @throws NotFoundException      if a referenced type cannot be
     *                                resolved.
     */
	public JaxwsEndpointApiImplCtClassBuilder(final String classname) throws CannotCompileException, NotFoundException  {
		this(ClassPoolFactory.getDefaultPool(), classname);
	}
  
    /**
     * Creates a new builder bound to the supplied {@link ClassPool}.
     *
     * @param pool      pool used to resolve types and create the
     *                  classes.
     * @param classname base class name.
     * @throws CannotCompileException if the implementation class
     *                                cannot be compiled.
     * @throws NotFoundException      if a referenced type cannot be
     *                                resolved.
     */
	public JaxwsEndpointApiImplCtClassBuilder(final ClassPool pool, final String classname) throws CannotCompileException, NotFoundException {

		super(pool, classname + "." + IMPL_CLASSNAME_PREFIX);

		this.classBuilder = new JaxwsEndpointApiInterfaceCtClassBuilder(pool, classname);

	}

    /**
     * Forwards the call to the interface builder so both generated
     * artifacts receive the {@code @WebService} annotation.
     *
     * @param name            the WSDL port type name.
     * @param targetNamespace the XML namespace for the service.
     * @return this builder for chaining.
     */
	public JaxwsEndpointApiImplCtClassBuilder webService(final String name, final String targetNamespace) {
		return this.webService(name, targetNamespace, null, null, null, null);
	}

    /**
     * Forwards the call to the interface builder so both generated
     * artifacts receive the {@code @WebService} annotation.
     *
     * @param name            the WSDL port type name.
     * @param targetNamespace the XML namespace for the service.
     * @param serviceName     the WSDL service name.
     * @return this builder for chaining.
     */
	public JaxwsEndpointApiImplCtClassBuilder webService(final String name, final String targetNamespace, String serviceName) {
		return this.webService(name, targetNamespace, serviceName, null, null, null);
	}

    /**
     * Forwards a fully-specified {@code @WebService} annotation to the
     * interface builder.
     *
     * @param name               the WSDL port type name.
     * @param targetNamespace    the XML namespace for the service.
     * @param serviceName        the WSDL service name.
     * @param portName           the WSDL port name.
     * @param wsdlLocation       URL of the WSDL document.
     * @param endpointInterface  fully qualified name of the SEI.
     * @return this builder for chaining.
     */
	public JaxwsEndpointApiImplCtClassBuilder webService(final String name, final String targetNamespace, String serviceName,
			String portName, String wsdlLocation, String endpointInterface) {
		return webService(new SoapService(name, targetNamespace, serviceName, portName, wsdlLocation, endpointInterface));
	}

    /**
     * Forwards the {@code @WebService} annotation to the interface
     * builder.
     *
     * @param service descriptor carrying the Web Service attributes.
     * @return this builder for chaining.
     */
	public JaxwsEndpointApiImplCtClassBuilder webService(final SoapService service) {
		this.classBuilder.webService(service);
		return this;
	}

    /**
     * Forwards the {@code @ServiceMode} annotation to the interface
     * builder.
     *
     * @param mode the service mode ({@code PAYLOAD} or {@code MESSAGE}).
     * @return this builder for chaining.
     */
	public JaxwsEndpointApiCtClassBuilder serviceMode(final Service.Mode mode) {

		this.classBuilder.serviceMode(mode);

		return this;
	}

    /**
     * Forwards the {@code @WebServiceProvider} annotation to the
     * interface builder.
     *
     * @param wsdlLocation    URL of the WSDL document.
     * @param serviceName     the WSDL service name.
     * @param targetNamespace the XML namespace for the service.
     * @param portName        the WSDL port name.
     * @return this builder for chaining.
     */
	public JaxwsEndpointApiCtClassBuilder webServiceProvider(String wsdlLocation, String serviceName,
			String targetNamespace, String portName) {

		this.classBuilder.webServiceProvider(wsdlLocation, serviceName, targetNamespace, portName);

		return this;
	}

    /**
     * Forwards the {@code @Addressing} annotation to the interface
     * builder.
     *
     * @param enabled   whether WS-Addressing is enabled.
     * @param required  whether WS-Addressing is required.
     * @param responses the addressing responses policy.
     * @return this builder for chaining.
     */
	public JaxwsEndpointApiCtClassBuilder annotAddressing(final boolean enabled, final boolean required,
			final Responses responses) {

		this.classBuilder.addressing(enabled, required, responses);

		return this;
	}

    /**
     * Attaches a {@code @WebBound} annotation by forwarding the
     * descriptor to the interface builder.
     *
     * @param bound descriptor carrying the bound values.
     * @return this builder for chaining.
     */
	public JaxwsEndpointApiImplCtClassBuilder bind(final SoapBound bound) {
		this.classBuilder.bind(bound);
		return this;
	}

    /**
     * Generates an abstract method on the companion interface and the
     * matching concrete method on the implementation class.
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
	@Override
	public <T> JaxwsEndpointApiImplCtClassBuilder newMethod(final SoapResult<T> result, final SoapMethod method, final SoapBound bound, SoapParam<?>... params) throws CannotCompileException, NotFoundException {
		this.classBuilder.abstractMethod(result, method, bound, params);
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
        
        //新增方法
        declaring.addMethod(ctMethod);
        
        return this;
	}
	
    /**
     * Hooks the generated implementation class to the companion
     * interface and returns the resulting {@link CtClass}.
     *
     * @return the implementation class.
     */
	@Override
	public CtClass build() {
		try {
			declaring.setSuperclass(classBuilder.build());
		} catch (CannotCompileException e) {
			e.printStackTrace();
		}
        return declaring;
	}

    /**
     * Loads the generated class (with the companion interface as its
     * superclass) and detaches the {@link CtClass} from the pool.
     *
     * @return the generated {@link Class}.
     * @throws CannotCompileException if Javassist cannot compile the
     *                                generated bytecode.
     */
	public Class<?> toClass() throws CannotCompileException {
        try {
   			declaring.setSuperclass(classBuilder.build());
			return declaring.toClass();
		} finally {
			declaring.detach();
		}
	}

    /**
     * Adds the {@link InvocationHandler}-accepting constructor, hooks
     * the implementation class to its interface, and instantiates the
     * proxy.
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
        	declaring.setSuperclass(classBuilder.build());
			declaring.addConstructor(JaxwsEndpointApiUtils.makeConstructor(pool, declaring));
			return declaring.toClass().getConstructor(InvocationHandler.class).newInstance(handler);
		} finally {
			declaring.detach();
		}
	}

}
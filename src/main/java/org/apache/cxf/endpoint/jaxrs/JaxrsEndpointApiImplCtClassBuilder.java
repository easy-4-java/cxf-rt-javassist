package org.apache.cxf.endpoint.jaxrs;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang3.builder.Builder;
import org.apache.cxf.endpoint.jaxrs.definition.RestBound;
import org.apache.cxf.endpoint.jaxrs.definition.RestMethod;
import org.apache.cxf.endpoint.jaxrs.definition.RestParam;
import org.apache.cxf.endpoint.utils.JaxrsEndpointApiUtils;

import io.github.easy4j.javassist.utils.ClassPoolFactory;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * Builder that produces a paired JAX-RS interface and implementation
 * class on top of {@link JaxrsEndpointApiCtClassBuilder}.
 *
 * <p>The implementation class is generated under the {@code $Impl}
 * suffix ({@link #IMPL_CLASSNAME_PREFIX}) and implements the
 * interface produced by the inner
 * {@link JaxrsEndpointApiInterfaceCtClassBuilder}. Class-level
 * configuration ({@code @Path}, {@code @Produces},
 * {@code @WebBound}) is forwarded to the interface builder so that
 * callers can treat the pair as a single fluent surface.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see JaxrsEndpointApiCtClassBuilder
 * @see JaxrsEndpointApiInterfaceCtClassBuilder
 */
public class JaxrsEndpointApiImplCtClassBuilder extends JaxrsEndpointApiCtClassBuilder implements Builder<CtClass> {

    /**
     * Suffix appended to the supplied class name to derive the
     * implementation class name.
     */
    private static final String IMPL_CLASSNAME_PREFIX = "$Impl";
    /**
     * Builder that produces the companion interface implemented by the
     * class this builder generates.
     */
	private JaxrsEndpointApiInterfaceCtClassBuilder classBuilder;

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
	public JaxrsEndpointApiImplCtClassBuilder(final String classname) throws CannotCompileException, NotFoundException  {
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
	public JaxrsEndpointApiImplCtClassBuilder(final ClassPool pool, final String classname) throws CannotCompileException, NotFoundException {

		super(pool, classname + "." + IMPL_CLASSNAME_PREFIX);

		this.classBuilder = new JaxrsEndpointApiInterfaceCtClassBuilder(pool, classname);

	}

    /**
     * Forwards the call to the interface builder so both generated
     * artifacts receive the {@code @Path} annotation.
     *
     * @param path URI template for the resource.
     * @return this builder for chaining.
     */
	public JaxrsEndpointApiImplCtClassBuilder path(final String path) {
		this.classBuilder.path(path);
		return this;
	}

    /**
     * Forwards the call to the interface builder so both generated
     * artifacts receive the {@code @Produces} annotation.
     *
     * @param mediaTypes produced media types.
     * @return this builder for chaining.
     */
	public JaxrsEndpointApiImplCtClassBuilder produces(final String... mediaTypes) {
		this.classBuilder.produces(mediaTypes);
		return this;
	}

    /**
     * Attaches a {@code @WebBound} annotation with the supplied
     * primary key and JSON payload by forwarding to
     * {@link #bind(RestBound)}.
     *
     * @param uid  primary key.
     * @param json JSON payload.
     * @return this builder for chaining.
     */
	public JaxrsEndpointApiCtClassBuilder bind(final String uid, final String json) {
		return bind(new RestBound(uid, json));
	}

    /**
     * Attaches a {@code @WebBound} annotation by forwarding the
     * descriptor to the interface builder.
     *
     * @param bound descriptor carrying the bound values.
     * @return this builder for chaining.
     */
	public JaxrsEndpointApiCtClassBuilder bind(final RestBound bound) {
		this.classBuilder.bind(bound);
		return this;
	}

    /**
     * Generates an abstract method on the companion interface and the
     * matching concrete method on the implementation class.
     *
     * @param rtClass return type of the generated method.
     * @param method  descriptor carrying the verb, name and path.
     * @param bound   method-level binding.
     * @param params  method-level parameters.
     * @param <T>     return type parameter.
     * @return this builder for chaining.
     * @throws CannotCompileException if the generated body cannot be
     *                                compiled.
     * @throws NotFoundException      if a referenced type cannot be
     *                                resolved.
     */
	@Override
	public <T> JaxrsEndpointApiCtClassBuilder newMethod(final Class<T> rtClass, final RestMethod method, final RestBound bound, RestParam<?>... params) throws CannotCompileException, NotFoundException {

		this.classBuilder.abstractMethod(rtClass, method, bound, params);

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
			// 设置接口
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
        	// 设置接口
   			declaring.setSuperclass(classBuilder.build());
        	// 通过类加载器加载该CtClass
			return declaring.toClass();
		} finally {
			// 将该class从ClassPool中删除
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
        	// 设置接口
        	declaring.setSuperclass(classBuilder.build());
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
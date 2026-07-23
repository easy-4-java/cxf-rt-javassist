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
 * 
 * 动态构建ws接口
 * <p> http://www.cnblogs.com/sunfie/p/5154246.html</p>
 * <p> http://blog.csdn.net/youaremoon/article/details/50766972</p>
 * <p> https://my.oschina.net/GameKing/blog/794580</p>
 * <p> http://wsmajunfeng.iteye.com/blog/1912983</p>
 */
public class JaxrsEndpointApiImplCtClassBuilder extends JaxrsEndpointApiCtClassBuilder implements Builder<CtClass> {

    /** 
     * 生成的实现类名前缀 
     */  
    private static final String IMPL_CLASSNAME_PREFIX = "$Impl";  
	private JaxrsEndpointApiInterfaceCtClassBuilder classBuilder;
	
	public JaxrsEndpointApiImplCtClassBuilder(final String classname) throws CannotCompileException, NotFoundException  {
		this(ClassPoolFactory.getDefaultPool(), classname);
	}
  
	public JaxrsEndpointApiImplCtClassBuilder(final ClassPool pool, final String classname) throws CannotCompileException, NotFoundException {
		
		super(pool, classname + "." + IMPL_CLASSNAME_PREFIX);
		
		this.classBuilder = new JaxrsEndpointApiInterfaceCtClassBuilder(pool, classname);
		
	}
	
	/**
	 * 添加类注解 @Path
	 * @param path : Defines a URI template for the resource class or method, must not include matrix parameters.
	 * @return {@link JaxrsEndpointApiImplCtClassBuilder} instance
	 */
	public JaxrsEndpointApiImplCtClassBuilder path(final String path) {
		this.classBuilder.path(path);
		return this;
	}
	
	/**
	 * 添加类注解 @Produces
	 * @param mediaTypes the media types
	 * @return {@link JaxrsEndpointApiImplCtClassBuilder} instance
	 */
	public JaxrsEndpointApiImplCtClassBuilder produces(final String... mediaTypes) {
		this.classBuilder.produces(mediaTypes);
		return this;
	}
	
	/**
	 * 通过给动态类增加 <code>@WebBound</code>注解实现，数据的绑定
	 * @param uid			: The value of uid
	 * @param json			: The value of json
	 * @return {@link JaxrsEndpointApiCtClassBuilder} instance
	 */
	public JaxrsEndpointApiCtClassBuilder bind(final String uid, final String json) {
		return bind(new RestBound(uid, json));
	}
	
	/**
	 * 通过给动态类增加 <code>@WebBound</code>注解实现，数据的绑定
	 * @param bound			: The {@link RestBound} instance
	 * @return {@link JaxrsEndpointApiCtClassBuilder} instance
	 */
	public JaxrsEndpointApiCtClassBuilder bind(final RestBound bound) {
		this.classBuilder.bind(bound);
		return this;
	}
	
	/**
	 * 
	 * 根据参数构造一个新的方法
	 * @param rtClass ：返回对象类型
	 * @param method ：方法注释信息
	 * @param bound  ：方法绑定数据信息
	 * @param params ： 参数信息
	 * @param <T> 	   ： 参数泛型
	 * @return {@link JaxrsEndpointApiCtClassBuilder} instance 
	 * @throws CannotCompileException if can't compile
	 * @throws NotFoundException  if not found
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
	 * 
	 * javassist在加载类时会用Hashtable将类信息缓存到内存中，这样随着类的加载，内存会越来越大，甚至导致内存溢出。
	 * 如果应用中要加载的类比较多，建议在使用完CtClass之后删除缓存
	 * @return The Class 
	 * @throws CannotCompileException if can't compile
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
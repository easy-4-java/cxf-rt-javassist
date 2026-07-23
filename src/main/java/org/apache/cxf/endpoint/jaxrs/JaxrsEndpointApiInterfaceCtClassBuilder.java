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
 * 
 * 动态构建ws接口
 * <p>http://www.cnblogs.com/sunfie/p/5154246.html</p>
 * <p>http://blog.csdn.net/youaremoon/article/details/50766972</p>
 * <p>https://blog.csdn.net/tscyds/article/details/78415172</p>
 * <p>https://my.oschina.net/GameKing/blog/794580</p>
 * <p>http://wsmajunfeng.iteye.com/blog/1912983</p>
 */
public class JaxrsEndpointApiInterfaceCtClassBuilder implements Builder<CtClass> {
	
	// 构建动态类
	private ClassPool pool = null;
	private CtClass declaring  = null;
	private ClassFile ccFile = null;
	
	//private Loader loader = new Loader(pool);
	
	public JaxrsEndpointApiInterfaceCtClassBuilder(final String classname) throws CannotCompileException, NotFoundException  {
		this(ClassPoolFactory.getDefaultPool(), classname);
	}
	
	public JaxrsEndpointApiInterfaceCtClassBuilder(final ClassPool pool, final String classname) throws CannotCompileException, NotFoundException {
		
		this.pool = pool;
		this.declaring = JaxrsEndpointApiUtils.makeInterface(pool, classname);
		
		/* 指定 Cloneable 作为动态接口的父类 */
		CtClass superclass = pool.get(Cloneable.class.getName());
		declaring.setSuperclass(superclass);
		
		this.ccFile = this.declaring.getClassFile();
	}
	
	/**
	 * 添加类注解 @Path
	 * @param path : Defines a URI template for the resource class or method, must not include matrix parameters.
	 * @return {@link JaxrsEndpointApiInterfaceCtClassBuilder} instance
	 */
	public JaxrsEndpointApiInterfaceCtClassBuilder path(final String path) {

		ConstPool constPool = this.ccFile.getConstPool();
		JavassistUtils.addClassAnnotation(declaring, JaxrsEndpointApiUtils.annotPath(constPool, path));
		
		return this;
	}
	
	/**
	 * 添加类注解 @Produces
	 * @param mediaTypes the media types
	 * @return {@link JaxrsEndpointApiInterfaceCtClassBuilder} instance
	 */
	public JaxrsEndpointApiInterfaceCtClassBuilder produces(final String... mediaTypes) {

		String[] noyNullMediaTypes = ArrayUtils.isNotEmpty(mediaTypes) ? mediaTypes : new String[] { "*/*" };
		ConstPool constPool = this.ccFile.getConstPool();
		JavassistUtils.addClassAnnotation(declaring, JaxrsEndpointApiUtils.annotProduces(constPool, noyNullMediaTypes));
		
		return this;
	}
	
	/**
	 * 通过给动态类增加 <code>@WebBound</code>注解实现，数据的绑定
	 * @param uid			: The value of uid
	 * @param json			: The value of json
	 * @return {@link JaxrsEndpointApiInterfaceCtClassBuilder} instance
	 */
	public JaxrsEndpointApiInterfaceCtClassBuilder bind(final String uid, final String json) {
		return bind(new RestBound(uid, json));
	}
	
	/**
	 * 通过给动态类增加 <code>@WebBound</code>注解实现，数据的绑定
	 * @param bound			: The {@link RestBound} instance
	 * @return {@link JaxrsEndpointApiInterfaceCtClassBuilder} instance
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
	
	public <T> JaxrsEndpointApiInterfaceCtClassBuilder removeField(final String fieldName) throws NotFoundException {
		
		// 检查字段是否已经定义
		if(!JavassistUtils.hasField(declaring, fieldName)) {
			return this;
		}
		
		declaring.removeField(declaring.getDeclaredField(fieldName));
		
		return this;
	}
	
	public <T> JaxrsEndpointApiInterfaceCtClassBuilder abstractMethod(final Class<T> rtClass, final HttpMethodEnum method, final String name,final String path, final RestBound bound, RestParam<?>... params) throws CannotCompileException, NotFoundException {
		return this.abstractMethod(rtClass , new RestMethod(method, name, path), bound, params);
	}
	
	public <T> JaxrsEndpointApiInterfaceCtClassBuilder abstractMethod(final Class<T> rtClass, final HttpMethodEnum method, final String name,final String path, RestParam<?>... params) throws CannotCompileException, NotFoundException {
		return this.abstractMethod(rtClass , new RestMethod(method, name, path), params);
	}
	
	/**
	 * 
	 * 根据参数构造一个新的方法
	 * @param rtClass ：对象类型
	 * @param method ：方法注释信息
	 * @param bound  ：方法绑定数据信息
	 * @param params ： 参数信息
	 * @param <T> 	   ： 参数泛型
	 * @return {@link JaxrsEndpointApiInterfaceCtClassBuilder} instance 
	 * @throws CannotCompileException if can't compile
	 * @throws NotFoundException  if not found
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
	
	public <T> JaxrsEndpointApiInterfaceCtClassBuilder abstractMethod(final Class<T> rtClass, final RestMethod method, RestParam<?>... params) throws CannotCompileException, NotFoundException {
		return this.abstractMethod(rtClass, method, null, params);
	}
	
	public JaxrsEndpointApiInterfaceCtClassBuilder abstractMethod(final HttpMethodEnum method, final String name,final String path, RestParam<?>... params) throws CannotCompileException, NotFoundException {
		return this.abstractMethod(null , new RestMethod(method, name, path), null, params);
	}
	
	public JaxrsEndpointApiInterfaceCtClassBuilder abstractMethod(final HttpMethodEnum method, final String name, final String path, final RestBound bound, RestParam<?>... params) throws CannotCompileException, NotFoundException {
		return this.abstractMethod(null, new RestMethod(method, name, path), bound, params);
	}
	
	public JaxrsEndpointApiInterfaceCtClassBuilder abstractMethod(final RestMethod method, final RestBound bound, RestParam<?>... params) throws CannotCompileException, NotFoundException {
		return this.abstractMethod(null, method, bound, params);
	}
	
	public JaxrsEndpointApiInterfaceCtClassBuilder abstractMethod(final RestMethod method, RestParam<?>... params) throws CannotCompileException, NotFoundException {
		return this.abstractMethod(null, method, null, params);
	}
	
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
	
	@Override
	public CtClass build() {
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
        	// 通过类加载器加载该CtClass
			return declaring.toClass();
		} finally {
			// 将该class从ClassPool中删除
			declaring.detach();
		} 
	}

}
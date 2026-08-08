package org.apache.cxf.endpoint.jaxrs.definition;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.Encoded;

/**
 * Enumeration of the JAX-RS parameter-injection strategies supported by
 * the generated REST endpoint builder.
 *
 * <p>Each constant maps to one of the standard {@code jakarta.ws.rs}
 * parameter annotations and is used by the code-generation helpers to
 * decide which annotation to attach to a generated method parameter.
 * Use {@link RestParam#setFrom(HttpParamEnum)} to switch the binding
 * source for a particular parameter.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see jakarta.ws.rs.BeanParam
 * @see jakarta.ws.rs.CookieParam
 * @see jakarta.ws.rs.HeaderParam
 * @see jakarta.ws.rs.MatrixParam
 * @see jakarta.ws.rs.FormParam
 * @see jakarta.ws.rs.PathParam
 * @see jakarta.ws.rs.QueryParam
 */
public enum HttpParamEnum {

	/**
	 * The annotation that may be used to inject custom JAX-RS "parameter
	 * aggregator" value object into a resource class field, property or resource
	 * method parameter.
	 * 
	 * @see jakarta.ws.rs.BeanParam
	 */
	BEAN,
	/**
	 * Binds the value of a HTTP cookie to a resource method parameter, resource
	 * class field, or resource class bean property. A default value can be
	 * specified using the {@link DefaultValue} annotation.
	 * 
	 * @see jakarta.ws.rs.CookieParam
	 */
	COOKIE,
	/**
	 * Binds the value(s) of a HTTP header to a resource method parameter, resource
	 * class field, or resource class bean property. A default value can be
	 * specified using the {@link DefaultValue} annotation.
	 * 
	 * @see jakarta.ws.rs.HeaderParam
	 */
	HEADER,
	/**
	 * * Binds the value(s) of a URI matrix parameter to a resource method
	 * parameter, resource class field, or resource class bean property. Values are
	 * URL decoded unless this is disabled using the {@link Encoded} annotation. A
	 * default value can be specified using the {@link DefaultValue} annotation.
	 * 
	 * @see jakarta.ws.rs.MatrixParam
	 */
	MATRIX,
	/**
	 * Binds the value(s) of a form parameter contained within a request entity body
	 * to a resource method parameter. Values are URL decoded unless this is
	 * disabled using the {@link Encoded} annotation. A default value can be
	 * specified using the {@link DefaultValue} annotation. If the request entity
	 * body is absent or is an unsupported media type, the default value is used.
	 * 
	 * @see jakarta.ws.rs.FormParam
	 */
	FORM,
	/**
	 * Binds the value of a URI template parameter or a path segment containing the
	 * template parameter to a resource method parameter, resource class field, or
	 * resource class bean property. The value is URL decoded unless this is
	 * disabled using the {@link Encoded &#64;Encoded} annotation. A default value
	 * can be specified using the {@link DefaultValue &#64;DefaultValue} annotation.
	 * 
	 * @see jakarta.ws.rs.PathParam
	 */
	PATH,
	/**
	 * Binds the value(s) of a HTTP query parameter to a resource method parameter,
	 * resource class field, or resource class bean property. Values are URL decoded
	 * unless this is disabled using the {@link Encoded} annotation. A default value
	 * can be specified using the {@link DefaultValue} annotation.
	 * 
	 * @see jakarta.ws.rs.QueryParam
	 */
	QUERY;

}

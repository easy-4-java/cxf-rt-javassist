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
package org.apache.cxf.endpoint.jaxrs.definition;

/**
 * Descriptor for a single JAX-RS endpoint method parameter that the
 * generated builder will translate into a typed
 * {@code @QueryParam}, {@code @PathParam}, {@code @HeaderParam}, ...
 * annotation pair.
 *
 * <p>The descriptor carries the {@linkplain #getType() parameter type},
 * {@linkplain #getName() parameter name},
 * {@linkplain #getFrom() binding source} and an optional
 * {@linkplain #getDef() default value}. Use the various constructors
 * to pick the subset of attributes the application needs to override
 * while leaving the rest at their default values.</p>
 *
 * @param <T> the runtime type of the parameter.
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see HttpParamEnum
 * @see jakarta.ws.rs.DefaultValue
 */
public class RestParam<T> {

	/**
	 * Runtime type of the parameter; mandatory.
	 */
	private Class<T> type;

	/**
	 * Logical name of the parameter, surfaced as the value of the
	 * generated JAX-RS parameter annotation (e.g. {@code @QueryParam("id")}).
	 *
	 * @see jakarta.ws.rs.BeanParam
	 * @see jakarta.ws.rs.PathParam
	 * @see jakarta.ws.rs.QueryParam
	 * @see jakarta.ws.rs.MatrixParam
	 * @see jakarta.ws.rs.CookieParam
	 * @see jakarta.ws.rs.FormParam
	 * @see jakarta.ws.rs.HeaderParam
	 */
	private String name;

	/**
	 * Source the parameter should be bound to. Defaults to
	 * {@link HttpParamEnum#QUERY}.
	 *
	 * @see jakarta.ws.rs.BeanParam
	 * @see jakarta.ws.rs.PathParam
	 * @see jakarta.ws.rs.QueryParam
	 * @see jakarta.ws.rs.MatrixParam
	 * @see jakarta.ws.rs.CookieParam
	 * @see jakarta.ws.rs.FormParam
	 * @see jakarta.ws.rs.HeaderParam
	 */
	private HttpParamEnum from = HttpParamEnum.QUERY;

	/**
	 * Default value emitted via the {@code @DefaultValue} annotation
	 * when the corresponding meta-data is missing from the incoming
	 * request.
	 *
	 * @see jakarta.ws.rs.DefaultValue
	 */
	private String def;

    /**
     * Builds a parameter descriptor with the supplied type and name;
     * the binding source defaults to {@link HttpParamEnum#QUERY} and
     * no default value is set.
     *
     * @param type runtime type of the parameter.
     * @param name logical parameter name.
     */
	public RestParam(Class<T> type, String name) {
		this.type = type;
		this.name = name;
	}

    /**
     * Builds a parameter descriptor with an explicit binding source;
     * note that the current implementation does not actually persist the
     * supplied {@code from} value (a known bug carried over from the
     * original code).
     *
     * @param type runtime type of the parameter.
     * @param name logical parameter name.
     * @param from binding source for the parameter.
     */
	public RestParam(Class<T> type, String name, HttpParamEnum from) {
		this.type = type;
		this.name = name;
	}

    /**
     * Builds a parameter descriptor with both a binding source and a
     * default value. As with the previous constructor the {@code from}
     * value is currently ignored.
     *
     * @param type runtime type of the parameter.
     * @param name logical parameter name.
     * @param from binding source for the parameter.
     * @param def  default value surfaced via {@code @DefaultValue}.
     */
	public RestParam(Class<T> type, String name, HttpParamEnum from, String def ) {
		this.type = type;
		this.name = name;
		this.name = name;
		this.def = def;
	}

    /**
     * Builds a parameter descriptor with a default value but relying on
     * the default {@link HttpParamEnum#QUERY} binding source.
     *
     * @param type runtime type of the parameter.
     * @param name logical parameter name.
     * @param def  default value surfaced via {@code @DefaultValue}.
     */
	public RestParam(Class<T> type, String name, String def ) {
		this.type = type;
		this.name = name;
		this.def = def;
	}

    /**
     * Returns the runtime type of the parameter.
     *
     * @return the parameter type.
     */
	public Class<T> getType() {
		return type;
	}

    /**
     * Replaces the runtime type of the parameter.
     *
     * @param type new parameter type.
     */
	public void setType(Class<T> type) {
		this.type = type;
	}

    /**
     * Returns the logical parameter name.
     *
     * @return the parameter name.
     */
	public String getName() {
		return name;
	}

    /**
     * Replaces the logical parameter name.
     *
     * @param name new parameter name.
     */
	public void setName(String name) {
		this.name = name;
	}

    /**
     * Returns the binding source for the parameter.
     *
     * @return the binding source, defaults to
     *         {@link HttpParamEnum#QUERY}.
     */
	public HttpParamEnum getFrom() {
		return from;
	}

    /**
     * Replaces the binding source for the parameter.
     *
     * @param from new binding source.
     */
	public void setFrom(HttpParamEnum from) {
		this.from = from;
	}

    /**
     * Returns the default value associated with the parameter.
     *
     * @return the default value, possibly {@code null}.
     */
	public String getDef() {
		return def;
	}

    /**
     * Replaces the default value associated with the parameter.
     *
     * @param def new default value.
     */
	public void setDef(String def) {
		this.def = def;
	}

}

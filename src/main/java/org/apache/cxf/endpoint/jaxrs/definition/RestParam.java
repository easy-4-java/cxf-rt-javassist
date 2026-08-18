/*
 * Copyright (c) 2018, hiwepy (https://github.com/easy-4-java).
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
 */
public class RestParam<T> {

	/**
	 * 参数对象类型
	 */
	private Class<T> type;
	
	/**
	 * name ：参数的名称
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
	 * from ：参数来源
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
	 * Defines the default value of request meta-data that is bound using one of the
	 * following annotations: 
	 * {@link jakarta.ws.rs.PathParam},
	 * {@link jakarta.ws.rs.QueryParam}, 
	 * {@link jakarta.ws.rs.MatrixParam},
	 * {@link jakarta.ws.rs.CookieParam}, 
	 * {@link jakarta.ws.rs.FormParam}, or
	 * {@link jakarta.ws.rs.HeaderParam}. 
	 * The default value is used if the corresponding meta-data is not present in the request.
	 * @see jakarta.ws.rs.DefaultValue
	 */
	private String def;

	public RestParam(Class<T> type, String name) {
		this.type = type;
		this.name = name;
	}
	
	public RestParam(Class<T> type, String name, HttpParamEnum from) {
		this.type = type;
		this.name = name;
	}

	public RestParam(Class<T> type, String name, HttpParamEnum from, String def ) {
		this.type = type;
		this.name = name;
		this.name = name;
		this.def = def;
	}
	
	public RestParam(Class<T> type, String name, String def ) {
		this.type = type;
		this.name = name;
		this.def = def;
	}

	public Class<T> getType() {
		return type;
	}

	public void setType(Class<T> type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public HttpParamEnum getFrom() {
		return from;
	}

	public void setFrom(HttpParamEnum from) {
		this.from = from;
	}

	public String getDef() {
		return def;
	}

	public void setDef(String def) {
		this.def = def;
	}

}

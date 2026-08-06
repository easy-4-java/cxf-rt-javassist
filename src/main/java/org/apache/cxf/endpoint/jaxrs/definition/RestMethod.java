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

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;

public class RestMethod {

	/**
	 * Java 方法的名称
	 */
	private final String name;

	/**
	 * Associates the name of a HTTP method with an annotation.
	 * @see GET
	 * @see POST
	 * @see PUT
	 * @see DELETE
	 * @see PATCH
	 * @see HEAD
	 * @see OPTIONS
	 */
	private final HttpMethodEnum method;

	/**
	 * Identifies the URI path that a resource class or class method will serve
	 * requests for.
	 * 
	 * @see jakarta.ws.rs.Path
	 */
	private final String path;

	/**
	 * A list of media types. Each entry may specify a single type or consist of a
	 * comma separated list of types, with any leading or trailing white-spaces in a
	 * single type entry being ignored. For example:
	 * 
	 * <pre>
	 * { "image/jpeg, image/gif ", " image/png" }
	 * </pre>
	 * 
	 * Use of the comma-separated form allows definition of a common string constant
	 * for use on multiple targets.
	 */
	private String[] mediaTypes = new String[] { "*/*" };
	
	/**
	 * Defines the media types that the methods of a resource class or
	 * {@link jakarta.ws.rs.ext.MessageBodyReader} can accept.
	 * 
	 * @see jakarta.ws.rs.Consumes
	 */
	private String[] consumes;

	public RestMethod(HttpMethodEnum method, String name, String path) {
		this.method = method;
		this.name = name;
		this.path = path;
	}

	public RestMethod(HttpMethodEnum method, String name, String path, String... consumes) {
		this.method = method;
		this.name = name;
		this.path = path;
		this.consumes = consumes;
	}

	public String[] getConsumes() {
		return consumes;
	}
	
	public String[] getMediaTypes() {
		return mediaTypes;
	}

	public void setMediaTypes(String[] mediaTypes) {
		this.mediaTypes = mediaTypes;
	}

	public void setConsumes(String[] consumes) {
		this.consumes = consumes;
	}

	public String getName() {
		return name;
	}

	public HttpMethodEnum getMethod() {
		return method;
	}

	public String getPath() {
		return path;
	}

}

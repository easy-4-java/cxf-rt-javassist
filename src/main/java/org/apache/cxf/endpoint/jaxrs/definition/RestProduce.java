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

public class RestProduce {

	/**
	 * Defines a URI template for the resource class or method, must not include
	 * matrix parameters.
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

	public RestProduce(String path, String... mediaTypes) {
		this.path = path;
		this.mediaTypes = mediaTypes;
	}

	public String[] getMediaTypes() {
		return mediaTypes;
	}

	public void setMediaTypes(String[] mediaTypes) {
		this.mediaTypes = mediaTypes;
	}

	public String getPath() {
		return path;
	}

}

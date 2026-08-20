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
 * Carrier for the {@code @Path} URI template and {@code @Produces}
 * media types attached to a generated JAX-RS endpoint method.
 *
 * <p>This value object bundles a required {@linkplain #getPath() URI
 * template} together with the list of {@linkplain #getMediaTypes()
 * produced media types}. It is consumed by the helpers in
 * {@link org.apache.cxf.endpoint.utils.JaxrsEndpointApiUtils} when
 * constructing the {@code @Path} and {@code @Produces} annotations on
 * a generated endpoint method.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see jakarta.ws.rs.Path
 * @see jakarta.ws.rs.Produces
 */
public class RestProduce {

	/**
	 * Defines a URI template for the resource class or method, must
	 * not include matrix parameters. Final because the path cannot be
	 * changed after construction.
	 */
	private final String path;
	/**
	 * A list of media types. Each entry may specify a single type or
	 * consist of a comma-separated list of types, with any leading or
	 * trailing white-spaces in a single type entry being ignored.
	 * Defaults to a single {@code *&#47;*} entry.
	 */
	private String[] mediaTypes = new String[] { "*/*" };

    /**
     * Builds a {@code RestProduce} with the supplied URI template and
     * produced media types.
     *
     * @param path       URI template for the resource.
     * @param mediaTypes media types produced by the resource.
     */
	public RestProduce(String path, String... mediaTypes) {
		this.path = path;
		this.mediaTypes = mediaTypes;
	}

    /**
     * Returns the media types produced by the resource.
     *
     * @return the produced media types, never {@code null}.
     */
	public String[] getMediaTypes() {
		return mediaTypes;
	}

    /**
     * Replaces the media types produced by the resource.
     *
     * @param mediaTypes new produced media types.
     */
	public void setMediaTypes(String[] mediaTypes) {
		this.mediaTypes = mediaTypes;
	}

    /**
     * Returns the URI template that backs this {@code RestProduce}.
     *
     * @return the URI template.
     */
	public String getPath() {
		return path;
	}

}

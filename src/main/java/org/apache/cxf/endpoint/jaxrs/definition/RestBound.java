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
 * Data-binding carrier used to populate the {@link org.apache.cxf.endpoint.annotation.WebBound}
 * annotation on a generated JAX-RS endpoint method.
 *
 * <p>{@link RestBound} keeps a primary key ({@link #getUid()}) and an
 * optional JSON payload ({@link #getJson()}) that the generated
 * endpoint makes available to the implementation through the
 * annotation values. Instances are immutable in their key but expose
 * setters so that callers can adjust the JSON payload without
 * rebuilding the bound object.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see org.apache.cxf.endpoint.annotation.WebBound
 * @see org.apache.cxf.endpoint.utils.JaxrsEndpointApiUtils#annotWebBound(javassist.bytecode.ConstPool, RestBound)
 */
public class RestBound {

    /**
     * Builds a bound with the supplied uid and an empty JSON payload.
     *
     * @param uid primary key for the bound target; never {@code null}.
     */
    public RestBound(String uid) {
    	this.uid = uid;
	}

    /**
     * Builds a bound with both a primary key and a JSON payload.
     *
     * @param uid  primary key for the bound target.
     * @param json JSON payload that describes the bound data.
     */
	public RestBound(String uid, String json) {
		this.uid = uid;
		this.json = json;
	}

	/**
	 * Primary key used to identify the bound target inside the
	 * generated endpoint. Defaults to an empty string.
	 */
	private String uid = "";

	/**
	 * JSON payload that carries the actual bound data, kept as a string
	 * for convenience. Defaults to an empty string.
	 */
	private String json = "";

    /**
     * Returns the configured primary key.
     *
     * @return the uid, never {@code null}.
     */
	public String getUid() {
		return uid;
	}

    /**
     * Overrides the primary key.
     *
     * @param uid new uid; must not be {@code null}.
     */
	public void setUid(String uid) {
		this.uid = uid;
	}

    /**
     * Returns the JSON payload that backs this bound.
     *
     * @return the JSON payload, possibly empty.
     */
	public String getJson() {
		return json;
	}

    /**
     * Overrides the JSON payload.
     *
     * @param json new JSON payload; may be {@code null} to clear the
     *             payload.
     */
	public void setJson(String json) {
		this.json = json;
	}

}


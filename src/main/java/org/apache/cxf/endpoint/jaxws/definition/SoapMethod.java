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
package org.apache.cxf.endpoint.jaxws.definition;

/**
 * Descriptor for a single JAX-WS endpoint method that the generated
 * builder will translate into a {@code @WebMethod} annotation.
 *
 * <p>The descriptor bundles the {@linkplain #getOperationName()
 * WSDL operation name}, an optional {@linkplain #getAction()
 * SOAPAction}, and an {@linkplain #isExclude() exclude} flag.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see jakarta.jws.WebMethod
 */
public class SoapMethod {

    /**
     * Builds a descriptor with all defaults (empty operation name,
     * empty action, exclude = false).
     */
    public SoapMethod() {
	}

    /**
     * Builds a descriptor with the supplied operation name.
     *
     * @param operationName the WSDL operation name.
     */
    public SoapMethod(String operationName) {
		this.operationName = operationName;
	}

    /**
     * Builds a fully specified descriptor.
     *
     * @param operationName the WSDL operation name.
     * @param action        the SOAPAction value.
     * @param exclude       whether to exclude this method from the
     *                      service.
     */
	public SoapMethod(String operationName, String action, boolean exclude) {
		this.operationName = operationName;
		this.action = action;
		this.exclude = exclude;
	}

	/**
	 * WSDL operation name. Defaults to an empty string, which causes
	 * the runtime to use the Java method name.
	 */
	private String operationName = "";

	/**
	 * SOAPAction value for this operation. Defaults to an empty string.
	 */
	private String action = "";

	/**
	 * Whether this method should be excluded from the Web Service.
	 * Defaults to {@code false}.
	 */
	private boolean exclude = false;

    /**
     * Returns the WSDL operation name.
     *
     * @return the operation name, possibly empty.
     */
	public String getOperationName() {
		return operationName;
	}

    /**
     * Replaces the WSDL operation name.
     *
     * @param operationName new operation name.
     */
	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

    /**
     * Returns the SOAPAction value.
     *
     * @return the action, possibly empty.
     */
	public String getAction() {
		return action;
	}

    /**
     * Replaces the SOAPAction value.
     *
     * @param action new action.
     */
	public void setAction(String action) {
		this.action = action;
	}

    /**
     * Returns whether this method is excluded from the Web Service.
     *
     * @return {@code true} if the method is excluded.
     */
	public boolean isExclude() {
		return exclude;
	}

    /**
     * Sets whether this method should be excluded from the Web Service.
     *
     * @param exclude {@code true} to exclude the method.
     */
	public void setExclude(boolean exclude) {
		this.exclude = exclude;
	}

}

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
 * Descriptor for the return value of a JAX-WS endpoint method that the
 * generated builder will translate into a {@code @WebResult} annotation.
 *
 * @param <T> the runtime type of the return value.
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see jakarta.jws.WebResult
 */
public class SoapResult<T> {

	/**
	 * Runtime type of the return value; mandatory.
	 */
	private Class<T> rtClass;

	/**
	 * WSDL name for the return value. For RPC and DOCUMENT/WRAPPED
	 * bindings, defaults to {@code "return"}.
	 */
	private String name = "";

	/**
	 * XML namespace for the return value element. Only used for RPC or
	 * DOCUMENT/BARE operations.
	 */
	private String targetNamespace = "";

	/**
	 * Whether the result is carried in the SOAP header. Defaults to
	 * {@code false}.
	 */
	private boolean header = false;
	/**
	 * WSDL part name for the result. Only used for RPC or
	 * DOCUMENT/BARE operations. Defaults to the {@code @WebResult}
	 * name value.
	 */
	private String partName = "";

    /**
     * Builds a result descriptor with the supplied type and name.
     *
     * @param rtClass runtime type of the return value.
     * @param name    the WSDL result name.
     */
	public SoapResult(Class<T> rtClass, String name) {
		this.rtClass = rtClass;
		this.name = name;
	}

    /**
     * Builds a fully specified result descriptor.
     *
     * @param rtClass         runtime type of the return value.
     * @param name            the WSDL result name.
     * @param targetNamespace XML namespace for the result element.
     * @param header          whether the result is in the SOAP header.
     * @param partName        the WSDL part name for the result.
     */
	public SoapResult(Class<T> rtClass, String name, String targetNamespace, boolean header, String partName) {
		this.rtClass = rtClass;
		this.name = name;
		this.targetNamespace = targetNamespace;
		this.header = header;
		this.partName = partName;
	}

    /**
     * Returns the runtime type of the return value.
     *
     * @return the return type.
     */
	public Class<T> getRtClass() {
		return rtClass;
	}

    /**
     * Replaces the runtime type of the return value.
     *
     * @param rtClass new return type.
     */
	public void setRtClass(Class<T> rtClass) {
		this.rtClass = rtClass;
	}

    /**
     * Returns the WSDL result name.
     *
     * @return the result name.
     */
	public String getName() {
		return name;
	}

    /**
     * Replaces the WSDL result name.
     *
     * @param name new result name.
     */
	public void setName(String name) {
		this.name = name;
	}

    /**
     * Returns the XML namespace for the result element.
     *
     * @return the target namespace, possibly empty.
     */
	public String getTargetNamespace() {
		return targetNamespace;
	}

    /**
     * Replaces the XML namespace for the result element.
     *
     * @param targetNamespace new target namespace.
     */
	public void setTargetNamespace(String targetNamespace) {
		this.targetNamespace = targetNamespace;
	}

    /**
     * Returns whether the result is in the SOAP header.
     *
     * @return {@code true} if the result is a header result.
     */
	public boolean isHeader() {
		return header;
	}

    /**
     * Sets whether the result is in the SOAP header.
     *
     * @param header {@code true} to place the result in the header.
     */
	public void setHeader(boolean header) {
		this.header = header;
	}

    /**
     * Returns the WSDL part name for the result.
     *
     * @return the part name, possibly empty.
     */
	public String getPartName() {
		return partName;
	}

    /**
     * Replaces the WSDL part name for the result.
     *
     * @param partName new part name.
     */
	public void setPartName(String partName) {
		this.partName = partName;
	}

}
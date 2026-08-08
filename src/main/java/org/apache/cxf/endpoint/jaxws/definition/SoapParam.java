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

import jakarta.jws.WebParam.Mode;

/**
 * Descriptor for a single JAX-WS endpoint method parameter that the
 * generated builder will translate into a {@code @WebParam} annotation.
 *
 * <p>The descriptor carries the {@linkplain #getType() parameter type},
 * {@linkplain #getName() parameter name}, optional
 * {@linkplain #getPartName() part name},
 * {@linkplain #getTargetNamespace() target namespace},
 * {@linkplain #getMode() mode} and a
 * {@linkplain #isHeader() header} flag.</p>
 *
 * @param <T> the runtime type of the parameter.
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see jakarta.jws.WebParam
 */
public class SoapParam<T> {

    /**
     * Builds a parameter descriptor with type and name; mode defaults
     * to {@code IN}, header to {@code false}.
     *
     * @param type runtime type of the parameter.
     * @param name logical parameter name.
     */
	public SoapParam(Class<T> type, String name) {
		this.type = type;
		this.name = name;
	}

    /**
     * Builds a parameter descriptor with an explicit header flag.
     *
     * @param type   runtime type of the parameter.
     * @param name   logical parameter name.
     * @param header whether the parameter is in the SOAP header.
     */
	public SoapParam(Class<T> type, String name, boolean header) {
		this.type = type;
		this.name = name;
		this.header = header;
	}

    /**
     * Builds a parameter descriptor with an explicit mode.
     *
     * @param type runtime type of the parameter.
     * @param name logical parameter name.
     * @param mode parameter flow direction.
     */
	public SoapParam(Class<T> type, String name, Mode mode) {
		this.type = type;
		this.name = name;
		this.mode = mode;
	}

    /**
     * Builds a parameter descriptor with explicit mode and header flag.
     *
     * @param type   runtime type of the parameter.
     * @param name   logical parameter name.
     * @param mode   parameter flow direction.
     * @param header whether the parameter is in the SOAP header.
     */
	public SoapParam(Class<T> type, String name, Mode mode, boolean header) {
		this.type = type;
		this.name = name;
		this.mode = mode;
		this.header = header;
	}

    /**
     * Builds a fully specified parameter descriptor.
     *
     * @param type            runtime type of the parameter.
     * @param name            logical parameter name.
     * @param partName        the WSDL part name.
     * @param targetNamespace XML namespace for the parameter element.
     * @param mode            parameter flow direction.
     * @param header          whether the parameter is in the SOAP
     *                        header.
     */
	public SoapParam(Class<T> type, String name, String partName, String targetNamespace, Mode mode,
			boolean header) {
		this.type = type;
		this.name = name;
		this.partName = partName;
		this.targetNamespace = targetNamespace;
		this.mode = mode;
		this.header = header;
	}

	/**
	 * Runtime type of the parameter; mandatory.
	 */
	private Class<T> type;
	/**
	 * Logical name of the parameter, surfaced as the {@code name}
	 * attribute of the generated {@code @WebParam} annotation.
	 */
	private String name = "";
	/**
	 * WSDL part name for this parameter. Only used when the operation
	 * type is RPC or the operation is document type and the parameter
	 * type is BARE. Defaults to an empty string.
	 */
	private String partName = "";
	/**
	 * XML namespace for the parameter element. Only applies to
	 * document bindings. Defaults to the Web Service target namespace.
	 */
	private String targetNamespace = "";
	/**
	 * Parameter flow direction. Defaults to {@code IN}.
	 */
	private jakarta.jws.WebParam.Mode mode = jakarta.jws.WebParam.Mode.IN;
	/**
	 * Whether the parameter is in the SOAP header rather than the body.
	 * Defaults to {@code false}.
	 */
	private boolean header = false;

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
     * Returns the WSDL part name.
     *
     * @return the part name, possibly empty.
     */
	public String getPartName() {
		return partName;
	}

    /**
     * Replaces the WSDL part name.
     *
     * @param partName new part name.
     */
	public void setPartName(String partName) {
		this.partName = partName;
	}

    /**
     * Returns the XML namespace for the parameter element.
     *
     * @return the target namespace, possibly empty.
     */
	public String getTargetNamespace() {
		return targetNamespace;
	}

    /**
     * Replaces the XML namespace for the parameter element.
     *
     * @param targetNamespace new target namespace.
     */
	public void setTargetNamespace(String targetNamespace) {
		this.targetNamespace = targetNamespace;
	}

    /**
     * Returns the parameter flow direction.
     *
     * @return the mode, never {@code null}.
     */
	public jakarta.jws.WebParam.Mode getMode() {
		return mode;
	}

    /**
     * Replaces the parameter flow direction.
     *
     * @param mode new mode.
     */
	public void setMode(jakarta.jws.WebParam.Mode mode) {
		this.mode = mode;
	}

    /**
     * Returns whether the parameter is in the SOAP header.
     *
     * @return {@code true} if the parameter is a header parameter.
     */
	public boolean isHeader() {
		return header;
	}

    /**
     * Sets whether the parameter is in the SOAP header.
     *
     * @param header {@code true} to place the parameter in the header.
     */
	public void setHeader(boolean header) {
		this.header = header;
	}

}

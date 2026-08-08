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
 * Descriptor for the {@code @WebService} annotation attributes attached
 * to a generated JAX-WS endpoint class.
 *
 * <p>The descriptor bundles the mandatory {@linkplain #getName() name}
 * and {@linkplain #getTargetNamespace() target namespace} together with
 * optional {@linkplain #getServiceName() service name},
 * {@linkplain #getPortName() port name},
 * {@linkplain #getWsdlLocation() WSDL location} and
 * {@linkplain #getEndpointInterface() endpoint interface}.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see jakarta.jws.WebService
 */
public class SoapService {

	/**
	 * WSDL port type name. Defaults to the simple class name.
	 */
	private final String name;
	/**
	 * XML namespace for the service. Defaults to the reversed package
	 * name of the implementation class.
	 */
	private final String targetNamespace;
	/**
	 * WSDL service name ({@code wsdl:service}). Defaults to the simple
	 * class name + {@code "Service"}.
	 */
	private String serviceName;
	/**
	 * WSDL port name ({@code wsdl:portName}). Defaults to
	 * {@code name + "Port"}.
	 */
	private String portName;
	/**
	 * URL of the WSDL document. May be relative or absolute.
	 */
	private String wsdlLocation;
	/**
	 * Fully qualified name of the Service Endpoint Interface (SEI).
	 */
	private String endpointInterface;

    /**
     * Builds a descriptor with the mandatory name and target namespace.
     *
     * @param name            the WSDL port type name.
     * @param targetNamespace the XML namespace for the service.
     */
	public SoapService(String name, String targetNamespace) {
		this.name = name;
		this.targetNamespace = targetNamespace;
	}

    /**
     * Builds a descriptor with name, target namespace and service name.
     *
     * @param name            the WSDL port type name.
     * @param targetNamespace the XML namespace for the service.
     * @param serviceName     the WSDL service name.
     */
	public SoapService(String name, String targetNamespace, String serviceName) {
		this.name = name;
		this.targetNamespace = targetNamespace;
		this.serviceName = serviceName;
	}

    /**
     * Builds a descriptor with name, target namespace, service name and
     * port name.
     *
     * @param name            the WSDL port type name.
     * @param targetNamespace the XML namespace for the service.
     * @param serviceName     the WSDL service name.
     * @param portName        the WSDL port name.
     */
	public SoapService(String name, String targetNamespace, String serviceName, String portName) {
		this.name = name;
		this.targetNamespace = targetNamespace;
		this.serviceName = serviceName;
		this.portName = portName;
	}

    /**
     * Builds a descriptor with name, target namespace, service name,
     * port name and WSDL location.
     *
     * @param name            the WSDL port type name.
     * @param targetNamespace the XML namespace for the service.
     * @param serviceName     the WSDL service name.
     * @param portName        the WSDL port name.
     * @param wsdlLocation    URL of the WSDL document.
     */
	public SoapService(String name, String targetNamespace, String serviceName, String portName, String wsdlLocation) {
		this.name = name;
		this.targetNamespace = targetNamespace;
		this.serviceName = serviceName;
		this.portName = portName;
		this.wsdlLocation = wsdlLocation;
	}

    /**
     * Builds a fully specified descriptor with all attributes.
     *
     * @param name               the WSDL port type name.
     * @param targetNamespace    the XML namespace for the service.
     * @param serviceName        the WSDL service name.
     * @param portName           the WSDL port name.
     * @param wsdlLocation       URL of the WSDL document.
     * @param endpointInterface  fully qualified name of the SEI.
     */
	public SoapService(String name, String targetNamespace, String serviceName, String portName, String wsdlLocation,
			String endpointInterface) {
		this.name = name;
		this.targetNamespace = targetNamespace;
		this.serviceName = serviceName;
		this.portName = portName;
		this.wsdlLocation = wsdlLocation;
		this.endpointInterface = endpointInterface;
	}

    /**
     * Returns the WSDL service name.
     *
     * @return the service name, possibly {@code null}.
     */
	public String getServiceName() {
		return serviceName;
	}

    /**
     * Replaces the WSDL service name.
     *
     * @param serviceName new service name.
     */
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

    /**
     * Returns the WSDL port name.
     *
     * @return the port name, possibly {@code null}.
     */
	public String getPortName() {
		return portName;
	}

    /**
     * Replaces the WSDL port name.
     *
     * @param portName new port name.
     */
	public void setPortName(String portName) {
		this.portName = portName;
	}

    /**
     * Returns the URL of the WSDL document.
     *
     * @return the WSDL location, possibly {@code null}.
     */
	public String getWsdlLocation() {
		return wsdlLocation;
	}

    /**
     * Replaces the URL of the WSDL document.
     *
     * @param wsdlLocation new WSDL location.
     */
	public void setWsdlLocation(String wsdlLocation) {
		this.wsdlLocation = wsdlLocation;
	}

    /**
     * Returns the fully qualified name of the SEI.
     *
     * @return the endpoint interface, possibly {@code null}.
     */
	public String getEndpointInterface() {
		return endpointInterface;
	}

    /**
     * Replaces the fully qualified name of the SEI.
     *
     * @param endpointInterface new endpoint interface.
     */
	public void setEndpointInterface(String endpointInterface) {
		this.endpointInterface = endpointInterface;
	}

    /**
     * Returns the WSDL port type name.
     *
     * @return the name, never {@code null}.
     */
	public String getName() {
		return name;
	}

    /**
     * Returns the XML namespace for the service.
     *
     * @return the target namespace, never {@code null}.
     */
	public String getTargetNamespace() {
		return targetNamespace;
	}

}

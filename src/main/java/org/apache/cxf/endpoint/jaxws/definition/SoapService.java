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
package org.apache.cxf.endpoint.jaxws.definition;

public class SoapService {

	/**
	 * 此属性的值包含XML Web Service的名称。在默认情况下，该值是实现XML Web Service的类的名称，wsdl:portType
	 * 的名称。缺省值为 Java 类或接口的非限定名称。（字符串）
	 */
	private final String name;
	/**
	 * 指定你想要的名称空间，默认是使用接口实现类的包名的反缀（字符串）
	 */
	private final String targetNamespace;
	/**
	 * 对外发布的服务名，指定 Web Service 的服务名称：wsdl:service。缺省值为 Java 类的简单名称 + Service。（字符串）
	 */
	private String serviceName;
	/**
	 * wsdl:portName。缺省值为 WebService.name+Port。（字符串）
	 */
	private String portName;
	/**
	 * 指定用于定义 Web Service 的 WSDL 文档的 Web 地址。Web 地址可以是相对路径或绝对路径。（字符串）
	 */
	private String wsdlLocation;
	/**
	 * 服务接口全路径, 指定做SEI（Service EndPoint Interface）服务端点接口（字符串）
	 */
	private String endpointInterface;

	public SoapService(String name, String targetNamespace) {
		this.name = name;
		this.targetNamespace = targetNamespace;
	}
	
	public SoapService(String name, String targetNamespace, String serviceName) {
		this.name = name;
		this.targetNamespace = targetNamespace;
		this.serviceName = serviceName;
	}
	
	public SoapService(String name, String targetNamespace, String serviceName, String portName) {
		this.name = name;
		this.targetNamespace = targetNamespace;
		this.serviceName = serviceName;
		this.portName = portName;
	}
	
	public SoapService(String name, String targetNamespace, String serviceName, String portName, String wsdlLocation) {
		this.name = name;
		this.targetNamespace = targetNamespace;
		this.serviceName = serviceName;
		this.portName = portName;
		this.wsdlLocation = wsdlLocation;
	}
	
	public SoapService(String name, String targetNamespace, String serviceName, String portName, String wsdlLocation,
			String endpointInterface) {
		this.name = name;
		this.targetNamespace = targetNamespace;
		this.serviceName = serviceName;
		this.portName = portName;
		this.wsdlLocation = wsdlLocation;
		this.endpointInterface = endpointInterface;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getPortName() {
		return portName;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}

	public String getWsdlLocation() {
		return wsdlLocation;
	}

	public void setWsdlLocation(String wsdlLocation) {
		this.wsdlLocation = wsdlLocation;
	}

	public String getEndpointInterface() {
		return endpointInterface;
	}

	public void setEndpointInterface(String endpointInterface) {
		this.endpointInterface = endpointInterface;
	}

	public String getName() {
		return name;
	}

	public String getTargetNamespace() {
		return targetNamespace;
	}

}

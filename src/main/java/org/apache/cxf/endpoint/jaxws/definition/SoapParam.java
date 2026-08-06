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
 * 注释用于定制从单个参数至 Web Service 消息部件和 XML 元素的映射。将此注释应用于客户机或服务器服务端点接口（SEI）上的方法，或者应用于 JavaBeans 端点的服务器端点实现类。
 * https://www.cnblogs.com/zhao-shan/p/5515174.html
 */
public class SoapParam<T> {

	public SoapParam(Class<T> type, String name) {
		this.type = type;
		this.name = name;
	}
	
	public SoapParam(Class<T> type, String name, boolean header) {
		this.type = type;
		this.name = name;
		this.header = header;
	}
	
	public SoapParam(Class<T> type, String name, Mode mode) {
		this.type = type;
		this.name = name;
		this.mode = mode;
	}
	
	public SoapParam(Class<T> type, String name, Mode mode, boolean header) {
		this.type = type;
		this.name = name;
		this.mode = mode;
		this.header = header;
	}
	
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
	 * 参数对象类型
	 */
	private Class<T> type;
	/**
	 * 1、name ：参数的名称。如果操作是远程过程调用（RPC）类型并且未指定partName 属性，那么这是用于表示参数的 wsdl:part 属性的名称。
	 * 如果操作是文档类型或者参数映射至某个头，那么 -name 是用于表示该参数的 XML 元素的局部名称。如果操作是文档类型、 参数类型为 BARE
	 * 并且方式为 OUT 或 INOUT，那么必须指定此属性。（字符串）
	 */
	private String name = "";
	/**
	 * 2、partName：定义用于表示此参数的 wsdl:part属性的名称。仅当操作类型为 RPC 或者操作是文档类型并且参数类型为BARE
	 * 时才使用此参数。（字符串）
	 */
	private String partName = "";
	/**
	 * 3、targetNamespace：指定参数的 XML 元素的 XML 名称空间。当属性映射至 XML 元素时，仅应用于文档绑定。缺省值为 Web
	 * Service 的 targetNamespace。（字符串）
	 */
	private String targetNamespace = "";
	/**
	 * 4、mode：此值表示此方法的参数流的方向。有效值为 IN、INOUT 和 OUT。（字符串）
	 */
	private jakarta.jws.WebParam.Mode mode = jakarta.jws.WebParam.Mode.IN;
	/**
	 * 5、header：指定参数是在消息头还是消息体中。缺省值为 false。（布尔值）
	 */
	private boolean header = false;

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

	public String getPartName() {
		return partName;
	}

	public void setPartName(String partName) {
		this.partName = partName;
	}

	public String getTargetNamespace() {
		return targetNamespace;
	}

	public void setTargetNamespace(String targetNamespace) {
		this.targetNamespace = targetNamespace;
	}

	public jakarta.jws.WebParam.Mode getMode() {
		return mode;
	}

	public void setMode(jakarta.jws.WebParam.Mode mode) {
		this.mode = mode;
	}

	public boolean isHeader() {
		return header;
	}

	public void setHeader(boolean header) {
		this.header = header;
	}
	
}

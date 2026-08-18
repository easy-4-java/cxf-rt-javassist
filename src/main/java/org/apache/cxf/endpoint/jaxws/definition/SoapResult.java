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

/**
 * 注释用于定制从返回值至 WSDL 部件或 XML 元素的映射。将此注释应用于客户机或服务器服务端点接口（SEI）上的方法，或者应用于 JavaBeans 端点的服务器端点实现类。
 * https://www.cnblogs.com/zhao-shan/p/5515174.html
 */
public class SoapResult<T> {

	/**
	 * 返回结果对象类型
	 */
	private Class<T> rtClass;
	
	/**
	 * 1、name：当返回值列示在 WSDL 文件中并且在连接上的消息中找到该返回值时，指定该返回值的名称。对于 RPC 绑定，这是用于表示返回值的
	 * wsdl:part属性的名称。对于文档绑定，-name 参数是用于表示返回值的 XML 元素的局部名。对于 RPC 和 DOCUMENT/WRAPPED
	 * 绑定，缺省值为 return。对于 DOCUMENT/BARE 绑定，缺省值为方法名 + Response。（字符串）
	 */
	private String name = "";

	/**
	 * 2、targetNamespace：指定返回值的 XML 名称空间。仅当操作类型为 RPC 或者操作是文档类型并且参数类型为 BARE
	 * 时才使用此参数。（字符串）
	 */
	private String targetNamespace = "";

	/**
	 * 3、header：指定头中是否附带结果。缺省值为false。（布尔值）
	 */
	private boolean header = false;
	/**
	 * 4、partName：指定 RPC 或 DOCUMENT/BARE 操作的结果的部件名称。缺省值为@WebResult.name。（字符串）
	 */
	private String partName = "";
	
	public SoapResult(Class<T> rtClass, String name) {
		this.rtClass = rtClass;
		this.name = name;
	}
	
	public SoapResult(Class<T> rtClass, String name, String targetNamespace, boolean header, String partName) {
		this.rtClass = rtClass;
		this.name = name;
		this.targetNamespace = targetNamespace;
		this.header = header;
		this.partName = partName;
	}

	public Class<T> getRtClass() {
		return rtClass;
	}

	public void setRtClass(Class<T> rtClass) {
		this.rtClass = rtClass;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTargetNamespace() {
		return targetNamespace;
	}

	public void setTargetNamespace(String targetNamespace) {
		this.targetNamespace = targetNamespace;
	}

	public boolean isHeader() {
		return header;
	}

	public void setHeader(boolean header) {
		this.header = header;
	}

	public String getPartName() {
		return partName;
	}

	public void setPartName(String partName) {
		this.partName = partName;
	}

}
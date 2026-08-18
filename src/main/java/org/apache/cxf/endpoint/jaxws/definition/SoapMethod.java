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
 * 注释表示作为一项 Web Service 操作的方法，将此注释应用于客户机或服务器服务端点接口（SEI）上的方法，或者应用于 JavaBeans 端点的服务器端点实现类。
 * 要点： 仅支持在使用 @WebService 注释来注释的类上使用 @WebMethod 注释
 * https://www.cnblogs.com/zhao-shan/p/5515174.html
 */
public class SoapMethod {
    
    public SoapMethod() {
	}
    
    public SoapMethod(String operationName) {
		this.operationName = operationName;
	}
    
	public SoapMethod(String operationName, String action, boolean exclude) {
		this.operationName = operationName;
		this.action = action;
		this.exclude = exclude;
	}

	/**
	 * 1、operationName：指定与此方法相匹配的wsdl:operation 的名称。缺省值为 Java 方法的名称。（字符串）
	 */
	private String operationName = "";

	/**
	 * 2、action：定义此操作的行为。对于 SOAP 绑定，此值将确定 SOAPAction 头的值。缺省值为 Java 方法的名称。（字符串）
	 */
	private String action = "";

	/**
	 * 3、exclude：指定是否从 Web Service 中排除某一方法。缺省值为 false。（布尔值）  
	 */
	private boolean exclude = false;

	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public boolean isExclude() {
		return exclude;
	}

	public void setExclude(boolean exclude) {
		this.exclude = exclude;
	}

}

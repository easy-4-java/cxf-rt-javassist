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
 * 数据绑定对象，用于通过<code>@WebBound</code>注解实现与方法相关数据的绑定
 */
public class SoapBound {
    
    public SoapBound(String uid) {
    	this.uid = uid;
	}
    
	public SoapBound(String uid, String json) {
		this.uid = uid;
		this.json = json;
	}

	/**
	 * 1、uid：某个数据主键，可用于传输主键ID在实现对象中进行数据提取
	 */
	private String uid = "";

	/**
	 * 2、json：绑定的数据对象JSON格式，为了方便，这里采用json进行数据传输
	 */
	private String json = "";

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getJson() {
		return json;
	}

	public void setJson(String json) {
		this.json = json;
	}

}


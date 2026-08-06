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
package org.apache.cxf.endpoint.jaxrs;

import java.lang.reflect.InvocationHandler;
import java.util.Calendar;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import org.apache.cxf.endpoint.EndpointApi;


@Path(value = "/sample")
@Produces("application/json")
public class EndpointApiSample extends EndpointApi {

	public EndpointApiSample() {
	}

	public EndpointApiSample(InvocationHandler handler) {
		super(handler);
	}
	
	@POST
	@Path("add")
	@Consumes
	public Response post(@BeanParam Customer myBean) {
		return Response.status(200).entity("getUserById is called, id : " + myBean.getId()).build(); 
	}

	@GET
	@Path("/{type}/list")
	@Consumes
	public Customer findCustomerByType(@PathParam("type") @DefaultValue("a") String type) {
		Customer customer = new Customer();
		customer.setId(type);
		customer.setName("xiaojing" + type);
		customer.setBirthday(Calendar.getInstance().getTime());
		System.out.println(">>>>>>>>>>>>>服务端信息：" + customer);
		return customer;
	}
	
	@GET
	@Path("/{id}/info")
	@Consumes
	public Customer findCustomerById(@PathParam("id") String id) {
		Customer customer = new Customer();
		customer.setId(id);
		customer.setName("xiaojing" + id);
		customer.setBirthday(Calendar.getInstance().getTime());
		System.out.println(">>>>>>>>>>>>>服务端信息：" + customer);
		return customer;
	}

	@GET
	@Path(value = "/search")
	public Customer findCustomerByName(@QueryParam("name") String name) {
		Customer customer = new Customer();
		customer.setId(name);
		customer.setName(name);
		customer.setBirthday(Calendar.getInstance().getTime());
		System.out.println(">>>>>>>>>>>>>服务端信息：" + customer);
		return customer;
	}
	
}
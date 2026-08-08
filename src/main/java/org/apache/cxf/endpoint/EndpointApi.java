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
package org.apache.cxf.endpoint;

import java.lang.reflect.InvocationHandler;

/**
 * Common base class for dynamically generated JAX-RS and JAX-WS endpoint APIs.
 *
 * <p>Every endpoint API created by the {@code Javassist}-based builders in this
 * module extends this class so that a single {@link InvocationHandler} can be
 * attached at construction time. The handler is later used to dispatch incoming
 * calls to the appropriate backend implementation, effectively turning the
 * generated subclass into a delegating proxy.</p>
 *
 * <p>This class is intentionally simple: it only stores the handler reference
 * and exposes it through {@link #getHandler()}. Concrete subclasses are
 * generated at runtime by
 * {@link org.apache.cxf.endpoint.jaxrs.JaxrsEndpointApiCtClassBuilder},
 * {@link org.apache.cxf.endpoint.jaxws.JaxwsEndpointApiCtClassBuilder} and
 * their related interface / implementation variants.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see InvocationHandler
 * @see org.apache.cxf.endpoint.jaxrs.JaxrsEndpointApiCtClassBuilder
 * @see org.apache.cxf.endpoint.jaxws.JaxwsEndpointApiCtClassBuilder
 */
public abstract class EndpointApi {

    /**
     * Handler invoked for every method call dispatched to the generated
     * endpoint API. May be {@code null} for instances created with the
     * default no-arg constructor; callers must tolerate that case.
     */
	private InvocationHandler handler;

    /**
     * No-argument constructor used by generated subclasses and the
     * reflection-based instantiation paths in the builder helpers.
     */
	public EndpointApi() {
	}

    /**
     * Stores the supplied {@link InvocationHandler} so it can later be
     * retrieved through {@link #getHandler()}.
     *
     * @param handler dispatcher that should receive every method invocation,
     *                may be {@code null}.
     */
	public EndpointApi(InvocationHandler handler) {
		this.handler = handler;
	}

    /**
     * Returns the {@link InvocationHandler} that was passed to the
     * constructor, or {@code null} when this instance was created via the
     * default constructor.
     *
     * @return the stored handler, possibly {@code null}.
     */
	public InvocationHandler getHandler() {
		return handler;
	}


}

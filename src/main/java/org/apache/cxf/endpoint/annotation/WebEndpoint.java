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
package org.apache.cxf.endpoint.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
/**
 * Class-level annotation that captures the deployment address and
 * interceptor / feature configuration for a generated web endpoint.
 *
 * <p>The {@link #addr()} attribute is mandatory and supplies the URL at
 * which the generated endpoint should be exposed. The remaining
 * attributes accept arrays of fully qualified class names that will be
 * instantiated by the runtime as in/out interceptors, fault handlers,
 * features, and generic JAX-WS / CXF handlers. Each list defaults to
 * an empty string so that no extras are wired in by default.</p>
 *
 * <p>This annotation is meant to be declared once per generated endpoint
 * class and is {@link Inherited} so subclasses inherit the same
 * configuration.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 */
public @interface WebEndpoint {

    /**
     * URL where the endpoint will be published.
     *
     * @return the deployment address; never {@code null}.
     */
	String addr();

    /**
     * Fully qualified class names of inbound interceptors to install.
     *
     * @return array of interceptor class names; defaults to a single
     *         empty entry.
     */
	String[] inInterceptors() default {""};

    /**
     * Fully qualified class names of outbound interceptors to install.
     *
     * @return array of interceptor class names; defaults to a single
     *         empty entry.
     */
	String[] outInterceptors() default {""};

    /**
     * Fully qualified class names of inbound fault handlers to install.
     *
     * @return array of fault-handler class names; defaults to a single
     *         empty entry.
     */
	String[] inFaults() default {""};

    /**
     * Fully qualified class names of outbound fault handlers to install.
     *
     * @return array of fault-handler class names; defaults to a single
     *         empty entry.
     */
	String[] outFaults() default {""};

    /**
     * Fully qualified class names of CXF features to enable.
     *
     * @return array of feature class names; defaults to a single empty
     *         entry.
     */
	String[] features() default {""};

    /**
     * Fully qualified class names of generic JAX-WS handlers to install.
     *
     * @return array of handler class names; defaults to a single empty
     *         entry.
     */
	String[] handlers() default {""};

}

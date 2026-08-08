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

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
/**
 * Marker annotation that binds arbitrary contextual data to a generated
 * JAX-RS or JAX-WS endpoint API class or method.
 *
 * <p>The {@link #uid()} attribute carries an opaque key (typically a primary
 * identifier or a token) while {@link #json()} carries an arbitrary JSON
 * payload that the runtime may surface to the implementation. Both
 * attributes default to values that make the annotation effectively
 * inert when no binding is required.</p>
 *
 * <p>The annotation may be placed on a class (to apply to every method of
 * the generated endpoint) or on an individual method. It is
 * {@link Inherited} so that subclasses of a generated endpoint inherit
 * the binding declared on a parent type.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see org.apache.cxf.endpoint.jaxrs.definition.RestBound
 * @see org.apache.cxf.endpoint.jaxws.definition.SoapBound
 */
public @interface WebBound {

    /**
     * Opaque key (usually a primary identifier) used by the runtime to
     * locate contextual data for the bound target.
     *
     * @return the configured uid, or an empty string when not set.
     */
	String uid() default "";

    /**
     * JSON payload attached to the bound target, serialised by the
     * implementation to expose structured metadata.
     *
     * @return the configured JSON payload, or an empty object when not set.
     */
	String json() default "{}";

}

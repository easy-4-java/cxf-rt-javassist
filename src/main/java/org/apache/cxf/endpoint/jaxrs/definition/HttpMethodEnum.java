package org.apache.cxf.endpoint.jaxrs.definition;

import jakarta.ws.rs.HttpMethod;

import java.util.NoSuchElementException;


/**
 * Enumeration of the standard JAX-RS / HTTP verbs that may be declared on a
 * generated REST endpoint method.
 *
 * <p>Each constant carries the canonical name (matching the constant in
 * {@link jakarta.ws.rs.HttpMethod}) so that case-insensitive lookup is
 * possible when parsing incoming requests. Use {@link #valueOfIgnoreCase(String)}
 * to resolve a key regardless of casing.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see jakarta.ws.rs.HttpMethod
 */
public enum HttpMethodEnum {

	/**
     * HTTP GET method.
     */
	GET(HttpMethod.GET),
	/**
     * HTTP POST method.
     */
	POST(HttpMethod.POST),
    /**
     * HTTP PUT method.
     */
    PUT(HttpMethod.PUT),
    /**
     * HTTP DELETE method.
     */
    DELETE(HttpMethod.DELETE),
    /**
     * HTTP PATCH method.
     */
    PATCH(HttpMethod.PATCH),
    /**
     * HTTP HEAD method.
     */
    HEAD(HttpMethod.HEAD),
    /**
     * HTTP OPTIONS method.
     */
    OPTIONS(HttpMethod.OPTIONS);

    /**
     * Canonical verb string (e.g. {@code "GET"}) carried by this enum
     * constant.
     */
	private String key;

	private HttpMethodEnum(String key) {
		this.key = key;
	}

    /**
     * Returns the canonical HTTP verb that backs this enum constant.
     *
     * @return the canonical verb, never {@code null}.
     */
	public String getKey() {
		return key;
	}

    /**
     * Resolves an enum constant by its canonical verb string, ignoring
     * case.
     *
     * @param key the verb to resolve; matched case-insensitively against
     *            {@link #getKey()}.
     * @return the matching {@link HttpMethodEnum} constant.
     * @throws NoSuchElementException if no constant carries the supplied
     *                                verb.
     */
	public static HttpMethodEnum valueOfIgnoreCase(String key) {
		for (HttpMethodEnum apiType : HttpMethodEnum.values()) {
			if(apiType.getKey().equalsIgnoreCase(key)) {
				return apiType;
			}
		}
    	throw new NoSuchElementException("Cannot found ApiType with key '" + key + "'.");
    }

}

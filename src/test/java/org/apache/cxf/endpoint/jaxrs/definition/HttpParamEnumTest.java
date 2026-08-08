package org.apache.cxf.endpoint.jaxrs.definition;

import static org.junit.Assert.*;

import org.junit.Test;

public class HttpParamEnumTest {

    @Test
    public void shouldHaveSevenConstants() {
        assertEquals(7, HttpParamEnum.values().length);
    }

    @Test
    public void shouldContainAllParamTypes() {
        assertNotNull(HttpParamEnum.BEAN);
        assertNotNull(HttpParamEnum.COOKIE);
        assertNotNull(HttpParamEnum.HEADER);
        assertNotNull(HttpParamEnum.MATRIX);
        assertNotNull(HttpParamEnum.FORM);
        assertNotNull(HttpParamEnum.PATH);
        assertNotNull(HttpParamEnum.QUERY);
    }

    @Test
    public void shouldResolveByName() {
        assertSame(HttpParamEnum.BEAN, HttpParamEnum.valueOf("BEAN"));
        assertSame(HttpParamEnum.COOKIE, HttpParamEnum.valueOf("COOKIE"));
        assertSame(HttpParamEnum.HEADER, HttpParamEnum.valueOf("HEADER"));
        assertSame(HttpParamEnum.MATRIX, HttpParamEnum.valueOf("MATRIX"));
        assertSame(HttpParamEnum.FORM, HttpParamEnum.valueOf("FORM"));
        assertSame(HttpParamEnum.PATH, HttpParamEnum.valueOf("PATH"));
        assertSame(HttpParamEnum.QUERY, HttpParamEnum.valueOf("QUERY"));
    }
}

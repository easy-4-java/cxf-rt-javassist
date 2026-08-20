package org.apache.cxf.endpoint.jaxrs.definition;

import static org.junit.Assert.*;

import org.junit.Test;

public class RestParamTest {

    @Test
    public void shouldCreateParamWithTypeAndName() {
        RestParam<String> param = new RestParam<>(String.class, "id");
        assertEquals(String.class, param.getType());
        assertEquals("id", param.getName());
        assertEquals(HttpParamEnum.QUERY, param.getFrom());
        assertNull(param.getDef());
    }

    @Test
    public void shouldCreateParamWithExplicitFrom() {
        RestParam<String> param = new RestParam<>(String.class, "id", HttpParamEnum.PATH);
        assertEquals(String.class, param.getType());
        assertEquals("id", param.getName());
        assertEquals(HttpParamEnum.PATH, param.getFrom());
    }

    @Test
    public void shouldCreateParamWithDefault() {
        RestParam<String> param = new RestParam<>(String.class, "name", "defaultVal");
        assertEquals(String.class, param.getType());
        assertEquals("name", param.getName());
        assertEquals("defaultVal", param.getDef());
    }

    @Test
    public void shouldCreateParamWithFromAndDefault() {
        RestParam<String> param = new RestParam<>(String.class, "name", HttpParamEnum.PATH, "defaultVal");
        assertEquals(String.class, param.getType());
        assertEquals("name", param.getName());
        assertEquals(HttpParamEnum.PATH, param.getFrom());
        assertEquals("defaultVal", param.getDef());
    }

    @Test
    public void shouldAllowTypeOverride() {
        RestParam<String> param = new RestParam<>(String.class, "val");
        param.setType(String.class);
        assertEquals(String.class, param.getType());
    }

    @Test
    public void shouldAllowNameOverride() {
        RestParam<String> param = new RestParam<>(String.class, "old");
        param.setName("new");
        assertEquals("new", param.getName());
    }

    @Test
    public void shouldAllowFromOverride() {
        RestParam<String> param = new RestParam<>(String.class, "id");
        param.setFrom(HttpParamEnum.HEADER);
        assertEquals(HttpParamEnum.HEADER, param.getFrom());
    }

    @Test
    public void shouldAllowDefOverride() {
        RestParam<String> param = new RestParam<>(String.class, "id");
        param.setDef("abc");
        assertEquals("abc", param.getDef());
    }
}

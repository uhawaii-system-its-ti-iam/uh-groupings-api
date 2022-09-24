package edu.hawaii.its.api.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;

public class ResponseCodeTest {

    @Test
    public void basics() {
        assertThat(ResponseCode.FAILURE, is(notNullValue()));
        assertThat(ResponseCode.SUCCESS, is(notNullValue()));
        assertThat(ResponseCode.SUCCESS_WASNT_IMMEDIATE, is(notNullValue()));

        assertThat(ResponseCode.FAILURE, equalTo("FAILURE"));
    }

    @Test
    public void constructorIsPrivate() throws Exception {
        Constructor<ResponseCode> constructor = ResponseCode.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}

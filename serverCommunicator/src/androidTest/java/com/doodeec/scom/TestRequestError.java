package com.doodeec.scom;

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by dusan.bartos on 5.2.2015.
 */
public class TestRequestError {

    @Test
    public void testRequestError_string() {
        RequestError error = new RequestError("This is some random test string");

        assertThat(error.getMessage(), is("This is some random test string"));
    }

    @Test
    public void testRequestError_exception() {
        IOException exc = new IOException("This is some other random test string");
        RequestError error = new RequestError(exc);

        assertThat(error.getMessage(), is("This is some other random test string"));
    }
}

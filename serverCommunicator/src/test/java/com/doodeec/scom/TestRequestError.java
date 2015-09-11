package com.doodeec.utils.network;

import org.junit.Test;

import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class TestRequestError {

    @Test
    public void testRequestError_string() {
        RequestError error = new RequestError("Request unable to complete");

        assertThat(error.getMessage(), is("Request unable to complete"));
    }

    @Test
    public void testRequestError_exception() {
        IOException exc = new IOException("Request not executable");
        RequestError error = new RequestError(exc);

        assertThat(error.getMessage(), is("Request not executable"));
    }

    @Test
    public void testRequestError_exception_types() {
        SocketTimeoutException socket = new SocketTimeoutException();
        assertThat(new RequestError(socket).getErrorType(), is(ErrorType.SocketTimeout));

        MalformedURLException url = new MalformedURLException();
        assertThat(new RequestError(url).getErrorType(), is(ErrorType.MalformedUrl));

        ConnectException connect = new ConnectException();
        assertThat(new RequestError(connect).getErrorType(), is(ErrorType.Connect));

        IOException io = new IOException();
        assertThat(new RequestError(io).getErrorType(), is(ErrorType.IO));

        Exception ex = new Exception();
        assertThat(new RequestError(ex).getErrorType(), is(ErrorType.Other));

        assertThat(new RequestError("Error message").getErrorType(), is(ErrorType.Custom));
    }
}

package com.doodeec.scom;

import org.junit.Test;

import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class TestServerRequest {

    @Test
    public void testServerRequest_charset() throws Exception {
        ServerRequest.setResponseCharset("ASCII");
        ServerRequest.setResponseCharset("US-ASCII");
        ServerRequest.setResponseCharset("utf-8");
        ServerRequest.setResponseCharset("utf-16");
        ServerRequest.setResponseCharset("utf-16be");
        ServerRequest.setResponseCharset("utf-16le");
        ServerRequest.setResponseCharset("cp1250");
        ServerRequest.setResponseCharset("cp852");
        ServerRequest.setResponseCharset("iso-8859-1");
        ServerRequest.setResponseCharset("iso-8859-2");

        try {
            ServerRequest.setResponseCharset("abc");
            fail("Test should fail with unsupported charset exception");
        } catch (Exception e) {
            if (e instanceof UnsupportedCharsetException || e instanceof IllegalCharsetNameException) {
                // asserts that exception is not thrown
                assertThat(1, is(1));
            } else {
                fail("Unexpected exception");
            }
        }
    }
}

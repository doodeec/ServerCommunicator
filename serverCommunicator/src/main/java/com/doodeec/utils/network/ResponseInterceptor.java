package com.doodeec.utils.network;

/**
 * @author Dusan Bartos
 */
public interface ResponseInterceptor {

    /**
     * Called after response code is available
     * @param statusCode response code
     * @return true if request should be interrupted, false if request should continue as expected
     */
    boolean onProcessStatus(int statusCode);
}

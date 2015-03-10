package com.doodeec.scom;

import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

/**
 * Server request error
 *
 * @author dusan.bartos
 */
public class RequestError {

    private String mRequestUrl;
    private Exception mException;
    private String mErrorMessage;
    private ErrorType mType;

    /**
     * Creates Request error from thrown Exception
     * Error type is associated from the class of Exception
     *
     * @param e   exception
     * @param url request url
     */
    public RequestError(Exception e, String url) {
        mException = e;
        mRequestUrl = url;

        if (e instanceof MalformedURLException) {
            mType = ErrorType.MalformedUrl;
        } else if (e instanceof ConnectException) {
            mType = ErrorType.Connect;
        } else if (e instanceof SocketTimeoutException) {
            mType = ErrorType.SocketTimeout;
        } else if (e instanceof IOException) {
            mType = ErrorType.IO;
        } else {
            mType = ErrorType.Other;
        }
    }

    /**
     * Creates Request error of type {@link com.doodeec.scom.ErrorType#Custom} with custom message
     *
     * @param message error message
     * @param url     request url
     */
    public RequestError(String message, String url) {
        mErrorMessage = message;
        mRequestUrl = url;
        mType = ErrorType.Custom;
    }

    /**
     * Gets {@link com.doodeec.scom.ErrorType} of this error
     *
     * @return type
     */
    public ErrorType getErrorType() {
        return mType;
    }

    /**
     * Gets error message
     *
     * @return message
     */
    public String getMessage() {
        return mException == null ? mErrorMessage :
                mErrorMessage != null ? mErrorMessage : mException.getLocalizedMessage();
    }

    /**
     * Gets request url
     *
     * @return url
     */
    public String getRequestUrl() {
        return mRequestUrl;
    }
}

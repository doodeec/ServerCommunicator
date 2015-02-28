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

    private Exception mException;
    private String mErrorMessage;
    private ErrorType mType;

    /**
     * Creates Request error from thrown Exception
     * Error type is associated from the class of Exception
     *
     * @param e exception
     */
    public RequestError(Exception e) {
        mException = e;

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
     */
    public RequestError(String message) {
        mErrorMessage = message;
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
}

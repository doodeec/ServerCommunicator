package com.doodeec.scom;

/**
 * Server request error
 *
 * @author dusan.bartos
 */
public class RequestError {

    private Exception mException;
    private String mErrorMessage;
    private ErrorType mType;

    public RequestError(Exception e) {
        mException = e;
    }

    public RequestError(String message) {
        mErrorMessage = message;
    }

    public String getMessage() {
        return mException == null ? mErrorMessage : mException.getLocalizedMessage();
    }
}

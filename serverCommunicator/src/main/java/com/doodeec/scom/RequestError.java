package com.doodeec.scom;

/**
 * Created by Dusan Doodeec Bartos on 21.12.2014.
 *
 * Server request error
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

package com.doodeec.utils.network;

/**
 * Response from {@link BaseServerRequest} class
 *
 * @author Dusan Bartos
 */
@SuppressWarnings("unused")
public class CommunicatorResponse<ReturnType> {
    private boolean mCancelled = false;
    private boolean mIntercepted = false;
    private boolean mHasError = false;
    private int mStatusCode;
    private ReturnType mResponseData;
    private String mUrl;
    private RequestError mError;

    protected CommunicatorResponse() {

    }

    protected void setError(RequestError requestError) {
        mError = requestError;
        mHasError = requestError != null;
        mIntercepted = requestError != null && requestError.equals(RequestError.INTERCEPT);
    }

    protected void setUrl(String url) {
        mUrl = url;
    }

    protected void setData(ReturnType data) {
        mResponseData = data;
    }

    protected void setStatusCode(int statusCode) {
        mStatusCode = statusCode;
    }

    public boolean isIntercepted() {
        return mIntercepted;
    }

    public boolean hasError() {
        return mHasError;
    }

    public int getStatusCode() {
        return mStatusCode;
    }

    public String getUrl() {
        return mUrl;
    }

    public ReturnType getData() {
        return mResponseData;
    }

    public RequestError getError() {
        return mError;
    }

    public boolean isCancelled() {
        return mCancelled;
    }

    public void setCancelled(boolean cancelled) {
        mCancelled = cancelled;
    }
}

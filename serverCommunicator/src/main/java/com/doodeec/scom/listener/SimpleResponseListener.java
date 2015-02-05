package com.doodeec.scom.listener;

import com.doodeec.scom.RequestError;

/**
 * Created by Dusan Bartos on 29.12.2014.
 *
 * Response listener
 * Intended to be used in network service, when request listener is processed from raw data to
 * typed data
 */
@SuppressWarnings("unused")
public interface SimpleResponseListener<ResponseType> {
    /**
     * Fired when response is successfully parsed to given responseType
     *
     * @param response response object
     */
    void onSuccess(ResponseType response);

    /**
     * Fired when error occurred either in asyncTask execution or data parsing
     *
     * @param error error
     */
    void onError(RequestError error);
}

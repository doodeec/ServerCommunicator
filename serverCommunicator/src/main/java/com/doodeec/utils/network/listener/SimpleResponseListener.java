package com.doodeec.utils.network.listener;

import com.doodeec.utils.network.RequestError;

/**
 * Response listener
 * Intended to be used in network service, when request listener is processed from raw data to
 * typed data
 *
 * @author dusan.bartos
 */
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

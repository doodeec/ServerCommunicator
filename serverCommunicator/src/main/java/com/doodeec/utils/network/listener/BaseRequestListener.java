package com.doodeec.utils.network.listener;

import com.doodeec.utils.network.RequestError;

/**
 * Base generic Request listener called by {@link com.doodeec.utils.network.BaseServerRequest}
 *
 * @author dusan.bartos
 */
public interface BaseRequestListener<ResponseType> {

    void onError(RequestError error);

    void onSuccess(ResponseType response);

    void onCancelled();

    void onProgress(Integer progress);
}

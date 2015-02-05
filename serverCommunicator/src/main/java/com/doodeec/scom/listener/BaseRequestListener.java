package com.doodeec.scom.listener;

import com.doodeec.scom.RequestError;

/**
 * @author dusan.bartos
 */
public interface BaseRequestListener<ResponseType> {

    void onError(RequestError error);

    void onSuccess(ResponseType response);

    void onCancelled();

    void onProgress(Integer progress);
}

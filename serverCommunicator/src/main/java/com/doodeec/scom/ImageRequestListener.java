package com.doodeec.scom;

import android.graphics.Bitmap;

/**
 * Created by Dusan Doodeec Bartos on 21.12.2014.
 *
 * Server request listener
 * Every listener is supposed to implement own {@link #onError(com.doodeec.scom.RequestError)} method
 *
 * Every listener should also implement (override) one of onSuccess methods
 * {@link #onSuccess(android.graphics.Bitmap)}
 *
 */
@SuppressWarnings("unused")
public abstract class ImageRequestListener {

    abstract public void onError(RequestError error);

    public void onSuccess(Bitmap image) {
        onError(new RequestError("OnSuccess(Bitmap) not implemented"));
    }

    public void onCancelled() {
    }

    public void onProgress(Integer progress) {
    }
}

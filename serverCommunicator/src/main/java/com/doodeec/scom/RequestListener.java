package com.doodeec.scom;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Dusan Doodeec Bartos on 21.12.2014.
 *
 * Server request listener
 * Every listener is supposed to implement own {@link #onError(RequestError)} method
 *
 * Every listener should also implement (override) one of onSuccess methods
 * {@link #onSuccess(org.json.JSONObject)} or {@link #onSuccess(org.json.JSONArray)}
 *
 */
@SuppressWarnings("unused")
public abstract class RequestListener {

    abstract public void onError(RequestError error);

    public void onSuccess() {
        onError(new RequestError("OnSuccess() not implemented"));
    }

    public void onSuccess(JSONObject object) {
        onError(new RequestError("OnSuccess(JSONObject) not implemented"));
    }

    public void onSuccess(JSONArray array) {
        onError(new RequestError("OnSuccess(JSONArray) not implemented"));
    }

    public void onCancelled() {
    }

    public void onProgress(Integer progress) {
    }
}

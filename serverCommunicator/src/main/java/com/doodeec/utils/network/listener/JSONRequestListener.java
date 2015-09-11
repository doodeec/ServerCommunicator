package com.doodeec.utils.network.listener;

import com.doodeec.utils.network.RequestError;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Server request listener
 * Every listener is supposed to implement own {@link #onError(com.doodeec.utils.network.RequestError)} method
 *
 * Every listener should also implement (override) one of onSuccess methods
 * {@link #onSuccess(org.json.JSONObject)} or {@link #onSuccess(org.json.JSONArray)}
 *
 * @author dusan.bartos
 */
public abstract class JSONRequestListener implements BaseRequestListener<JSONObject> {

    public void onSuccess() {
        onError(new RequestError("OnSuccess() not implemented", getClass().getSimpleName()));
    }

    public void onSuccess(JSONObject object) {
        onError(new RequestError("OnSuccess(JSONObject) not implemented", getClass().getSimpleName()));
    }

    public void onSuccess(JSONArray array) {
        onError(new RequestError("OnSuccess(JSONArray) not implemented", getClass().getSimpleName()));
    }
}

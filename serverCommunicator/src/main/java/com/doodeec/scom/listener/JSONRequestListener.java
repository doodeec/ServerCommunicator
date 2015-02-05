package com.doodeec.scom.listener;

import com.doodeec.scom.RequestError;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author dusan.bartos
 *
 *         Server request listener
 *         Every listener is supposed to implement own {@link #onError(com.doodeec.scom.RequestError)} method
 *
 *         Every listener should also implement (override) one of onSuccess methods
 *         {@link #onSuccess(org.json.JSONObject)} or {@link #onSuccess(org.json.JSONArray)}
 */
@SuppressWarnings("unused")
public abstract class JSONRequestListener implements BaseRequestListener<JSONObject> {

    public void onSuccess() {
        onError(new RequestError("OnSuccess() not implemented"));
    }

    public void onSuccess(JSONObject object) {
        onError(new RequestError("OnSuccess(JSONObject) not implemented"));
    }

    public void onSuccess(JSONArray array) {
        onError(new RequestError("OnSuccess(JSONArray) not implemented"));
    }
}

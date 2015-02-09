package com.doodeec.scom;

import com.doodeec.scom.listener.JSONRequestListener;

import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * Backend Server request
 * Wrapper around {@link java.net.HttpURLConnection}
 * Can be executed with {@link #THREAD_POOL_EXECUTOR} to evaluate requests in parallel
 *
 * @author dusan.bartos
 * @see com.doodeec.scom.listener.JSONRequestListener
 * @see com.doodeec.scom.RequestError
 */
public class ServerRequest extends BaseServerRequest<String> {

    // response headers
    private static final String REQ_CONTENT_TYPE_KEY = HTTP.CONTENT_TYPE;
    private static final String REQ_CONTENT_TYPE_VALUE = "application/json; charset=utf-8";

    public ServerRequest(RequestType type, JSONRequestListener listener) {
        super(type, listener);
    }

    public ServerRequest(RequestType type, String data, JSONRequestListener listener) {
        super(type, data, listener);
    }

    @Override
    protected void initHeaders() {
        mRequestHeaders.put(REQ_CONTENT_TYPE_KEY, REQ_CONTENT_TYPE_VALUE);
    }

    @Override
    protected String processInputStream(String contentType, InputStream inputStream) {
        try {
            int ch;
            StringBuilder sb = new StringBuilder();
            while ((ch = inputStream.read()) != -1) {
                sb.append((char) ch);
            }
            return sb.toString();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(String responseString) {
        if (responseString != null) {
            // POST can have empty response, but 200 response code
            if (mType.equals(RequestType.POST) && (responseString.equals("") || responseString.equals("OK"))) {
                ((JSONRequestListener) mListener).onSuccess();
            } else {
                try {
                    // propagate json object to listener
                    ((JSONRequestListener) mListener).onSuccess(new JSONObject(responseString));
                } catch (JSONException e) {
                    // not a json object
                    try {
                        // propagate json array to listener
                        ((JSONRequestListener) mListener).onSuccess(new JSONArray(responseString));
                    } catch (JSONException arrayException) {
                        // not a json array, not a json object
                        mListener.onError(new RequestError("Response cannot be parsed to neither JSONObject or JSONArray"));
                    }
                }
            }
        } else if (mRequestError != null) {
            mListener.onError(mRequestError);
        } else {
            mListener.onError(new RequestError("Response string empty"));
        }
    }
}

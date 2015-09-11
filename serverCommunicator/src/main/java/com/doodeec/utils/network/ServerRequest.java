package com.doodeec.utils.network;

import android.util.Log;

import com.doodeec.utils.network.listener.BaseRequestListener;
import com.doodeec.utils.network.listener.JSONRequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

/**
 * Server request
 * {@link android.os.AsyncTask} wrapper around {@link java.net.HttpURLConnection}
 * Can be executed with {@link #THREAD_POOL_EXECUTOR} to evaluate requests in parallel
 *
 * @author dusan.bartos
 * @see com.doodeec.utils.network.listener.BaseRequestListener
 * @see com.doodeec.utils.network.listener.JSONRequestListener
 * @see com.doodeec.utils.network.ErrorType
 * @see com.doodeec.utils.network.RequestError
 */
@SuppressWarnings("unused")
public class ServerRequest extends BaseServerRequest<String> {

    // response headers
    private static final String REQ_CONTENT_TYPE_KEY = "Content-Type";
    private static final String REQ_CONTENT_TYPE_VALUE = "application/json";

    // default response charset
    private static Charset mCharset = Charset.forName("UTF-8");

    /**
     * Creates server request of given type
     *
     * @see com.doodeec.utils.network.BaseServerRequest#BaseServerRequest(com.doodeec.utils.network.RequestType, com.doodeec.utils.network.listener.BaseRequestListener)
     */
    public ServerRequest(RequestType type, JSONRequestListener listener) {
        super(type, listener);
    }

    /**
     * Creates server request with POST data (payload)
     *
     * @see com.doodeec.utils.network.BaseServerRequest#BaseServerRequest(com.doodeec.utils.network.RequestType, String, com.doodeec.utils.network.listener.BaseRequestListener)
     */
    public ServerRequest(RequestType type, String data, JSONRequestListener listener) {
        super(type, data, listener);
    }

    /**
     * Helper for cloning request
     */
    private ServerRequest(RequestType type, String data, BaseRequestListener listener) {
        super(type, data, listener);
    }

    /**
     * Globally sets response charset
     * It is required only once, since charset is static
     * *
     * Charset can be:
     * - ASCII
     * - US-ASCII
     * - utf-8
     * - utf-16
     * - utf-16be
     * - utf-16le
     * - cp1250
     * - cp852
     * - iso-8859-1
     * - iso-8859-2
     *
     * @param canonicalName charset name
     *
     * @throws IllegalCharsetNameException
     * @throws UnsupportedCharsetException
     */
    public static void setResponseCharset(String canonicalName) throws IllegalCharsetNameException,
            UnsupportedCharsetException {
        mCharset = Charset.forName(canonicalName);
    }

    @Override
    protected void initHeaders() {
        mRequestHeaders.put(REQ_CONTENT_TYPE_KEY, REQ_CONTENT_TYPE_VALUE);
    }

    @Override
    protected String processInputStream(String contentType, InputStream inputStream) {
        try {
            InputStreamReader streamReader = new InputStreamReader(inputStream, mCharset.name());
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[2048];
            int charsRead;
            while ((charsRead = streamReader.read(buf, 0, 2048)) > 0) {
                sb.append(buf, 0, charsRead);
            }

            return sb.toString();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(CommunicatorResponse<String> response) {
        if (response.isIntercepted()) {
            //do nothing
            Log.d(getClass().getSimpleName(), "Response intercepted. Not proceeding to response listener");
        } else if (response.hasError()) {
            mListener.onError(response.getError());
        } else if (response.getData() != null) {
            // POST/PUT can have empty response, but 200 response code
            if ((mType.equals(RequestType.POST) || mType.equals(RequestType.PUT)) &&
                    (response.getData().equals("") || response.getData().equals("OK"))) {
                ((JSONRequestListener) mListener).onSuccess();
            } else {
                try {
                    // propagate json object to listener
                    ((JSONRequestListener) mListener).onSuccess(new JSONObject(response.getData()));
                } catch (JSONException e) {
                    // not a json object
                    try {
                        // propagate json array to listener
                        ((JSONRequestListener) mListener).onSuccess(new JSONArray(response.getData()));
                    } catch (JSONException arrayException) {
                        // not a json array, not a json object
                        mListener.onError(new RequestError("Response cannot be parsed to neither JSONObject or JSONArray", null));
                    }
                }
            }
        } else {
            mListener.onError(new RequestError("Response empty", null));
        }
    }

    @Override
    public ServerRequest cloneRequest() {
        ServerRequest clonedRequest = new ServerRequest(mType, mPostData, mListener);
        clonedRequest.mTimeout = mTimeout;
        clonedRequest.mReadTimeout = mReadTimeout;
        clonedRequest.mRequestHeaders = mRequestHeaders;
        return clonedRequest;
    }
}

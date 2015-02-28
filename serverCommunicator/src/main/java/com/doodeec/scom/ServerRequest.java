package com.doodeec.scom;

import com.doodeec.scom.listener.JSONRequestListener;

import org.apache.http.protocol.HTTP;
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
 * @see com.doodeec.scom.listener.BaseRequestListener
 * @see com.doodeec.scom.listener.JSONRequestListener
 * @see com.doodeec.scom.ErrorType
 * @see com.doodeec.scom.RequestError
 */
public class ServerRequest extends BaseServerRequest<String> {

    // response headers
    private static final String REQ_CONTENT_TYPE_KEY = HTTP.CONTENT_TYPE;
    private static final String REQ_CONTENT_TYPE_VALUE = "application/json; charset=utf-8";

    // default response charset
    private static Charset mCharset = Charset.forName("UTF-8");

    /**
     * Creates server request of given type
     *
     * @see com.doodeec.scom.BaseServerRequest#BaseServerRequest(com.doodeec.scom.RequestType, com.doodeec.scom.listener.BaseRequestListener)
     */
    public ServerRequest(RequestType type, JSONRequestListener listener) {
        super(type, listener);
    }

    /**
     * Creates server request with POST data (payload)
     *
     * @see com.doodeec.scom.BaseServerRequest#BaseServerRequest(com.doodeec.scom.RequestType, String, com.doodeec.scom.listener.BaseRequestListener)
     */
    public ServerRequest(RequestType type, String data, JSONRequestListener listener) {
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
            int ch;
            InputStreamReader streamReader = new InputStreamReader(inputStream, mCharset.name());
            StringBuilder sb = new StringBuilder();
            while ((ch = streamReader.read()) != -1) {
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

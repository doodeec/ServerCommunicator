package com.doodeec.utils.network;

import android.util.Log;

import com.doodeec.utils.network.listener.BaseRequestListener;
import com.doodeec.utils.network.listener.GSONRequestListener;
import com.doodeec.utils.network.listener.JSONRequestListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

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
public class ServerRequest<LT> extends BaseServerRequest<String> {

    // response headers
    private static final String REQ_CONTENT_TYPE_KEY = "Content-Type";
    private static final String REQ_CONTENT_TYPE_VALUE = "application/json";

    // default response charset
    private static Charset sCharset = Charset.forName("UTF-8");

    private static Gson sGsonConverter = new GsonBuilder().create();

    public static void setGsonConverter(Gson gsonConverter) {
        sGsonConverter = gsonConverter;
    }

    /**
     * Request listener
     *
     * @see com.doodeec.utils.network.listener.JSONRequestListener
     */
    protected BaseRequestListener<LT> mListener;
    /**
     * Class type of GSON response
     */
    private Class<LT> mGsonClass;
    /**
     * Flag if GSON converter should be used
     */
    private boolean mUseGson = false;

    public ServerRequest(RequestType type, GSONRequestListener<LT> listener, Class<LT> cls) {
        this(type, listener);
        mGsonClass = cls;
        mUseGson = true;
    }

    public ServerRequest(RequestType type, String data, GSONRequestListener<LT> listener, Class<LT> cls) {
        this(type, data, listener);
        mGsonClass = cls;
        mUseGson = true;
    }

    public ServerRequest(RequestType type, BaseRequestListener<LT> listener) {
        super(type);
        mListener = listener;
    }

    public ServerRequest(RequestType type, String data, BaseRequestListener<LT> listener) {
        super(type, data);
        mListener = listener;
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
        sCharset = Charset.forName(canonicalName);
    }

    @Override
    protected void initHeaders() {
        mRequestHeaders.put(REQ_CONTENT_TYPE_KEY, REQ_CONTENT_TYPE_VALUE);
    }

    @Override
    protected String processInputStream(String contentType, InputStream inputStream) {
        try {
            InputStreamReader streamReader = new InputStreamReader(inputStream, sCharset.name());
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
            if (sDebugEnabled) {
                Log.d(getClass().getSimpleName(), "Response intercepted. Not proceeding to response listener");
            }
        } else if (response.hasError()) {
            mListener.onError(response.getError());
        } else if (response.getData() != null) {
            if (mUseGson) {
                try {
                    mListener.onSuccess(sGsonConverter.fromJson(response.getData(), mGsonClass));
                } catch (JsonSyntaxException e) {
                    if (sDebugEnabled) {
                        e.printStackTrace();
                    }
                    mListener.onError(new RequestError(e.getMessage(), response.getUrl()));
                }
            } else {
                try {
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
                                mListener.onError(new RequestError("Response cannot be parsed to neither JSONObject or JSONArray", response.getUrl()));
                            }
                        }
                    }
                } catch (ClassCastException e) {
                    if (sDebugEnabled) {
                        e.printStackTrace();
                    }
                    mListener.onError(new RequestError(e.getMessage(), response.getUrl()));
                }
            }
        } else {
            mListener.onError(new RequestError("Response empty", response.getUrl()));
        }
    }

    @Override
    public ServerRequest<LT> cloneRequest() {
        ServerRequest<LT> clonedRequest = new ServerRequest<>(mType, mPostData, mListener);
        clonedRequest.mTimeout = mTimeout;
        clonedRequest.mReadTimeout = mReadTimeout;
        clonedRequest.mRequestHeaders = mRequestHeaders;
        return clonedRequest;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        mListener.onProgress(values[0]);
    }

    @Override
    protected void onCancelled() {
        mListener.onCancelled();
    }
}

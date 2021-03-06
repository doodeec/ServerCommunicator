package com.doodeec.utils.network;

import android.util.Log;

import com.doodeec.utils.network.listener.BaseRequestListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

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
public class ServerRequest<LT> extends BaseServerRequest<LT, String> {

    // response headers
    private static final String REQ_CONTENT_TYPE_KEY = "Content-Type";
    private static final String REQ_CONTENT_TYPE_VALUE = "application/json";

    /**
     * response charset
     *
     * @see #setResponseCharset(String)
     */
    private static Charset sCharset = Charset.forName("UTF-8");

    /**
     * GSON converter
     * uses default converter by default, can be changed via {@link #setGsonConverter(Gson)}
     */
    private static Gson sGsonConverter = new GsonBuilder().create();

    /**
     * Buffer size used for reading input stream from response
     *
     * @see #setBufferSize(int)
     */
    private static int sCharBufferSize = 2048;

    /**
     * Sets custom GSON converter
     *
     * @param gsonConverter custom converter
     */
    public static void setGsonConverter(Gson gsonConverter) {
        if (gsonConverter == null) return;
        sGsonConverter = gsonConverter;
    }

    /**
     * Sets buffer size
     *
     * @param size size of the buffer
     */
    public static void setBufferSize(int size) {
        sCharBufferSize = size;
    }

    /**
     * Response listener
     *
     * @see com.doodeec.utils.network.listener.BaseRequestListener
     */
    protected BaseRequestListener<LT> mListener;

    /**
     * Class of the response
     * Used in GSON parser
     */
    private Class<LT> mGsonClass;

    /**
     * Constructs ServerRequest
     *
     * @param type     type of request {@link RequestType}
     * @param listener response listener
     * @param cls      class of response object
     */
    public ServerRequest(RequestType type, BaseRequestListener<LT> listener, Class<LT> cls) {
        super(type);
        mListener = listener;
        mGsonClass = cls;
    }

    /**
     * Constructs ServerRequest with payload data
     *
     * @param type     type of request {@link RequestType}
     * @param data     payload data
     * @param listener response listener
     * @param cls      class of response object
     */
    public ServerRequest(RequestType type, String data, BaseRequestListener<LT> listener, Class<LT> cls) {
        super(type, data);
        mListener = listener;
        mGsonClass = cls;
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
            char[] buf = new char[sCharBufferSize];
            int charsRead;
            while ((charsRead = streamReader.read(buf, 0, sCharBufferSize)) > 0) {
                sb.append(buf, 0, charsRead);
            }

            return sb.toString();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected LT instantiateStream(String s) throws JsonSyntaxException {
        return sGsonConverter.fromJson(s, mGsonClass);
    }

    @Override
    protected void onPostExecute(CommunicatorResponse<LT> response) {
        if (response.isIntercepted()) {
            //do nothing
            if (sDebugEnabled) {
                Log.d(getClass().getSimpleName(), "Response intercepted. Not proceeding to response listener");
            }
        } else if (response.hasError()) {
            mListener.onError(response.getError());
        } else if (response.getData() != null) {
            mListener.onSuccess(response.getData());
        } else if (isStatusOk(response.getStatusCode())) {
            mListener.onSuccess(null);
        } else {
            mListener.onError(new RequestError("Response empty", response.getUrl()));
        }
    }

    @Override
    public ServerRequest<LT> cloneRequest() {
        ServerRequest<LT> clonedRequest = new ServerRequest<>(mType, mPostData, mListener, mGsonClass);
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

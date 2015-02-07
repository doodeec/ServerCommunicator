package com.doodeec.scom;

import android.os.AsyncTask;

import com.doodeec.scom.listener.BaseRequestListener;

import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * @author dusan.bartos
 */
@SuppressWarnings("unused")
public abstract class BaseServerRequest<ReturnType> extends
        AsyncTask<String, Integer, ReturnType> implements CancellableServerRequest {

    // asyncTask progress
    private static final int PROGRESS_IDLE = 0;
    private static final int PROGRESS_OPENED = 10;
    private static final int PROGRESS_CONNECTED = 20;
    private static final int PROGRESS_RESPONSE_CODE = 40;
    private static final int PROGRESS_RESPONSE_TYPE = 50;
    private static final int PROGRESS_CONTENT = 70;
    private static final int PROGRESS_CONNECTION_CLOSE = 80;
    private static final int PROGRESS_DISCONNECTING = 90;
    private static final int PROGRESS_DONE = 100;

    // response headers
    protected static final String REQ_CHARSET_KEY = "Accept-Charset";
    protected static final String REQ_ENCODING_KEY = "Accept-Encoding";
    protected static final String REQ_ENCODING_VALUE = "gzip";

    private static final String ENCODING_KEY = "Content-Encoding";

    public static enum RequestType {
        GET("GET"),
        POST("POST"),
        PUT("PUT"),
        DELETE("DELETE");

        private String value;

        private RequestType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * Post data to add to request body
     */
    protected String mPostData;

    /**
     * Connection timeout
     * default to 30sec
     */
    protected int mTimeout = 30000;

    /**
     * Additional request headers
     */
    protected Map<String, String> mRequestHeaders = new HashMap<>();

    /**
     * Request type
     * allowed values are
     * {@link com.doodeec.scom.ServerRequest.RequestType#GET}
     * {@link com.doodeec.scom.ServerRequest.RequestType#POST}
     * {@link com.doodeec.scom.ServerRequest.RequestType#PUT}
     * {@link com.doodeec.scom.ServerRequest.RequestType#DELETE}
     */
    protected RequestType mType;

    /**
     * Request listener
     *
     * @see com.doodeec.scom.listener.JSONRequestListener
     */
    protected BaseRequestListener mListener;

    /**
     * Request Error
     * Holds the reason why request was not successful
     */
    protected RequestError mRequestError;

    protected BaseServerRequest(RequestType type, BaseRequestListener listener) {
        mType = type;
        mListener = listener;

        initHeaders();
    }

    protected BaseServerRequest(RequestType type, String data, BaseRequestListener listener) {
        this(type, listener);
        mPostData = data;
    }

    /**
     * Sets connection timeout
     *
     * @param timeout timeout in milliseconds
     */
    public void setTimeout(int timeout) {
        mTimeout = timeout;
    }

    /**
     * Sets additional headers
     * Headers are added to existing set
     *
     * @param headersMap headers
     */
    public void setHeaders(Map<String, String> headersMap) {
        mRequestHeaders.putAll(headersMap);
    }

    /**
     * Clears all headers
     */
    public void clearHeaders() {
        mRequestHeaders.clear();
    }

    /**
     * Can be used to define default headers for the class
     *
     * @see #BaseServerRequest(RequestType, com.doodeec.scom.listener.BaseRequestListener)
     */
    protected void initHeaders() {

    }

    @Override
    protected ReturnType doInBackground(String... params) {
        URL url;
        HttpURLConnection connection;
        String response;

        publishProgress(PROGRESS_IDLE);

        try {
            url = new URL(params[0]);
        } catch (MalformedURLException e) {
            //IO exception
            mRequestError = new RequestError("Cannot read target URL");
            return null;
        }

        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            //IO exception
            mRequestError = new RequestError("Cannot open connection");
            return null;
        }

        publishProgress(PROGRESS_OPENED);

        // set connection timeouts
        connection.setConnectTimeout(mTimeout);
        connection.setReadTimeout(mTimeout);

        // set connection header properties
        connection.setRequestProperty(REQ_CHARSET_KEY, HTTP.UTF_8);
        connection.setRequestProperty(REQ_ENCODING_KEY, REQ_ENCODING_VALUE);

        // set additional request headers
        if (mRequestHeaders != null) {
            for (String property : mRequestHeaders.keySet()) {
                connection.setRequestProperty(property, mRequestHeaders.get(property));
            }
        }

        // set connection type
        // since RequestType is enum, exception should never occur
        try {
            connection.setRequestMethod(mType.getValue());
        } catch (ProtocolException e) {
            throw new IllegalArgumentException("Request type has invalid value");
        }

        ReturnType decodedResponseData = null;

        try {
            connection.connect();
            publishProgress(PROGRESS_CONNECTED);

            // append post data if available
            if (mPostData != null) {
                byte[] outputInBytes = mPostData.getBytes(HTTP.UTF_8);
                OutputStream os = connection.getOutputStream();
                os.write(outputInBytes);
                os.close();
            }

            // Checking for cancelled flag in major thread breakpoints
            if (isCancelled()) {
                connection.disconnect();
                return null;
            }

            int status = connection.getResponseCode();
            publishProgress(PROGRESS_RESPONSE_CODE);

            if (status != HttpURLConnection.HTTP_OK) {
                connection.getResponseMessage();
                mRequestError = new RequestError("Server returned status code " + status);
                return null;
            }

            // Checking for cancelled flag in major thread breakpoints
            if (isCancelled()) {
                connection.disconnect();
                return null;
            }

            publishProgress(PROGRESS_RESPONSE_TYPE);

            InputStream inputStream = null;
            try {
                inputStream = connection.getInputStream();
                publishProgress(PROGRESS_CONTENT);

                // Checking for cancelled flag in major thread breakpoints
                if (isCancelled()) {
                    connection.disconnect();
                    return null;
                }

                // handle gzipped input stream
                Map<String, List<String>> headers = connection.getHeaderFields();
                List<String> contentEncodings = headers.get(HTTP.CONTENT_ENCODING);
                if (contentEncodings != null) {
                    for (String header : contentEncodings) {
                        if (header.equalsIgnoreCase(REQ_ENCODING_VALUE)) {
                            inputStream = new GZIPInputStream(inputStream);
                            break;
                        }
                    }
                }

                decodedResponseData = processInputStream(connection.getContentType(), inputStream);
            } finally {
                publishProgress(PROGRESS_CONNECTION_CLOSE);
                if (inputStream != null) {
                    inputStream.close();
                }
            }

            // Checking for cancelled flag in major thread breakpoints
            if (isCancelled()) {
                connection.disconnect();
                return null;
            }
        } catch (ConnectException e) {
            // connect exception, server not responding
            mRequestError = new RequestError("Server not responding");
            return null;
        } catch (SocketTimeoutException e) {
            // timeout exception
            mRequestError = new RequestError("Connection timeout");
            return null;
        } catch (IOException e) {
            // io exception
            mRequestError = new RequestError("IO exception");
            return null;
        } finally {
            publishProgress(PROGRESS_DISCONNECTING);
            connection.disconnect();
            publishProgress(PROGRESS_DONE);
        }

        return decodedResponseData;
    }

    protected abstract ReturnType processInputStream(String contentType, InputStream inputStream);

    @Override
    protected void onProgressUpdate(Integer... values) {
        mListener.onProgress(values[0]);
    }

    @Override
    protected void onCancelled() {
        mListener.onCancelled();
    }

    @Override
    protected abstract void onPostExecute(ReturnType returnType);

    /**
     * Executes asyncTask in parallel with other tasks
     * Wrapper for {@link android.os.AsyncTask#THREAD_POOL_EXECUTOR}
     *
     * @param params params
     * @return asyncTask
     */
    public BaseServerRequest executeInParallel(String... params) {
        return (BaseServerRequest) executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    }
}

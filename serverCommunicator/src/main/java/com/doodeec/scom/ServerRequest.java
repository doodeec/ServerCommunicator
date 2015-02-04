package com.doodeec.scom;

import android.os.AsyncTask;

import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Map;

/**
 * Created by Dusan Doodeec Bartos on 20.12.2014.
 *
 * Backend Server request
 * Wrapper around {@link java.net.HttpURLConnection}
 * Can be executed with {@link #THREAD_POOL_EXECUTOR} to evaluate requests in parallel
 *
 * @see com.doodeec.scom.RequestListener
 * @see com.doodeec.scom.RequestError
 */
@SuppressWarnings("unused")
public class ServerRequest extends AsyncTask<String, Integer, String> implements CancellableServerRequest {

    private static final String UTF_8 = HTTP.UTF_8;

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
    private static final String REQ_CHARSET_KEY = "Accept-Charset";
    private static final String REQ_CHARSET_VALUE = UTF_8;
    private static final String REQ_ENCODING_KEY = "Accept-Encoding";
    private static final String REQ_ENCODING_VALUE = "gzip";
    private static final String REQ_CONTENT_TYPE_KEY = HTTP.CONTENT_TYPE;
    private static final String REQ_CONTENT_TYPE_VALUE = "application/json; charset=utf-8";

    // response types
//    private static final String[] IMAGE_RESPONSE = new String[]{"image/png", "image/jpg", "image/jpeg"};
//    private static final String[] JSON_RESPONSE = new String[]{"application/json"};

    public enum RequestType {
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
    private String mPostData;

    /**
     * Connection timeout
     * default to 30sec
     */
    private int mTimeout = 30000;

    /**
     * Additional request headers
     */
    private Map<String, String> mRequestHeaders = null;

    /**
     * Request type
     * allowed values are
     * {@link com.doodeec.scom.ServerRequest.RequestType#GET}
     * {@link com.doodeec.scom.ServerRequest.RequestType#POST}
     * {@link com.doodeec.scom.ServerRequest.RequestType#PUT}
     * {@link com.doodeec.scom.ServerRequest.RequestType#DELETE}
     */
    private RequestType mType;

    /**
     * Request listener
     *
     * @see com.doodeec.scom.RequestListener
     */
    private RequestListener mListener;

    /**
     * Request Error
     * Holds the reason why request was not successful
     */
    private RequestError mRequestError;

    public ServerRequest(RequestType type, RequestListener listener) {
        mType = type;
        mListener = listener;
    }

    public ServerRequest(RequestType type, String data, RequestListener listener) {
        mType = type;
        mListener = listener;
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
     *
     * @param headersMap headers
     */
    public void setHeaders(Map<String, String> headersMap) {
        mRequestHeaders = headersMap;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        URL url;
        HttpURLConnection connection;
        String response;

        publishProgress(PROGRESS_IDLE);

        try {
//            url = new URL(params[1]);
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
        connection.setRequestProperty(REQ_CHARSET_KEY, REQ_CHARSET_VALUE);
        connection.setRequestProperty(REQ_ENCODING_KEY, REQ_ENCODING_VALUE);
        connection.setRequestProperty(REQ_CONTENT_TYPE_KEY, REQ_CONTENT_TYPE_VALUE);

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

        try {
            connection.connect();
            publishProgress(PROGRESS_CONNECTED);

            // append post data if available
            if (mPostData != null) {
                byte[] outputInBytes = mPostData.getBytes(UTF_8);
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

            String contentType = connection.getContentType();
            InputStream inputStream = null;
            try {
                inputStream = connection.getInputStream();
                publishProgress(PROGRESS_CONTENT);

                // Checking for cancelled flag in major thread breakpoints
                if (isCancelled()) {
                    connection.disconnect();
                    return null;
                }

                response = readResponse(inputStream);
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
            //TODO handle GZIPped input stream
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

        return response;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        mListener.onProgress(values[0]);
    }

    @Override
    protected void onPostExecute(String responseString) {
        if (responseString != null) {
            // POST can have empty response, but 200 response code
            if (mType.equals(RequestType.POST) && (responseString.equals("") || responseString.equals("OK"))) {
                mListener.onSuccess();
            } else {
                try {
                    // propagate json object to listener
                    mListener.onSuccess(new JSONObject(responseString));
                } catch (JSONException e) {
                    // not a json object
                    try {
                        // propagate json array to listener
                        mListener.onSuccess(new JSONArray(responseString));
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

    @Override
    protected void onCancelled() {
        mListener.onCancelled();
    }

    /**
     * Reads response as a string
     *
     * @param inputStream input stream
     * @return response as a string
     * @throws IOException
     */
    private String readResponse(InputStream inputStream) throws IOException {
        try {
            int ch;
            StringBuilder sb = new StringBuilder();
            while ((ch = inputStream.read()) != -1) {
                sb.append((char) ch);
            }
            return sb.toString();
        } catch (IOException e) {
            throw e;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    /**
     * Executes asyncTask in parallel with other tasks
     * Wrapper for {@link android.os.AsyncTask#THREAD_POOL_EXECUTOR}
     *
     * @param params params
     * @return asyncTask
     */
    public ServerRequest executeInParallel(String... params) {
        return (ServerRequest) executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    }
}

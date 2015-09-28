package com.doodeec.utils.network;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.io.EOFException;
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
 * Abstract base which is common for both {@link com.doodeec.utils.network.ServerRequest}
 * and {@link com.doodeec.utils.network.ImageServerRequest}
 *
 * @author dusan.bartos
 */
@SuppressWarnings("unused")
public abstract class BaseServerRequest<ReturnType> extends
        AsyncTask<String, Integer, CommunicatorResponse<ReturnType>> implements CancellableServerRequest {

    // asyncTask progress
    public static final int PROGRESS_IDLE = 0;
    public static final int PROGRESS_OPENED = 10;
    public static final int PROGRESS_CONNECTED = 20;
    public static final int PROGRESS_RESPONSE_CODE = 40;
    public static final int PROGRESS_RESPONSE_TYPE = 50;
    public static final int PROGRESS_CONTENT = 60;
    public static final int PROGRESS_INPUT_STREAM = 70;
    public static final int PROGRESS_CONNECTION_CLOSE = 80;
    public static final int PROGRESS_DISCONNECTING = 90;
    public static final int PROGRESS_DONE = 100;

    protected static boolean sDebugEnabled = false;

    public static void enableDebug(boolean enable) {
        sDebugEnabled = enable;
    }

    /**
     * Maximum number of retries when EOFException occurs
     */
    private static final int MAX_RETRIES = 3;
    private int mRetryCount = 0;

    /**
     * Post data to add to request body (payload)
     */
    protected String mPostData;

    /**
     * Connection timeout
     * in milliseconds
     * default to 30sec
     */
    protected int mTimeout = 30000;

    /**
     * Read timeout
     * in milliseconds
     * default to 30sec
     */
    protected int mReadTimeout = 30000;

    /**
     * Additional request headers
     */
    protected Map<String, String> mRequestHeaders = new HashMap<>();

    /**
     * Request type
     * allowed values are
     * {@link com.doodeec.utils.network.RequestType#GET}
     * {@link com.doodeec.utils.network.RequestType#POST}
     * {@link com.doodeec.utils.network.RequestType#PUT}
     * {@link com.doodeec.utils.network.RequestType#DELETE}
     */
    protected RequestType mType;

    /**
     * Request interceptor
     *
     * @see ResponseInterceptor
     */
    protected ResponseInterceptor mInterceptor;

    /**
     * Connection Response
     *
     * @see CommunicatorResponse
     */
    protected CommunicatorResponse<ReturnType> mCommunicatorResponse = new CommunicatorResponse<>();

    /**
     * Constructs basic Server Request without body data (i.e. GET request)
     * Available types are:
     * {@link com.doodeec.utils.network.RequestType#GET}
     * {@link com.doodeec.utils.network.RequestType#POST}
     * {@link com.doodeec.utils.network.RequestType#PUT}
     * {@link com.doodeec.utils.network.RequestType#DELETE}
     *
     * @param type     request type
     */
    protected BaseServerRequest(RequestType type) {
        mType = type;

        initHeaders();

        if (sDebugEnabled) {
            Log.d(getClass().getSimpleName(), "New request created. type=" + type.getValue());
        }
    }

    /**
     * Constructs Server Request with POST payload data
     *
     * @param type     request type (typically {@link com.doodeec.utils.network.RequestType#POST}) for this constructor
     * @param data     post data
     *
     * @see #BaseServerRequest(com.doodeec.utils.network.RequestType)
     */
    protected BaseServerRequest(RequestType type, String data) {
        this(type);
        mPostData = data;
    }

    /**
     * Sets response interceptor
     *
     * @param interceptor interceptor interface
     */
    public void setInterceptor(ResponseInterceptor interceptor) {
        mInterceptor = interceptor;
    }

    /**
     * Sets connection timeout
     * Default value is 30 seconds
     *
     * @param timeout timeout in milliseconds
     */
    public void setTimeout(int timeout) {
        mTimeout = timeout;
    }

    /**
     * Sets read timeout
     * Default value is 30 seconds
     *
     * @param timeout timeout in milliseconds
     */
    public void setReadTimeout(int timeout) {
        mReadTimeout = timeout;
    }

    /**
     * Sets additional headers
     * Headers are added to existing set of headers, in case only this set should be available,
     * {@link #clearHeaders()} should be called first
     *
     * @param headersMap headers
     */
    public void setHeaders(Map<String, String> headersMap) {
        mRequestHeaders.putAll(headersMap);
    }

    /**
     * Clears all stored headers
     */
    public void clearHeaders() {
        mRequestHeaders.clear();
    }

    /**
     * Can be used (overridden in request implementation) to define default headers for the
     * overridden class
     *
     * @see #BaseServerRequest(RequestType)
     */
    protected void initHeaders() {
    }

    @Override
    protected CommunicatorResponse<ReturnType> doInBackground(String... params) {
        URL url;
        HttpURLConnection connection;

        // progress 0%
        publishProgress(PROGRESS_IDLE);

        try {
            url = new URL(params[0]);
        } catch (MalformedURLException e) {
            if (sDebugEnabled) {
                e.printStackTrace();
            }
            //Invalid URL
            mCommunicatorResponse.setError(new RequestError("Cannot read target URL", params[0]));
            return mCommunicatorResponse;
        }

        if (sDebugEnabled) {
            Log.d(getClass().getSimpleName(), "Request URL parsed. url=" + url.toString());
        }

        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            if (sDebugEnabled) {
                e.printStackTrace();
            }
            //IO exception
            mCommunicatorResponse.setError(new RequestError("Cannot open connection", url.toString()));
            return mCommunicatorResponse;
        }

        if (sDebugEnabled) {
            Log.d(getClass().getSimpleName(), "Connection opened. url=" + url.toString());
        }

        // progress 10%
        publishProgress(PROGRESS_OPENED);

        // set connection timeouts
        connection.setConnectTimeout(mTimeout);
        connection.setReadTimeout(mReadTimeout);
        connection.setInstanceFollowRedirects(true);
        connection.setChunkedStreamingMode(0);

        // set additional settings for POST request
        if (mType.equals(RequestType.POST) || mType.equals(RequestType.PUT)) {
            connection.setDoInput(true);
            connection.setUseCaches(false);
        }

        // set connection header properties
        connection.setRequestProperty("Accept-Charset", "UTF-8");
        connection.setRequestProperty("Accept-Encoding", "gzip");

        // set additional request headers
        if (mRequestHeaders != null) {
            if (sDebugEnabled) {
                Log.d(getClass().getSimpleName(), "Setting custom headers. headers=" + mRequestHeaders.toString());
            }

            for (String property : mRequestHeaders.keySet()) {
                connection.setRequestProperty(property, mRequestHeaders.get(property));
            }
        }

        if (sDebugEnabled) {
            Log.d(getClass().getSimpleName(), "Headers set. url=" + url.toString());
        }

        // set connection type
        // since RequestType is enum, exception should never occur
        try {
            connection.setRequestMethod(mType.getValue());
        } catch (ProtocolException e) {
            if (sDebugEnabled) {
                e.printStackTrace();
            }
            throw new IllegalArgumentException("Request type has invalid value");
        }

        try {
            connection.connect();
            // progress 20%
            publishProgress(PROGRESS_CONNECTED);

            // append post data if available
            if (mPostData != null) {
                byte[] outputInBytes = mPostData.getBytes("UTF-8");
                OutputStream os = connection.getOutputStream();
                os.write(outputInBytes);
                os.close();
            }

            // Checking for cancelled flag in major thread breakpoints
            if (isCancelled()) {
                connection.disconnect();
                mCommunicatorResponse.setCancelled(true);
                return mCommunicatorResponse;
            }

            int status = connection.getResponseCode();
            if (sDebugEnabled) {
                Log.d(getClass().getSimpleName(), "Connection status code " + status + ". url=" + url.toString());
            }
            mCommunicatorResponse.setStatusCode(status);
            // progress 40%
            publishProgress(PROGRESS_RESPONSE_CODE);

            //try to hook interceptor
            if (mInterceptor != null && mInterceptor.onProcessStatus(status)) {
                //response intercepted
                if (sDebugEnabled) {
                    Log.d(getClass().getSimpleName(), "Response intercepted. url=" + url.toString());
                }
                mCommunicatorResponse.setError(RequestError.INTERCEPT);
                return mCommunicatorResponse;
            }

            // free interceptor, no longer needed
            mInterceptor = null;

            if (status != HttpURLConnection.HTTP_OK) {
                connection.getResponseMessage();
                mCommunicatorResponse.setError(new RequestError("Server returned status code " + status, url.toString()));
                return mCommunicatorResponse;
            }

            // Checking for cancelled flag in major thread breakpoints
            if (isCancelled()) {
                connection.disconnect();
                mCommunicatorResponse.setCancelled(true);
                return mCommunicatorResponse;
            }

            // progress 50%
            publishProgress(PROGRESS_RESPONSE_TYPE);

            InputStream inputStream = null;
            try {
                if (sDebugEnabled) {
                    Log.d(getClass().getSimpleName(), "Reading input stream. url=" + url.toString());
                }

                inputStream = connection.getInputStream();
                // progress 60%
                publishProgress(PROGRESS_CONTENT);

                // Checking for cancelled flag in major thread breakpoints
                if (isCancelled()) {
                    connection.disconnect();
                    mCommunicatorResponse.setCancelled(true);
                    return mCommunicatorResponse;
                }

                // handle gzipped input stream
                Map<String, List<String>> headers = connection.getHeaderFields();
                List<String> contentEncodings = headers.get("Content-Encoding");
                if (contentEncodings != null) {
                    for (String header : contentEncodings) {
                        if (header.equalsIgnoreCase("gzip")) {
                            if (sDebugEnabled) {
                                Log.d(getClass().getSimpleName(), "Decoding GZIPped stream. url=" + url.toString());
                            }
                            inputStream = new GZIPInputStream(inputStream);
                            break;
                        }
                    }
                }

                // progress 70%
                publishProgress(PROGRESS_INPUT_STREAM);

                if (sDebugEnabled) {
                    Log.d(getClass().getSimpleName(), "Processing input stream. url=" + url.toString());
                }
                mCommunicatorResponse.setData(
                        processInputStream(connection.getContentType(), inputStream));
            } finally {
                // progress 80%
                publishProgress(PROGRESS_CONNECTION_CLOSE);
                if (inputStream != null) {
                    inputStream.close();
                }
            }

            // Checking for cancelled flag in major thread breakpoints
            if (isCancelled()) {
                connection.disconnect();
                mCommunicatorResponse.setCancelled(true);
                return mCommunicatorResponse;
            }
        } catch (ConnectException e) {
            if (sDebugEnabled) {
                e.printStackTrace();
            }
            // connect exception, server not responding
            mCommunicatorResponse.setError(new RequestError("Server not responding", url.toString()));
            return mCommunicatorResponse;
        } catch (SocketTimeoutException e) {
            if (sDebugEnabled) {
                e.printStackTrace();
            }
            // timeout exception
            mCommunicatorResponse.setError(new RequestError("Connection timeout", url.toString()));
            return mCommunicatorResponse;
        } catch (EOFException e) {
            // known bug when POST request is thrown with EOFException sometimes
            // retry the request a few times
            if (sDebugEnabled) {
                Log.i(getClass().getSimpleName(), "EOFException occured, retrying to send a request");
            }
            if (mRetryCount < MAX_RETRIES) {
                mRetryCount++;
                return doInBackground(params);
            } else {
                if (sDebugEnabled) {
                    e.printStackTrace();
                }
                mCommunicatorResponse.setError(new RequestError(e, url.toString()));
                return mCommunicatorResponse;
            }
        } catch (IOException e) {
            if (sDebugEnabled) {
                e.printStackTrace();
            }
            // io exception
            mCommunicatorResponse.setError(new RequestError(e, url.toString()));
            return mCommunicatorResponse;
        } catch (Exception e) {
            if (sDebugEnabled) {
                e.printStackTrace();
            }
            // other exception
            mCommunicatorResponse.setError(new RequestError(e, url.toString()));
            return mCommunicatorResponse;
        } finally {
            // progress 90%
            publishProgress(PROGRESS_DISCONNECTING);
            connection.disconnect();
            // progress 100%
            publishProgress(PROGRESS_DONE);
        }

        if (sDebugEnabled) {
            Log.d(getClass().getSimpleName(), "Request complete, returning to UI thread. url=" + url.toString());
        }

        return mCommunicatorResponse;
    }

    /**
     * Processes input stream to create defined generic object instance
     *
     * @param contentType content type from response headers
     * @param inputStream input stream to be processed
     *
     * @return generic object instance
     */
    protected abstract ReturnType processInputStream(String contentType, InputStream inputStream);

    @Override
    protected abstract void onPostExecute(CommunicatorResponse<ReturnType> returnType);

    /**
     * Clones request parameters to the new instance
     *
     * @return cloned instance
     */
    public abstract BaseServerRequest<ReturnType> cloneRequest();

    /**
     * Executes asyncTask in parallel with other tasks
     * Wrapper for {@link android.os.AsyncTask#THREAD_POOL_EXECUTOR}
     *
     * @param params params
     *
     * @return asyncTask
     */
    public BaseServerRequest executeInParallel(String... params) {
        if (sDebugEnabled) {
            Log.d(getClass().getSimpleName(), "Executing request in pool. url=" + params[0]);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return (BaseServerRequest) executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        } else {
            return (BaseServerRequest) execute(params);
        }
    }
}

package com.doodeec.utils.network;

import android.os.AsyncTask;
import android.os.Build;
<<<<<<< HEAD
import android.util.Log;
=======
>>>>>>> parent of 5674e78... 1.2.0 javadoc

import com.doodeec.utils.network.listener.BaseRequestListener;

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

<<<<<<< HEAD
    private static boolean sDebugEnabled = false;

    public static void enableDebug(boolean enable) {
        sDebugEnabled = enable;
    }

=======
>>>>>>> parent of 5674e78... 1.2.0 javadoc
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
     * Request listener
     *
     * @see com.doodeec.utils.network.listener.JSONRequestListener
     */
    protected BaseRequestListener mListener;

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
     * @param listener listener
     */
    protected BaseServerRequest(RequestType type, BaseRequestListener listener) {
        mType = type;
        mListener = listener;

        initHeaders();
<<<<<<< HEAD

        if (sDebugEnabled) {
            Log.d(getClass().getSimpleName(), "New request created. type=" + type.getValue());
        }
=======
>>>>>>> parent of 5674e78... 1.2.0 javadoc
    }

    /**
     * Constructs Server Request with POST payload data
     *
     * @param type     request type (typically {@link com.doodeec.utils.network.RequestType#POST}) for this constructor
     * @param data     post data
     * @param listener listener
     *
     * @see #BaseServerRequest(com.doodeec.utils.network.RequestType, com.doodeec.utils.network.listener.BaseRequestListener)
     */
    protected BaseServerRequest(RequestType type, String data, BaseRequestListener listener) {
        this(type, listener);
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
     * @see #BaseServerRequest(RequestType, com.doodeec.utils.network.listener.BaseRequestListener)
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
<<<<<<< HEAD
            if (sDebugEnabled) {
                e.printStackTrace();
            }
            //Invalid URL
=======
            //IO exception
>>>>>>> parent of 5674e78... 1.2.0 javadoc
            mCommunicatorResponse.setError(new RequestError("Cannot read target URL", params[0]));
            return mCommunicatorResponse;
        }

<<<<<<< HEAD
        if (sDebugEnabled) {
            Log.d(getClass().getSimpleName(), "Request URL parsed. url=" + url.toString());
        }

        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            if (sDebugEnabled) {
                e.printStackTrace();
            }
=======
        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
>>>>>>> parent of 5674e78... 1.2.0 javadoc
            //IO exception
            mCommunicatorResponse.setError(new RequestError("Cannot open connection", url.toString()));
            return mCommunicatorResponse;
        }

<<<<<<< HEAD
        if (sDebugEnabled) {
            Log.d(getClass().getSimpleName(), "Connection opened. url=" + url.toString());
        }

=======
>>>>>>> parent of 5674e78... 1.2.0 javadoc
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
<<<<<<< HEAD
            if (sDebugEnabled) {
                Log.d(getClass().getSimpleName(), "Setting custom headers. headers=" + mRequestHeaders.toString());
            }

=======
>>>>>>> parent of 5674e78... 1.2.0 javadoc
            for (String property : mRequestHeaders.keySet()) {
                connection.setRequestProperty(property, mRequestHeaders.get(property));
            }
        }

<<<<<<< HEAD
        if (sDebugEnabled) {
            Log.d(getClass().getSimpleName(), "Headers set. url=" + url.toString());
        }

=======
>>>>>>> parent of 5674e78... 1.2.0 javadoc
        // set connection type
        // since RequestType is enum, exception should never occur
        try {
            connection.setRequestMethod(mType.getValue());
        } catch (ProtocolException e) {
<<<<<<< HEAD
            if (sDebugEnabled) {
                e.printStackTrace();
            }
=======
>>>>>>> parent of 5674e78... 1.2.0 javadoc
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
<<<<<<< HEAD
            if (sDebugEnabled) {
                Log.d(getClass().getSimpleName(), "Connection status code " + status + ". url=" + url.toString());
            }
=======
>>>>>>> parent of 5674e78... 1.2.0 javadoc
            mCommunicatorResponse.setStatusCode(status);
            // progress 40%
            publishProgress(PROGRESS_RESPONSE_CODE);

            //try to hook interceptor
            if (mInterceptor != null && mInterceptor.onProcessStatus(status)) {
                //response intercepted
<<<<<<< HEAD
                if (sDebugEnabled) {
                    Log.d(getClass().getSimpleName(), "Response intercepted. url=" + url.toString());
                }
=======
>>>>>>> parent of 5674e78... 1.2.0 javadoc
                mCommunicatorResponse.setError(RequestError.INTERCEPT);
                return mCommunicatorResponse;
            }

<<<<<<< HEAD
            // free interceptor, no longer needed
            mInterceptor = null;

=======
>>>>>>> parent of 5674e78... 1.2.0 javadoc
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
<<<<<<< HEAD
                if (sDebugEnabled) {
                    Log.d(getClass().getSimpleName(), "Reading input stream. url=" + url.toString());
                }

=======
>>>>>>> parent of 5674e78... 1.2.0 javadoc
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
<<<<<<< HEAD
                            if (sDebugEnabled) {
                                Log.d(getClass().getSimpleName(), "Decoding GZIPped stream. url=" + url.toString());
                            }
=======
>>>>>>> parent of 5674e78... 1.2.0 javadoc
                            inputStream = new GZIPInputStream(inputStream);
                            break;
                        }
                    }
                }

                // progress 70%
                publishProgress(PROGRESS_INPUT_STREAM);

<<<<<<< HEAD
                if (sDebugEnabled) {
                    Log.d(getClass().getSimpleName(), "Processing input stream. url=" + url.toString());
                }
=======
>>>>>>> parent of 5674e78... 1.2.0 javadoc
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
<<<<<<< HEAD
            if (sDebugEnabled) {
                e.printStackTrace();
            }
=======
>>>>>>> parent of 5674e78... 1.2.0 javadoc
            // connect exception, server not responding
            mCommunicatorResponse.setError(new RequestError("Server not responding", url.toString()));
            return mCommunicatorResponse;
        } catch (SocketTimeoutException e) {
<<<<<<< HEAD
            if (sDebugEnabled) {
                e.printStackTrace();
            }
=======
>>>>>>> parent of 5674e78... 1.2.0 javadoc
            // timeout exception
            mCommunicatorResponse.setError(new RequestError("Connection timeout", url.toString()));
            return mCommunicatorResponse;
        } catch (IOException e) {
<<<<<<< HEAD
            if (sDebugEnabled) {
                e.printStackTrace();
            }
=======
>>>>>>> parent of 5674e78... 1.2.0 javadoc
            // io exception
            mCommunicatorResponse.setError(new RequestError(e, url.toString()));
            return mCommunicatorResponse;
        } finally {
            // progress 90%
            publishProgress(PROGRESS_DISCONNECTING);
            connection.disconnect();
            // progress 100%
            publishProgress(PROGRESS_DONE);
        }

<<<<<<< HEAD
        if (sDebugEnabled) {
            Log.d(getClass().getSimpleName(), "Request complete, returning to UI thread. url=" + url.toString());
        }

=======
>>>>>>> parent of 5674e78... 1.2.0 javadoc
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
    protected void onProgressUpdate(Integer... values) {
        mListener.onProgress(values[0]);
    }

    @Override
    protected void onCancelled() {
        mListener.onCancelled();
    }

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
<<<<<<< HEAD
        if (sDebugEnabled) {
            Log.d(getClass().getSimpleName(), "Executing request in pool. url=" + params[0]);
        }

=======
>>>>>>> parent of 5674e78... 1.2.0 javadoc
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return (BaseServerRequest) executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        } else {
            return (BaseServerRequest) execute(params);
        }
    }
}

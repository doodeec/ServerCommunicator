package com.doodeec.scom;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

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
import java.util.Arrays;
import java.util.Map;

/**
 * Created by Dusan Doodeec Bartos on 29.12.2014.
 *
 * Backend Server request for Image resource
 * Wrapper around {@link java.net.HttpURLConnection}
 * Can be executed with {@link #THREAD_POOL_EXECUTOR} to evaluate requests in parallel
 *
 * @see RequestListener
 * @see RequestError
 */
@SuppressWarnings("unused")
public class ImageServerRequest extends AsyncTask<String, Integer, Bitmap> implements CancellableServerRequest {

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

    // response types
    private static final String[] IMAGE_RESPONSE = new String[]{"image/png", "image/jpg", "image/jpeg"};

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
     */
    private ServerRequest.RequestType mType;

    /**
     * Request listener
     *
     * @see RequestListener
     */
    private ImageRequestListener mListener;

    public ImageServerRequest(ServerRequest.RequestType type, ImageRequestListener listener) {
        mType = type;
        mListener = listener;
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
    protected Bitmap doInBackground(String... params) {
        URL url;
        HttpURLConnection connection;
        String response;

        publishProgress(PROGRESS_IDLE);

        try {
            url = new URL(params[0]);
        } catch (MalformedURLException e) {
            //IO exception
            return null;
        }

        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            //IO exception
            return null;
        }

        publishProgress(PROGRESS_OPENED);

        // set connection timeouts
        connection.setConnectTimeout(mTimeout);
        connection.setReadTimeout(mTimeout);

        // set connection header properties
        connection.setRequestProperty(REQ_CHARSET_KEY, REQ_CHARSET_VALUE);
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
                //TODO propagate error code
                connection.getResponseMessage();
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

                if (Arrays.asList(IMAGE_RESPONSE).contains(contentType)) {
                    // decoding stream data back into image Bitmap that android understands
                    return BitmapFactory.decodeStream(inputStream);
                }
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
            //TODO GZIP input stream
        } catch (ConnectException e) {
            // connect exception, server not responding
            return null;
        } catch (SocketTimeoutException e) {
            // timeout exception
            return null;
        } catch (IOException e) {
            // io exception
            return null;
        } finally {
            publishProgress(PROGRESS_DISCONNECTING);
            connection.disconnect();
            publishProgress(PROGRESS_DONE);
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        mListener.onProgress(values[0]);
    }

    @Override
    protected void onPostExecute(Bitmap imageResource) {
        if (imageResource != null) {
            // POST can have empty response, but 200 response code
            mListener.onSuccess(imageResource);
        } else {
            //TODO handle other exceptions from doInBg
            mListener.onError(new RequestError("Response bitmap empty"));
        }
    }

    @Override
    protected void onCancelled() {
        mListener.onCancelled();
    }

    /**
     * Executes asyncTask in parallel with other tasks
     * Wrapper for {@link android.os.AsyncTask#THREAD_POOL_EXECUTOR}
     *
     * @param params params
     * @return asyncTask
     */
    public ImageServerRequest executeInParallel(String... params) {
        return (ImageServerRequest) executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    }
}

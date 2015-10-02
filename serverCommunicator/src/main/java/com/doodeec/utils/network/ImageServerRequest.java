package com.doodeec.utils.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.doodeec.utils.network.listener.BaseRequestListener;

import java.io.InputStream;
import java.util.Arrays;

/**
 * Server request for Image resource
 * {@link android.os.AsyncTask} wrapper around {@link java.net.HttpURLConnection}
 * Can be executed with {@link #THREAD_POOL_EXECUTOR} to evaluate requests in parallel
 *
 * @author dusan.bartos
 * @see com.doodeec.utils.network.BaseServerRequest
 * @see com.doodeec.utils.network.listener.JSONRequestListener
 * @see RequestError
 */
@SuppressWarnings("unused")
public class ImageServerRequest extends BaseServerRequest<Bitmap, Bitmap> {

    // response types
    private static final String[] IMAGE_RESPONSE = new String[]{"image/png", "image/jpg", "image/jpeg"};

    /**
     * Request listener
     *
     * @see com.doodeec.utils.network.listener.JSONRequestListener
     */
    protected BaseRequestListener<Bitmap> mListener;

    public ImageServerRequest(RequestType type, BaseRequestListener<Bitmap> listener) {
        super(type);
        mListener = listener;
    }

    @Override
    protected Bitmap processInputStream(String contentType, InputStream inputStream) {
        if (Arrays.asList(IMAGE_RESPONSE).contains(contentType)) {
            // decoding stream data back into image Bitmap that android understands
            return BitmapFactory.decodeStream(inputStream);
        } else {
            return null;
        }
    }

    @Override
    protected Bitmap instantiateStream(Bitmap bitmap) {
        return bitmap;
    }

    @Override
    protected void onPostExecute(CommunicatorResponse<Bitmap> response) {
        if (response.isIntercepted()) {
            //do nothing
            if (sDebugEnabled) {
                Log.d(getClass().getSimpleName(), "Response intercepted. Not proceeding to response listener");
            }
        } else if (response.hasError()) {
            mListener.onError(response.getError());
        } else if (response.getData() != null) {
            // POST can have empty response, but 200 response code
            //noinspection unchecked
            mListener.onSuccess(response.getData());
        } else {
            mListener.onError(new RequestError("Response bitmap empty", response.getUrl()));
        }
    }

    @Override
    public ImageServerRequest cloneRequest() {
        ImageServerRequest clonedRequest = new ImageServerRequest(mType, mListener);
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

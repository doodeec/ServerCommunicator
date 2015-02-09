package com.doodeec.scom;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.doodeec.scom.listener.BaseRequestListener;

import java.io.InputStream;
import java.util.Arrays;

/**
 * Backend Server request for Image resource
 * Wrapper around {@link java.net.HttpURLConnection}
 * Can be executed with {@link #THREAD_POOL_EXECUTOR} to evaluate requests in parallel
 *
 * @author dusan.bartos
 * @see com.doodeec.scom.BaseServerRequest
 * @see com.doodeec.scom.listener.JSONRequestListener
 * @see RequestError
 */
public class ImageServerRequest extends BaseServerRequest<Bitmap> {

    // response types
    private static final String[] IMAGE_RESPONSE = new String[]{"image/png", "image/jpg", "image/jpeg"};

    public ImageServerRequest(RequestType type, BaseRequestListener<Bitmap> listener) {
        super(type, listener);
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
    protected void onPostExecute(Bitmap imageResource) {
        if (imageResource != null) {
            // POST can have empty response, but 200 response code
            mListener.onSuccess(imageResource);
        } else if (mRequestError != null) {
            mListener.onError(mRequestError);
        } else {
            mListener.onError(new RequestError("Response bitmap empty"));
        }
    }
}

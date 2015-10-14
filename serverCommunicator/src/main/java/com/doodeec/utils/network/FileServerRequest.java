package com.doodeec.utils.network;

import android.content.ContextWrapper;
import android.net.Uri;
import android.util.Log;

import com.doodeec.utils.network.listener.BaseRequestListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Dusan Bartos
 */
@SuppressWarnings("unused")
public class FileServerRequest extends BaseServerRequest<File, File> {

    /**
     * @see ServerRequest#setBufferSize(int)
     */
    private static int sCharBufferSize = 4096 * 2;

    /**
     * @see ServerRequest#setBufferSize(int)
     */
    public static void setBufferSize(int size) {
        sCharBufferSize = size;
    }

    /**
     * Response listener
     *
     * @see com.doodeec.utils.network.listener.BaseRequestListener
     */
    protected BaseRequestListener<File> mListener;

    /**
     * File directory where temporary file will be saved
     * Can be {@link ContextWrapper#getFilesDir()} or {@link ContextWrapper#getCacheDir()}
     * or elsewhere where you can later retrieve it
     */
    private final File mFileDir;

    /**
     * Name of the file, which will be used to save downloaded file
     * When null, URL will be used to determine the name (via last path segment)
     */
    private String mFileName = null;

    public FileServerRequest(File fileDir, RequestType type, BaseRequestListener<File> listener) {
        this(fileDir, type, null, listener);
    }

    public FileServerRequest(File fileDir, RequestType type, String data, BaseRequestListener<File> listener) {
        super(type, data);
        mFileDir = fileDir;
        mListener = listener;
    }

    /**
     * Sets custom filename which will be used to save temp file
     *
     * @param fileName name of the file
     */
    public void setFileName(String fileName) {
        mFileName = fileName;
    }

    @Override
    protected File processInputStream(String contentType, InputStream inputStream) {
        File file = null;
        FileOutputStream fos = null;

        try {
            if (sDebugEnabled) {
                Log.d(getClass().getSimpleName(), "Processing input stream");
            }

            String fileName = mFileName != null ? mFileName :
                    Uri.parse(mCommunicatorResponse.getUrl()).getLastPathSegment();

            // tempFile creation would throw an exception when file prefix is shorter than 3 characters
            if (fileName.length() < 3) {
                fileName += "_";
                fileName += mType.getValue();
            }

            if (sDebugEnabled) {
                Log.d(getClass().getSimpleName(), "Processing input stream");
            }

            file = new File(mFileDir, fileName);
            fos = new FileOutputStream(file);

            byte[] buf = new byte[sCharBufferSize];
            int charsRead;
            int position = 0;
            while ((charsRead = inputStream.read(buf, 0, sCharBufferSize)) > 0) {
                fos.write(buf, position, charsRead);
                position += charsRead;
            }

            if (sDebugEnabled) {
                Log.d(getClass().getSimpleName(), "Closing streams");
            }
        } catch (IOException e) {
            if (sDebugEnabled) {
                e.printStackTrace();
                mCommunicatorResponse.setError(new RequestError(e, mCommunicatorResponse.getUrl()));
            }
            return null;
        } finally {
            try {
                if (fos != null) fos.close();
            } catch (IOException e) {
                if (sDebugEnabled) {
                    e.printStackTrace();
                    Log.d(getClass().getSimpleName(), "Error closing outputStream");
                }
            }
        }

        return file;
    }

    @Override
    protected File instantiateStream(File file) {
        return file;
    }

    @Override
    protected void onPostExecute(CommunicatorResponse<File> response) {
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
    public FileServerRequest cloneRequest() {
        FileServerRequest clonedRequest = new FileServerRequest(mFileDir, mType, mPostData, mListener);
        clonedRequest.mTimeout = mTimeout;
        clonedRequest.mReadTimeout = mReadTimeout;
        clonedRequest.mRequestHeaders = mRequestHeaders;
        return clonedRequest;
    }
}

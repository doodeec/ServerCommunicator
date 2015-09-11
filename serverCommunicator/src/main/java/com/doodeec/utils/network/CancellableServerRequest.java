package com.doodeec.utils.network;

import android.os.AsyncTask;

/**
 * Simplified interface for allowing server request to be cancelled
 *
 * @author dusan.bartos
 */
@SuppressWarnings("unused")
public interface CancellableServerRequest {

    /**
     * @see android.os.AsyncTask#cancel(boolean)
     */
    boolean cancel(boolean mayInterruptIfRunning);

    /**
     * @see android.os.AsyncTask#getStatus()
     */
    AsyncTask.Status getStatus();
}

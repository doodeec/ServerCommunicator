package com.doodeec.scom;

import android.os.AsyncTask;

/**
 * Created by Dusan Doodeec Bartos on 21.12.2014.
 *
 * Simplified interface for allowing server request to be cancelled
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

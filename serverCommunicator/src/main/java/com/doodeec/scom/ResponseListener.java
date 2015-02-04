package com.doodeec.scom;

/**
 * Created by Dusan Doodeec Bartos on 21.12.2014.
 *
 * Response listener
 * Intended to be used in network service, when request listener is processed from raw data to
 * typed data
 */
@SuppressWarnings("unused")
public interface ResponseListener<ResponseType> extends SimpleResponseListener<ResponseType> {

    /**
     * Fired if asyncTask was cancelled
     */
    void onCancelled();

    /**
     * Fired in major asyncTask checkpoints
     *
     * @param progress progress in percent
     */
    void onProgress(Integer progress);
}

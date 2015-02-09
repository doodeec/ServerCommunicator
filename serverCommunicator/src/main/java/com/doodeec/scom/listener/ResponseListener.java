package com.doodeec.scom.listener;

/**
 * Response listener
 * Intended to be used in network service, when request listener is processed from raw data to
 * typed data
 *
 * The difference with {@link com.doodeec.scom.listener.SimpleResponseListener} is that this
 * interface declares also Cancel and Progress event listeners
 *
 * @author dusan.bartos
 */
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

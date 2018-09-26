package com.furahitechpay.data;

/**
 * Listen for request connection state changes
 */
public interface ConnectionRequestListener {

    /**
     * Invoked when request failed
     * @param message Failure reason
     */
    void onRequestFailed(String message);

    /**
     * Invoked when request completed
     * @param response Request response
     */
    void onRequestCompleted(FurahitechResponse response);

    /**
     * Invoked when there is network error
     */
    void onNetworkError();
}

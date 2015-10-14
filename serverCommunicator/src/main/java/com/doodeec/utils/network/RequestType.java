package com.doodeec.utils.network;

/**
 * Request type enum
 * GET | POST | PUT | DELETE
 */
@SuppressWarnings("unused")
public enum RequestType {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE");

    String mValue;

    RequestType(String value) {
        this.mValue = value;
    }

    public String getValue() {
        return mValue;
    }
}

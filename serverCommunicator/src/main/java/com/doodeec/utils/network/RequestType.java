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

    private String value;

    private RequestType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

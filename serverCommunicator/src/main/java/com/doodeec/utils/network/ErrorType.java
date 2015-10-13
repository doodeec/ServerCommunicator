package com.doodeec.utils.network;

/**
 * Request error types
 * Type depends on a class of exception thrown during request execution
 * If Error is created manually with custom message, type is Custom
 *
 * @author dusan.bartos
 */
public enum ErrorType {
    MalformedUrl(0),
    Connect(0),
    SocketTimeout(0),
    IO(0),
    Other(0),
    Custom(0),
    BadRequest(400),
    Unauthorized(401),
    Forbidden(403),
    NotFound(404),
    BadMethod(404);

    private int mCode;

    ErrorType(int code) {
        mCode = code;
    }

    public static ErrorType forStatusCode(int statusCode) {
        for (ErrorType type: values()) {
            if (statusCode == type.mCode) return type;
        }
        return Other;
    }
}

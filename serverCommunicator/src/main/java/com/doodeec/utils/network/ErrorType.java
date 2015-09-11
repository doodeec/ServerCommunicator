package com.doodeec.utils.network;

/**
 * Request error types
 * Type depends on a class of exception thrown during request execution
 * If Error is created manually with custom message, type is Custom
 *
 * @author dusan.bartos
 */
public enum ErrorType {
    MalformedUrl,
    Connect,
    SocketTimeout,
    IO,
    Other,
    Custom
}

package com.contractnegotiation.backend.exception;

public class FastApiUnavailableException extends RuntimeException {

    public FastApiUnavailableException(String message) {
        super(message);
    }

    public FastApiUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

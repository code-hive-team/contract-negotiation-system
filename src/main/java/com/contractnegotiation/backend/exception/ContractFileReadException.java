package com.contractnegotiation.backend.exception;

public class ContractFileReadException extends RuntimeException {

    public ContractFileReadException(String message) {
        super(message);
    }

    public ContractFileReadException(String message, Throwable cause) {
        super(message, cause);
    }
}
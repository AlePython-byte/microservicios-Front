package com.ale.observability.exception;

public class ServiceOperationException extends RuntimeException {

    public ServiceOperationException(String message) {
        super(message);
    }
}
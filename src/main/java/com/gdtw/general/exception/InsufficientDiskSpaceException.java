package com.gdtw.general.exception;

public class InsufficientDiskSpaceException extends RuntimeException {
    public InsufficientDiskSpaceException(String message) {
        super(message);
    }
}

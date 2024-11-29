package com.GDTW.general.exception;

public class InsufficientDiskSpaceException extends RuntimeException {
    public InsufficientDiskSpaceException(String message) {
        super(message);
    }
}

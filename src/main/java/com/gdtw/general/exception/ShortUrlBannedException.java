package com.gdtw.general.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.GONE)
public class ShortUrlBannedException extends RuntimeException {
    public ShortUrlBannedException(String message) {
        super(message);
    }
}

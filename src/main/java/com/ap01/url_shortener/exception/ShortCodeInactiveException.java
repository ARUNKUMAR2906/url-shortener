package com.ap01.url_shortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.GONE)
public class ShortCodeInactiveException extends RuntimeException {

    public ShortCodeInactiveException(String shortCode) {
        super("Short code is inactive: " + shortCode);
    }
}

package com.ap01.url_shortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.GONE)
public class ShortCodeExpiredException extends RuntimeException {

    public ShortCodeExpiredException(String shortCode) {
        super("Short code has expired: " + shortCode);
    }
}

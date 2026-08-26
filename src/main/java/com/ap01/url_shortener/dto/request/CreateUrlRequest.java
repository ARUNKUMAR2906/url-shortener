package com.ap01.url_shortener.dto.request;

import java.time.LocalDateTime;

public class CreateUrlRequest {
    private String originalUrl;

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }
}

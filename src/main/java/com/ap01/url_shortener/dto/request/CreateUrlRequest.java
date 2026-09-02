package com.ap01.url_shortener.dto.request;

import com.ap01.url_shortener.enums.ExpirationOption;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CreateUrlRequest {

    @NotBlank(message = "URL can not be empty")
    @Pattern(
            regexp = "^(https?://).+",
            message = "URl must start with http or https"
    )
    private String originalUrl;
    private ExpirationOption expiration;

    public ExpirationOption getExpiration() {
        return expiration;
    }

    public void setExpiration(ExpirationOption expiration) {
        this.expiration = expiration;
    }


    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }
}

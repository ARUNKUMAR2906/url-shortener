package com.ap01.url_shortener.dto.response;

public class UrlAnalyticsCountryResponse {
    private String country;
    private Long count;

    public UrlAnalyticsCountryResponse(String country, Long count) {
        this.country = country;
        this.count = count;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}

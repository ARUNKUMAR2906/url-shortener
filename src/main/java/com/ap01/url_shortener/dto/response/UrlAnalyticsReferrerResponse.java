package com.ap01.url_shortener.dto.response;

public class UrlAnalyticsReferrerResponse {

    private String referrer;
    private Long count;
    public UrlAnalyticsReferrerResponse(String referrer, Long count) {
        this.referrer = referrer;
        this.count = count;
    }
    public String getReferrer() {
        return referrer;
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

}

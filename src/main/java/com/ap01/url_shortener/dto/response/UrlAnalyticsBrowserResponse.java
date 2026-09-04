package com.ap01.url_shortener.dto.response;

public class UrlAnalyticsBrowserResponse {
    private String browser;
    private Long clicks;

    public UrlAnalyticsBrowserResponse(String browser, Long clicks) {
        this.browser = browser;
        this.clicks = clicks;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public Long getClicks() {
        return clicks;
    }

    public void setClicks(Long clicks) {
        this.clicks = clicks;
    }
}

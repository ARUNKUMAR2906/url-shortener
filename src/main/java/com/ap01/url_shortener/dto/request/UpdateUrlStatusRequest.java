package com.ap01.url_shortener.dto.request;

public class UpdateUrlStatusRequest {

    private boolean active;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

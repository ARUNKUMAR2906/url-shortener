package com.ap01.url_shortener.dto.response;

import java.time.LocalDate;

public class ClicksByDateResponse {
    private LocalDate clickedAt;
    private Long count;

    public ClicksByDateResponse(LocalDate clickedAt, Long count) {
        this.clickedAt = clickedAt;
        this.count = count;
    }

    public LocalDate getClickedAt() {
        return clickedAt;
    }

    public void setClickedAt(LocalDate clickedAt) {
        this.clickedAt = clickedAt;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}

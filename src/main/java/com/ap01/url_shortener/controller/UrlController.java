package com.ap01.url_shortener.controller;

import com.ap01.url_shortener.dto.request.CreateUrlRequest;
import com.ap01.url_shortener.dto.request.UpdateUrlStatusRequest;
import com.ap01.url_shortener.dto.response.CreateUrlResponse;
import com.ap01.url_shortener.dto.response.UrlAnalyticsResponse;
import com.ap01.url_shortener.dto.response.UrlResponse;
import com.ap01.url_shortener.entity.Url;
import com.ap01.url_shortener.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.Map;

@RestController()
@RequestMapping("/shortlinks/api")
public class UrlController {

    private final UrlService urlService;
    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/urls")
    public CreateUrlResponse createUrl(@Valid @RequestBody CreateUrlRequest request) {

        return urlService.save(request);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirectUrl(@PathVariable String shortCode, HttpServletRequest request) {
        Url url = urlService.getByShortCode(shortCode,request);
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(url.getOriginalUrl()))
                .build();
    }

    @PatchMapping("/urls/{shortCode}/status")
    public ResponseEntity<Void> updateIsActive(
            @PathVariable String shortCode,
            @RequestBody UpdateUrlStatusRequest request) {

        urlService.updateIsActive(shortCode, request.isActive());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/urls/{shortCode}")
    public UrlResponse getUrl(@PathVariable String shortCode) {
        return urlService.getUrlDetails(shortCode);
    }

    @GetMapping("/urls")
    public Page<UrlResponse> getUrlsByUser(
            @RequestParam long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction) {
        if (page < 0) page = 0;
        if (size < 1) size = 10;
        if (size > 100) size = 100;
        return urlService.getUrlsByUser(
                userId, page, size, sortBy, direction
        );
    }


    @GetMapping("/urls/{shortCode}/analytics")
    public UrlAnalyticsResponse  getUrlAnalytics(@PathVariable String shortCode) {
        return  urlService.getUrlAnalytics(shortCode);
    }

    @GetMapping("/urls/{shortCode}/analytics/browsers")
    public Map<String,Long> getUrlAnalyticsBrowser(@PathVariable String shortCode) {
        return urlService.getUrlAnalyticsBrowser(shortCode);
    }

    @GetMapping("/urls/{shortCode}/analytics/referrers")
    public Map<String,Long> getUrlAnalyticsReferrer(@PathVariable String shortCode) {
        return urlService.getUrlAnalyticsReferrer(shortCode);
    }

    @GetMapping("/urls/{shortCode}/analytics/clicks")
    public Map<LocalDate,Long> getUrlClicksByDate(@PathVariable String shortCode) {
        return urlService.getUrlClicksByDate(shortCode);
    }

}

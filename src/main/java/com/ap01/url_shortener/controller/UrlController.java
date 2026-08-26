package com.ap01.url_shortener.controller;

import com.ap01.url_shortener.dto.request.CreateUrlRequest;
import com.ap01.url_shortener.dto.response.CreateUrlResponse;
import com.ap01.url_shortener.entity.Url;
import com.ap01.url_shortener.service.UrlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController()
@RequestMapping("/shortlinks/api")
public class UrlController {

    private UrlService urlService;
    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/urls")
    public CreateUrlResponse createUrl(@RequestBody CreateUrlRequest request) {

        return urlService.save(request);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirectUrl(@PathVariable String shortCode) {
        Url url = urlService.getByShortCode(shortCode);
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(url.getOriginalUrl()))
                .build();
    }
}

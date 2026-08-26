package com.ap01.url_shortener.controller;

import com.ap01.url_shortener.dto.request.CreateUrlRequest;
import com.ap01.url_shortener.dto.response.CreateUrlResponse;
import com.ap01.url_shortener.service.UrlService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

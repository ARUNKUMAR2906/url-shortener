package com.ap01.url_shortener.service;

import com.ap01.url_shortener.dto.request.CreateUrlRequest;
import com.ap01.url_shortener.dto.response.CreateUrlResponse;
import com.ap01.url_shortener.entity.Url;
import com.ap01.url_shortener.exception.ShortCodeNotFoundException;
import com.ap01.url_shortener.repository.UrlRepository;
import com.ap01.url_shortener.utils.Base62;
import org.springframework.stereotype.Service;

@Service
public class UrlService {
    private final String baseUrl = "https://short.ly";
    private final UrlRepository urlRepository;

    public UrlService(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    public CreateUrlResponse save(CreateUrlRequest request) {
        CreateUrlResponse response = new CreateUrlResponse();
        Url url = new Url();
        url.setOriginalUrl(request.getOriginalUrl());

        Url savedUrl = urlRepository.save(url);
        Long savedId = savedUrl.getId();
        String encodedId = Base62.encode(savedId);

        savedUrl.setShortCode(encodedId);
        urlRepository.save(savedUrl);

        response.setShortUrl(baseUrl + "/" + savedUrl.getShortCode());
        response.setShortCode(encodedId);
        response.setOriginalUrl(savedUrl.getOriginalUrl());
        response.setExpiresAt(url.getExpiresAt());

        return response;
    }

    public Url getByShortCode(String shortCode) {
        return urlRepository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new ShortCodeNotFoundException(shortCode));
    }
}

package com.ap01.url_shortener.service;

import com.ap01.url_shortener.dto.request.CreateUrlRequest;
import com.ap01.url_shortener.dto.response.CreateUrlResponse;
import com.ap01.url_shortener.entity.Url;
import com.ap01.url_shortener.exception.ShortCodeNotFoundException;
import com.ap01.url_shortener.repository.UrlRepository;
import com.ap01.url_shortener.utils.Base62;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class UrlService {
    @Value("${app.base-url}")
    private String baseUrl;
    @Value("${app.cache.ttl-hours}")
    private long cacheExpireTime;
    private final StringRedisTemplate stringRedisTemplate;
    private final UrlRepository urlRepository;

    public UrlService(UrlRepository urlRepository, StringRedisTemplate stringRedisTemplate) {
        this.urlRepository = urlRepository;
        this.stringRedisTemplate = stringRedisTemplate;
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
        String key = "url:" + shortCode;
        String cachedUrl = stringRedisTemplate.opsForValue().get(key);

        //check redis
        if(cachedUrl != null){
            Url url = new Url();
            url.setShortCode(shortCode);
            url.setOriginalUrl(cachedUrl);
            System.out.println("cache hit");
            return url;
        }

        //cache miss -> MySql
        System.out.println("cache miss");
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new ShortCodeNotFoundException(shortCode));

        //Put OriginalUrl into redis
        stringRedisTemplate.opsForValue().set(key, url.getOriginalUrl(), Duration.ofHours(cacheExpireTime));
        return url;
    }

}

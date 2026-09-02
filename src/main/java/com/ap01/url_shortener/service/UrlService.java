package com.ap01.url_shortener.service;

import ch.qos.logback.core.pattern.Converter;
import com.ap01.url_shortener.dto.request.CreateUrlRequest;
import com.ap01.url_shortener.dto.response.CreateUrlResponse;
import com.ap01.url_shortener.dto.response.UrlResponse;
import com.ap01.url_shortener.entity.Url;
import com.ap01.url_shortener.entity.User;
import com.ap01.url_shortener.enums.ExpirationOption;
import com.ap01.url_shortener.exception.ShortCodeExpiredException;
import com.ap01.url_shortener.exception.ShortCodeInactiveException;
import com.ap01.url_shortener.exception.ShortCodeNotFoundException;
import com.ap01.url_shortener.exception.UserNotFoundException;
import com.ap01.url_shortener.repository.UrlRepository;
import com.ap01.url_shortener.repository.UserRepository;
import com.ap01.url_shortener.utils.Base62;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Function;

@Service
public class UrlService {
    @Value("${app.base-url}")
    private String baseUrl;
    @Value("${app.cache.ttl-hours}")
    private long cacheExpireTime;
    private final StringRedisTemplate stringRedisTemplate;
    private final UrlRepository urlRepository;
    private final UserRepository userRepository;

    public UrlService(UrlRepository urlRepository, StringRedisTemplate stringRedisTemplate, UserRepository userRepository) {
        this.urlRepository = urlRepository;
        this.stringRedisTemplate = stringRedisTemplate;
        this.userRepository = userRepository;
    }

    public CreateUrlResponse save(CreateUrlRequest request) {
        CreateUrlResponse response = new CreateUrlResponse();
        Url url = new Url();
        url.setOriginalUrl(request.getOriginalUrl());
        url.setExpiresAt(getExpiresAt(request.getExpiration()));

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

        //checks expiration
        if (url.getExpiresAt() != null &&
                !url.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new ShortCodeExpiredException(shortCode);
        }
        //checks isActive
        if (!url.getActive()) {
            throw new ShortCodeInactiveException(shortCode);
        }

        //Put OriginalUrl into redis
        Duration duration = getCacheDuration(url);
        stringRedisTemplate.opsForValue().set(key, url.getOriginalUrl(), duration);


        return url;
    }

    public void updateIsActive(String shortCode, boolean isActive) {

        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new ShortCodeNotFoundException(shortCode));

        url.setActive(isActive);

        urlRepository.save(url);

        stringRedisTemplate.delete("url:" + shortCode);
    }

    public UrlResponse getUrlDetails(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new ShortCodeNotFoundException(shortCode));
        UrlResponse response = new UrlResponse();
        response.setShortCode(url.getShortCode());
        response.setShortUrl(baseUrl + "/" + url.getShortCode());
        response.setOriginalUrl(url.getOriginalUrl());
        response.setCreatedAt(url.getCreatedAt());
        response.setExpiresAt(url.getExpiresAt());
        response.setActive(url.getActive());
        return response;
    }

    public Page<UrlResponse> getUrlsByUser(
            long userId,
            int page,
            int size,
            String sortBy,
            Sort.Direction direction) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->
                        new UserNotFoundException(userId)
                );
        Sort sort = Sort.by(direction,sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Url> urls = urlRepository.findByUser(user, pageable);
        return urls.map(new Function<Url, UrlResponse>() {
            @Override
            public UrlResponse apply(Url url) {
                UrlResponse response = new UrlResponse();
                response.setShortCode(url.getShortCode());
                response.setShortUrl(baseUrl + "/" + url.getShortCode());
                response.setOriginalUrl(url.getOriginalUrl());
                response.setCreatedAt(url.getCreatedAt());
                response.setExpiresAt(url.getExpiresAt());
                response.setActive(url.getActive());
                return response;
            }
        });
    }




    //private helpers methods
    private LocalDateTime getExpiresAt(ExpirationOption expirationOption) {
            if(expirationOption == null) return null;

            return switch (expirationOption) {
                case ONE_DAY ->  LocalDateTime.now().plusDays(1);
                case TWO_DAYS ->  LocalDateTime.now().plusDays(2);
                case THREE_DAYS ->  LocalDateTime.now().plusDays(3);
            };
    }

    private Duration getCacheDuration(Url url) {

        Duration configuredTtl =
                Duration.ofHours(cacheExpireTime);

        if (url.getExpiresAt() == null) {
            return configuredTtl;
        }

        Duration remaining =
                Duration.between(
                        LocalDateTime.now(),
                        url.getExpiresAt()
                );

        return remaining.compareTo(configuredTtl) < 0
                ? remaining
                : configuredTtl;
    }
}

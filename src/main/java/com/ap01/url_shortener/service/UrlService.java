package com.ap01.url_shortener.service;

import com.ap01.url_shortener.dto.request.CreateUrlRequest;
import com.ap01.url_shortener.dto.response.*;
import com.ap01.url_shortener.entity.Click;
import com.ap01.url_shortener.entity.Url;
import com.ap01.url_shortener.entity.User;
import com.ap01.url_shortener.enums.ExpirationOption;
import com.ap01.url_shortener.exception.ShortCodeExpiredException;
import com.ap01.url_shortener.exception.ShortCodeInactiveException;
import com.ap01.url_shortener.exception.ShortCodeNotFoundException;
import com.ap01.url_shortener.exception.UserNotFoundException;
import com.ap01.url_shortener.repository.ClickRepository;
import com.ap01.url_shortener.repository.UrlRepository;
import com.ap01.url_shortener.repository.UserRepository;
import com.ap01.url_shortener.utils.Base62;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UrlService {
    @Value("${app.base-url}")
    private String baseUrl;
    @Value("${app.cache.ttl-hours}")
    private long cacheExpireTime;
    private final StringRedisTemplate stringRedisTemplate;
    private final UrlRepository urlRepository;
    private final UserRepository userRepository;
    private final ClickRepository clickRepository;

    public UrlService(UrlRepository urlRepository, StringRedisTemplate stringRedisTemplate, UserRepository userRepository, ClickRepository clickRepository) {
        this.urlRepository = urlRepository;
        this.stringRedisTemplate = stringRedisTemplate;
        this.userRepository = userRepository;
        this.clickRepository = clickRepository;
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

    public Url getByShortCode(String shortCode, HttpServletRequest request){
        String key = "url:" + shortCode;
        String cachedUrl = stringRedisTemplate.opsForValue().get(key);

        //check redis
        if(cachedUrl != null){
            Url url = new Url();
            url.setShortCode(shortCode);
            url.setOriginalUrl(cachedUrl);
            System.out.println("cache hit");
            recordClick(url,request);
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

        recordClick(url,request);
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
        return urls.map(url -> {
            UrlResponse response = new UrlResponse();
            response.setShortCode(url.getShortCode());
            response.setShortUrl(baseUrl + "/" + url.getShortCode());
            response.setOriginalUrl(url.getOriginalUrl());
            response.setCreatedAt(url.getCreatedAt());
            response.setExpiresAt(url.getExpiresAt());
            response.setActive(url.getActive());
            return response;
        });
    }


    public UrlAnalyticsResponse getUrlAnalytics(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(()->
                        new ShortCodeNotFoundException(shortCode));
        UrlAnalyticsResponse response = new UrlAnalyticsResponse();
        response.setShortCode(shortCode);
        response.setTotalClicks(clickRepository.countByShortCode(shortCode));
        return response;
    }


    public Map<String,Long> getUrlAnalyticsBrowser(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(()->
                        new ShortCodeNotFoundException(shortCode));
        List<UrlAnalyticsBrowserResponse> res = clickRepository.findBrowsers(shortCode);
        return res.stream().collect(Collectors.toMap(
                UrlAnalyticsBrowserResponse::getBrowser,
                UrlAnalyticsBrowserResponse::getClicks
        ));
    }

    public Map<String,Long> getUrlAnalyticsReferrer(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(()->
                        new ShortCodeNotFoundException(shortCode));
        List<UrlAnalyticsReferrerResponse> res = clickRepository.findReferrers(shortCode);
        return res.stream().collect(Collectors.toMap(
                UrlAnalyticsReferrerResponse :: getReferrer,
                UrlAnalyticsReferrerResponse :: getCount
        ));
    }

    public Map<LocalDate,Long> getUrlClicksByDate(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new ShortCodeNotFoundException(shortCode));
        List<ClicksByDateResponse> res = clickRepository.findClicksByDate(shortCode);
        return res.stream().collect(Collectors.toMap(
                ClicksByDateResponse::getClickedAt,
                ClicksByDateResponse::getCount
        ));
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

    private void recordClick(Url url,HttpServletRequest request){
        Click click = new Click();
        click.setShortCode(url.getShortCode());
        click.setClickedAt(LocalDateTime.now());
        click.setIp(request.getRemoteAddr());
        click.setBrowser(request.getHeader("User-Agent"));
        click.setReferrer(request.getHeader("Referer"));
        clickRepository.save(click);
    }

}

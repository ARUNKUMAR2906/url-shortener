package com.ap01.url_shortener.service;

import com.ap01.url_shortener.entity.Click;
import com.ap01.url_shortener.entity.Url;
import com.ap01.url_shortener.repository.ClickRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ClickService {

    private final ClickRepository clickRepository;

    public ClickService(ClickRepository clickRepository) {
        this.clickRepository = clickRepository;
    }

    @Async("customTaskExecutor")
    public void recordClick(Url url, String ip, String browser, String referrer, String country) {
        System.out.println(
                "recordClick thread: " + Thread.currentThread().getName()
                        + " | time: " + LocalDateTime.now()
        );
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Click click = new Click();
        click.setShortCode(url.getShortCode());
        click.setClickedAt(LocalDateTime.now());
        click.setIp(ip);
        click.setBrowser(browser);
        click.setReferrer(referrer);
        click.setCountry(country);
        clickRepository.save(click);
    }
}

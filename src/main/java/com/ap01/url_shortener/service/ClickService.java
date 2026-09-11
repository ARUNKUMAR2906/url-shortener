package com.ap01.url_shortener.service;

import com.ap01.url_shortener.entity.Click;
import com.ap01.url_shortener.event.ClickEvent;
import com.ap01.url_shortener.repository.ClickRepository;
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
    public void recordClick(ClickEvent event) {
        System.out.println(
                "recordClick thread: " + Thread.currentThread().getName()
                        + " | time: " + LocalDateTime.now()
        );

        Click click = new Click();
        click.setShortCode(event.getShortCode());
        click.setClickedAt(event.getTime());
        click.setIp(event.getIp());
        click.setBrowser(event.getBrowser());
        click.setReferrer(event.getReferrer());
        click.setCountry(event.getCountry());
        System.out.println(event.getTime() + "--" + LocalDateTime.now());
        clickRepository.save(click);
    }
}

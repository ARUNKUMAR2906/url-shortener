package com.ap01.url_shortener.repository;

import com.ap01.url_shortener.entity.Click;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClickRepository extends JpaRepository<Click, Long> {

    long countByShortCode(String shortCode);
}

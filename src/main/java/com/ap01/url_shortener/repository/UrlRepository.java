package com.ap01.url_shortener.repository;

import com.ap01.url_shortener.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UrlRepository extends JpaRepository<Url, Long> {

    Optional<Url> findByShortCode(String shortCode);

}

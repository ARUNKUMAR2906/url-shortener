package com.ap01.url_shortener.repository;

import com.ap01.url_shortener.dto.response.ClicksByDateResponse;
import com.ap01.url_shortener.dto.response.UrlAnalyticsBrowserResponse;
import com.ap01.url_shortener.dto.response.UrlAnalyticsReferrerResponse;
import com.ap01.url_shortener.entity.Click;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface ClickRepository extends JpaRepository<Click, Long> {

    long countByShortCode(String shortCode);

    @Query("SELECT new com.ap01.url_shortener.dto.response.UrlAnalyticsBrowserResponse(c.browser, COUNT(c.id)) " +
            "FROM Click c " +
            "WHERE c.shortCode = :shortCode " +
            "GROUP BY c.browser")
    List<UrlAnalyticsBrowserResponse> findBrowsers(@Param("shortCode") String shortCode);
    @Query("SELECT new com.ap01.url_shortener.dto.response.UrlAnalyticsReferrerResponse(c.referrer,COUNT(c.id))" +
            "FROM Click c " +
            "WHERE c.shortCode = :shortCode" +
            " GROUP BY c.referrer")
    List<UrlAnalyticsReferrerResponse> findReferrers(@Param("shortCode") String shortCode);

    @Query("SELECT new com.ap01.url_shortener.dto.response.ClicksByDateResponse(CAST(c.clickedAt as localdate),COUNT(c.id))"+
            "FROM Click c "+
            "WHERE c.shortCode = :shortCode" +
            " GROUP BY CAST(c.clickedAt as localdate)")
    List<ClicksByDateResponse> findClicksByDate(@Param("shortCode") String shortCode);
}

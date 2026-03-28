package com.medibridge.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RateLimitService.class);
    private final Cache<String, Integer> cache;

    public RateLimitService() {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(1))
                .maximumSize(1000)
                .build();
    }

    public boolean isAllowed(String ip) {
        if (ip == null) return true;
        log.info("Rate limit check for IP: {}", ip);
        Integer count = cache.getIfPresent(ip);
        if (count == null) count = 0;
        if (count >= 10) return false;
        cache.put(ip, count + 1);
        return true;
    }
}

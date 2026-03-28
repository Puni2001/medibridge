package com.medibridge.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RateLimitService {

    private final Cache<String, AtomicInteger> requestCache;

    public RateLimitService() {
        this.requestCache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(1))
                .build();
    }

    public boolean isAllowed(String clientIp) {
        AtomicInteger count = requestCache.get(clientIp, k -> new AtomicInteger(0));
        return count.incrementAndGet() <= 10; // Max 10 requests per minute per IP
    }
}

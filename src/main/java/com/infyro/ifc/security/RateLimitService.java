package com.infyro.ifc.security;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitService {
    private record Window(long startedAt, int count) {}
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public void check(String key, int limit, long windowSeconds) {
        long now = Instant.now().getEpochSecond();
        windows.compute(key, (k, old) -> {
            if (old == null || now - old.startedAt() >= windowSeconds) return new Window(now, 1);
            return new Window(old.startedAt(), old.count() + 1);
        });
        Window current = windows.get(key);
        if (current != null && current.count() > limit)
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many requests");
        if (windows.size() > 10000) windows.entrySet().removeIf(e -> now - e.getValue().startedAt() > 3600);
    }
}
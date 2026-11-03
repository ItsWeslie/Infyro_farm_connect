package com.infyro.ifc.security;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;

class RateLimitServiceTest {
    @Test
    void permitsExactlyConfiguredNumberOfRequests() {
        RateLimitService limiter = new RateLimitService();
        for (int i = 0; i < 5; i++) limiter.check("signup:test", 5, 3600);
        assertEquals(429, assertThrows(ResponseStatusException.class,
                () -> limiter.check("signup:test", 5, 3600)).getStatusCode().value());
    }

    @Test
    void failedLoginLimitBlocksAfterConfiguredNumberOfFailures() {
        RateLimitService limiter = new RateLimitService();
        for (int i = 0; i < 5; i++) limiter.recordFailure("login:test", 3600);
        assertEquals(429, assertThrows(ResponseStatusException.class,
                () -> limiter.checkAllowed("login:test", 5, 3600)).getStatusCode().value());
    }
}

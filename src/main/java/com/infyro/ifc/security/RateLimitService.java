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

    public void check(String key, int limit, long seconds) {
        long now=Instant.now().getEpochSecond();
        Window current=windows.compute(key,(k,old)->old==null || now-old.startedAt()>=seconds
                ? new Window(now,1) : new Window(old.startedAt(),old.count()+1));
        enforce(current,limit);
        cleanup(now);
    }
    public void checkAllowed(String key,int limit,long seconds) {
        long now=Instant.now().getEpochSecond();
        Window current=windows.get(key);
        if(current!=null && now-current.startedAt()<seconds) enforce(current,limit);
        cleanup(now);
    }
    public void recordFailure(String key,long seconds) {
        long now=Instant.now().getEpochSecond();
        windows.compute(key,(k,old)->old==null || now-old.startedAt()>=seconds
                ? new Window(now,1) : new Window(old.startedAt(),old.count()+1));
        cleanup(now);
    }
    public void reset(String key) { windows.remove(key); }
    private void enforce(Window current,int limit) {
        if(current!=null && current.count()>=limit)
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,"Too many requests");
    }
    private void cleanup(long now) {
        if(windows.size()>10000) windows.entrySet().removeIf(e->now-e.getValue().startedAt()>3600);
    }
}
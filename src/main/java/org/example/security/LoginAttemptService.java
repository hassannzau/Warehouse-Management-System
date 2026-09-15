package org.example.security;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCKOUT_DURATION = Duration.ofMinutes(15);

    private final ConcurrentHashMap<String, Attempt> attemptsByUsername = new ConcurrentHashMap<>();

    public boolean isLocked(String username) {
        Attempt attempt = attemptsByUsername.get(key(username));
        return attempt != null && attempt.lockedUntil != null && Instant.now().isBefore(attempt.lockedUntil);
    }

    public void recordFailure(String username) {
        attemptsByUsername.compute(key(username), (k, existing) -> {
            boolean expired = existing != null && existing.lockedUntil != null && Instant.now().isAfter(existing.lockedUntil);
            int count = (existing == null || expired) ? 1 : existing.count + 1;
            Instant lockedUntil = count >= MAX_ATTEMPTS ? Instant.now().plus(LOCKOUT_DURATION) : null;
            return new Attempt(count, lockedUntil);
        });
    }

    public void recordSuccess(String username) {
        attemptsByUsername.remove(key(username));
    }

    private String key(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }

    private record Attempt(int count, Instant lockedUntil) {
    }
}

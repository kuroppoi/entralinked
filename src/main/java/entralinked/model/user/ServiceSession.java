package entralinked.model.user;

import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;

public record ServiceSession(User user, String service, String branchCode, String challengeHash, LocalDateTime expiry) {
    
    public ServiceSession(User user, String service, String branchCode, String challengeHash, long expiry, TemporalUnit expiryUnit) {
        this(user, service, branchCode, challengeHash, LocalDateTime.now().plus(expiry, expiryUnit));
    }
    
    public boolean hasExpired() {
        return LocalDateTime.now().isAfter(expiry);
    }
}

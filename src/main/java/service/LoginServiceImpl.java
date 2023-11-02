package service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LoginServiceImpl implements LoginService {

    public static final int DEFAULT_SESSION_EXPIRATION_MINUTES = 10;
    public static final int DEFAULT_PRUNE_SCHEDULE_MINUTES = 15;

    private static class ExpirationRecord {
        final Instant expiration;
        final int userId;

        public ExpirationRecord(Instant expiration, int userId) {
            this.expiration = expiration;
            this.userId = userId;
        }
    }

    private final ScheduledExecutorService pruningScheduler;
    private final Map<Integer, String> sessionKeysByUserId = new HashMap<>();
    private final Map<String, ExpirationRecord> sessionKeysExpiration = new HashMap<>();

    private int sessionKeyExpirationAmount = DEFAULT_SESSION_EXPIRATION_MINUTES;
    private ChronoUnit sessionKeyExpirationUnit = ChronoUnit.MINUTES;

    private int pruneScheduleAmount = DEFAULT_PRUNE_SCHEDULE_MINUTES;
    private TimeUnit pruneScheduleUnit = TimeUnit.MINUTES;

    public LoginServiceImpl(ScheduledExecutorService pruningScheduler) {
        this.pruningScheduler = pruningScheduler;
        schedulePruning();
    }

    public LoginServiceImpl(int sessionKeyExpirationAmount, ChronoUnit sessionKeyExpirationUnit,
                            ScheduledExecutorService pruningScheduler,
                            int pruneScheduleAmount, TimeUnit pruneScheduleUnit) {
        this.sessionKeyExpirationAmount = sessionKeyExpirationAmount;
        this.sessionKeyExpirationUnit = sessionKeyExpirationUnit;
        this.pruningScheduler = pruningScheduler;
        this.pruneScheduleAmount = pruneScheduleAmount;
        this.pruneScheduleUnit = pruneScheduleUnit;
        schedulePruning();
    }

    @Override
    public synchronized String getSessionKey(int userId) {
        if (!sessionKeysByUserId.containsKey(userId)) {
            return createNewSessionKey(userId);
        } else {
            final String sessionKey = sessionKeysByUserId.get(userId);
            if (isSessionKeyExpired(sessionKey)) {
                return createNewSessionKey(userId);
            } else {
                return sessionKey;
            }
        }
    }

    @Override
    public synchronized boolean isSessionKeyExpired(String sessionKey) {
        final Instant now = Instant.now();
        final Instant expirationTime = sessionKeysExpiration.get(sessionKey).expiration;
        return expirationTime == null || expirationTime.isBefore(now);
    }

    @Override
    public synchronized Integer getUserIdForSession(String sessionKey) {
        final ExpirationRecord expirationRecord = sessionKeysExpiration.get(sessionKey);
        return expirationRecord == null ? null : expirationRecord.userId;
    }

    @Override
    public void evictSession(int userId) {
        if (sessionKeysByUserId.containsKey(userId)) {
            String sessionKey = sessionKeysByUserId.get(userId);
            sessionKeysByUserId.remove(userId);
            sessionKeysExpiration.remove(sessionKey);
        }
    }

    private String createNewSessionKey(int userId) {
        final Instant now = Instant.now();
        final String sessionKey = UUID.randomUUID().toString();
        sessionKeysByUserId.put(userId, sessionKey);
        sessionKeysExpiration.put(sessionKey, new ExpirationRecord(
                now.plus(sessionKeyExpirationAmount,
                        sessionKeyExpirationUnit), userId));
        return sessionKey;
    }

    private void schedulePruning() {
        if (pruningScheduler != null) {
            pruningScheduler.scheduleWithFixedDelay(this::pruneExpiredSessionKeys,
                    pruneScheduleAmount, pruneScheduleAmount, pruneScheduleUnit);
        }
    }

    private synchronized void pruneExpiredSessionKeys() {
        final Instant now = Instant.now();
        List<String> expiredSessionKeys = new LinkedList<>();
        List<Integer> expiredUserIds = new LinkedList<>();
        sessionKeysExpiration.forEach((sessionKey, expirationRecord) -> {
            if (expirationRecord.expiration.isBefore(now)) {
                expiredSessionKeys.add(sessionKey);
                expiredUserIds.add(expirationRecord.userId);
            }
        });
        expiredSessionKeys.forEach(sessionKeysExpiration::remove);
        expiredUserIds.forEach(sessionKeysByUserId::remove);
    }
}

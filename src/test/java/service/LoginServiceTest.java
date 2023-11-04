package service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.impl.LoginServiceImpl;
import service.impl.UserServiceImpl;

import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class LoginServiceTest {
    static final int sessionKeyExpirationAmount = 500;
    static final ChronoUnit sessionKeyExpirationUnit = ChronoUnit.MILLIS;

    static final int pruneScheduleAmount = 1000;
    final TimeUnit pruneScheduleUnit = TimeUnit.MILLISECONDS;

    Random random;

    LoginService loginService;
    UserService userService;
    ScheduledExecutorService scheduler;

    @BeforeEach
    void setUp() {
        random = new Random();
        scheduler = Executors.newScheduledThreadPool(1);
        userService = new UserServiceImpl();
        loginService = new LoginServiceImpl(
                sessionKeyExpirationAmount, sessionKeyExpirationUnit,
                scheduler,
                pruneScheduleAmount, pruneScheduleUnit);
    }

    @AfterEach
    void teardown() {
        scheduler.shutdown();
    }

    @Test
    @DisplayName("Given a new user, " +
            "when user first logs in, " +
            "then return non-null sessionKey, " +
            "and sessionKey maps to userId, " +
            "and session is not expired")
    void loginNewUser() {
        final int userId = 1;
        final String sessionKey = loginService.getSessionKey(userId);
        assertNotEquals(null, sessionKey);
        final Integer userIdForSession = loginService.getUserIdForSession(sessionKey);
        assertNotEquals(null, userIdForSession);
        assertEquals(userId, userIdForSession);
        final boolean isSessionKeyExpired = loginService.isSessionKeyExpired(sessionKey);
        assertFalse(isSessionKeyExpired);
    }

    @Test
    @DisplayName("Given a new user, " +
            "when user logs in twice, " +
            "then return same sessionKey as first time")
    void loginNewUserTwice() {
        final int userId = 1;
        final String firstSessionKey = loginService.getSessionKey(userId);
        final String secondSessionKey = loginService.getSessionKey(userId);
        assertEquals(firstSessionKey, secondSessionKey);
    }

    @Test
    @DisplayName("Given a new user, " +
            "when user logs in twice, " +
            "and expiration period has passed, " +
            "then second session key is different, " +
            "and first session key is expired")
    void loginAfterExpirationPeriod() throws InterruptedException {
        final int userId = 1;
        final String firstSessionKey = loginService.getSessionKey(userId);
        Thread.sleep(sessionKeyExpirationAmount);
        final String secondSessionKey = loginService.getSessionKey(userId);
        assertNotEquals(firstSessionKey, secondSessionKey);
        assertTrue(loginService.isSessionKeyExpired(firstSessionKey));
    }

    @Test
    @DisplayName("Given a new user, " +
            "when user logs in twice, " +
            "and expiration period has passed, " +
            "and expired session keys are purged, " +
            "then first session key maps to null, " +
            "and second session key maps to userId")
    void loginAfterExpirationPeriodAndPurge() throws InterruptedException {
        final int userId = 1;
        final String firstSessionKey = loginService.getSessionKey(userId);
        Thread.sleep(sessionKeyExpirationAmount);
        Thread.sleep(pruneScheduleAmount);
        final String secondSessionKey = loginService.getSessionKey(userId);
        final Integer userIdForFirstSession = loginService.getUserIdForSession(firstSessionKey);
        final Integer userIdForSecondSession = loginService.getUserIdForSession(secondSessionKey);
        assertNull(userIdForFirstSession);
        assertEquals(userId, userIdForSecondSession);
    }

    @Test
    @DisplayName("Given multiple users, " +
            "when all users login concurrently, " +
            "then all session keys are unique")
    void loginMultipleUsersConcurrently() throws InterruptedException {
        final int repeat = 10;

        for (int iteration = 0; iteration < repeat; iteration += 1) {
            final int n = 100_000;
            final long distinctSessionKeys = IntStream
                    .iterate(1, i -> i + 1)
                    .limit(n)
                    .parallel()
                    .mapToObj(loginService::getSessionKey)
                    .distinct()
                    .count();
            assertEquals(n, distinctSessionKeys);

            Thread.sleep(10 + random.nextInt(150));
        }
    }

}
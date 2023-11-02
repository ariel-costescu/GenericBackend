package service;

public interface LoginService {
    String getSessionKey(int userId);

    boolean isSessionKeyExpired(String sessionKey);

    Integer getUserIdForSession(String sessionKey);
}

package service;

public interface UserService {
    Integer registerUser(int userId);

    boolean isUserRegistered(int userId);

    void unRegisterUser(int userId);
}

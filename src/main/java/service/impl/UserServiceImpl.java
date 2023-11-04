package service.impl;

import service.UserService;

import java.util.HashSet;
import java.util.Set;

public class UserServiceImpl implements UserService {
    private final Set<Integer> registeredUsers = new HashSet<>();

    @Override
    public synchronized Integer registerUser(int userId) {
        registeredUsers.add(userId);
        return userId;
    }

    @Override
    public synchronized boolean isUserRegistered(int userId) {
        return registeredUsers.contains(userId);
    }

    @Override
    public synchronized void unregisterUser(int userId) {
        registeredUsers.remove(userId);
    }
}

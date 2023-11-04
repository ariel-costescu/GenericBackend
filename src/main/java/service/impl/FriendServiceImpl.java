package service.impl;

import service.FriendService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FriendServiceImpl implements FriendService {
    private final Map<Integer, Set<Integer>> friendListsByUserId= new HashMap<>();
    private final Map<Integer, Set<Integer>> friendRequestReceivedByUserId = new HashMap<>();
    private final Map<Integer, Set<Integer>> friendRequestSentByUserId = new HashMap<>();

    @Override
    public synchronized Integer addFriendRequest(Integer authenticatedUserId, Integer friendUserId) {
        getFriendRequestSentBy(authenticatedUserId).add(friendUserId);
        getFriendRequestsReceivedBy(friendUserId).add(authenticatedUserId);
        return friendUserId;
    }

    @Override
    public synchronized void removeFriendRequest(Integer authenticatedUserId, Integer friendUserId) {
        getFriendRequestSentBy(authenticatedUserId).remove(friendUserId);
        getFriendRequestsReceivedBy(friendUserId).remove(authenticatedUserId);
    }

    @Override
    public Integer acceptFriendRequest(Integer authenticatedUserId, Integer friendUserId) {
        final boolean wasRequestedByFriend = getFriendRequestsReceivedBy(authenticatedUserId).contains(friendUserId);
        if (!wasRequestedByFriend) {
            return null;
        } else {
            getFriendRequestsReceivedBy(authenticatedUserId).remove(friendUserId);
            getFriendRequestSentBy(friendUserId).remove(authenticatedUserId);
            getFriendListBy(authenticatedUserId).add(friendUserId);
            getFriendListBy(friendUserId).add(authenticatedUserId);
            return friendUserId;
        }
    }

    @Override
    public Integer declineFriendRequest(Integer authenticatedUserId, Integer friendUserId) {
        getFriendRequestsReceivedBy(authenticatedUserId).remove(friendUserId);
        getFriendRequestSentBy(friendUserId).remove(authenticatedUserId);
        return friendUserId;
    }

    @Override
    public List<Integer> getFriendList(Integer authenticatedUserId) {
        return getFriendListBy(authenticatedUserId).stream().toList();
    }

    @Override
    public List<Integer> getFriendRequestsSent(Integer authenticatedUserId) {
        return getFriendRequestSentBy(authenticatedUserId).stream().toList();
    }

    @Override
    public List<Integer> getFriendRequestsReceived(Integer authenticatedUserId) {
        return getFriendRequestsReceivedBy(authenticatedUserId).stream().toList();
    }

    private Set<Integer> getFriendRequestSentBy(Integer userId) {
        return friendRequestSentByUserId.computeIfAbsent(userId,
                key -> new HashSet<>());
    }

    private Set<Integer> getFriendRequestsReceivedBy(Integer userId) {
        return friendRequestReceivedByUserId.computeIfAbsent(userId,
                key -> new HashSet<>());
    }

    private Set<Integer> getFriendListBy(Integer userId) {
        return friendListsByUserId.computeIfAbsent(userId,
                key -> new HashSet<>());
    }
}

package service;

import java.util.List;

public interface FriendService {

    Integer addFriendRequest(Integer authenticatedUserId, Integer friendUserId);

    void removeFriendRequest(Integer authenticatedUserId, Integer friendUserId);

    Integer acceptFriendRequest(Integer authenticatedUserId, Integer friendUserId);

    Integer declineFriendRequest(Integer authenticatedUserId, Integer friendUserId);

    List<Integer> getFriendList(Integer authenticatedUserId);

    List<Integer> getFriendRequestsSent(Integer authenticatedUserId);

    List<Integer> getFriendRequestsReceived(Integer authenticatedUserId);
}

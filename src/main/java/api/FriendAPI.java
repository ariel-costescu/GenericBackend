package api;

import api.annotation.MethodType;
import api.annotation.RequestParam;
import api.annotation.RestMethod;
import com.sun.net.httpserver.HttpExchange;

public interface FriendAPI {

    @RestMethod(methodType = MethodType.POST, pathPattern = "/friend/request/(?<friendUserId>\\d+)")
    void addFriendRequest(HttpExchange exchange, Integer authenticatedUserId,
                      @RequestParam(name = "friendUserId") String friendUserIdParam);

    @RestMethod(methodType = MethodType.DELETE, pathPattern = "/friend/request/(?<friendUserId>\\d+)")
    void removeFriendRequest(HttpExchange exchange, Integer authenticatedUserId,
                      @RequestParam(name = "friendUserId") String friendUserIdParam);

    @RestMethod(methodType = MethodType.POST, pathPattern = "/friend/accept/(?<friendUserId>\\d+)")
    void acceptFriendRequest(HttpExchange exchange, Integer authenticatedUserId,
                      @RequestParam(name = "friendUserId") String friendUserIdParam);

    @RestMethod(methodType = MethodType.POST, pathPattern = "/friend/decline/(?<friendUserId>\\d+)")
    void declineFriendRequest(HttpExchange exchange, Integer authenticatedUserId,
                      @RequestParam(name = "friendUserId") String friendUserIdParam);

    @RestMethod(methodType = MethodType.GET, pathPattern = "/friend/list")
    void getFriendList(HttpExchange exchange, Integer authenticatedUserId);

    @RestMethod(methodType = MethodType.GET, pathPattern = "/friend/requests/sent")
    void getFriendRequestsSent(HttpExchange exchange, Integer authenticatedUserId);

    @RestMethod(methodType = MethodType.GET, pathPattern = "/friend/requests/received")
    void getFriendRequestsReceived(HttpExchange exchange, Integer authenticatedUserId);
}

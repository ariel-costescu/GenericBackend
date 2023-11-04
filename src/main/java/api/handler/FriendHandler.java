package api.handler;

import api.FriendAPI;
import api.annotation.MethodType;
import api.annotation.RequestParam;
import api.annotation.RestMethod;
import com.sun.net.httpserver.HttpExchange;
import service.FriendService;
import service.LoginService;
import service.UserService;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.System.Logger.Level.ERROR;

public class FriendHandler extends AbstractHandler implements FriendAPI {

    private static final System.Logger LOGGER = System.getLogger("api.handler.FriendHandler");
    private final UserService userService;
    private final FriendService friendService;

    public FriendHandler(LoginService loginService, UserService userService, FriendService friendService) {
        this.loginService = loginService;
        this.userService = userService;
        this.friendService = friendService;
    }

    @Override
    public System.Logger getLogger() {
        return LOGGER;
    }

    @Override
    @RestMethod(methodType = MethodType.POST, pathPattern = "/friend/request/(?<friendUserId>\\d+)")
    public void addFriendRequest(HttpExchange exchange, Integer authenticatedUserId,
                                 @RequestParam(name = "friendUserId") String friendUserIdParam) {
        Integer friendUserId = checkFriendUserId(exchange, friendUserIdParam);
        if (friendUserId != null) {
            Integer responseId = friendService.addFriendRequest(authenticatedUserId, friendUserId);
            if (responseId == null) {
                LOGGER.log(ERROR, "Unable to add friend request for friendUserId");
                handleInternalServerError(exchange);
            } else {
                sendResponse(exchange, responseId.toString());
            }
        }
    }

    @Override
    @RestMethod(methodType = MethodType.DELETE, pathPattern = "/friend/request/(?<friendUserId>\\d+)")
    public void removeFriendRequest(HttpExchange exchange, Integer authenticatedUserId,
                                    @RequestParam(name = "friendUserId") String friendUserIdParam) {
        Integer friendUserId = checkFriendUserId(exchange, friendUserIdParam);
        if (friendUserId != null) {
            friendService.removeFriendRequest(authenticatedUserId, friendUserId);
            respondWithStatusCode(exchange, 200);
        }
    }

    @Override
    @RestMethod(methodType = MethodType.POST, pathPattern = "/friend/accept/(?<friendUserId>\\d+)")
    public void acceptFriendRequest(HttpExchange exchange, Integer authenticatedUserId,
                                    @RequestParam(name = "friendUserId") String friendUserIdParam) {
        Integer friendUserId = checkFriendUserId(exchange, friendUserIdParam);
        if (friendUserId != null) {
            Integer responseId = friendService.acceptFriendRequest(authenticatedUserId, friendUserId);
            if (responseId == null) {
                LOGGER.log(ERROR, "Unable to accept friend request for friendUserId {0}", friendUserId);
                handleBadRequest(exchange);
            } else {
                sendResponse(exchange, responseId.toString());
            }
        }
    }

    @Override
    @RestMethod(methodType = MethodType.POST, pathPattern = "/friend/decline/(?<friendUserId>\\d+)")
    public void declineFriendRequest(HttpExchange exchange, Integer authenticatedUserId,
                                     @RequestParam(name = "friendUserId") String friendUserIdParam) {
        Integer friendUserId = checkFriendUserId(exchange, friendUserIdParam);
        if (friendUserId != null) {
            Integer responseId = friendService.declineFriendRequest(authenticatedUserId, friendUserId);
            if (responseId == null) {
                LOGGER.log(ERROR, "Unable to decline friend request for friendUserId");
                handleInternalServerError(exchange);
            } else {
                sendResponse(exchange, responseId.toString());
            }
        }
    }

    @Override
    @RestMethod(methodType = MethodType.GET, pathPattern = "/friend/list")
    public void getFriendList(HttpExchange exchange, Integer authenticatedUserId) {
        List<Integer> friendList = friendService.getFriendList(authenticatedUserId);
        String response = intListToJson(friendList);
        sendResponse(exchange, response);
    }

    @Override
    @RestMethod(methodType = MethodType.GET, pathPattern = "/friend/requests/sent")
    public void getFriendRequestsSent(HttpExchange exchange, Integer authenticatedUserId) {
        List<Integer> friendList = friendService.getFriendRequestsSent(authenticatedUserId);
        String response = intListToJson(friendList);
        sendResponse(exchange, response);
    }

    @Override
    @RestMethod(methodType = MethodType.GET, pathPattern = "/friend/requests/received")
    public void getFriendRequestsReceived(HttpExchange exchange, Integer authenticatedUserId) {
        List<Integer> friendList = friendService.getFriendRequestsReceived(authenticatedUserId);
        String response = intListToJson(friendList);
        sendResponse(exchange, response);
    }

    private Integer getFriendUserId(String friendUserIdParam) {
        if (friendUserIdParam == null) {
            LOGGER.log(ERROR, "Bad friend user id ");
            return null;
        } else {
            int friendUserId;
            try {
                friendUserId = Integer.parseInt(friendUserIdParam);
            } catch (NumberFormatException e) {
                LOGGER.log(ERROR, "Couldn't parse friendUserId {0}", friendUserIdParam, e);
                return null;
            }
            return friendUserId;
        }
    }

    private Integer checkFriendUserId(HttpExchange exchange, String friendUserIdParam) {
        Integer friendUserId = getFriendUserId(friendUserIdParam);
        if (friendUserId == null) {
            handleBadRequest(exchange);
            return null;
        } else {
            if (!userService.isUserRegistered(friendUserId)) {
                LOGGER.log(ERROR, "Friend user id {0} is not registered", friendUserId);
                handleNotFoundRequest(exchange);
                return null;
            }
        }
        return friendUserId;
    }

    private String intListToJson(List<Integer> friendList) {
        return String.format("[%s]",
                friendList.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(",")));
    }
}

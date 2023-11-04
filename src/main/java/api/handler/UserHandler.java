package api.handler;

import api.UserAPI;
import api.annotation.MethodType;
import api.annotation.RequestParam;
import api.annotation.RestMethod;
import com.sun.net.httpserver.HttpExchange;
import service.LoginService;
import service.UserService;

import static java.lang.System.Logger.Level.INFO;

public class UserHandler extends AbstractHandler implements UserAPI {

    private static final System.Logger LOGGER = System.getLogger("api.handler.UserHandler");
    private final UserService userService;

    public UserHandler(LoginService loginService, UserService userService) {
        this.loginService = loginService;
        this.userService = userService;
    }

    @Override
    public System.Logger getLogger() {
        return LOGGER;
    }
    @RestMethod(methodType = MethodType.POST, pathPattern = "/user/(?<userId>\\d+)", authenticated = false)
    public void registerUser(HttpExchange exchange,
                         @RequestParam(name = "userId") String userIdParam) {
        Integer userId = getUserIdFromRequestParam(userIdParam);
        if (userId != null) {
            String response;
            if (userService.isUserRegistered(userId)) {
                response = userId.toString();
            } else {
                response = userService.registerUser(userId).toString();
            }
            sendResponse(exchange, response);
        } else {
            handleBadRequest(exchange);
        }
    }

    @Override
    @RestMethod(methodType = MethodType.DELETE, pathPattern = "/user")
    public void unregisterUser(HttpExchange exchange, Integer authenticatedUserId) {
        loginService.evictSession(authenticatedUserId);
        userService.unregisterUser(authenticatedUserId);
        respondWithStatusCode(exchange, 200);
        LOGGER.log(INFO, "UserId {0} was unregistered", authenticatedUserId);
    }
}

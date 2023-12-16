package api.handler;

import api.AuthAPI;
import api.annotation.MethodType;
import api.annotation.RequestParam;
import api.annotation.RestMethod;
import com.sun.net.httpserver.HttpExchange;
import service.LoginService;
import service.UserService;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class AuthHandler extends AbstractHandler implements AuthAPI {

    private static final System.Logger LOGGER = System.getLogger("api.handler.AuthHandler");
    private final UserService userService;

    public AuthHandler(LoginService loginService, UserService userService) {
        this.loginService = loginService;
        this.userService = userService;
    }

    @Override
    public System.Logger getLogger() {
        return LOGGER;
    }

    @Override
    @RestMethod(methodType = MethodType.GET, pathPattern = "/login/(?<userId>\\d+)", authenticated = false)
    public void login(HttpExchange exchange,
                      @RequestParam(name = "userId") String userIdParam) {
        boolean badRequest = true;

        if (userIdParam != null) {
            Integer userId = getUserIdFromRequestParam(userIdParam);
            if (userId != null) {
                if (!userService.isUserRegistered(userId)) {
                    LOGGER.log(ERROR, "User {0} is not registered", userId);
                    handleNotFoundRequest(exchange);
                    return;
                } else {
                    String response = loginService.getSessionKey(userId);
                    if (response != null) {
                        badRequest = false;
                        sendResponse(exchange, response);
                        LOGGER.log(INFO, "UserId {0} logged in with session key {1}", userId, response);
                    }
                }
            }
        }

        if (badRequest) {
            handleBadRequest(exchange);
        }
    }

    @Override
    @RestMethod(methodType = MethodType.DELETE, pathPattern = "/login")
    public void logout(HttpExchange exchange, Integer authenticatedUserId) {
        loginService.evictSession(authenticatedUserId);
        respondWithStatusCode(exchange, 200);
        LOGGER.log(INFO, "UserId {0} logged out", authenticatedUserId);
    }
}

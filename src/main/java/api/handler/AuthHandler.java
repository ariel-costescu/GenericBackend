package api.handler;

import api.AuthAPI;
import api.annotation.MethodType;
import api.annotation.RequestParam;
import api.annotation.RestMethod;
import com.sun.net.httpserver.HttpExchange;
import service.LoginService;

import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;

public class AuthHandler extends AbstractHandler implements AuthAPI {

    private static final System.Logger LOGGER = System.getLogger("api.handler.AuthHandler");

    public AuthHandler(LoginService loginService) {
        this.loginService = loginService;
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
            Integer userId = null;
            try {
                userId = Integer.parseInt(userIdParam);
            } catch (NumberFormatException e) {
                LOGGER.log(WARNING, "Unable to parse userId from path param {0}", userIdParam);
            }
            if (userId != null) {
                String response = loginService.getSessionKey(userId);
                if (response != null) {
                    badRequest = false;
                    sendResponse(exchange, response);
                    LOGGER.log(INFO, "UserId {0} logged in with session key {1}", userId, response);
                }
            }
        }

        if (badRequest) {
            handleBadRequest(exchange);
        }
    }

    @Override
    @RestMethod(methodType = MethodType.DELETE, pathPattern = "/login/(?<userId>\\d+)")
    public void logout(HttpExchange exchange,
                      @RequestParam(name = "userId") String userIdParam) {
        boolean badRequest = true;

        if (userIdParam != null) {
            Integer userId = null;
            try {
                userId = Integer.parseInt(userIdParam);
            } catch (NumberFormatException e) {
                LOGGER.log(WARNING, "Unable to parse userId from path param {0}", userIdParam);
            }
            if (userId != null) {
                badRequest = false;
                loginService.evictSession(userId);
                respondWithStatusCode(exchange, 200);
                LOGGER.log(INFO, "UserId {0} logged out", userId);
            }
        }

        if (badRequest) {
            handleBadRequest(exchange);
        }
    }
}

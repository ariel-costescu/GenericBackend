package handler;

import com.sun.net.httpserver.HttpExchange;
import service.LoginService;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;

/**
 * Request: GET /<userid>/login
 * Response: <sessionkey>
 */
public class LoginHandler extends Handleable {

    private static final System.Logger LOGGER = System.getLogger("handler.LoginHandler");

    private final LoginService loginService;

    public LoginHandler(LoginService loginService) {
        this.loginService = loginService;
    }

    private static final Pattern pathPattern =
            Pattern.compile("/(?<userId>\\d+)/login");

    @Override
    public System.Logger getLogger() {
        return LOGGER;
    }

    @Override
    public boolean canHandleRequest(String requestMethod, URI requestURI) {
        if (requestMethod.equals("GET")) {
            final String userIdPathParam = getUserIdPathParam(requestURI);
            return userIdPathParam != null;
        }
        return false;
    }

    @Override
    public void handle(HttpExchange exchange) {
        boolean badRequest = true;

        final String userIdPathParam = getUserIdPathParam(exchange.getRequestURI());
        if (userIdPathParam != null) {
            Integer userId = null;
            try {
                userId = Integer.parseInt(userIdPathParam);
            } catch (NumberFormatException e) {
                LOGGER.log(WARNING, "Unable to parse userId from path param {0}", userIdPathParam);
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

    private String getUserIdPathParam(URI requestURI) {
        final String path = requestURI.getPath();
        final Matcher matcher = pathPattern.matcher(path);
        if (matcher.matches()) {
            return matcher.group("userId");
        } else {
            return null;
        }
    }
}

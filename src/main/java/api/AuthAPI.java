package api;

import api.annotation.MethodType;
import api.annotation.RequestParam;
import api.annotation.RestMethod;
import com.sun.net.httpserver.HttpExchange;

public interface AuthAPI {

    /**
     * Logs in an existing user, if they are registered.
     * Response body will contain a unique session key if the user is registered, otherwise response code is 404
     */
    @RestMethod(methodType = MethodType.GET, pathPattern = "/auth/(?<userId>\\d+)", authenticated = false)
    void login(HttpExchange exchange,
               @RequestParam(name = "userId") String userIdParam);

    /**
     * Logs out an existing user that is authenticated
     */
    @RestMethod(methodType = MethodType.DELETE, pathPattern = "/auth")
    void logout(HttpExchange exchange, Integer authenticatedUserId);
}

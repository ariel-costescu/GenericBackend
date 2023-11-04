package api;

import api.annotation.MethodType;
import api.annotation.RequestParam;
import api.annotation.RestMethod;
import com.sun.net.httpserver.HttpExchange;

public interface UserAPI {

    @RestMethod(methodType = MethodType.POST, pathPattern = "/user/(?<userId>\\d+)", authenticated = false)
    void registerUser(HttpExchange exchange,
                      @RequestParam(name = "userId") String userIdParam);

    @RestMethod(methodType = MethodType.DELETE, pathPattern = "/user")
    void unregisterUser(HttpExchange exchange, Integer authenticatedUserId);
}

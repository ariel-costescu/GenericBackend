package api.handler;

import api.annotation.MethodType;
import api.annotation.RequestParam;
import api.annotation.RestMethod;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.LoginService;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.Logger.Level.ERROR;

public abstract class AbstractHandler implements HttpHandler {
    protected LoginService loginService;

    public abstract System.Logger getLogger();

    public boolean canHandleRequest(String requestMethod, URI requestURI) {
        return findMatchingRestMethod(requestMethod, requestURI) != null;
    }

    private Method findMatchingRestMethod(String requestMethod, URI requestURI) {
        Class<? extends AbstractHandler> classInfo = this.getClass();
        for (Method declaredMethod : classInfo.getDeclaredMethods()) {
            if (declaredMethod.isAnnotationPresent(RestMethod.class)) {
                RestMethod restMethod = declaredMethod.getAnnotation(RestMethod.class);
                if (MethodType.valueOf(requestMethod) == restMethod.methodType()) {
                    Pattern pathPattern = Pattern.compile(restMethod.pathPattern());
                    Matcher matcher = pathPattern.matcher(requestURI.getPath());
                    if (matcher.matches()) {
                        return declaredMethod;
                    }
                }
            }
        }
        return null;
    }

    protected Integer getUserIdFromRequestParam(String userIdParam) {
        Integer userId = null;
        try {
            userId = Integer.parseInt(userIdParam);
        } catch (NumberFormatException e) {
            getLogger().log(ERROR, "Unable to parse userId from path param {0}", userIdParam);
        }
        return userId;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        URI requestURI = exchange.getRequestURI();
        Method matchingMethod = findMatchingRestMethod(requestMethod, requestURI);
        assert matchingMethod != null;
        RestMethod restMethod = matchingMethod.getAnnotation(RestMethod.class);
        Pattern pathPattern = Pattern.compile(restMethod.pathPattern());
        Matcher pathMatcher = pathPattern.matcher(requestURI.getPath());
        if (!pathMatcher.matches()) {
            getLogger().log(ERROR, "Unable to obtain request param: no match found");
            handleBadRequest(exchange);
            return;
        }

        Integer authenticatedUserId = null;
        if (restMethod.authenticated()) {
            authenticatedUserId = getAuthenticatedUserId(exchange);
            if (authenticatedUserId == null) {
                handleUnauthorized(exchange);
                return;
            }
        }

        List<Object> requestParams = getRequestParams(exchange, authenticatedUserId, matchingMethod, pathMatcher);
        if (requestParams == null) return;
        try {
            matchingMethod.invoke(this, requestParams.toArray());
        } catch (IllegalAccessException | InvocationTargetException e) {
            getLogger().log(ERROR, "Unable to invoke rest method", e);
            handleInternalServerError(exchange);
        }
    }

    private Integer getAuthenticatedUserId(HttpExchange exchange) {
        Headers requestHeaders = exchange.getRequestHeaders();
        if (!requestHeaders.containsKey("Authorization")) {
            getLogger().log(ERROR, "Unable to authenticate: no Authorization header");
            return null;
        } else {
            List<String> authorizationHeader = requestHeaders.get("Authorization");
            String bearerToken = authorizationHeader.get(0);
            Pattern bearerPattern = Pattern.compile("Bearer (?<token>[0-9a-f\\-]+)");
            Matcher bearerMatcher = bearerPattern.matcher(bearerToken);
            if (!bearerMatcher.matches()) {
                getLogger().log(ERROR, "Unable to authenticate: bad Authorization header [{0}]", bearerToken);
                return null;
            } else {
                String token = bearerMatcher.group("token");
                if (loginService.isSessionKeyExpired(token)) {
                    getLogger().log(ERROR, "Unable to authenticate: expired token [{0}]", token);
                    return null;
                } else {
                    return loginService.getUserIdForSession(token);
                }
            }
        }
    }

    private List<Object> getRequestParams(HttpExchange exchange, Integer authenticatedUserId, Method matchingMethod, Matcher matcher) {
        List<Object> requestParams = new ArrayList<>();
        requestParams.add(exchange);
        if (authenticatedUserId != null) {
            requestParams.add(authenticatedUserId);
        }
        for (Parameter parameter : matchingMethod.getParameters()) {
            if (parameter.isAnnotationPresent(RequestParam.class)) {
                RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
                String paramValue;
                try {
                    String paramName = requestParam.name();
                    paramValue = matcher.group(paramName);
                } catch (IllegalStateException|IllegalArgumentException e) {
                    getLogger().log(ERROR, "Unable to obtain request param", e);
                    handleInternalServerError(exchange);
                    return null;
                }
                if (paramValue == null) {
                    handleBadRequest(exchange);
                    return null;
                } else {
                    requestParams.add(paramValue);
                }
            }
        }
        return requestParams;
    }

    protected void respondWithStatusCode(HttpExchange exchange, int statusCode) {
        try {
            exchange.sendResponseHeaders(statusCode, -1);
        } catch (IOException e) {
            getLogger().log(ERROR, "Unable to send response headers due to IOException", e);
        } finally {
            try {
                exchange.getResponseBody().close();
            } catch (IOException e) {
                getLogger().log(ERROR, "Unable to close body OutputStream due to IOException", e);
            }
        }
    }

    public void handleBadRequest(HttpExchange exchange) {
        respondWithStatusCode(exchange, 400);
    }

    public void handleNotFoundRequest(HttpExchange exchange) {
        respondWithStatusCode(exchange, 404);
    }

    public void handleUnauthorized(HttpExchange exchange) {
        respondWithStatusCode(exchange, 401);
    }

    public void handleInternalServerError(HttpExchange exchange) {
        respondWithStatusCode(exchange, 500);
    }

    public void sendResponse(HttpExchange exchange, String response) {
        final int length = response.length();
        try {
            if (length == 0) {
                // API specifies that a responseLength of value 0 will result in chunked encoding,
                // so to avoid that just use -1 for an empty response
                exchange.sendResponseHeaders(200, -1);
            } else {
                exchange.sendResponseHeaders(200, length);
                exchange.getResponseBody().write(response.getBytes());
            }
        } catch (IOException e) {
            getLogger().log(ERROR, "Unable to send response due to IOException", e);
        } finally {
            try {
                exchange.getResponseBody().close();
            } catch (IOException e) {
                getLogger().log(ERROR, "Unable to close response body OutputStream due to IOException", e);
            }
        }
    }

    public String getRequestBody(HttpExchange exchange) {
        final InputStream requestBody = exchange.getRequestBody();
        byte[] requestBytes = null;
        try {
            requestBytes = requestBody.readAllBytes();
        } catch (IOException e) {
            getLogger().log(ERROR, "Unable to read request body due to IOException", e);
        } catch (OutOfMemoryError e) {
            getLogger().log(ERROR, "Unable to read request body due to size not fitting in memory", e);
        } finally {
            try {
                requestBody.close();
            } catch (IOException e) {
                getLogger().log(ERROR, "Unable to close request body InputStream due to IOException", e);
            }
        }

        if (requestBytes != null) {
            return new String(requestBytes, StandardCharsets.UTF_8);
        } else {
            return null;
        }
    }
}

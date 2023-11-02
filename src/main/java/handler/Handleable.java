package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static java.lang.System.Logger.Level.ERROR;

public abstract class Handleable implements HttpHandler {

    public abstract boolean canHandleRequest(String requestMethod, URI requestURI);

    public abstract System.Logger getLogger();

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

    public void handleUnauthorized(HttpExchange exchange) {
        respondWithStatusCode(exchange, 401);
    }

    public void sendResponse(HttpExchange exchange, String response) {
        final int length = response.length();
        try {
            if (length <= 0) {
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

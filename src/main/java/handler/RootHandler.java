package handler;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import static java.lang.System.Logger.Level.ERROR;

public class RootHandler extends Handleable implements HandlerRegistry {

    private static final System.Logger LOGGER = System.getLogger("handler.RootHandler");

    private final List<Handleable> handlers = new LinkedList<>();

    @Override
    public System.Logger getLogger() {
        return LOGGER;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        final String requestMethod = exchange.getRequestMethod();
        final URI requestURI = exchange.getRequestURI();
        boolean handled = false;
        for (Handleable handler : handlers) {
            if (handler.canHandleRequest(requestMethod, requestURI)) {
                handler.handle(exchange);
                handled = true;
            }
        }
        if (!handled) {
            LOGGER.log(ERROR, "Couldn't find a handler for requestURI {0}", requestURI.toString());
            handleBadRequest(exchange);
        }
    }

    @Override
    public void registerHandler(Handleable handler) {
        handlers.add(handler);
    }

    @Override
    public boolean canHandleRequest(String requestMethod, URI requestURI) {
        return requestURI.getPath().startsWith("/");
    }
}

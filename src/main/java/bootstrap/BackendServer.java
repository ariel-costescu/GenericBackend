package bootstrap;

import com.sun.net.httpserver.HttpServer;
import api.handler.AuthHandler;
import api.handler.RootHandler;
import service.LoginService;
import service.impl.LoginServiceImpl;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

import static java.lang.System.Logger.Level.INFO;

public class BackendServer {

    private static final System.Logger LOGGER = System.getLogger("bootstrap.BackendServer");

    private final HttpServer httpServer;
    private final LoginService loginService;
    private final ScheduledExecutorService scheduler;

    public BackendServer(HttpServer httpServer,
                         Executor handlerExecutor,
                         ScheduledExecutorService scheduler) {
        this.httpServer = httpServer;
        this.scheduler = scheduler;
        this.loginService = new LoginServiceImpl(scheduler);
        registerHandlers();
        this.httpServer.setExecutor(handlerExecutor);
    }

    public void start() {
        httpServer.start();
        LOGGER.log(INFO, "BackendServer started");
    }

    public void stop() {
        LOGGER.log(INFO, "BackendServer stopping");
        scheduler.shutdown();
        httpServer.stop(2);
    }

    private void registerHandlers() {
        final RootHandler rootHandler = new RootHandler(loginService);
        httpServer.createContext("/", rootHandler);
        rootHandler.registerHandler(new AuthHandler(loginService));
    }
}

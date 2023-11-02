package httpServer;

import com.sun.net.httpserver.HttpServer;
import handler.LoginHandler;
import handler.RootHandler;
import service.LoginService;
import service.LoginServiceImpl;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

import static java.lang.System.Logger.Level.INFO;

public class BackendServer {

    private static final System.Logger LOGGER = System.getLogger("httpServer.BackendServer");

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
        final RootHandler rootHandler = new RootHandler();
        httpServer.createContext("/", rootHandler);
        rootHandler.registerHandler(new LoginHandler(loginService));
    }
}

package bootstrap;

import com.sun.net.httpserver.HttpServer;
import httpServer.BackendServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.lang.System.Logger.Level.*;

public class Bootstrap {

    private static final int PORT = 8080;
    private static final int BACKLOG = 0;
    private static final int N_CORES = Runtime.getRuntime().availableProcessors();
    private static final System.Logger LOGGER = System.getLogger("bootstrap.Bootstrap");

    public static void main(String[] args) {
        final BackendServer backendServer = initBackendServer(args);

        if (backendServer != null) {
            backendServer.start();
            final ShutdownHook shutdownHook = new ShutdownHook(backendServer);
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        } else {
            LOGGER.log(WARNING, "Couldn't init backend server");
        }
    }

    private static BackendServer initBackendServer(String[] args) {
        int port = PORT;
        int backlog = BACKLOG;
        for (int i = 0; i < args.length; i += 2) {
            String argName = args[i];
            final int argValue = Integer.parseInt(args[i + 1]);
            if (argName.equals("-port")) {
                port = argValue;
            } else if (argName.equals("-backlog")) {
                backlog = argValue;
            }
        }

        LOGGER.log(INFO, "Starting up backend server on port {0} with a maximum backlog of {1}", port, backlog);

        InetSocketAddress socketAddress = new InetSocketAddress(port);
        HttpServer httpServer = null;
        try {
            httpServer = HttpServer.create(socketAddress, backlog);
        } catch (IOException e) {
            LOGGER.log(ERROR, "Unable to create http server due to IOException", e);
        }

        if (httpServer != null) {
            Executor handlerExecutor = Executors.newFixedThreadPool(N_CORES);
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            return new BackendServer(httpServer, handlerExecutor, scheduler);
        } else {
            return null;
        }
    }
}

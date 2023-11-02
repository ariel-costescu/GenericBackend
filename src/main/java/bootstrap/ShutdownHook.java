package bootstrap;

public class ShutdownHook extends Thread {
    final BackendServer backendServer;

    public ShutdownHook(BackendServer backendServer) {
        this.backendServer = backendServer;
    }

    @Override
    public void run() {
        backendServer.stop();
    }
}

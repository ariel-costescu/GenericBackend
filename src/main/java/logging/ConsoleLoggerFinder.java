package logging;

public class ConsoleLoggerFinder extends System.LoggerFinder {

    @Override
    public System.Logger getLogger(String loggerName, Module callerModule) {
        return new ConsoleLogger();
    }
}

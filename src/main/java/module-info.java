import logging.ConsoleLoggerFinder;

module GenericBackend {
    requires jdk.httpserver;
    provides System.LoggerFinder
            with ConsoleLoggerFinder;
    exports logging;
}
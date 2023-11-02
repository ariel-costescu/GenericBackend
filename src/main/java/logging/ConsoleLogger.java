package logging;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class ConsoleLogger implements System.Logger {

    public static final String name = "ConsoleLogger";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isLoggable(Level level) {
        return true;
    }

    @Override
    public void log(Level level, ResourceBundle bundle, String msg, Throwable throwable) {
        System.out.printf("[%s]: %s - %s\n", level, msg, throwable);
        throwable.printStackTrace(System.out);
    }

    @Override
    public void log(Level level, ResourceBundle bundle, String formattedMsg, Object... values) {
        System.out.printf("[%s]: %s\n", level, MessageFormat.format(formattedMsg, values));
    }
}

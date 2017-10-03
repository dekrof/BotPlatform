package ua.ukrposhta.utils.Loggers;

import org.apache.log4j.Logger;

public class ViberLogger extends BotLogger {
    private static ViberLogger instance;
    private Logger logger = Logger.getLogger("viberLogger");

    private ViberLogger() {

    }

    public static ViberLogger getInstance() {
        if (instance == null) {
            instance = new ViberLogger();
        }

        return instance;
    }

    public void info(Object message) {
        logger.info(message);
        consoleLogger.info(message);
    }

    public void warn(Object message) {
        logger.warn(message);
        consoleLogger.warn(message);
    }

    public void error(Object message) {
        logger.error(message);
        consoleLogger.error(message);
    }
}

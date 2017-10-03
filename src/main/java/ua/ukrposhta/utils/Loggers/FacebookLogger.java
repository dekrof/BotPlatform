package ua.ukrposhta.utils.Loggers;

import org.apache.log4j.Logger;

public class FacebookLogger extends BotLogger {
    private static FacebookLogger instance;
    private Logger logger = Logger.getLogger("facebookLogger");

    private FacebookLogger() {

    }

    public static FacebookLogger getInstance() {
        if (instance == null) {
            instance = new FacebookLogger();
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
